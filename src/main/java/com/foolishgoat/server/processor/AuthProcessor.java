package com.foolishgoat.server.processor;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foolishgoat.server.router.Router;

public class AuthProcessor implements Processor{
	
	public static final Logger logger = Logger.getLogger("UTF8Processor");
	@Override
	public void doProcessor(Router router, HttpServletRequest req, HttpServletResponse rep, ProcessorChain chain) {
		req.getSession().getAttribute("username");
		chain.doProcessor(router, req, rep, chain);
	}

}
