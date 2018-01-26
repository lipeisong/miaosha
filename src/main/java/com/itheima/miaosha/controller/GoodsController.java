package com.itheima.miaosha.controller;

import com.itheima.miaosha.entity.MiaoshaUser;
import com.itheima.miaosha.result.Result;
import com.itheima.miaosha.service.GoodsService;
import com.itheima.miaosha.vo.GoodsDetailVo;
import com.itheima.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //注入thymeleaf的视图解析器
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 商品列表
     * @param miaoshaUser
     * @param model
     * @return
     */
    @RequestMapping(value = "/goods/to_list",produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request,HttpServletResponse response,
                         MiaoshaUser miaoshaUser, Model model){

        //先从缓存当中取页面，如果能取到直接返回给客户端
        String html = redisTemplate.opsForValue().get("goodsList:goods");

        model.addAttribute("user",miaoshaUser);
        List<GoodsVo> goodsVos = goodsService.findAllGoods();
        model.addAttribute("goodsList",goodsVos);

        //没有取到缓存就手动渲染页面
        if (html == null) {

            IContext iContext=new WebContext(request,response,request.getServletContext(),
                    request.getLocale(),model.asMap());

            html = thymeleafViewResolver.getTemplateEngine()
                    .process("goods_list", iContext);

            //把页面放到缓存当中
            redisTemplate.opsForValue().set("goodsList:goods",html,60, TimeUnit.SECONDS);
        }


        return html;
    }


    /**
     * 商品详情页
     * @param miaoshaUser
     * @param model
     * @return
     */
    @RequestMapping(value = "/goods/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> toDetail(
                           MiaoshaUser miaoshaUser,
                           @PathVariable("goodsId") Long goodsId){



       GoodsVo goods=goodsService.findGoodById(goodsId);

        //判断是否已经开始
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now=System.currentTimeMillis();

        //秒杀的状态
        int miaoshaStatus=0;

        //倒计时
        int remianSeconds=0;

        if(now<startAt){//秒杀还没开始，倒计时
            miaoshaStatus=0;
            remianSeconds= (int) ((startAt-now)/1000);
        }else if(now>endAt){//秒杀已经结束
            miaoshaStatus=2;
            remianSeconds=-1;
        }else {//秒杀进行中
            miaoshaStatus=1;
            remianSeconds=0;
        }

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoods(goods);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remianSeconds);
        goodsDetailVo.setUser(miaoshaUser);


        return Result.success(goodsDetailVo);
    }
}
