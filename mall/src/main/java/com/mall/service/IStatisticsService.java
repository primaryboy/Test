package com.mall.service;

import com.mall.common.ServerResponse;


public interface IStatisticsService {
    ServerResponse getOrderCountByDay ( );

    ServerResponse getOrderMoneyByDay ( );

}
