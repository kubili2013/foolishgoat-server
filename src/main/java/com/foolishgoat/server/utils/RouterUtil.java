package com.foolishgoat.server.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.foolishgoat.server.exception.RouterConfigException;

public class RouterUtil {
	/**
	 * 是否匹配当前router规则
	 * @param target
	 * @param router
	 * @return
	 * @throws RouterConfigException
	 */
	public static boolean isAccordant(String target,String router) throws RouterConfigException{
		String _router = router;
		// String _target  = target;
		if(_router.charAt(0) == '/'){
			_router = _router.substring(1, _router.length());
		}
		// if(_target.charAt(0) == '/'){
		// 	_target = _target.substring(1, _target.length());
		// }
		String[] router_atoms = _router.replace("//", "/").split("/");
		// String[] target_atoms = _target.replace("//", "/").split("/");
		// 只有两个数组长度相等 才有可能符合
		String regEx = "^";
		// 次循环用来构造正则表达式
		for(int j=0;j<router_atoms.length;j++){
			// 判断 是否是类似{id:number|string:min:max}的结构
			// 开头^，结尾$
			// 字符串 ([A-Za-z0-9_-]{min,max})
			// 数字： [1-9][0-9]{min,max}
			if(router_atoms[j].charAt(0) == '{' && router_atoms[j].charAt(router_atoms[j].length()-1) == '}'){
				String[] s = router_atoms[j].replace("{", "").replace("}", "").split(":");
				if(s != null && s.length > 0 && !"".equals(s[0])){
					switch(s.length){
						case(1):regEx += "\\/" + "([A-Za-z0-9_-]*)";break;
						case(2):
							if("INT".equals(s[1].toUpperCase()) || "INT UNSIGNED".equals(s[1].toUpperCase())){
								regEx += "\\/" + "([0-9]*)";
							}else if("VARCHAR".equals(s[1].toUpperCase())){
								regEx += "\\/" + "([A-Za-z0-9_-]*)";
							}else{
								throw new RouterConfigException(router + "路由配置错误！");
							}
							break;
						case(3):
							if("INT".equals(s[1].toUpperCase()) || "INT UNSIGNED".equals(s[1].toUpperCase())){
								regEx += "\\/" + "([0-9]{1," +s[2]+ "})";
							}else if("VARCHAR".equals(s[1].toUpperCase()) ){
								regEx += "\\/" + "([A-Za-z0-9_-]{1," +s[2]+ "})";
							}else{
								throw new RouterConfigException(router + "路由配置错误！");
							}
							break;
						case(4):
							if("INT".equals(s[1].toUpperCase()) || "INT UNSIGNED".equals(s[1].toUpperCase())){
								regEx += "\\/" + "([0-9]{" +s[2]+ "," +s[3]+ "})";
							}else if("VARCHAR".equals(s[1].toUpperCase())){
								regEx += "\\/" + "([A-Za-z0-9_-]{" +s[2]+ "," +s[3]+ "})";
							}else{
								throw new RouterConfigException(router + "路由配置错误！");
							}
							break;
					}
				}else{
					throw new RouterConfigException(router + "路由配置错误！");
				}
			}else{
				regEx += "\\/" + router_atoms[j];
			}
		}
		regEx += "$";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(target.replace("//", "/"));
		return matcher.matches();
	}
	/**
	 * 提取参数与数值
	 * @param target
	 * @param router
	 * @param req
	 * @return
	 * @throws RouterConfigException
	 */
	public static Map<String,Object> accordant(String target,String router,HttpServletRequest req) throws RouterConfigException{
		String _router = router;
		String _target  = target;
		if(_router.charAt(0) == '/'){
			_router = _router.substring(1, _router.length());
		}
		if(_target.charAt(0) == '/'){
			_target = _target.substring(1, _target.length());
		}
		String[] router_atoms = _router.replace("//", "/").split("/");
		String[] target_atoms = _target.replace("//", "/").split("/");
		Map<String,Object> params = new HashMap<String,Object>();
		// 只有两个数组长度相等 才有可能符合
		if(router_atoms.length == target_atoms.length){
			for(int j=0;j<router_atoms.length;j++){
				if(router_atoms[j].charAt(0) == '{' && router_atoms[j].charAt(router_atoms[j].length()-1) == '}'){
					String[] s = router_atoms[j].replace("{", "").replace("}", "").split(":");
					if(s != null && s.length > 0 && !"".equals(s[0])){
						params.put(s[0], TableUtil.convert(( s.length > 1 && s[1].isEmpty() )?"INT":s[1], target_atoms[j]));
						req.setAttribute(s[0], TableUtil.convert(( s.length > 1 && s[1].isEmpty() ) ?"INT":s[1], target_atoms[j]));
					}else{
						throw new RouterConfigException(router + "路由配置错误！");
					}
				}
			}
		}
		return params;
	}
}
