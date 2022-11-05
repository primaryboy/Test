package com.mall.dao;

import com.mall.pojo.PayInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PayInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PayInfo record);

    int insertSelective(PayInfo record);

    PayInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PayInfo record);

    int updateByPrimaryKey(PayInfo record);

    List<Map<Date, Integer>> selectOrderCountByDay ( );
}