package com.itheima.miaosha.dao;

import com.itheima.miaosha.entity.MiaoshaGoods;
import com.itheima.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    @Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from goods g, miaosha_goods mg where g.id=mg.goods_id")
    List<GoodsVo> findAllGoods();

    @Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from goods g, miaosha_goods mg where g.id=mg.goods_id and g.id=#{goodsId}")
    GoodsVo findGoodById(@Param("goodsId") Long goodsId);

    //通过控制库存数量大于0来解决卖超的问题
    @Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count>0")
    int reduceStock(MiaoshaGoods goods);
}
