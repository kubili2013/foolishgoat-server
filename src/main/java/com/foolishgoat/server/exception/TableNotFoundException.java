package com.foolishgoat.server.exception;

public class TableNotFoundException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2275108792749964441L;
	/**
	 * 没有数据库表异常
	 * @param msg
	 */
	public TableNotFoundException(String msg){
		super(msg);
	}
}
