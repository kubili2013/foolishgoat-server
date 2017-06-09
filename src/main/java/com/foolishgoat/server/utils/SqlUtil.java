package com.foolishgoat.server.utils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.foolishgoat.server.db.Table;
import com.foolishgoat.server.exception.SQLWhereCondationException;

public class SqlUtil {

	public static String getPageSelectColumnsWhere(Table table,HttpServletRequest req) throws SQLWhereCondationException{
		String sql = "SELECT ";
		// 排序字段
		String sort = (req.getParameter("sort") != null && req.getParameter("sort").isEmpty()) ? table.getPrimaryKey()[0] : req.getParameter("sort");
		// 每页数量
		int size = (req.getParameter("size") != null && req.getParameter("size").isEmpty()) ? 15:Integer.valueOf(req.getParameter("size"));
		// 当前页数
		int curr = (req.getParameter("page") != null && req.getParameter("page").isEmpty()) ? 1:Integer.valueOf(req.getParameter("page"));
		// 要获取列
		String columns =  req.getParameter("columns");
		
		sql += columns.isEmpty() ? " * " : columns;
		sql += " FROM " + table.getName();
		List<String> condition = new ArrayList<String>();
		List<String> null_condition = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();
		for(int i=0;i<table.getColumns().length;i++){
			String p_value_eq = req.getParameter(table.getColumns()[i] + "-eq");
			if(p_value_eq != null && 
					!"".equals(p_value_eq)){
				condition.add(" " + table.getTypes()[i] + " = ? ");
				values.add(TableUtil.convert(table.getTypes()[i], p_value_eq));
			}
			String p_value_bt = req.getParameter(table.getColumns()[i] + "-bt");
			if(p_value_bt != null && 
					!"".equals(p_value_bt)){
				condition.add(" " + table.getTypes()[i] + " BETWEEN " + " ? AND ? ");
				String[] bt = p_value_bt.split("|");
				if(bt == null || bt.length < 2){
					throw new SQLWhereCondationException(table.getColumns()[i] + "-bt" + "参数格式出错，格式为：2010-01-01 00:00:00|2010-02-02 00:00:00");
				}
				values.add(TableUtil.convert(table.getTypes()[i], p_value_bt.split("|")[0]));
				values.add(TableUtil.convert(table.getTypes()[i], p_value_bt.split("|")[1]));
			}
			String p_value_lk = req.getParameter(table.getColumns()[i] + "-lk");
			if(p_value_lk != null && 
					!"".equals(p_value_lk)){
				condition.add(" " + table.getTypes()[i] + " like ? ");
				values.add("%" + TableUtil.convert(table.getTypes()[i], p_value_lk).toString() + "%");
			}
			String p_value_gt = req.getParameter(table.getColumns()[i] + "-gt");
			if(p_value_gt != null && 
					!"".equals(p_value_gt)){
				condition.add(" " + table.getTypes()[i] + " > ? ");
				values.add(TableUtil.convert(table.getTypes()[i], p_value_gt));
			}
			String p_value_lt = req.getParameter(table.getColumns()[i] + "-lt");
			if(p_value_lt != null && 
					!"".equals(p_value_lt)){
				condition.add(" " + table.getTypes()[i] + " < ? ");
				values.add(TableUtil.convert(table.getTypes()[i], p_value_lt));
			}
			String p_value_nu = req.getParameter(table.getColumns()[i] + "-nu");
			if(p_value_nu != null && 
					!"".equals(p_value_nu)){
				if("0".equals(p_value_nu)){
					null_condition.add(" " + table.getTypes()[i] + " IS NOT NULL ");
				}else if("1".equals(p_value_nu)){
					null_condition.add(" " + table.getTypes()[i] + " IS NULL ");
				}
				
			}
		}
		if(condition.size() > 0 || null_condition.size() > 0){
			sql += " WHERE ";
		}
		for(int i=0;i<condition.size();i++){
			if(i==0){
				sql += condition.get(i);
			}else{
				sql += " AND " + condition.get(i);
			}
		}
		for(int i=0;i<null_condition.size();i++){
			if(i==0 && condition.size() < 0){
				sql += null_condition.get(i);
			}else{
				sql += " AND " + null_condition.get(i);
			}
		}
		// 条件
		sql += " ORDER BY " + sort ;
		sql += " LIMIT " + size * curr + "," + size;
		return sql;
	}
	
}
