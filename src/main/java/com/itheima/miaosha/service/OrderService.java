package com.itheima.miaosha.service;
import com.itheima.miaosha.dao.OrderDao;
import com.itheima.miaosha.entity.MiaoshaOrder;
import com.itheima.miaosha.entity.MiaoshaUser;
import com.itheima.miaosha.entity.OrderInfo;
import com.itheima.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    public MiaoshaOrder findOrderByUserIdAndGoodsId(Long userId, Long goodsId) {
        return orderDao.findOrderByUserIdAndGoodsId(userId,goodsId);
    }

    public OrderInfo getOrderInfoById(Long orderId){
        return orderDao.getOrderById(orderId);
    }

    @Transactional
    public OrderInfo createOrder(MiaoshaUser miaoshaUser, GoodsVo goods) {
        //补全订单的属性
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setStatus(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(miaoshaUser.getId());
        //插入订单详情表
        Long orderId=orderDao.insert(orderInfo);
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setOrderId(orderId);
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setUserId(miaoshaUser.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);
        return orderInfo;
    }
}
