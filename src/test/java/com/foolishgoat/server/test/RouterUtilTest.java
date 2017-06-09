package com.foolishgoat.server.test;

import com.foolishgoat.server.exception.RouterConfigException;
import com.foolishgoat.server.utils.RouterUtil;

public class RouterUtilTest {
	
	public static void main(String args[]) throws RouterConfigException{
		// false
		System.out.println(!RouterUtil.isAccordant("/test", "/test/{id}"));
		// true
		System.out.println(RouterUtil.isAccordant("/test/1234", "/test/{id}"));
		// true
		System.out.println(RouterUtil.isAccordant("/test/1234-ss_ss", "/test/{id}"));
		// false
		System.out.println(!RouterUtil.isAccordant("/test/1234ssss/", "/test/{id}"));
		// false
		System.out.println(!RouterUtil.isAccordant("/test/1234ssss/test", "/test/{id}"));
		// true
		System.out.println(RouterUtil.isAccordant("/test/123412", "/test/{id:number}"));
		// false
		System.out.println(!RouterUtil.isAccordant("/test/123412s", "/test/{id:number}"));
		// true
		System.out.println(RouterUtil.isAccordant("/test/123412", "/test/{id:number:6}"));
		// true
		System.out.println(RouterUtil.isAccordant("/test/123412", "/test/{id:number:6:8}"));
		// false
		System.out.println(!RouterUtil.isAccordant("/test/123", "/test/{id:number:6}"));
		// false
		System.out.println(!RouterUtil.isAccordant("/test/12345452345234523452345243", "/test/{id:number:6:8}"));
		System.out.println(!RouterUtil.isAccordant("/test/1231ssss", "/test/{id:number:6:8}"));
		System.out.println(RouterUtil.isAccordant("/test/1231ssss", "/test/{id:string:6:8}"));
		System.out.println(!RouterUtil.isAccordant("/test/1-_435345ss", "/test/{id:string:6}"));
		System.out.println(!RouterUtil.isAccordant("/test/1231ssss345ddgad", "/test/{id:string:6:8}"));
		System.out.println(RouterUtil.isAccordant("/test/1231123/test1/12311231", "/test/{id:number:6:8}/test1/{id:number:6:8}"));
		// System.out.println(RouterUtil.isAccordant("/test/1231123/test1/12311231", "/test/{id:numbe}/test1/{id:number:6:8}"));
		// System.out.println(RouterUtil.isAccordant("/test/1231123/test1/12311231", "/test/{}/test1/{id:number:6:8}"));
//		Map<String,Object> map = RouterUtil.accordant("/test/1231123/test1/12311231", "/test/{id:number:6:8}/test1/{sid:number:6:8}");
//		for(String key:map.keySet()){
//			System.out.println(key + ":" + map.get(key));
//		}
	}
	
}
