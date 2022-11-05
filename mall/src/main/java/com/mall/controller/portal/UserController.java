package com.mall.controller.portal;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Signature;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller//添加spring的注解：controller，这样UserController就可以起到Controller的作用
@RequestMapping("/user/")//把请求地址全部打到/user命名空间下，把其写在类上面，如果写在方法下面，每次都要写，为了重用

public class UserController {
    @Autowired
    private IUserService iUserService;

    /**
     * User登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)//接口定义：login.do，登录指定是post请求
    @ResponseBody//在返回时自动通过SpringMVC的jackson插件将我们的返回值序列化成Jackson
    public ServerResponse<User> login(String username, String password, HttpSession session,Signature signature ){
        //service-->mybatis调用dao层
        //调用service方法
        ServerResponse<User> response=iUserService.login(username,password,signature);
        if(response.isSuccess()){
            //在session里面把 User放进去
            session.setAttribute(Const.CURRENT_USER,response.getData());
            //System.out.println("success");
        }
        return response;
    }
    //登出，将添加的currentuser删除掉，将session的key删除掉，使用删除成功的响应
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)//登录指定是post请求
    @ResponseBody//在返回时自动通过SpringMVC的jackson插件将我们的返回值序列化成Jackson
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }
    @RequestMapping(value = "register.do",method = RequestMethod.POST)//登录指定是post请求
    @ResponseBody//在返回时自动通过SpringMVC的jackson插件将我们的返回值序列化成Jackson
    //注册，通过对象User对User所有的属性进行一个包装
    public ServerResponse<String> register(User user,Signature signature){
        return iUserService.register(user, signature);
    }
    //校验email和User名是否存在，防止恶意User通过接口调用注册接口
    //在注册时，输入User名后点击下一个input框要实时调用一个校验接口
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)//登录指定是post请求
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str,type);
    }
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.GET)
    @ResponseBody
    //获取User登录信息,不需要传入参数
    public ServerResponse<User> getUserInfo(HttpSession seesion){
        //在session里面获取user
        User user=(User) seesion.getAttribute(Const.CURRENT_USER);//进行一个强转
        if(user !=null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
    }
    //忘记密码功能
    @RequestMapping(value = "forget_question.do",method = RequestMethod.POST)//登录指定是post请求
    @ResponseBody
    public ServerResponse<String> forgetQuestion(String username){

        return iUserService.selectQuestion(username);
    }
    //使用本地缓存校验问题答案是否正确,并存token
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)//登录指定是post请求
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.CheckAnswer(username,question,answer);
    }

    //忘记密码中的重置密码，需要使用token,同缓存做对比
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)//登录指定是post请求
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String newPassword,String forgetToken){
        return iUserService.forgetResetpassword (username, newPassword, forgetToken);
    }
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    //User登录状态下更新密码
    public ServerResponse<String> resetPassword ( HttpSession session, String oldPassword, String newPassword ) {
        //从session中获取当前User（判断是否登录）
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            //判空
            return ServerResponse.createByErrorMessage ("用户未登录");
        }
        return iUserService.resetPassword (oldPassword, newPassword, user);

    }

    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    //更新User信息
    public ServerResponse<User> updateInformation ( HttpSession session, User user ) {
        //从session中获取当前User（判断是否登录）
        User currentUser = (User) session.getAttribute (Const.CURRENT_USER);
        if (currentUser == null) {
            //判空
            return ServerResponse.createByErrorMessage ("用户未登录");
        }
        //设置userid（前端不能自己设置id，防止产生横向越权）
        user.setId (currentUser.getId ());
        user.setCreateTime (currentUser.getCreateTime());
        user.setUpdateTime(currentUser.getUpdateTime());
        //防止被抓包
        user.setUsername(currentUser.getUsername());
        //更新
        ServerResponse<User> serverResponse = iUserService.updateInformation (user);
        //更新成功
        if (serverResponse.isSuccess ()) {
            session.setAttribute (Const.CURRENT_USER, serverResponse.getData ());
        }
        return serverResponse;
    }

    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    //获取当前登录User详细信息
    public ServerResponse<User> getInformation ( HttpSession session ) {
        //从session中获取当前User（判断是否登录），如果没有登录需要进行强制登录
        User currentUser = (User) session.getAttribute (Const.CURRENT_USER);
        if (currentUser == null) {
            //判空
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), "未登录，需要进行登录");

        }
        //已登录
        return iUserService.getInformation (currentUser.getId ());
    }
}
