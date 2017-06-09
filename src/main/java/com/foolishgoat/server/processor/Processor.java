package com.foolishgoat.server.processor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foolishgoat.server.router.Router;
/**
 * 处理器
 * 
 * @author kubili2013@gmail.com
 *
 */
public interface Processor {
	public abstract void doProcessor(Router router,HttpServletRequest req,HttpServletResponse rep, ProcessorChain chain);
}