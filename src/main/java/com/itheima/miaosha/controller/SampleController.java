package com.itheima.miaosha.controller;

import com.itheima.miaosha.entity.User;
import com.itheima.miaosha.result.Result;
import com.itheima.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SampleController {

    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","joshua");
        return "hello";
    }

    @Autowired
    private UserService userService;

    @RequestMapping("db/get")
    @ResponseBody
    public Result<User> get(){
        User user = userService.getById(1);
        return Result.success(user);
    }

}
