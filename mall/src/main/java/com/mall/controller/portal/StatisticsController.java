package com.mall.controller.portal;

import com.mall.common.ServerResponse;
import com.mall.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/statistics")
public class StatisticsController {
    @Autowired
    private IStatisticsService iStatisticsService;

    @RequestMapping("/get_order_count_by_day.do")
    @ResponseBody
    //获取每一天的销售量
    public ServerResponse getOrderCountByDay ( ) {
        return iStatisticsService.getOrderCountByDay ();
    }

    @RequestMapping("/get_order_money_by_day.do")
    @ResponseBody
    //获取每天的销售金额
    public ServerResponse getOrderMoneyByDay ( ) {
        return iStatisticsService.getOrderMoneyByDay ();
    }

}
