package com.mall.common;

public enum ResponseCode {
    //成功,描述是SUCCESS
    SUCCESS(0,"SUCCESS"),
    //错误
    ERROR(1,"ERROR"),
    //需要登录
    NEED_LOGIN(10,"NEED_LOGIN"),
    //参数错误
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");
    //声明两个属性
    private final int code;
    private final String desc;
    //枚举的构造器

    ResponseCode(int code,String desc){
        this.code=code;
        this.desc=desc;
    }
    //将code、desc开放出去
    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }
}
