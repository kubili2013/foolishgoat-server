package com.foolishgoat.server.utils;

public class TableUtil {
	/**
	 * 字符串转为数据库类型
	 * @param type
	 * @param value
	 * @return
	 */
	public static Object convert(String type, String value) {
		if (value == null) {
			return null;
		}
		type = type.toUpperCase();
		// bit 转为 布尔类型
		if ("BIT".equals(type)) {
			return TableUtil.stringToBoolean(value);
		}
		if ("INT".equals(type) || "SMALLINT".equals(type) || "SMALLINT".equals(type)) {
			return TableUtil.stringToInt(value);
		}
		if ("INTEGER".equals(type)) {
			return TableUtil.stringToLong(value);
		}
		// BIGINT 不支持
		// BOOLEAN 不支持
		if ("FLOAT".equals(type)) {
			return TableUtil.stringToFloat(value);
		}
		if ("DOUBLE".equals(type)) {
			return TableUtil.stringToDouble(value);
		}
		if ("DATE".equals(type)) {
			java.util.Date d = DateUtil.strToDate(value);
			return new java.sql.Date(d.getTime());
		}
		if ("TIME".equals(type)) {
			java.util.Date d = DateUtil.strToDate(value);
			return new java.sql.Time(d.getTime());
		}
		if ("DATETIME".equals(type) || "TIMESTAMP".equals(type)) {
			java.util.Date d = DateUtil.strToDate(value);
			return new java.sql.Timestamp(d.getTime());
		}
		if ("VARCHAR".equals(type) || "CHAR".equals(type) || "TEXT".equals(type)) {
			return value;
		}
		return value;
	}

	
	public static Integer stringToInt(String intstr) {
		Integer integer;
		integer = Integer.valueOf(intstr);
		return integer;
	}
	public static Long stringToLong(String longvallue) {
		Long l;
		l = Long.valueOf(longvallue);
		return l;
	}
	public static String intToString(int value) {
		Integer integer = new Integer(value);
		return integer.toString();
	}

	public static Double stringToDouble(String value) {
		Double d = new Double(value);
		return d;
	}
	public static String doubleToString(double value) {
		Double d = new Double(value);
		return d.toString();
	}
	public static Float stringToFloat(String floatstr) {
		Float floatee;
		floatee = Float.valueOf(floatstr);
		return floatee;
	}

	public static String floatToString(float value) {
		Float floatee = new Float(value);
		return floatee.toString();
	}

	public static java.util.Date stringToDate(String dateStr) {
		
		return DateUtil.strToDate(dateStr);
	}

	public static String dateToString(java.sql.Date datee) {
		return datee.toString();
	}
	
	public static Boolean stringToBoolean(String bool) {
		if("true".equals(bool) || "1".equals(bool) ){
			return true;
		}
		return false;
	}
}
