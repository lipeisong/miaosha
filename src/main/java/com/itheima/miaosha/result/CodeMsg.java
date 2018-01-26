package com.itheima.miaosha.result;

public class CodeMsg {
    private int code;
    private String msg;

    //通用的异常
    public static CodeMsg ERROR_SERVER=new CodeMsg(50010,"服务器端异常");
    public static CodeMsg REQUEST_ILLEGAL=new CodeMsg(500102,"请求非法");
    public static CodeMsg ACCESS_LIMIT_REACHED=new CodeMsg(500103,"访问太过频繁");


    //这一个带参数的异常了
    public static CodeMsg BIND_ERROR=new CodeMsg(500101,"参数校验异常：%s");


    //登录模块  5002xx
//登录模块 5002XX
    public static CodeMsg SESSION_ERROR = new CodeMsg(500210, "Session不存在或者已经失效");
    public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500211, "登录密码不能为空");
    public static CodeMsg MOBILE_EMPTY = new CodeMsg(500212, "手机号不能为空");
    public static CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
    public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500214, "手机号不存在");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");
    public static CodeMsg VERIFYCODE_ERROR=new CodeMsg(500216,"验证码不正确");
    //商品模块 5003XX


    //订单模块 5004xx


    //秒杀模块 5005xx
public static CodeMsg MIAO_SHA_OVER=new CodeMsg(500500,"商品已经秒杀完毕");

public static CodeMsg REPEATE_MIAOSHA=new CodeMsg(500501,"不能重复秒杀");

public static CodeMsg MIAOSHA_FAIL=new CodeMsg(500502,"秒杀失败");

    private CodeMsg(int code, String msg) {
        this.code=code;
        this.msg=msg;

    }

    public CodeMsg fillArgs(Object...args){
        int code=this.code;
        String message = String.format(this.msg, args);
        return new CodeMsg(code,message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "CodeMsg{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
