package com.mall.controller.portal;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;


@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger (OrderController.class);

    @Autowired
    private IOrderService iOrderService;

//-----------------------------------支付相关接口------------------------------------

    //订单支付
    //传订单号供支付
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay ( HttpSession session, Long orderNo ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        //获取路径（此路径用于保存上传的文件）
//        String path=request.getSession ().getServletContext ().getRealPath ("upload");
        String path = "C:\\ftpfile\\img";
        return iOrderService.pay (orderNo, user.getId (), path);
    }

    //支付宝回调接口，传递的参数都在request中
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayRollback ( HttpServletRequest request ) {
        //保存获取的参数
        logger.info("alipay_callback.do");
        Map<String, String> params = Maps.newHashMap ();
        //requestParams是<String,String[]>类型
        Map requestParams = request.getParameterMap ();
        for (Iterator iterator = requestParams.keySet ().iterator (); iterator.hasNext (); ) {
            //获取key值
            String name = (String) iterator.next ();
            //获取values值
            String[] values = (String[]) requestParams.get (name);
            String valuestr = "";
            //将数组values变为String并用逗号分隔开
            for (int i = 0; i < values.length; i++) {
                valuestr = (i == values.length - 1) ? valuestr + values[ i ] : valuestr + values[ i ] + ",";
            }
            params.put (name, valuestr);
        }
        logger.info ("支付宝回调，sign：{},trade_status：{}，参数：{}", params.get ("sign"), params.get ("trade_status"), params.toString ());
        logger.info ("交易号：", params.get ("trade_no"));
        //验证回调的正确性（是不是支付宝发的，避免重复通知）
        //待验签参数要除去sign(SDK源码中会去除),sign_type
        params.remove ("sign_type");
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2 (params, Configs.getAlipayPublicKey (), "utf-8", Configs.getSignType ());
            if (!alipayRSACheckedV2) {
                return ServerResponse.createByErrorMessage ("非法请求，请不要恶意请求！");
            }
        } catch (AlipayApiException e) {
            logger.error ("支付宝验证回调异常", e);
        }
        //todo 验证各种数据

        ServerResponse serverResponse = iOrderService.aliCallback (params);
        if (serverResponse.isSuccess ()) {
            //返回success
            return Const.AlipayCallBack.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallBack.RESPONSE_FAILED;

    }

    //查看订单是否支付
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus ( HttpSession session, Long orderNo ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus (orderNo, user.getId ());
        if (serverResponse.isSuccess ()) {
            return ServerResponse.createBySuccess (true);
        }
        return ServerResponse.createByError ();
    }


    //-----------------------------------订单相关接口------------------------------------
    //创建订单
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create ( HttpSession session, Integer shippingId ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iOrderService.createOrder (user.getId (), shippingId);
    }

    //取消订单
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel ( HttpSession session, Long orderNo ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iOrderService.cancel (user.getId (), orderNo);
    }
    //取消订单
    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse del ( HttpSession session, Long orderNo ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iOrderService.del (user.getId (), orderNo);
    }
    //获取订单中的产品信息
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct ( HttpSession session ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iOrderService.getOrderCartProduct (user.getId ());
    }

    //查看订单详情
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail ( HttpSession session, Long orderNo ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iOrderService.getOrderDetail (user.getId (), orderNo);
    }

    //查看所有订单（个人中心）
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list ( HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize ) {
        User user = (User) session.getAttribute (Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.NEED_LOGIN.getCode (), ResponseCode.NEED_LOGIN.getDesc ());
        }
        return iOrderService.getOrderList (user.getId (), pageNum, pageSize);
    }

}
