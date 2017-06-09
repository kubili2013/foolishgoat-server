package com.foolishgoat.server.utils;

import javax.servlet.http.HttpServletRequest;

import com.foolishgoat.server.exception.RouterConfigException;

public class RequestUtil {
	
	public static void setUrlAttributes(String target,String router,HttpServletRequest req) throws RouterConfigException{
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
		// 只有两个数组长度相等 才有可能符合
		if(router_atoms.length == target_atoms.length){
			for(int j=0;j<router_atoms.length;j++){
				if(router_atoms[j].charAt(0) == '{' && router_atoms[j].charAt(router_atoms[j].length()-1) == '}'){
					String[] s = router_atoms[j].replace("{", "").replace("}", "").split(":");
					if(s != null && s.length > 0 && !"".equals(s[0])){
						req.setAttribute(s[0], TableUtil.convert(( s.length > 1 && s[1].isEmpty() ) ?"INT":s[1], target_atoms[j]));
					}else{
						throw new RouterConfigException(router + "路由配置错误！");
					}
				}
			}
		}
	}

}
