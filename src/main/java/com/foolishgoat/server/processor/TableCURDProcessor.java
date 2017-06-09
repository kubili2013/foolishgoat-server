package com.foolishgoat.server.processor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.foolishgoat.server.db.C3P0DBPool;
import com.foolishgoat.server.db.Page;
import com.foolishgoat.server.db.Table;
import com.foolishgoat.server.exception.SQLWhereCondationException;
import com.foolishgoat.server.exception.TableNotFoundException;
import com.foolishgoat.server.router.Router;
import com.foolishgoat.server.utils.FoolishGoatStatu;
import com.foolishgoat.server.utils.PreparedStatementUtil;
import com.foolishgoat.server.utils.ResultSetUtil;
import com.foolishgoat.server.utils.TableUtil;

public class TableCURDProcessor implements Processor {
	
	public static final Logger logger = Logger.getLogger("DBProcessor");
	
	private static Map<String,Table> resources  = null;
	
	public TableCURDProcessor(){
		if(resources == null){
			resources = new HashMap<String,Table>();
			// 获取当前库中所有表结构
			Connection con = C3P0DBPool.getInstance().getConnection();
			DatabaseMetaData metaData;
			try {
				metaData = con.getMetaData();
				ResultSet rs_table = metaData.getTables(con.getCatalog(), metaData.getUserName(), null, new String[]{"TABLE"});
				while(rs_table.next()){
					Table table = new Table(rs_table.getString("TABLE_NAME"),con);
					resources.put(rs_table.getString("TABLE_NAME"), table);
				}
				rs_table.close();
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "DBResource初始化失败", e);
				// 退出
	            System.exit(0);
			}finally{
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void doProcessor(Router router, HttpServletRequest req, HttpServletResponse rep, ProcessorChain chain) {
		Table table = TableCURDProcessor.resources.get(router.getResourceName());
		if(table == null){
			try {
				throw new TableNotFoundException(router.getResourceName() + "表没有找见。");
			} catch (TableNotFoundException e) {
				logger.log(Level.WARNING, router.getResourceName() + "表没有找见。", e);
			}finally{
				try {
					rep.getWriter().print("{\"code\":" + FoolishGoatStatu.TABLE_NOT_FOUND_ERROR + ",\"message\":\"资源未找见！\"}");
				} catch (IOException e) {
					logger.log(Level.WARNING, "执行" + router.getName() + "DB处理器发送数据失败！", e);
				}
			}
			return;
		}
		if("GET".equals(router.getMethod())){
			Object[] obj = new Object[table.getPrimaryKey().length];
			for(int i=0;i < obj.length;i++){
				obj[i] = req.getAttribute(table.getName() + "." + table.getPrimaryKey()[i]);
			}
			if(obj[0] == null){
				try {
					rep.getWriter().print(retrieve(table,req).toJson().toString());
				} catch (IOException e) {
					logger.log(Level.WARNING, router.getName() + ",TableCURD处理器发送数据失败！", e);
				}catch (SQLWhereCondationException e) {
					logger.log(Level.WARNING, router.getName() + ",TableCURD处理器发送数据失败！", e);
				}
			}else{
				try {
					rep.getWriter().print(retrieve(table,req,obj).toString());
				} catch (IOException e) {
					logger.log(Level.WARNING, router.getName() + ",TableCURD处理器发送数据失败！", e);
				}
			}
		}else if("POST".equals(router.getMethod())){
			try {
				rep.getWriter().print("{\"code\":" + FoolishGoatStatu.SUCCESS + ",\"message\":\"" + this.create(table, req, rep)+ "条记录增加成功！\"}");
			} catch (IOException e) {
				logger.log(Level.WARNING, "执行" + router.getName() + "TableCURD处理器发送数据失败！", e);
			}
		}else if("PUT".equals(router.getMethod())){
			Object[] obj = new Object[table.getPrimaryKey().length];
			for(int i=0;i < obj.length;i++){
				obj[i] = req.getAttribute(table.getName() + "." + table.getPrimaryKey()[i]);
			}
			if(obj.length > 0 && obj[0] != null){
				try {
					rep.getWriter().print("{\"code\":" + FoolishGoatStatu.SUCCESS + ",\"message\":\"" + this.update(table, req,obj) + "条记录修改成功！\"}");
				} catch (IOException e) {
					logger.log(Level.WARNING, "执行" + router.getName() + "TableCURD处理器发送数据失败！", e);
				}
			}else{
				try {
					rep.getWriter().print("{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"不正确的访问方式！\"}");
				} catch (IOException e) {
					logger.log(Level.WARNING, "执行" + router.getName() + "TableCURD处理器发送数据失败！", e);
				}
			}
		}else if("DELETE".equals(router.getMethod())){
			Object[] obj = new Object[table.getPrimaryKey().length];
			for(int i=0;i < obj.length;i++){
				obj[i] = req.getAttribute(table.getName() + "." + table.getPrimaryKey()[i]);
			}
			if(obj.length > 0 && obj[0] != null){
				try {
					rep.getWriter().print("{\"code\":" + FoolishGoatStatu.SUCCESS + ",\"message\":\"" + this.delete(table, req,obj)+ "条记录删除成功！\"}");
				} catch (IOException e) {
					logger.log(Level.WARNING, "执行" + router.getName() + "TableCURD处理器发送数据失败！", e);
				}
			}else{
				try {
					rep.getWriter().print("{\"code\":" + FoolishGoatStatu.VALIDATE_ERROR + ",\"message\":\"不正确的访问方式！\"}");
				} catch (IOException e) {
					logger.log(Level.WARNING, "执行" + router.getName() + "TableCURD处理器发送数据失败！", e);
				}
			}
		}
		chain.doProcessor(router, req, rep, chain);
	}
	/**
	 * 新增
	 * @param table
	 * @param req
	 * @param rep
	 * @return
	 */
	public int create(Table table, HttpServletRequest req, HttpServletResponse rep){
		String _val = "";
		String cols = "";
		List<Object> values = new ArrayList<Object>();
		for(int i=0;i<table.getColumns().length;i++){
			String p_value = req.getParameter(table.getColumns()[i]);
			if(p_value != null && 
					!"".equals(p_value)){
				_val += "?,";
				cols += table.getColumns()[i] + ",";
				values.add(TableUtil.convert(table.getTypes()[i], p_value));
			}
		}
		String sql = "INSERT INTO " + table.getName() + "(" + cols.substring(0, cols.length()-1) + ") VALUES (" + _val.substring(0, _val.length()-1) + ");";
		Connection con = C3P0DBPool.getInstance().getConnection();
		PreparedStatement ps = null;
		int rs = 0;
		try {
			ps = con.prepareStatement(sql);
			logger.log(Level.INFO, sql);
			for(int i=1;i<=values.size();i++){
				PreparedStatementUtil.setPreparedStatement(ps, values.get(i-1), i);
			}
			rs = ps.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "sql组装失败", e);
		}finally{
			try {
				if(ps != null)
				ps.close();
				if(con != null)
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rs;
	}
	/**
	 * 查询多个
	 * @param table
	 * @param req
	 * @return
	 * @throws SQLWhereCondationException 
	 */
	public Page retrieve(Table table, HttpServletRequest req) throws SQLWhereCondationException{
		Page page = new Page();
		// 排序字段
		String sort = (req.getParameter("sort") == null || req.getParameter("sort").isEmpty()) ? table.getPrimaryKey()[0] : req.getParameter("sort");
		// 每页数量
		int size = (req.getParameter("size") == null || req.getParameter("size").isEmpty()) ? 15:Integer.valueOf(req.getParameter("size"));
		page.setPageSize(size);
		// 当前页数
		int curr = (req.getParameter("page") == null || req.getParameter("page").isEmpty()) ? 1:Integer.valueOf(req.getParameter("page"));
		page.setCurrentPage(curr);
		// 要获取列
		String columns =  (req.getParameter("columns") == null || req.getParameter("columns").isEmpty()) ? " * " : req.getParameter("columns");
		
		String from = " FROM " + table.getName();
		List<String> condition = new ArrayList<String>();
		List<String> null_condition = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();
		for(int i=0;i<table.getColumns().length;i++){
			String p_value_eq = req.getParameter(table.getColumns()[i] + "-eq");
			if(p_value_eq != null && 
					!"".equals(p_value_eq)){
				condition.add(" " + table.getColumns()[i] + " = ? ");
				values.add(TableUtil.convert(table.getTypes()[i], p_value_eq));
			}
			String p_value_bt = req.getParameter(table.getColumns()[i] + "-bt");
			if(p_value_bt != null && 
					!"".equals(p_value_bt)){
				condition.add(" " + table.getColumns()[i] + " BETWEEN " + " ? AND ? ");
				String[] bt = p_value_bt.split(",");
				if(bt == null || bt.length < 2){
					throw new SQLWhereCondationException(table.getColumns()[i] + "-bt" + "参数格式出错，格式为：2010-01-01 00:00:00,2010-02-02 00:00:00");
				}
				values.add(TableUtil.convert(table.getTypes()[i], bt[0]));
				values.add(TableUtil.convert(table.getTypes()[i], bt[1]));
			}
			String p_value_lk = req.getParameter(table.getColumns()[i] + "-lk");
			if(p_value_lk != null && 
					!"".equals(p_value_lk)){
				condition.add(" " + table.getColumns()[i] + " like ? ");
				values.add("%" + TableUtil.convert(table.getTypes()[i], p_value_lk).toString() + "%");
			}
			String p_value_gt = req.getParameter(table.getColumns()[i] + "-gt");
			if(p_value_gt != null && 
					!"".equals(p_value_gt)){
				condition.add(" " + table.getColumns()[i] + " > ? ");
				values.add(TableUtil.convert(table.getTypes()[i], p_value_gt));
			}
			String p_value_lt = req.getParameter(table.getColumns()[i] + "-lt");
			if(p_value_lt != null && 
					!"".equals(p_value_lt)){
				condition.add(" " + table.getColumns()[i] + " < ? ");
				values.add(TableUtil.convert(table.getTypes()[i], p_value_lt));
			}
			String p_value_nu = req.getParameter(table.getColumns()[i] + "-nu");
			if(p_value_nu != null && 
					!"".equals(p_value_nu)){
				if("0".equals(p_value_nu)){
					null_condition.add(" " + table.getColumns()[i] + " IS NOT NULL ");
				}else if("1".equals(p_value_nu)){
					null_condition.add(" " + table.getColumns()[i] + " IS NULL ");
				}
				
			}
		}
		String where = "";
		if(condition.size() > 0 || null_condition.size() > 0){
			where += " WHERE ";
		}
		for(int i=0;i<condition.size();i++){
			if(i==0){
				where += condition.get(i);
			}else{
				where += " AND " + condition.get(i);
			}
		}
		for(int i=0;i<null_condition.size();i++){
			if(i==0 && condition.size() < 0){
				where += null_condition.get(i);
			}else{
				where += " AND " + null_condition.get(i);
			}
		}
		// 条件
		String orderBy = " ORDER BY " + sort ;
		String limit = " LIMIT " + (page.getPageSize() * (page.getCurrentPage() - 1)) + "," + page.getPageSize();
		logger.log(Level.INFO, "SELECT " + columns + from + where + orderBy + limit);
		Connection con = C3P0DBPool.getInstance().getConnection();
		PreparedStatement ps_1 = null;
		PreparedStatement ps_2 = null;
		try {
			ps_1 = con.prepareStatement("SELECT count(*) " + from + where);
			logger.log(Level.INFO, "SELECT count(*) " + from + where);
			for(int i=1;i<=values.size();i++){
				PreparedStatementUtil.setPreparedStatement(ps_1, values.get(i-1), i);
			}
			ResultSet rs_c = ps_1.executeQuery();
			rs_c.next();
			page.setTotalSize(rs_c.getInt(1));
			rs_c.close();
			rs_c = null;
			ps_2 = con.prepareStatement("SELECT " + columns + from + where + orderBy + limit);
			logger.log(Level.INFO, "SELECT " + columns + from + where + orderBy + limit);
			for(int i=1;i<=values.size();i++){
				PreparedStatementUtil.setPreparedStatement(ps_2, values.get(i-1), i);
			}
			rs_c = ps_2.executeQuery();
			page.setData(ResultSetUtil.resultSetToJSONArray(rs_c));
			rs_c.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "sql组装失败", e);
		}finally{
			try {
				if(ps_1 != null)
					ps_1.close();
				if(ps_2 != null)
					ps_2.close();
				if(con != null)
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return page;
	}
	/**
	 * 根据主键获得一个记录
	 * @param table
	 * @param req
	 * @param id
	 * @return
	 */
	public JSONObject retrieve(Table table, HttpServletRequest req,Object... id){
		// 支持
		JSONObject json = null;
		String sql = "SELECT * FROM " + table.getName() + " WHERE " ;
		for(int i=0;i<table.getPrimaryKey().length;i++){
			if(i == 0){
				sql +=  table.getPrimaryKey()[i] + " = ? ";
			}else{
				sql +=  " AND " + table.getPrimaryKey()[i] + " = ? ";
			}
		}
		sql += ";";
		logger.log(Level.INFO, sql);
		
		Connection con = C3P0DBPool.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			for(int i=0;i< id.length;i++){
				PreparedStatementUtil.setPreparedStatement(ps, id[i], i+1);
			}
			ResultSet rs = ps.executeQuery();
			json = ResultSetUtil.getOneToJson(rs);
			rs.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "sql组装失败", e);
		}finally{
			try {
				if(ps != null)
				ps.close();
				if(con != null)
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return json;
	}
	
	public int delete(Table table, HttpServletRequest req,Object... id){
		// 支持
		String sql = "DELETE FROM " + table.getName() + " WHERE " ;
		for(int i=0;i<table.getPrimaryKey().length;i++){
			if(i == 0){
				sql +=  table.getPrimaryKey()[i] + " = ? ";
			}else{
				sql +=  " AND " + table.getPrimaryKey()[i] + " = ? ";
			}
		}
		sql += ";";
		logger.log(Level.INFO, sql);
		int rs = 0 ;
		Connection con = C3P0DBPool.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			for(int i=0;i< id.length;i++){
				PreparedStatementUtil.setPreparedStatement(ps, id[i], i+1);
			}
			rs = ps.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "sql组装失败", e);
		}finally{
			try {
				if(ps != null)
				ps.close();
				if(con != null)
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rs;
	}
	
	public int update(Table table, HttpServletRequest req,Object... id){
		// 组装更新列 与 值
		List<String> set = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();
		for(int i=0;i<table.getColumns().length;i++){
			String p_value = req.getParameter(table.getColumns()[i]);
			if(p_value != null && 
					!"".equals(p_value)){
				set.add(" " + table.getColumns()[i] + " = ? ");
				values.add(TableUtil.convert(table.getTypes()[i], p_value));
			}
		}
		// 组装where 条件
		String where = " WHERE " ;
		for(int i=0;i<table.getPrimaryKey().length;i++){
			if(i == 0){
				where +=  table.getPrimaryKey()[i] + " = ? ";
			}else{
				where +=  " AND " + table.getPrimaryKey()[i] + " = ? ";
			}
			// 将查询条件值放置在list里面
			values.add(id[i]);
		}
		// 组装sql 
		String sql = "UPDATE " + table.getName() + " SET ";
		for(int i=0;i<set.size();i++){
			if(i == 0){
				sql += " " + set.get(i) ;
			}else{
				sql += "," +set.get(i) ;
			}
		}
		sql += where;
		logger.log(Level.INFO, sql);
		int rs = 0 ;
		Connection con = C3P0DBPool.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			for(int i=0;i< values.size();i++){
				PreparedStatementUtil.setPreparedStatement(ps, values.get(i), i+1);
			}
			rs = ps.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "sql组装失败", e);
		}finally{
			try {
				if(ps != null)
				ps.close();
				if(con != null)
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rs;
	}
	/**
	 * 获取表
	 * @param key
	 * @return
	 */
	public Table getTable(String key){
		return resources.get(key);
	}
	
}
