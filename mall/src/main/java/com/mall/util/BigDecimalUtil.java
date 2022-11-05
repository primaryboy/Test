package com.mall.util;

import java.math.BigDecimal;


public class BigDecimalUtil {
//私有构造器，不能被外部访问
    private BigDecimalUtil(){

    }

    public static BigDecimal add(double num1,double num2){
        BigDecimal b1=new BigDecimal(Double.toString (num1));
        BigDecimal b2=new BigDecimal(Double.toString (num2));
        return b1.add (b2);
    }

    public static BigDecimal sub(double num1,double num2){
        BigDecimal b1=new BigDecimal(Double.toString (num1));
        BigDecimal b2=new BigDecimal(Double.toString (num2));
        return b1.subtract (b2);
    }

    public static BigDecimal mul(double num1,double num2){
        BigDecimal b1=new BigDecimal(Double.toString (num1));
        BigDecimal b2=new BigDecimal(Double.toString (num2));
        return b1.multiply (b2);
    }

    public static BigDecimal div(double num1,double num2){
        BigDecimal b1=new BigDecimal(Double.toString (num1));
        BigDecimal b2=new BigDecimal(Double.toString (num2));
        //除不尽时，保留两位小数（四舍五入）
        return b1.divide (b2,2,BigDecimal.ROUND_HALF_DOWN);
    }
}
