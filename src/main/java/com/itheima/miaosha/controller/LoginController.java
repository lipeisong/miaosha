package com.itheima.miaosha.controller;

import com.itheima.miaosha.result.Result;
import com.itheima.miaosha.service.MiaoshaUserService;
import com.itheima.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class LoginController {

    @Autowired
    private MiaoshaUserService miaoshaUserService;
    /**
     * 跳转到登录页面
     */
    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    /**
     * 登录的逻辑实现
     */
    @RequestMapping("/login/do_login")
    @ResponseBody
    public Result<Boolean> login(HttpServletResponse response, @Valid LoginVo loginVo){
        miaoshaUserService.login(loginVo,response);
        //因为异常的信息都已经往外抛了，这个时候可以直接返回一个true就可以了
        return Result.success(true);

    }
}
