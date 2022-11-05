package com.mall.service.impl;

import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.common.TokenCache;
import com.mall.dao.SignatureMapper;
import com.mall.dao.UserMapper;
import com.mall.pojo.Signature;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import com.mall.util.DESUtil;
import com.mall.util.RSAUtil;
import com.mall.util.SHAUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;



//进行接口的实现，报错点击灯泡---第一个---ok---接口方法自动填充
    //通用数据端响应对象common---类ServerResponse
@Service("iUserService")//向上注入的时候注入iUserService这个接口
public class UserServiceImpl implements IUserService{
    @Autowired//通过autowired的注解把mapper注入进来(mybatis的autowired)
    private UserMapper userMapper;
    @Autowired//通过autowired的注解把mapper注入进来(mybatis的autowired)
    private SignatureMapper signatureMapper;
    @Override
    //User登录
    public ServerResponse<User> login(String username, String password,Signature signature) {
        //验证身份
        int getname=signatureMapper.checkUsername(username);
        String publickey=signatureMapper.getpublickey(username);
        if(getname==0) {
            return ServerResponse.createByErrorMessage("没有您身份的认证信息，请重新输入用户名！");
        }
        //登录
        //检查登录的User名存不存在
        int resulCount= userMapper.checkUsername(username);
        if (resulCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在，请重新输入！");
        }
        String result=signatureMapper.getSignature(username);
        if(RSAUtil.RSAcheck(result,username,publickey)==0){
            return ServerResponse.createByErrorMessage("不是用户本人！");
        }
        String SHAPassword=SHAUtil.encode(password);
        //验证登录User名password是否正确
        User user=userMapper.selectLogin(username,SHAPassword);
        if(user==null){
            return  ServerResponse.createByErrorMessage("密码错误，请重新输入！");
        }
        //把password置为空
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);

    }

    //注册，校验User名是不是存在，校验email是否存在
    public ServerResponse<String> register(User user,Signature signature ){
//        Signature signature = new Signature();
        ServerResponse validResponse=this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);//新注册的身份默认为顾客
        //MD5加密，对password进行加密
        user.setPassword(SHAUtil.encode(user.getPassword()));
        int resulCount=userMapper.insert(user);
        //如果插入成功，返回1
        if(resulCount==0){
            return ServerResponse.createByErrorMessage("注册失败！");
        }
        //获取当前用户名
        String name=user.getUsername();
        //获取私钥
        //byte[] privatek=RSAUtil.PrivateKey();
        signature.setUsername(name);
        signature.setSignature(RSAUtil.RSAsign(user.getUsername()));

        //保存私钥

        signature.setPrivatekey(DESUtil.encrypt(RSAUtil.PrivateKey(),name));
        //signature.setPrivatekey(RSAUtil.PrivateKey());
        //保存公钥
        signature.setPublickey(RSAUtil.PublicKey());
        resulCount =signatureMapper.insert(signature);
        if(resulCount==0){
            return ServerResponse.createByErrorMessage("用户身份不合法！");
        }
