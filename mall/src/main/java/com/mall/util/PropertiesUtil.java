package com.mall.util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;


public class PropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties props;
    //使用静态块解决tomcat（在类被加载的时候执行，且只执行一次）>普通代码块>构造代码块（运行每次都执行）
    static {
        //使用resources下的mall.properties
        String fileName = "mall.properties";
        props = new Properties();
        //必须要捕获异常
        try {
            //读取配置文件
            //找到当前流的位置
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取异常",e);
        }
    }
    public static String getProperty(String key){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }
    //进行一个重载，封装value，如果传过来是空的，就把defaultValue传过去
    public static String getProperty(String key,String defaultValue){

        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }



}
