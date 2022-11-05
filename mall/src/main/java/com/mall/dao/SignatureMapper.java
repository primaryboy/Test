package com.mall.dao;

import com.mall.pojo.Signature;
import org.apache.ibatis.annotations.Param;

public interface SignatureMapper {
    int deleteByPrimaryKey(String username);

    int insert(Signature record);

    int insertSelective(Signature record);

    Signature selectByPrimaryKey(String username);

    int updateByPrimaryKeySelective(Signature record);

    int updateByPrimaryKey(Signature record);
    int checkUsername(String username);
    int updateByName(@Param("username")String username, @Param("sign")String sign);
    String getSignature(String username);
    String getpublickey(String username);
}