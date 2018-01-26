package com.itheima.miaosha.Exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.itheima.miaosha.result.CodeMsg;
import com.itheima.miaosha.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


//相当于配置一个切面
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
   //拦截所有的异常信息
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){
        //打印异常信息
        e.printStackTrace();
        if(e instanceof  GlobalException){
            GlobalException ex=(GlobalException)e;
            CodeMsg cm = ex.getCm();
            return Result.error(cm);

        }else if(e instanceof BindException){
            BindException ex=(BindException)e;
            //获取异常的信息
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError objectError = errors.get(0);
            String message = objectError.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(message));
        }
        //返回服务器端异常
        return Result.error(CodeMsg.ERROR_SERVER);
    }
}
