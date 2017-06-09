package com.foolishgoat.server.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResultSetUtil {

	public static JSONArray resultSetToJSONArray(ResultSet rs) throws SQLException{
		JSONArray array = new JSONArray();
		ResultSetMetaData metaData = rs.getMetaData();  
		int columnCount = metaData.getColumnCount();
		while(rs.next()){
			JSONObject obj = new JSONObject();
			// 遍历每一列
	       for (int i = 1; i <= columnCount; i++) {  
	           String columnName =metaData.getColumnLabel(i);
	           Object value =  rs.getObject(columnName);
	           obj.put(columnName, value);
	       }
	       array.put(obj);
		}
		return array;
	}
	
	public static JSONObject getOneToJson(ResultSet rs) throws SQLException,JSONException  
	{  
	   // 获取列数  
	   ResultSetMetaData metaData = rs.getMetaData();  
	   int columnCount = metaData.getColumnCount();
	   JSONObject jsonObj = new JSONObject();
	   if(rs.next()){
	       // 遍历每一列
	       for (int i = 1; i <= columnCount; i++) {  
	           String columnName =metaData.getColumnLabel(i);
	           Object value =  rs.getObject(columnName);
	           jsonObj.put(columnName, value);
	       }
       }
	   return jsonObj;
	}
}
