package com.itheima.miaosha.service;
import com.itheima.miaosha.Exception.GlobalException;
import com.itheima.miaosha.dao.MiaoshaUserDao;
import com.itheima.miaosha.entity.MiaoshaUser;
import com.itheima.miaosha.result.CodeMsg;
import com.itheima.miaosha.util.JsonUtils;
import com.itheima.miaosha.util.MD5Util;
import com.itheima.miaosha.util.UUIDUtil;
import com.itheima.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Service
public class MiaoshaUserService {
    //引入redis模板
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    public boolean login(LoginVo loginVo, HttpServletResponse response){
        //依据表单提交的id去查询秒杀的用户信息

        //先查缓存
        String jsonUser = redisTemplate.opsForValue().get("Miaouser:id" + loginVo.getMobile());

        MiaoshaUser miaoshaUser;

        if(StringUtils.isEmpty(jsonUser)){
            //从数据库中取
           miaoshaUser = miaoshaUserDao.getMiaoShaUserById(Long.parseLong(loginVo.getMobile()));

           if(miaoshaUser!=null){
               //把用户信息放到缓存中
               redisTemplate.opsForValue().set("Miaouser:id"+loginVo.getMobile(),JsonUtils.objectToJson(miaoshaUser));
           }



        }else {
            miaoshaUser = JsonUtils.jsonToPojo(jsonUser, MiaoshaUser.class);
        }


        if(miaoshaUser==null){
            //手机号码不存在
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }else{
            // 比对页面输入的密码和数据的密码
            String inputPassword = loginVo.getPassword();

            //获得第一次md5加密的盐值
            String salt = miaoshaUser.getSalt();
            String dbPassword = MD5Util.formPassToDBPass(inputPassword, salt);
            if(!dbPassword.equals(miaoshaUser.getPassword())){
                //密码错误
                throw new GlobalException(CodeMsg.PASSWORD_ERROR);
            }else {
                //生成token
                String token = UUIDUtil.uuid();
                //把user放入到缓存中,并设置有效时间
                redisTemplate.opsForValue().set("user:"+token, JsonUtils.objectToJson(miaoshaUser), 60*60,TimeUnit.SECONDS);

                Cookie cookie = new Cookie("token",token);
                //设置cookie的有效期和redis的值保存一致
                cookie.setMaxAge(3600);
                cookie.setPath("/");
                //把cookie添加到响应体中
                response.addCookie(cookie);
                return true;
            }

        }

    }

    public Object getByToken(HttpServletResponse response, String token) {
        //依照提供的token去redis中查找对于的值
        String jsonUser = redisTemplate.opsForValue().get("user:" + token);
        redisTemplate.opsForValue().set("user:"+token,jsonUser,60*60,TimeUnit.SECONDS);
        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge(3600);
        cookie.setPath("/");
        response.addCookie(cookie);
        MiaoshaUser miaoshaUser = JsonUtils.jsonToPojo(jsonUser, MiaoshaUser.class);
        return miaoshaUser;

    }
}
