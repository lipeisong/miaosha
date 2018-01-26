package com.itheima.miaosha.Exception;


import com.itheima.miaosha.result.CodeMsg;
//当出现异常情况的时候就直接抛这个异常
public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm) {
		super(cm.toString());
		this.cm = cm;
	}

	public CodeMsg getCm() {
		return cm;
	}

}
