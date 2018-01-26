package com.itheima.miaosha.activemq;

import com.itheima.miaosha.entity.MiaoshaUser;
import com.itheima.miaosha.service.GoodsService;
import com.itheima.miaosha.service.MiaoshaService;
import com.itheima.miaosha.util.JsonUtils;
import com.itheima.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private GoodsService goodsService;

    @JmsListener(destination = "sample.queue")
    public void receiveQueue(String message) {

        MiaoMessage miaoMessage = JsonUtils.jsonToPojo(message, MiaoMessage.class);
        //取出用户
        MiaoshaUser user = miaoMessage.getMiaoshaUser();
        Long id = miaoMessage.getGoodsId();
        GoodsVo goodsVo = goodsService.findGoodById(id);
        miaoshaService.miaosha(user,goodsVo);

    }
}
