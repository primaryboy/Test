package com.mall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;


//使用泛型声明这个类，可以传一个泛型T，代表响应里边要封装的数据对象的类型,并实现序列化接口
//将ServerResponse进行序列化，返回给前端，isSuccess加上Jackson的注解就不会显示在json里，返回给前端
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)//出错时只返回status、msg，data不返回（data默认为一个有key的空结点，value=null）
//保证序列化json的时候，如果时null的对象，key也会消失
public class ServerResponse<T> implements Serializable {
    //响应对象里面有三个属性：status、msg、data
    private int status;
    private  String msg;
    private T data;
    //不同类型的私有构造器，封装public方法时调用比较优雅简明通用
    //把参数的status传给这个对象的status
    private ServerResponse(int status){
        this.status=status;

    }

    //第二个构造方法
    private ServerResponse(int status,T data){
        this.status=status;
        this.data=data;
    }
    //第三个构造方法
    private ServerResponse(int status,String msg,T data){
        this.status=status;
        this.msg=msg;
        this.data=data;
    }
    private ServerResponse(int status,String msg){
        this.status=status;
        this.msg=msg;
    }
    //封装
    //枚举魔法数字做归类
    @JsonIgnore
    //使之不在json序列化结果中
    public boolean isSuccess(){
        //如果该状态与RC的code状态相同==0返回true
        return  this.status==ResponseCode.SUCCESS.getCode();
    }
    public  int getStatus(){
        return status;
    }
    public  T getData(){
        return data;
    }
    public  String getMsg(){
        return msg;
    }
    //泛型,通过一个成功的创建这个对象
    public static <T> ServerResponse<T> createBySuccess(){
        //调用私有构造器，不需要传参数,返回一个status
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());

    }
    //成功，返回一个文本供前端提示使用
    public static <T> ServerResponse<T> createBySuccessMessage(String msg){
        //不能直接复制上述语句，需要msg
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    //返回一个正确的数据,创建一个成功的服务器响应，将data传输进去，只会调用第二个构造方法
    public static <T> ServerResponse<T> createBySuccess(T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    //使用第三个构造器，传了消息和数据
    public static <T> ServerResponse<T> createBySuccess(String msg,T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }
    //出错
    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }
    //调用第四个构造方法，情况：用户名存在
    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }
    //需要登录，提示参数错误带code，把code做成一个变量的方法，如果参数错误，需要暴露出参数错误的响应
    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage) {
        return new ServerResponse<T>(errorCode, errorMessage);
    }
}
