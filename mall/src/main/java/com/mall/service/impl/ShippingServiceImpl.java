package com.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.dao.ShippingMapper;
import com.mall.pojo.Shipping;
import com.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add ( Integer userId, Shipping shipping ) {
        //设置收货地址的用户id
        shipping.setUserId (userId);
        int resultCount = shippingMapper.insert (shipping);
        if (resultCount > 0) {
            Map result = new HashMap ();
            result.put ("shippingId", shipping.getId ());
            return ServerResponse.createBySuccess ("新建地址成功!", result);
        }
        return ServerResponse.createBySuccessMessage ("新建地址失败!");
    }

    //防止横向越权
    public ServerResponse<String> del ( Integer userId, Integer shippingId ) {
        int resultCount = shippingMapper.deleteByUserIdShippingId (userId, shippingId);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess ("删除地址成功!");
        }
        return ServerResponse.createByErrorMessage ("删除地址失败!");
    }

    //防止横向越权
    public ServerResponse update ( Integer userId, Shipping shipping ) {
        shipping.setUserId (userId);
        //updateByShipping会判断userId
        int resultCount = shippingMapper.updateByShipping (shipping);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess ("更新地址成功!");
        }
        return ServerResponse.createBySuccessMessage ("更新地址失败!");
    }

    //防止横向越权
    public ServerResponse<Shipping> select (Integer userId, Integer shippingId ) {
        Shipping shipping = shippingMapper.selectByUserIdShippingId (userId, shippingId);
        if (shipping == null) {
            return ServerResponse.createByErrorMessage ("无法查询到该地址!");
        }
        return ServerResponse.createBySuccess ("查询地址成功!", shipping);
    }

    //地址分页
    public ServerResponse<PageInfo> list (Integer userId, int pageNum, int pageSize ) {
        PageHelper.startPage (pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByuserId (userId);
        PageInfo pageInfo = new PageInfo (shippingList);
        return ServerResponse.createBySuccess (pageInfo);
    }

}
