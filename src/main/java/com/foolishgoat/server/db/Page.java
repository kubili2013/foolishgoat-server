package com.foolishgoat.server.db;

import org.json.JSONArray;
import org.json.JSONObject;

public class Page {
	private int currentPage = 1;
	private int pageSize = 15;
	private int totalPage;
	private int totalSize;
	private JSONArray data;
	
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public int getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(int totalSize) {
		totalPage = (totalSize + pageSize - 1) / pageSize;
        this.totalSize = totalSize;
        if (totalPage < currentPage) {
        		currentPage = totalPage;
        }
	}
	public JSONArray getData() {
		return data;
	}
	public void setData(JSONArray data) {
		this.data = data;
	}
	
	public JSONObject toJson(){
		JSONObject json = new JSONObject();
		json.put("currentPage", currentPage);
		json.put("totalPage", totalPage);
		json.put("totalSize", totalSize);
		json.put("pageSize", pageSize);
		json.put("data", data);
		return json;
	}
	
}
