package com.foolishgoat.server.processor;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foolishgoat.server.router.Router;

public class UTF8Processor implements Processor{
	
	public static final Logger logger = Logger.getLogger("UTF8Processor");
	@Override
	public void doProcessor(Router router, HttpServletRequest req, HttpServletResponse rep, ProcessorChain chain) {
		try {
			req.setCharacterEncoding("utf-8");
			rep.setCharacterEncoding("utf-8");
			rep.setHeader("Content-type", "application/json;charset=UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, "执行" + router.getName() + "时,数据格式，编码设置失败！", e);
		}
		chain.doProcessor(router, req, rep, chain);
	}

}
