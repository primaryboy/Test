package com.mall.controller.administrator;

import com.mall.common.Const;
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


@Controller
@RequestMapping("/administrate/user/")
public class UserAdministrateController {
    //注入userservice
    @Autowired
    private IUserService iUserService;
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    ServerResponse<User> login(String username, String password, HttpSession sessoin, Signature signature){
        ServerResponse<User> response=iUserService.login(username,password,signature);
        if(response!=null ){
            User user=response.getData();
            if(user.getRole()== Const.Role.ROLE_ADMIN){
                //登录的是管理员
                sessoin.setAttribute(Const.CURRENT_USER,user);
                return response;
            }
            return ServerResponse.createByErrorMessage("不是管理员，请重新登录！");
        }
        return response;
    }
}
