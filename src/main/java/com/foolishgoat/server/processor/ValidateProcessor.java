package com.foolishgoat.server.processor;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foolishgoat.server.router.Router;
import com.foolishgoat.server.utils.DateUtil;
import com.foolishgoat.server.utils.FoolishGoatStatu;
import com.mysql.jdbc.StringUtils;

public class ValidateProcessor implements Processor {
	
	public static final Logger logger = Logger.getLogger("ValidateProcessor");

	@Override
	public void doProcessor(Router router,HttpServletRequest req, HttpServletResponse rep, ProcessorChain chain) {
		// 校验
		Map<String,String> vali = router.getValidators();
		if(vali != null){
			for(String key:vali.keySet()){
				String msg = ValidateProcessor.validator(key,req.getParameter(key),vali.get(key));
				if(null != msg){
					try {
						rep.getWriter().print(msg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						logger.log(Level.WARNING, "执行" + router.getName() + "是,校验处理器发送数据失败！", e);
					}
					return;
				}
			}
		}
		chain.doProcessor(router, req, rep, chain);
	
	}

	public static String validator(String key, String value,String rule){
		String[] rules = rule.split("\\|");
		for(int i = 0;i<rules.length;i++){
			// 必须字段
			if(StringUtils.isNullOrEmpty(value)){
				if("required".equals(rules[i].toLowerCase())){
					return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "字段值是必须的！\"}";
				}else{
					return null;
				}
			}
			// varchar 类型
			if(rules[i].toLowerCase().indexOf("varchar") >= 0 
					|| rules[i].toLowerCase().indexOf("text") >= 0 
					|| rules[i].toLowerCase().indexOf("char") >= 0
					|| rules[i].toLowerCase().indexOf("string") >= 0){
				String[] params = rules[i].split(":");
				if(params.length == 1 && value.length() < 255){
					// 不限制长度 默认最大只能255字符长度
				}else if(params.length == 2){
					String[] leng = params[1].split(",");
					if(leng.length == 1 && value.length() > Integer.valueOf(leng[0])){
						// varchar:64 表示类型是 varchar 长度不能超过 64
						return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "字段长度不能大于" + leng[0] + "！\"}";
					}else if(leng.length > 1 && ( value.length() < Integer.valueOf(leng[0]) || value.length() > Integer.valueOf(leng[1]))){
						return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "字段长度控制在 " + leng[0] + "-" + leng[1] + "之间！\"}";
					}
				}
			}
			// int 类型
			if(rule.toLowerCase().indexOf("int") >= 0){
				if(!Pattern.compile("^[1-9][0-9]*$").matcher(value).find()){
					return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "必须是整数！\"}";
				}
				String[] params = rules[i].split(":");
				if(params.length == 1  && value.length() < 11){
					// 不限制长度 默认最大只能255字符长度
				}else if(params.length == 2){
					String[] leng = params[1].split(",");
					if(leng.length == 1 && value.length() > Integer.valueOf(leng[0])){
						// varchar:64 表示类型是 varchar 长度不能超过 64
						return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "字段长度不能大于" + leng[0] + "！\"}";
					}else if(leng.length > 1 && ( value.length() < Integer.valueOf(leng[0]) || value.length() > Integer.valueOf(leng[1]))){
						return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "字段长度控制在 " + leng[0] + "-" + leng[1] + "之间！\"}";
					}
				}
			}
			// float double 类型
			if(rule.toLowerCase().indexOf("float") >= 0 || rule.toLowerCase().indexOf("double") >= 0){
				if(!Pattern.compile("^[0-9]*(\\.)[0-9]*$").matcher(value).find()){
					return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "必须是浮点数！\"}";
				}
				String[] params = rules[i].split(":");
				if(params.length == 1 && value.length() < 64){
					// 不限制长度 默认最大只能255字符长度
				}else if(params.length == 2){
					String[] leng = params[1].split(",");
					if(leng.length == 1 && value.length() > Integer.valueOf(leng[0])){
						// varchar:64 表示类型是 varchar 长度不能超过 64
						return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "字段长度不能大于" + leng[0] + "！\"}";
					}else if(leng.length > 1 && ( value.length() < Integer.valueOf(leng[0]) || value.length() > Integer.valueOf(leng[1]))){
						return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "字段长度控制在 " + leng[0] + "-" + leng[1] + "之间！\"}";
					}
				}
			}
			// timestamp 类型
			if(rule.toLowerCase().indexOf("timestamp") >= 0 || rule.toLowerCase().indexOf("date") >= 0 || rule.toLowerCase().indexOf("datetime") >= 0){
				if(!DateUtil.isDateTime(value)){
					return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "必须为正确的日期时间格式，eg:(2010-01-03 00:00:00)。\"}";
				}
			}
			// bit
			if(rule.toLowerCase().indexOf("bit") >= 0 || rule.toLowerCase().indexOf("boolean") >= 0){
				if(!"1".equals(value) && !"0".equals(value)){
					return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "的值必须为0或者1。\"}";
				}
			}
			// email 类型
			
			if(rule.toLowerCase().indexOf("email") >= 0){
				if(!Pattern.compile("^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$").matcher(value).find()){
					return "{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"" + key + "的格式必须为 E-mail。\"}";
				}
			}
		}
		// 返回 null 表明校验是通过的
		return null;
	}
}
