package com.mall.service.impl;

import com.mall.common.ServerResponse;
import com.mall.dao.OrderMapper;
import com.mall.dao.PayInfoMapper;
import com.mall.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Service("iStatisticsService")
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private OrderMapper orderMapper;

    public ServerResponse getOrderCountByDay(){
        List<Map<Date, Integer>> map;
        map=payInfoMapper.selectOrderCountByDay ();
        return ServerResponse.createBySuccess (map);
    }

    public ServerResponse getOrderMoneyByDay(){
        List<Map<Date, Double>> map;
        map= orderMapper.selectOrderMoneyByDay ();
        return ServerResponse.createBySuccess (map);
    }



}
