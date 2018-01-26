package com.itheima.miaosha.dao;

import com.itheima.miaosha.entity.MiaoshaOrder;
import com.itheima.miaosha.entity.OrderInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {

    @Select("select * from miaosha_order  where user_id=#{userId} and goods_Id=#{goodsId} ")
    MiaoshaOrder findOrderByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    //向订单详情表插入数据
    @Insert("insert into order_info (user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)"+
            "values(#{userId},#{goodsId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate})")
    @SelectKey(keyColumn = "id",keyProperty = "id",before = false,resultType = Long.class,statement ="select last_insert_id()")
    Long insert(OrderInfo orderInfo);

    //向秒杀订单表插入数据
    @Insert("insert into miaosha_order (user_id, goods_id, order_id) values(#{userId},#{orderId},#{goodsId})")
    int insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);

    @Select("select * from order_info where id = #{orderId}")
    public OrderInfo getOrderById(@Param("orderId")long orderId);
}
