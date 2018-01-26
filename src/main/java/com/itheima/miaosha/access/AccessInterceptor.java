package com.itheima.miaosha.access;

import com.itheima.miaosha.entity.MiaoshaUser;
import com.itheima.miaosha.result.CodeMsg;
import com.itheima.miaosha.service.MiaoshaUserService;
import com.itheima.miaosha.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Component
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private MiaoshaUserService miaoshaUserService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod hm=(HandlerMethod)handler;

            //从请求总获取cookie的值
            String token=getCookieValue(request,"token");




            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);

            if(accessLimit==null){
                return true;
            }

            MiaoshaUser user = (MiaoshaUser) miaoshaUserService.getByToken(response, token);

            //获取请求的uri
            String key = request.getRequestURI();


            if(user!=null){
                //把user绑定在当前线程中
                UserContext.setUser(user);
                key+=user.getId();
            }

            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();


            if(needLogin){
                if(user==null){
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
            }

            String strCount = redisTemplate.opsForValue().get("user:maxcount:" + key);
            Integer count;
            if(strCount==null){
                count=1;
                redisTemplate.opsForValue().set("user:maxcount:" + key,1+"",seconds, TimeUnit.SECONDS);
            }else {
                count = Integer.parseInt(strCount);
            }
            if(count<maxCount){
                count+=1;
                redisTemplate.opsForValue().set("user:maxcount:" + key,1+"");
            }else {
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }

        }
          return true;
    }

    private void render(HttpServletResponse response, CodeMsg sessionError) {
        ServletOutputStream out = null;
        try {
            response.setContentType("application/json;charset=utf-8");
            out=response.getOutputStream();
            String str = JsonUtils.objectToJson(sessionError);
            out.write(str.getBytes("UTF-8"));

        }catch (Exception e){
            e.printStackTrace();

        }


    }


    private String getCookieValue(HttpServletRequest request, String s) {
        //获取所有的cookie
        Cookie[] cookies = request.getCookies();
        //遍历cookie
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(s)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
