package com.foolishgoat.server.exception;

public class RouterConfigException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 路由配置错误异常
	 * @param msg
	 */
	public RouterConfigException(String msg){
		super(msg);
	}

}
