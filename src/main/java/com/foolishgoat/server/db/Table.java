package com.foolishgoat.server.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.foolishgoat.server.router.Router;

public class Table {

	public static final Logger logger = Logger.getLogger("Table");
	
	private String name;
	private String[] primaryKey;
	private String[] primaryKeyType;
	private int[] primaryKeyLength;
	private String[] columns;
	private int[] length;
	private String[] types;
	private boolean[] nullabled;

	public Table(String name, Connection con) {
		try {
			DatabaseMetaData metaData = con.getMetaData();
			this.setName(name);
			ResultSet rs_primary_key = metaData.getPrimaryKeys(con.getCatalog(), metaData.getUserName(), name);
			rs_primary_key.last();
			String[] primary_key = new String[rs_primary_key.getRow()];
			primaryKeyType = new String[rs_primary_key.getRow()];
			primaryKeyLength = new int[rs_primary_key.getRow()];
			rs_primary_key.first();
			int i = 0;
			primary_key[0] = rs_primary_key.getString("COLUMN_NAME");
			// primaryKeyType[0] = rs_primary_key.getString("TYPE_NAME");
			// primaryKeyLength[0] = rs_primary_key.getInt("COLUMN_SIZE");
			while (rs_primary_key.next()) {
				i++;
				primary_key[i] = rs_primary_key.getString("COLUMN_NAME");
				// primaryKeyType[i] = rs_primary_key.getString("TYPE_NAME");
				// primaryKeyLength[i] = rs_primary_key.getInt("COLUMN_SIZE");
				logger.log(Level.INFO, "表" + name + "主键0：" + primary_key[i]);
			}
			// 获取主键
			this.setPrimaryKey(primary_key);
			ResultSet rs_column = metaData.getColumns(con.getCatalog(), metaData.getUserName(), name, null);
			rs_column.last();
			String[] _columns = new String[rs_column.getRow()];
			int[] _length = new int[rs_column.getRow()];
			boolean[] _nullabled = new boolean[rs_column.getRow()];
			String[] _types = new String[rs_column.getRow()];
			rs_column.first();
			i=0;
			_columns[0] = rs_column.getString("COLUMN_NAME");
			_types[0] = rs_column.getString("TYPE_NAME");
			_length[0] = rs_column.getInt("COLUMN_SIZE");
			_nullabled[0] = rs_column.getBoolean("NULLABLE");
			// 提取主键信息 并记录
			for(int j=0;j<this.primaryKey.length;j++){
				if(_columns[0].equals(this.primaryKey[j])){
					primaryKeyType[j] = _types[0];
					primaryKeyLength[j] = _length[0];
				}
			}
			while (rs_column.next()) {
				i++;
				_columns[i] = rs_column.getString("COLUMN_NAME");
				_types[i] = rs_column.getString("TYPE_NAME");
				_length[i] = rs_column.getInt("COLUMN_SIZE");
				_nullabled[i] = rs_column.getBoolean("NULLABLE");
				// 提取主键信息 并记录
				for(int j=0;j<this.primaryKey.length;j++){
					if(_columns[i].equals(this.primaryKey[j])){
						primaryKeyType[j] = _types[i];
						primaryKeyLength[j] = _length[i];
					}
				}
				logger.log(Level.INFO, "表" + name + "列" + i + "：" + _columns[i] + " " + _types[i] + " " + _length[i] + " " + _nullabled[i]);
			}
			this.setColumns(_columns);
			this.setLength(_length);
			this.setTypes(_types);
			this.setNullabled(_nullabled);
			logger.log(Level.INFO, name + "表装载成功");
		} catch (SQLException e) {
			logger.log(Level.WARNING, name + "表装载失败", e);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String[] primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String[] getColumns() {
		return columns;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	public int[] getLength() {
		return length;
	}

	public void setLength(int[] length) {
		this.length = length;
	}

	public String[] getTypes() {
		return types;
	}

	public void setTypes(String[] types) {
		this.types = types;
	}

	public boolean[] getNullabled() {
		return nullabled;
	}

	public void setNullabled(boolean[] nullabled) {
		this.nullabled = nullabled;
	}

	/**
	 * 将本数据库表映射为C(增加)U(更新)R(查询单个查询多个)D(删除)五个router
	 * @return
	 */
	public List<Router> toRouter(){
		List<Router> routers = new ArrayList<Router>();
		Map<String,String> c_validators = new HashMap<String,String>();
		for(int i=0;i<this.columns.length;i++){
			for(int j=0;j<this.primaryKey.length;j++){
				if(this.columns[i].equals(this.primaryKey[j])){
					// 删改的时候，主键参数不是必须的，因为url中自带。如果增加记录的时候，主键自增
					c_validators.put(this.columns[i], this.types[i].toLowerCase() + ":" + this.length[i]);
				}else{
					c_validators.put(this.columns[i], (this.nullabled[i]?"":"required|") + this.types[i].toLowerCase() + ":" + this.length[i]);
				}
			}
		}
		String key = "";
		for(int i=0;i<this.primaryKey.length;i++){
			key += "/" + "{" + this.name + "." + this.primaryKey[i] + ":" + this.primaryKeyType[i].toLowerCase()  + ":" + this.primaryKeyLength[i] + "}";
		}
		// c 创建
		Router c = new Router();
		c.setName("/" + this.getName());
		c.setProcessors("utf8=>validate=>table_curd");
		c.setResourceName(this.getName());
		c.setMethod("POST");
		c.setValidators(c_validators);
		routers.add(c);
		// d 删除实例
		Router d = new Router();
		d.setName("/" + this.getName() + key);
		d.setProcessors("utf8=>validate=>table_curd");
		d.setResourceName(this.getName());
		d.setMethod("DELETE");
		d.setValidators(null);
		routers.add(d);
		Map<String,String> u_validators = new HashMap<String,String>();
		for(int i=0;i<this.columns.length;i++){
			u_validators.put(this.columns[i], this.types[i].toLowerCase() + ":" + this.length[i]);
		}
		// u 更新实例
		Router u = new Router();
		u.setName("/" + this.getName() + key);
		u.setProcessors("utf8=>validate=>table_curd");
		u.setResourceName(this.getName());
		u.setMethod("PUT");
		u.setValidators(u_validators);
		routers.add(u);
		// r 获取单个实例
		Router r_1 = new Router();
		r_1.setName("/" + this.getName() + key);
		r_1.setProcessors("utf8=>validate=>table_curd");
		r_1.setResourceName(this.getName());
		r_1.setMethod("GET");
		r_1.setValidators(null);
		routers.add(r_1);
		// r 分页获取
		Map<String,String> validators_query = new HashMap<String,String>();
		validators_query.put("sort", "varchar:32");
		validators_query.put("size", "int:3");
		validators_query.put("page", "int:8");
		validators_query.put("columns", "varchar:" + this.columns.length * 32);
		for(int i=0;i<this.columns.length;i++){
			// validators_query.put(this.columns[i], this.types[i].toLowerCase() + ":" + this.length[i]);
			validators_query.put(this.columns[i] + "-eq", this.types[i].toLowerCase() + ":" + this.length[i]);
			validators_query.put(this.columns[i] + "-bt", "varchar:" + (this.length[i] * 2 + 1) );
			validators_query.put(this.columns[i] + "-lk", this.types[i].toLowerCase() + ":" + this.length[i]);
			validators_query.put(this.columns[i] + "-gt", this.types[i].toLowerCase() + ":" + this.length[i]);
			validators_query.put(this.columns[i] + "-lt", this.types[i].toLowerCase() + ":" + this.length[i]);
			validators_query.put(this.columns[i] + "-nu", "bit:1");
		}
		Router r_2 = new Router();
		r_2.setName("/" + this.getName());
		r_2.setProcessors("utf8=>validate=>table_curd");
		r_2.setResourceName(this.getName());
		r_2.setMethod("GET");
		r_2.setValidators(validators_query);
		routers.add(r_2);
		return routers;
	}
}
