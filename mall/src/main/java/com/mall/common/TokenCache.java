package com.mall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class TokenCache {
    //声明日志，选择loggerback里面的类型
    private  static Logger logger=LoggerFactory.getLogger(TokenCache.class);
    public static final String TOKEN_PREFIX="token_";
    //声明一个静态的内存块---是guawa里面的本地缓存(key和value都是string类型)
    //构建本地的cache，是调用链的模式---并进行初始化，设置缓存的初始化容量
    //调用maximumsize方法设置缓存的最大化容量，当超过这个容量时，guawa的这个cache就会用LRU算法（最少使用算法）来移除缓存项
    //改缓存的有效期是12个小时
    //build使用cacheloder抽象类，我们需要写一个匿名的实现
    private static LoadingCache<String,String> localCahce= CacheBuilder.newBuilder()
            .initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现，当调用get取值时，如果key没有对应的值，就调用这个方法进行加载----key没有命中就调用load方法
                @Override
                public String load(String s) throws Exception {
                    //使用假null
                    return "null";
                }
            });
    //设置key
    public static void setKey(String key,String value){
        localCahce.put(key,value);
    }
    //得到key
    public static String getKey(String key){
        String value=null;
        //可能出现异常try--catch一下
        try {
            value=localCahce.get(key);
            //用到假null
            //这个key没有对应值就会返回一个内容：null
            if("null".equals(value)){
                return null;
            }
            return value;
        }

        catch (Exception e){
            //打印异常堆栈
            logger.error("localCache get error",e);
        }
        return null;
    }
}
