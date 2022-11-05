package com.mall.service.impl;

//import com.alipay.api.AlipayResponse;
//import com.alipay.api.response.AlipayTradePrecreateResponse;
//import com.alipay.demo.trade.config.Configs;
//import com.alipay.demo.trade.model.ExtendParams;
//import com.alipay.demo.trade.model.GoodsDetail;
//import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
//import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
//import com.alipay.demo.trade.service.AlipayTradeService;
//import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
//import com.alipay.demo.trade.utils.ZxingUtils;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.dao.*;
import com.mall.pojo.*;
import com.mall.service.IOrderService;
import com.mall.util.BigDecimalUtil;
import com.mall.util.DateTimeUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.OrderItemVo;
import com.mall.vo.OrderProductVo;
import com.mall.vo.OrderVo;
import com.mall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;


@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger (OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    //-----------------------------------支付相关Service------------------------------------

    //根据购物车中选中的产品返回订单信息
    private ServerResponse getCartOrderItem ( Integer userId, List<Cart> cartList ) {
        List<OrderItem> orderItemList = Lists.newArrayList ();
        if (CollectionUtils.isEmpty (cartList)) {
            return ServerResponse.createByErrorMessage ("未选择产品!");
        }
        for (Cart cartItem : cartList) {
            //根据选中的产品的信息实例化OrderItem对象
            OrderItem orderItem = new OrderItem ();
            orderItem.setUserId (userId);
            //一个CartItem中放有一种产品
            Product product = productMapper.selectByPrimaryKey (cartItem.getProductId ());
            //校验该产品状态
            if (Const.ProductStatusEnum.ON_SALE.getCode () != product.getStatus ()) {
                return ServerResponse.createByErrorMessage ("产品" + product.getName () + "不是在售状态");
            }
            //校验该产品库存
            if (cartItem.getQuantity () > product.getStock ()) {
                return ServerResponse.createByErrorMessage ("产品" + product.getName () + "超出库存");
            }
            //填充orderItem
            orderItem.setProductId (product.getId ());
            orderItem.setProductName (product.getName ());
            orderItem.setProductImage (product.getMainImage ());
            orderItem.setCurrentUnitPrice (product.getPrice ());
            orderItem.setQuantity (cartItem.getQuantity ());
            orderItem.setTotalPrice (BigDecimalUtil.mul (product.getPrice ().doubleValue (), cartItem.getQuantity ()));
            orderItemList.add (orderItem);
        }
        return ServerResponse.createBySuccess (orderItemList);
    }

    private BigDecimal getOrderTotalPrice ( List<OrderItem> orderItemList ) {
        BigDecimal payment = new BigDecimal ("0");
        for (OrderItem orderItem : orderItemList) {
            //更新payment
            payment = BigDecimalUtil.add (payment.doubleValue (), orderItem.getTotalPrice ().doubleValue ());
        }
        return payment;
    }

    //生成订单号
    private long generateOrderNo ( ) {
        long currentTime = System.currentTimeMillis ();
        //减少并发事务使生成订单失败
        return currentTime + new Random ().nextInt (100);
    }

    //组装Order
    private Order assembleOrder ( Integer userId, Integer shippingId, BigDecimal payment ) {
        Order order = new Order ();
        long orderNo = this.generateOrderNo ();
        //开始组装
        order.setOrderNo (orderNo);
        order.setStatus (Const.OrderStatusEnum.NO_PAY.getCode ());
        order.setPostage (0);
        order.setPaymentType (Const.PaymentTypeEnum.ONLINE_PAY.getCode ());
        order.setPayment (payment);
        order.setUserId (userId);
        order.setShippingId (shippingId);

        int resultCount = orderMapper.insert (order);
        if (resultCount > 0) {
            return order;
        }
        return null;
    }

    private void reduceProductStock ( List<OrderItem> orderItemList ) {
        for (OrderItem orderItem : orderItemList) {
            //获取该产品
            Product product = productMapper.selectByPrimaryKey (orderItem.getProductId ());
            //更新产品库存
            product.setStock (product.getStock () - orderItem.getQuantity ());
            productMapper.updateByPrimaryKeySelective (product);
        }


    }

    private void cleanCart ( List<Cart> cartList ) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey (cart.getId ());
        }
    }

    //Shipping->ShippingVo
    private ShippingVo assembleShippingVo ( Shipping shipping ) {
        ShippingVo shippingVo = new ShippingVo ();
        shippingVo.setReceiverName (shipping.getReceiverName ());
        shippingVo.setReceiverAddress (shipping.getReceiverAddress ());
        shippingVo.setReceiverProvince (shipping.getReceiverProvince ());
        shippingVo.setReceiverCity (shipping.getReceiverCity ());
        shippingVo.setReceiverDistrict (shipping.getReceiverDistrict ());
        shippingVo.setReceiverMobile (shipping.getReceiverMobile ());
        shippingVo.setReceiverZip (shipping.getReceiverZip ());
        shippingVo.setReceiverPhone (shippingVo.getReceiverPhone ());
        return shippingVo;
    }

    //OrderItem->OrderItemVo
    private OrderItemVo assembleOrderItemVo ( OrderItem orderItem ) {
        OrderItemVo orderItemVo = new OrderItemVo ();
        orderItemVo.setOrderNo (orderItem.getOrderNo ());
        orderItemVo.setProductId (orderItem.getProductId ());
        orderItemVo.setProductName (orderItem.getProductName ());
        orderItemVo.setProductImage (orderItem.getProductImage ());
        orderItemVo.setCurrentUnitPrice (orderItem.getCurrentUnitPrice ());
        orderItemVo.setQuantity (orderItem.getQuantity ());
        orderItemVo.setTotalPrice (orderItem.getTotalPrice ());
        orderItemVo.setCreateTime (DateTimeUtil.dateToStr (orderItem.getCreateTime ()));
        return orderItemVo;
    }

    //Order->OrderVo
    private OrderVo assembleOrderVo ( Order order, List<OrderItem> orderItemList ) {
        OrderVo orderVo = new OrderVo ();
        //组装
        orderVo.setOrderNo (order.getOrderNo ());
        orderVo.setPayment (order.getPayment ());
        orderVo.setPaymentType (order.getPaymentType ());
        //支付方式描述
        orderVo.setPaymentTypeDesc (Const.PaymentTypeEnum.codeOf (order.getPaymentType ()).getValue ());
        //运费
        orderVo.setPostage (order.getPostage ());
        orderVo.setStatus (order.getStatus ());
        //订单状态描述
        orderVo.setStatusDesc (Const.OrderStatusEnum.codeOf (order.getStatus ()).getValue ());
        orderVo.setShippingId (order.getShippingId ());
        //收货地址
        Shipping shipping = shippingMapper.selectByPrimaryKey (order.getShippingId ());
        if (shipping != null) {
            //继续组装
            orderVo.setReceiverName (shipping.getReceiverName ());
            orderVo.setShippingVo (assembleShippingVo (shipping));
        }
        //继续组装
        orderVo.setPaymentTime (DateTimeUtil.dateToStr (order.getPaymentTime ()));
        orderVo.setSendTime (DateTimeUtil.dateToStr (order.getSendTime ()));
        orderVo.setEndTime (DateTimeUtil.dateToStr (order.getEndTime ()));
        orderVo.setCreateTime (DateTimeUtil.dateToStr (order.getCreateTime ()));
        orderVo.setCloseTime (DateTimeUtil.dateToStr (order.getCloseTime ()));
        orderVo.setImageHost (PropertiesUtil.getProperty ("ftp.server.http.prefix"));
        //组装OrderVo的订单明细List<OrderItemVo>
        List<OrderItemVo> orderItemVoList = Lists.newArrayList ();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo (orderItem);
            orderItemVoList.add (orderItemVo);
        }
        orderVo.setOrderItemVoList (orderItemVoList);

        return orderVo;
    }

    //List<Order>->List<OrderVo>
    private List<OrderVo> assembleOrderVoList ( List<Order> orderList, Integer userId ) {
        List<OrderVo> orderVoList = Lists.newArrayList ();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList ();
            if (userId == null) {
                orderItemList = orderItemMapper.getByOrderNo (order.getOrderNo ());
            } else {
                orderItemList = orderItemMapper.getByOrderNoUserId (order.getOrderNo (), userId);
            }
//            //获取所有订单Item
//            orderItemList = orderItemMapper.getByOrderNoUserId(order.getOrderNo(),userId);
            //组装OrderVo
            OrderVo orderVo = assembleOrderVo (order, orderItemList);
            orderVoList.add (orderVo);
        }
        return orderVoList;
    }

    //创建订单
    public ServerResponse createOrder ( Integer userId, Integer shippingId ) {
        //获取选中的购物车产品
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId (userId);
        //根据选中的产品生成OrderItem集合
        ServerResponse serverResponse = this.getCartOrderItem (userId, cartList);
        //List<OrderItem>为空
        if (!serverResponse.isSuccess ()) {
            return serverResponse;
        }
        //计算订单总价
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData ();
        BigDecimal payment = this.getOrderTotalPrice (orderItemList);
        //生成订单
        Order order = this.assembleOrder (userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage ("生成订单错误");
        }
        if (CollectionUtils.isEmpty (orderItemList)) {
            return ServerResponse.createByErrorMessage ("未选择产品");
        }
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo (order.getOrderNo ());
        }
        //mybatis 批量插入
        orderItemMapper.batchInsert (orderItemList);
        //生成成功,减少库存
        this.reduceProductStock (orderItemList);
        //购物车更新
        this.cleanCart (cartList);
        //返回给前端数据
        OrderVo orderVo = this.assembleOrderVo (order, orderItemList);
        return ServerResponse.createBySuccess (orderVo);
    }

    //取消订单
    public ServerResponse<String> cancel ( Integer userId, Long orderNo ) {
        //选取订单
        Order order = orderMapper.selectByUserIdAndOrderNo (userId, orderNo);
        //order为空
        if (order == null) {
            return ServerResponse.createByErrorMessage ("该用户此订单不存在");
        }
        //已支付,不能取消订单
        if (order.getStatus () != Const.OrderStatusEnum.NO_PAY.getCode ()) {
            return ServerResponse.createByErrorMessage ("已付款,无法取消订单");
        }
        //更新订单状态为取消支付
        Order updateOrder = new Order ();
        updateOrder.setId (order.getId ());
        updateOrder.setStatus (Const.OrderStatusEnum.CANCELED.getCode ());

        int row = orderMapper.updateByPrimaryKeySelective (updateOrder);
        if (row > 0) {
            return ServerResponse.createBySuccess ();
        }
        return ServerResponse.createByError ();
    }
    //删除订单
    public ServerResponse<String> del ( Integer userId, Long orderNo ) {
        //选取订单
        Order order = orderMapper.selectByUserIdAndOrderNo (userId, orderNo);
        //order为空
        if (order == null) {
            return ServerResponse.createByErrorMessage ("该用户此订单不存在");
        }
        int result=orderMapper.deleteByUserIdOrderNo(userId,orderNo);
        if(result>0){
            return ServerResponse.createBySuccessMessage("删除订单成功！");
        }
        return ServerResponse.createByErrorMessage("订单未删除成功！");

    }
    //获取订单中的产品信息
    public ServerResponse getOrderCartProduct ( Integer userId ) {
        OrderProductVo orderProductVo = new OrderProductVo ();
        //获取用户购物车中选中的产品
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId (userId);
        ServerResponse serverResponse = this.getCartOrderItem (userId, cartList);
        if (!serverResponse.isSuccess ()) {
            return serverResponse;
        }
        //获取List<OrderItem>
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData ();
        //List<OrderItem>->List<OrderItemVo>
        List<OrderItemVo> orderItemVoList = Lists.newArrayList ();
        BigDecimal payment = new BigDecimal ("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add (payment.doubleValue (), orderItem.getTotalPrice ().doubleValue ());
            //orderItem->orderItemVo,并加入List<OrderItemVo>
            orderItemVoList.add (assembleOrderItemVo (orderItem));
        }
        //组装orderProductVo
        orderProductVo.setProductTotalPrice (payment);
        orderProductVo.setOrderItemVoList (orderItemVoList);
        orderProductVo.setImageHost (PropertiesUtil.getProperty ("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess (orderProductVo);
    }

    //查看订单详情
    public ServerResponse<OrderVo> getOrderDetail ( Integer userId, Long orderNo ) {
        //获取订单
        Order order = orderMapper.selectByUserIdAndOrderNo (userId, orderNo);
        if (order != null) {
            //获取所有订单Item
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId (orderNo, userId);
            //组装OrderVo
            OrderVo orderVo = new OrderVo ();
            orderVo = this.assembleOrderVo (order, orderItemList);
            return ServerResponse.createBySuccess (orderVo);
        }
        return ServerResponse.createByErrorMessage ("没有找到该订单");

    }

    public ServerResponse<PageInfo> getOrderList ( Integer userId, int pageNum, int pageSize ) {
        //开始分页
        PageHelper.startPage (pageNum, pageSize);
        //选取用户订单
        List<Order> orderList = orderMapper.selectByUserId (userId);
        //orderList是一个Page对象
        PageInfo pageInfo = new PageInfo (orderList);
        //List<Order>->List<OrderVo>
        List<OrderVo> orderVoList = this.assembleOrderVoList (orderList, userId);
        pageInfo.setList (orderVoList);
        return ServerResponse.createBySuccess (pageInfo);
    }

    //后台管理员
    public ServerResponse<PageInfo> manageList ( int pageNum, int pageSize ) {
        //开始分页
        PageHelper.startPage (pageNum, pageSize);
        //选择所有订单
        List<Order> orderList = orderMapper.selectAllOrder ();
        PageInfo pageInfo = new PageInfo (orderList);
        List<OrderVo> orderVoList = this.assembleOrderVoList (orderList, null);
        pageInfo.setList (orderList);
        return ServerResponse.createBySuccess (pageInfo);
    }

    public ServerResponse<OrderVo> manageDetail ( Long orderNo ) {
        //选取该订单
        Order order = orderMapper.selectByOrderNo (orderNo);
        if (order != null) {
            //获取订单各项
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo (orderNo);
            //组装OrderVo
            OrderVo orderVo = this.assembleOrderVo (order, orderItemList);
            return ServerResponse.createBySuccess (orderVo);
        }
        return ServerResponse.createByErrorMessage ("订单不存在！");
    }

    public ServerResponse<PageInfo> manageSearch ( Long orderNo, int pageNum, int pageSize ) {
        //开始分页
        PageHelper.startPage (pageNum, pageSize);
        //查询该订单
        Order order = orderMapper.selectByOrderNo (orderNo);
        if (order != null) {
            //获取订单各项
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo (orderNo);
            PageInfo pageInfo = new PageInfo (Lists.newArrayList (order));
            //组装OrderVo
            OrderVo orderVo = this.assembleOrderVo (order, orderItemList);
            pageInfo.setList (Lists.newArrayList (orderVo));
            return ServerResponse.createBySuccess (pageInfo);
        }
        return ServerResponse.createByErrorMessage ("订单不存在！");
    }

    public ServerResponse<String> manageSendGoods ( Long orderNo ) {
        //查询该订单
        Order order = orderMapper.selectByOrderNo (orderNo);
        if (order != null) {
            //已经付款
            if (order.getStatus () == Const.OrderStatusEnum.PAID.getCode ()) {
                //改为已发货（40）
                order.setStatus (Const.OrderStatusEnum.SHIPPED.getCode ());
                order.setSendTime (new Date ());
                orderMapper.updateByPrimaryKeySelective (order);
                return ServerResponse.createBySuccess ("发货成功");
            }
            return ServerResponse.createByErrorMessage ("未付款，不能发货！");
        }
        return ServerResponse.createByErrorMessage ("订单不存在！");
    }


    //-----------------------------------支付相关Service------------------------------------

     //简单打印应答
    private void dumpResponse ( AlipayResponse response ) {
        if (response != null) {
            logger.info (String.format ("code:%s, msg:%s", response.getCode (), response.getMsg ()));
            if (StringUtils.isNotEmpty (response.getSubCode ())) {
                logger.info (String.format ("subCode:%s, subMsg:%s", response.getSubCode (),
                        response.getSubMsg ()));
            }
            logger.info ("body:" + response.getBody ());
        }
    }

    /**
     * @param orderNo 支付的订单号
     * @param userId  支付订单的用户
     * @param path    二维码上传的路径
     * @return
     */
    public ServerResponse pay ( Long orderNo, Integer userId, String path ) {
        Map<String, String> resultMap = Maps.newHashMap ();
        Order order = orderMapper.selectByUserIdAndOrderNo (userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage ("用户没有该订单");
        }
        resultMap.put ("orderNo", String.valueOf (order.getOrderNo ()));
        //生成支付二维码（从官网demo中获取）
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = String.valueOf (order.getOrderNo ());//订单号

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder ().append ("网上商城扫码支付，订单号：").append(outTradeNo).toString ();//扫码时看到的描述

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment ().toString ();//总价

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";//不能打折的金额


        //默认
        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";//使用默认的与zfb签约的pid
        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder ().append ("订单号：").append (outTradeNo).append ("购买商品共").append (totalAmount).append ("元").toString ();
        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";
        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";
        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams ();
        extendParams.setSysServiceProviderId ("2088100200300400500");
        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";



        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId (orderNo, userId);
        //构建goods
        for (OrderItem orderItem : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance (orderItem.getProductId ().toString (),
                    orderItem.getProductName (), BigDecimalUtil.mul (orderItem.getCurrentUnitPrice ().doubleValue (), new Double (100).doubleValue ()).longValue (),
                    orderItem.getQuantity ());
            //填充goodsDetailList
            goodsDetailList.add (goods);

        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder ()
                .setNotifyUrl (PropertiesUtil.getProperty ("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setSubject (subject).setTotalAmount (totalAmount).setOutTradeNo (outTradeNo)
                .setUndiscountableAmount (undiscountableAmount).setSellerId (sellerId).setBody (body)
                .setOperatorId (operatorId).setStoreId (storeId).setExtendParams (extendParams)
                .setTimeoutExpress (timeoutExpress)

                .setGoodsDetailList (goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init ("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder ().build ();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate (builder);

        switch (result.getTradeStatus ()) {
            case SUCCESS:
                logger.info ("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse ();
                dumpResponse (response);

                File folder = new File (path);
                //文件不存在，则创建
                if (!folder.exists ()) {
                    folder.setWritable (true);
                    folder.mkdirs ();

                }
                // 需要修改为运行机器上的路径
                //注意“/”
                //设置文件（二维码）放置路径
                String qrPath = String.format (path + "/qr-%s.png", response.getOutTradeNo ());
                //设置文件名称，response.getOutTradeNo()会替换到%s
                String qrFileName = String.format ("/qr-%s.png", response.getOutTradeNo ());
                //生成二维码图片
                ZxingUtils.getQRCodeImge (response.getQrCode (), 256, qrPath);
                //生成文件
                File targetFile = new File (path, qrFileName);
                logger.info ("qrPath:" + qrPath);

                //获取二维码图片的url地址
                String qrUrl = PropertiesUtil.getProperty ("ftp.server.http.prefix") + targetFile.getName ();

                resultMap.put ("qrUrl", qrUrl);
                return ServerResponse.createBySuccess (resultMap);

            case FAILED:
                logger.error ("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage ("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error ("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage ("系统异常，预下单状态未知!!!");

            default:
                logger.error ("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage ("不支持的交易状态，交易返回异常!!!");
        }
    }

    public ServerResponse aliCallback ( Map<String, String> params ) {
        //获取商户订单号
        Long orderNo = Long.valueOf (params.get ("out_trade_no"));
        //获取支付宝交易号
        String tradeNo = params.get ("trade_no");
        String tradeStatus = params.get ("trade_status");
        Order order = orderMapper.selectByOrderNo (orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage ("非此网站订单！");
        }
        //订单已付款
        if (order.getStatus () >= Const.OrderStatusEnum.PAID.getCode ()) {
            return ServerResponse.createBySuccess ("支付宝重复调用！");
        }
        //判断trade_status是否为success
        if (Const.AlipayCallBack.TRADE_STATUS_TRADE_SUCCESS.equals (tradeStatus)) {
            //修改数据库中此订单状态
            order.setPaymentTime (DateTimeUtil.strToDate (params.get ("gmt_payment")));
            order.setStatus (Const.OrderStatusEnum.PAID.getCode ());
            orderMapper.updateByPrimaryKeySelective (order);
        }
        PayInfo payInfo = new PayInfo ();
        payInfo.setUserId (order.getUserId ());
        payInfo.setOrderNo (order.getOrderNo ());
        //支付平台，状态，流水号
        payInfo.setPayPlatform (Const.PayPlatformEnum.ALIPAY.getCode ());
        payInfo.setPlatformStatus (tradeStatus);
        payInfo.setPlatformNumber (tradeNo);
        //插入支付信息记录
        payInfoMapper.insert (payInfo);
        return ServerResponse.createBySuccess ();

    }

    public ServerResponse queryOrderPayStatus ( Long orderNo, Integer userId ) {
        Order order = orderMapper.selectByUserIdAndOrderNo (userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage ("用户没有该订单！");
        }
        //订单付款成功
        if (order.getStatus () >= Const.OrderStatusEnum.PAID.getCode ()) {
            return ServerResponse.createBySuccess ();
        }
        return ServerResponse.createByError ();
    }


}
