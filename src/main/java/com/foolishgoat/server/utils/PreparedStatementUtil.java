package com.foolishgoat.server.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementUtil {

	public static void setPreparedStatement(PreparedStatement ps, Object param, int index) throws SQLException {
		if (param instanceof Integer) {
			int value = ((Integer) param).intValue();
			ps.setInt(index, value);
		} else if (param instanceof String) {
			String s = (String) param;
			ps.setString(index, s);
		} else if (param instanceof Double) {
			double d = ((Double) param).doubleValue();
			ps.setDouble(index, d);
		} else if (param instanceof Float) {
			float f = ((Float) param).floatValue();
			ps.setFloat(index, f);
		} else if (param instanceof Long) {
			long l = ((Long) param).longValue();
			ps.setLong(index, l);
		} else if (param instanceof Boolean) {
			boolean b = ((Boolean) param).booleanValue();
			ps.setBoolean(index, b);
		} else if (param instanceof java.sql.Date) {
			ps.setDate(index, (java.sql.Date)param);
		}else if (param instanceof java.sql.Time) {
			ps.setTime(index, (java.sql.Time)param);
		}else if (param instanceof java.sql.Timestamp) {
			ps.setTimestamp(index, (java.sql.Timestamp)param);
		}else{
			String s = (String) param;
			ps.setString(index, s);
		}
	}

}
