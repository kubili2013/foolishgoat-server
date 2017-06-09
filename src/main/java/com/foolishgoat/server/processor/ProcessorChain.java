package com.foolishgoat.server.processor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foolishgoat.server.router.Router;

/**
 * 处理器链
 * @author apple
 *
 */
public class ProcessorChain implements Processor{

	List<Processor> processors = new ArrayList<Processor>();
	int index = 0;
	
	public ProcessorChain addProcessor(Processor processor){
		processors.add(processor);
		return this;
	}
	
	public ProcessorChain removeProcessor(Processor processor){
		processors.remove(processor);
		return this;
	}
	
	public void doProcessor(final Router router,final HttpServletRequest req,final HttpServletResponse rep, ProcessorChain chain) {
		if(index == processors.size()){  
            return;
        }  
		Processor f = processors.get(index);
        index ++;
        f.doProcessor(router,req, rep, chain);
	}

}
