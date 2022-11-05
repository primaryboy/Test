package com.mall.common;

import com.google.common.collect.Sets;

import java.util.Set;


public class Const {
    //登录返回给前端的状态
    public static final String CURRENT_USER="CurrentUser";
    //判断用户名，email是否存在
    public static final String EMAIL="email";
    public static final String USERNAME="username";
    //身份
    public interface Role{
        int ROLE_CUSTOMER=0;//普通顾客
        int ROLE_ADMIN=1;//管理员
    }
    //产品排序方式
    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet ("price_desc", "price_asc");
    }
    //购物车状态
    public interface Cart {
        //选中该购物车该产品
        int CHECKED = 1;
        //未选中该购物车该产品
        int UN_CHECKED = 0;
        //限制购买数量失败
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        //限制购买数量成功
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }
    //产品状态
    public enum ProductStatusEnum {
        ON_SALE (1, "在线");
        private String value;
        private int code;

        public String getValue () {
            return value;
        }

        public int getCode () {
            return code;
        }

        ProductStatusEnum ( int code, String value ) {
            this.code = code;
            this.value = value;
        }
    }
    //订单状态
    public enum OrderStatusEnum {
        CANCELED (0, "已取消"),
        NO_PAY (10, "未支付"),
        PAID (20, "已付款"),
        SHIPPED (40, "已发货"),
        ORDER_SUCCESS (50, "订单完成"),
        ORDER_CLOSE (60, "订单关闭");

        private String value;
        private int code;

        OrderStatusEnum ( int code, String value ) {
            this.code = code;
            this.value = value;
        }

        public String getValue () {
            return value;
        }

        public int getCode () {
            return code;
        }

        public static OrderStatusEnum codeOf ( int code ) {
            for (OrderStatusEnum orderStatusEnum : values ()) {
                if (orderStatusEnum.getCode () == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException ("没有找到对应的枚举");
        }
    }
    //支付宝回调返回的参数和商户端的响应参数
    public interface AlipayCallBack {
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";

    }
    //支付平台
    public enum PayPlatformEnum {
        ALIPAY (1, "支付宝");

        PayPlatformEnum ( int code, String value ) {
            this.code = code;
            this.value = value;
        }

        private String value;
        private int code;

        public String getValue () {
            return value;
        }

        public int getCode () {
            return code;
        }
    }

    public enum PaymentTypeEnum {
        ONLINE_PAY (1, "在线支付");

        private String value;
        private int code;

        PaymentTypeEnum ( int code, String value ) {
            this.code = code;
            this.value = value;
        }

        public String getValue () {
            return value;
        }

        public int getCode () {
            return code;
        }

        public static PaymentTypeEnum codeOf ( int code ) {
            //values是该类的枚举实例数组
            for (PaymentTypeEnum paymentTypeEnum : values ()) {
                if (paymentTypeEnum.getCode () == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException ("没有找到对应的枚举");
        }

    }
}
