package com.mall.controller.administrator;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.pojo.User;
import com.mall.service.IProductService;
import com.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrate/product/")
public class ProducAdministrateController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @RequestMapping("save.do")
    @ResponseBody
    //增加/保存产品
    public ServerResponse productSave (HttpSession session, Product product ) {
        //校验是否登录
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (),
                    "用户未登录，请登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole (user).isSuccess ()) {
            //增加（保存）产品
            return iProductService.saveOrUpdateProduct (product);
        } else {
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限！");
        }
    }

    @RequestMapping("set_sale_status.do")
    @ResponseBody
    //设置产品销售状态
    public ServerResponse setSaleStatus ( HttpSession session, Integer productId, Integer status ) {
        //校验是否登录
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), "用户未登录，请登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole (user).isSuccess ()) {
            //设置状态
            return iProductService.setSaleStatus (productId, status);

        } else {
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限！");
        }
    }
    //获取产品详情
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail ( HttpSession session, Integer productId ) {
        //校验是否登录
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), "用户未登录，请登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole (user).isSuccess ()) {
            //管理商品
            return iProductService.administerProductDetail(productId);

        } else {
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限！");
        }
    }
    //list----分页管理
    @RequestMapping("list.do")
    @ResponseBody
    //获取商品详情
    public ServerResponse getList ( HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize ) {
        //校验是否登录
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), "用户未登录，请登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole (user).isSuccess ()) {
            //获取商品列表
            return iProductService.getProductList (pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限！");
        }
    }

    //搜索商品
    @RequestMapping("search.do")
    @ResponseBody

    public ServerResponse productSearch (HttpSession session, String productName, Integer productId,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize ) {
        //校验是否登录
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), "用户未登录，请登录");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole (user).isSuccess ()) {
            return iProductService.searchProduct (productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage ("不是管理员，没有权限！");
        }
    }

}
