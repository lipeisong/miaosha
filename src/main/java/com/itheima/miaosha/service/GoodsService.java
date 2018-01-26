package com.itheima.miaosha.service;
import com.itheima.miaosha.dao.GoodsDao;
import com.itheima.miaosha.entity.MiaoshaGoods;
import com.itheima.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    private GoodsDao goodsDao;

    public List<GoodsVo> findAllGoods(){
        return goodsDao.findAllGoods();
    }

    public GoodsVo findGoodById(Long goodsId) {
        return goodsDao.findGoodById(goodsId);
    }

    public int reduceStock(GoodsVo goods) {
        MiaoshaGoods miaoshaGoods = new MiaoshaGoods();
        miaoshaGoods.setGoodsId(goods.getId());


        return goodsDao.reduceStock(miaoshaGoods);
    }
}
