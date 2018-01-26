package com.itheima.miaosha.service;
import com.itheima.miaosha.entity.MiaoshaOrder;
import com.itheima.miaosha.entity.MiaoshaUser;
import com.itheima.miaosha.entity.OrderInfo;
import com.itheima.miaosha.vo.GoodsVo;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class MiaoshaService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Transactional
    public OrderInfo miaosha(MiaoshaUser miaoshaUser, GoodsVo goods) {
        //减库存
        goodsService.reduceStock(goods);

        //创建订单
        OrderInfo orderInfo=orderService.createOrder(miaoshaUser,goods);


        return orderInfo;
    }

    /**
     * 获取图片业务层
     */

    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisTemplate.opsForValue().set("verifyCode:"+user.getId()+":"+goodsId,rnd+"");
        //输出图片
        return image;
    }

    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private static char[] ops = new char[] {'+', '-', '*'};

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }
        Integer codeOld = Integer.parseInt(redisTemplate.opsForValue().get("verifyCode:" + user.getId() + ":" + goodsId));


        if(codeOld == null || codeOld - verifyCode != 0 ) {
            return false;
        }
        //删除验证码
        redisTemplate.delete("verifyCode:" + user.getId() + ":" + goodsId);
        return true;
    }

    public long getMiaoshaResult(Long userId,long goodsId){
        MiaoshaOrder order = orderService.findOrderByUserIdAndGoodsId(userId, goodsId);
        if(order!=null){
            return order.getOrderId();
        }else{
            boolean isOVer=getGoodsOver(goodsId);
            if(isOVer){
                return -1;
            }else {
                return 0;
            }
        }

    }

    private boolean getGoodsOver(long goodsId) {
       return redisTemplate.hasKey("miao:isOver"+goodsId);
    }

    public void setGoodsOver(long goodsId){
        redisTemplate.opsForValue().set("miao:isOver"+goodsId,true+"");
    }

}
