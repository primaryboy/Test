package com.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;
import com.mall.pojo.User;
import com.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/shipping/")
public class ShippingController {
    @Autowired
    private IShippingService iShippingService;

    //添加收货地址
    @RequestMapping("/add.do")
    @ResponseBody
    public ServerResponse add ( HttpSession session, Shipping shipping ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iShippingService.add (user.getId (), shipping);
    }

    //删除收货地址
    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse delete ( HttpSession session, Integer shippingId ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iShippingService.del (user.getId (), shippingId);
    }

    //更新收货地址
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update ( HttpSession session, Shipping shipping ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iShippingService.update (user.getId (), shipping);
    }

    //查询收货地址
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<Shipping> select (HttpSession session, Integer shippingId ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iShippingService.select (user.getId (), shippingId);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list (HttpSession session,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iShippingService.list (user.getId (), pageNum, pageSize);
    }

}