/*      signature.getSignature();
        signature.getUsername();*/
        return ServerResponse.createBySuccessMessage("注册成功！");

        //str是一个值，type判断是username还是email

    }
    //校验email、username是否存在
    public ServerResponse<String> checkValid(String str,String type){
        if (org.apache.commons.lang3.StringUtils.isNoneBlank(type)){
            //开始校验
            if(Const.USERNAME.equals(type)){
                int resulCount= userMapper.checkUsername(str);
                if (resulCount>0){
                    return ServerResponse.createByErrorMessage("用户名已经存在，请重新输入！");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resulCount= userMapper.checkEmail(str);
                if (resulCount>0){
                    return ServerResponse.createByErrorMessage("邮箱已经存在，请重新输入！");
                }
            }
        }
        else{
            return ServerResponse.createByErrorMessage("参数错误！请重新输入");
        }
        return ServerResponse.createBySuccessMessage("校验成功！");
    }
    //查找该User是否有找回password问题
    public ServerResponse<String> selectQuestion(String username){
        //直接复用checkvalid
        ServerResponse validresponse=checkValid(username,Const.USERNAME);
        if (validresponse.isSuccess()){
            //User不存在
            return ServerResponse.createByErrorMessage("该用户不存在");
        }
        //查找问题
        String question;
        question=userMapper.selectQuestionByUsername(username);
        if(org.apache.commons.lang3.StringUtils.isNotBlank(question)){
            return  ServerResponse.createBySuccess(question);
        }
        return  ServerResponse.createByErrorMessage("未设置找回问题");


    }
    //验证问题
    public ServerResponse<String> CheckAnswer(String username,String question,String answer){
        int resultCount=userMapper.checkAnswer(username,question,answer);
        if(resultCount>0){
            //说明问题及问题答案是这个User的，并且是正确的
            //声明一个token，使用java里面的uuid来生成
            String forgetToken= UUID.randomUUID().toString();

            //把forgettoken放到decache中，并设置有效期---common---tokencache（class）
            //调用本地的tokencache设置key
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("答案错误，请重新输入！");
    }

    //重置password
    public ServerResponse<String> forgetResetpassword(String username,String newPassword,String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        //
        ServerResponse validresponse=checkValid(username,Const.USERNAME);
        if (validresponse.isSuccess()){
            //User不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //cache里面获取token
        String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        //对cache里的token进行校验
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        //比较忘记password时生成的token和缓存的token（可传null）
        if (StringUtils.equals (forgetToken, token)) {
            //加密
            String SHApassword = SHAUtil.encode (newPassword);
            //更新password
            int rowCount = userMapper.updatePasswordByUsername (username, SHApassword);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            //token不同
            return ServerResponse.createByErrorMessage ("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage ("修改密码失败!");

    }
    //登录状态修改password
    public ServerResponse<String> resetPassword ( String passwordOld, String passwordNew, User user ) {
        //防止横向越权，需确定修改的password为当前User的password
        int resultCount = userMapper.checkPassword (SHAUtil.encode(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage ("旧密码错误");
        }
        //设置新password
        user.setPassword(SHAUtil.encode(passwordNew));
        //session里面的数据在未来扩展时，可能是一个缩小版，可能有User名这些，有选择性的更新提高效率
        int updateCount = userMapper.updateByPrimaryKeySelective (user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage ("密码修改成功");
        }
        return ServerResponse.createByErrorMessage ("密码修改失败");
    }
    //登录后更新User信息
    public ServerResponse<User> updateInformation ( User user ) {
        //username是不能被更新的
        //Email也需要校验，如果更新后的Email与原来相同则不需要更新
        int resultCount = userMapper.checkEmailByUserId (user.getEmail (), user.getId ());
        //与原Email相同
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage ("邮箱已经存在，请输入新的邮箱!");
        }
        //与原Email不相同，则开始更新User信息（只更新不为空的信息，需要new一个User对象）
        User updateUser = new User ();
        updateUser.setId (user.getId());
        //前端获取
        updateUser.setEmail (user.getEmail ());
        updateUser.setQuestion (user.getQuestion ());
        updateUser.setPhone (user.getPhone ());
        updateUser.setAnswer (user.getAnswer ());
        //选择性更新
        int updateCount = userMapper.updateByPrimaryKeySelective (updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess ("个人信息更新成功", user);
        }
        //更新失败
        return ServerResponse.createByErrorMessage ("个人信息更新失败！");

    }
    //获取登录User信息
    public ServerResponse<User> getInformation ( Integer userId ) {
        User user = userMapper.selectByPrimaryKey (userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage ("用户未登录，请登录！");
        }
        //password置空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess (user);

    }
    //校验是不是管理员
    public ServerResponse checkAdminRole ( User user ) {
        //Integer->int
        if (user != null && user.getRole ().intValue () == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess ();
        }
        return ServerResponse.createByError ();
    }
}
