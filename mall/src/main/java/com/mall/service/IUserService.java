package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.Signature;
import com.mall.pojo.User;

//写完RC、SR，把响应改为ServerResponse<User>---同时修改UserService
public interface IUserService  {
    ServerResponse<User> login(String username, String password,Signature signature );//做个泛型做一个通用的数据响应对象----改为ServerResponse<User
    ServerResponse<String> register(User user, Signature signature);
    ServerResponse<String> checkValid(String str,String type);
    ServerResponse<String> selectQuestion(String username);
    ServerResponse<String> CheckAnswer(String username,String question,String answer);
    ServerResponse<String> forgetResetpassword(String username,String newPassword,String forgetToken);
    ServerResponse<String> resetPassword ( String passwordOld, String passwordNew, User user );
    ServerResponse<User> updateInformation ( User user );
    ServerResponse<User> getInformation ( Integer userId );
    ServerResponse checkAdminRole( User user );
}
