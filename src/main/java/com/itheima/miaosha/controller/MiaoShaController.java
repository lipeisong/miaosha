package com.itheima.miaosha.controller;

import com.itheima.miaosha.access.AccessLimit;
import com.itheima.miaosha.activemq.Producer;
import com.itheima.miaosha.entity.MiaoshaOrder;
import com.itheima.miaosha.entity.MiaoshaUser;
import com.itheima.miaosha.entity.OrderInfo;
import com.itheima.miaosha.result.CodeMsg;
import com.itheima.miaosha.result.Result;
import com.itheima.miaosha.service.GoodsService;
import com.itheima.miaosha.service.MiaoshaService;
import com.itheima.miaosha.service.OrderService;
import com.itheima.miaosha.util.UUIDUtil;
import com.itheima.miaosha.vo.GoodsVo;
import com.itheima.miaosha.activemq.MiaoMessage;
import com.itheima.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

@Controller
public class MiaoShaController implements InitializingBean {
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private HashMap<Long,Boolean> localOverMap=new HashMap<>();

    @Autowired
    private Producer mqProducer;

    /**
     * 获取秒杀的接口路径
     * @param goodsId
     * @param verifyCode
     * @param user
     * @return
     */
    @RequestMapping("/miaosha/path")
    @ResponseBody
    @AccessLimit(needLogin = true,maxCount = 5,seconds = 50000)
    public Result<String> getMiaoShaPath(@RequestParam("goodsId")Long goodsId,MiaoshaUser user,Integer verifyCode){
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check){
            return Result.error(CodeMsg.VERIFYCODE_ERROR);
        }
        String uuid = UUIDUtil.uuid();
        String path=user.getId().toString()+goodsId.toString()+uuid;

        //把路径放到缓存当中
        redisTemplate.opsForValue().set("miaosha:path"+user.getId().toString(),path);
        return Result.success(path);
    }



    @RequestMapping("/miaosha/{path}/do_miaosha")
    @ResponseBody
    public Result<Integer> miaoSha(@RequestParam("goodsId")Long goodsId, Model model, MiaoshaUser miaoshaUser,
                          @PathVariable(value = "path") String path){
        //判断用户是否已经登录
        if(miaoshaUser==null){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        model.addAttribute("user",miaoshaUser);


        //判断库存还有没有
        ////内存标记，减少redis访问
        //先判断库存数量的标记，如果没有了直接返回
        Boolean flag = localOverMap.get(goodsId);

        if(flag){
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
        //redis的预减库存
        Integer count =Integer.parseInt(redisTemplate.opsForValue().get(goodsId.toString())) ;

        count-=1;

        if(count<0){
            localOverMap.put(goodsId,true);
            miaoshaService.setGoodsOver(goodsId);
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
        //重新把库存放到redis中
        redisTemplate.opsForValue().set(goodsId.toString(),count.toString());

        GoodsVo goods = goodsService.findGoodById(goodsId);

        //判断有没有已经秒杀到
       MiaoshaOrder order=orderService.findOrderByUserIdAndGoodsId(miaoshaUser.getId(),goodsId);

        if(order!=null){
           model.addAttribute("errormsg",CodeMsg.REPEATE_MIAOSHA);
           return Result.error(CodeMsg.REPEATE_MIAOSHA);

       }
       /* //减库存，创建订单
       OrderInfo orderInfo= miaoshaService.miaosha(miaoshaUser,goods);

        //秒杀成功返回一个订单详情页面
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goods);*/

       //消息入队
        MiaoMessage mm = new MiaoMessage();

        mm.setMiaoshaUser(miaoshaUser);
        mm.setGoodsId(goodsId);

        mqProducer.send(mm);
        return Result.success(0);//排队中
    }

    /**
     * 客户端轮询秒杀结果
     *
     * @return
     * orderId: 成功
     * -1：秒杀失败
     * 0：排队中
     */
    @RequestMapping("/miaosha/result")
    @ResponseBody
    public Result<Long> getResult(Model model,@RequestParam("goodsId")Long goodsId,MiaoshaUser miaoshaUser){

        if(miaoshaUser==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user",miaoshaUser);
        long result = miaoshaService.getMiaoshaResult(miaoshaUser.getId(), goodsId);

        return Result.success(result);
    }


    /**
     * 系统初始化，把商品数量存到缓存中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //查询出所有的商品
        List<GoodsVo> goods = goodsService.findAllGoods();
        if(goods!=null){
            for(GoodsVo goodsVo:goods){
                redisTemplate.opsForValue().set(goodsVo.getId().toString(),goodsVo.getStockCount().toString());
                localOverMap.put(goodsVo.getId(),false);

            }
        }

    }

    /**
     * 获取图片验证码
     */
    @RequestMapping("/miaosha/verifyCode")
    @ResponseBody
    public Result<String> getVerifyCode(HttpServletResponse response,@RequestParam("goodsId")Long goodsId,MiaoshaUser user){
       //判断用户有没有登录
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        try{
            BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);

            ServletOutputStream out = response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;

        }catch (Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }



    }

    /**
     * 获取订单详情
     */
    @RequestMapping("/order/detail")
    @ResponseBody
    public Result<OrderDetailVo> getOrderInfo(@RequestParam("orderId")Long orderId){
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        OrderInfo orderInfo = orderService.getOrderInfoById(orderId);
        orderDetailVo.setOrder(orderInfo);
        GoodsVo goodsVo = goodsService.findGoodById(orderInfo.getGoodsId());
        orderDetailVo.setGoods(goodsVo);
        return Result.success(orderDetailVo);
    }
}
