package com.foolishgoat.server.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0DBPool {
	
	private static ComboPooledDataSource ds;
	private static C3P0DBPool pool;
	// 单例模式
	private C3P0DBPool(){
		ds = new ComboPooledDataSource();
	}
	
	/**
	 * 单例模式
	 * @return
	 */
	public static final C3P0DBPool getInstance(){
		if(pool == null){
		pool = new C3P0DBPool();
		}
		return pool;
	}
	
	/**
	 * 获取连接池中的链接
	 * @return
	 */
	public synchronized final Connection getConnection(){
		try {
			Connection conn = ds.getConnection();
			return conn;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
