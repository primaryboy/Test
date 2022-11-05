package com.mall.controller.administrator;

import com.github.pagehelper.PageInfo;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IOrderService;
import com.mall.service.IUserService;
import com.mall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/administrate/order")
public class OrderAdministrateController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IOrderService iOrderService;

    //查看所有订单
    @RequestMapping("/list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList( HttpSession session,
                                     @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize",defaultValue = "10") int pageSize ){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取所有订单
            return iOrderService.manageList (pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //查看订单详情
    @RequestMapping("/detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail( HttpSession session, Long orderNo ){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取订单详情
            return iOrderService.manageDetail (orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //搜索订单详情
    @RequestMapping("/search.do")
    @ResponseBody
    public ServerResponse orderSearch( HttpSession session,Long orderNo,
                                       @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                       @RequestParam(value = "pageSize",defaultValue = "10") int pageSize  ){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取订单详情
            return iOrderService.manageSearch (orderNo,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //发货
    @RequestMapping("/send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods( HttpSession session, Long orderNo ){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取订单详情
            return iOrderService.manageSendGoods(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
}
