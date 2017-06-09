package com.foolishgoat.server.router;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Router {

	private String name;
	private String processors;
	private String resourceName;
	private String method;
	private Map<String,String> validators;
	
	public Router(){}
	
	public Router(JSONObject router){
		this.setName(router.getString("name"));
		this.setProcessors(router.getString("processors"));
		this.setResourceName(router.getString("resourceName"));
		this.setMethod(router.getString("method"));
		validators = new HashMap<String,String>();
		JSONObject vali = router.getJSONObject("validators");
		String[] names = JSONObject.getNames(vali);
		if(names == null){
			return;
		}
		for(int i=0;i<names.length;i++){
			validators.put(names[i], vali.getString(names[i]));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProcessors() {
		return processors;
	}

	public void setProcessors(String processors) {
		this.processors = processors;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, String> getValidators() {
		return validators;
	}

	public void setValidators(Map<String, String> validators) {
		this.validators = validators;
	}
	
	public JSONObject toJson(){
		JSONObject jObj = new JSONObject();
		jObj.put("method", this.getMethod());
		jObj.put("resourceName", this.getResourceName());
		jObj.put("url", this.getName());
		jObj.put("processors", this.getProcessors());
		jObj.put("validators", this.getValidators());
		return jObj;
	}
}
