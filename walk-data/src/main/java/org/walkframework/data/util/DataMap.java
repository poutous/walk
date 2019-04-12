package org.walkframework.data.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class DataMap<K,V> extends HashMap<K,V> implements IData<K,V> {
	private static final long serialVersionUID = 1L;

	@Override
	public V get(Object name) {
		return super.get(name);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object name, Object def) {
		Object value = get(name);
		return (V)(value == null ? def : value);
	}

	@Override
	public String[] getNames() {
		String[] names = (String[]) keySet().toArray(new String[0]);
		Arrays.sort(names);
		return names;
	}
	
	@Override
	public String getString(String name) {
		Object value = get(name);
		return value == null ? null : value.toString();
	}
	
	@Override
	public String getString(String name, String defaultValue) {
		Object value = get(name, defaultValue);
		return value == null ? null : value.toString();
	}
	
	@Override
	public Short getShort(String name) {
		return getShort(name, null);
	}

	@Override
	public Short getShort(String name, Short defaultValue) {
		String value = getString(name, "");
		if("".equals(value)){
			return defaultValue;
		}
		return Short.parseShort(value);
	}
	
	@Override
	public Integer getInteger(String name) {
		return getInteger(name, null);
	}
	
	@Override
	public Integer getInteger(String name, Integer defaultValue) {
		String value = getString(name, "");
		if("".equals(value)){
			return defaultValue;
		}
		return Integer.parseInt(value);
	}
	
	@Override
	public Long getLong(String name) {
		return getLong(name, null);
	}

	@Override
	public Long getLong(String name, Long defaultValue) {
		String value = getString(name, "");
		if("".equals(value)){
			return defaultValue;
		}
		return Long.parseLong(value);
	}
	
	@Override
	public Float getFloat(String name) {
		return getFloat(name, null);
	}

	@Override
	public Float getFloat(String name, Float defaultValue) {
		String value = getString(name, "");
		if("".equals(value)){
			return defaultValue;
		}
		return Float.parseFloat(value);
	}
	
	@Override
	public Double getDouble(String name) {
		return getDouble(name, null);
	}
	
	@Override
	public Double getDouble(String name, Double defaultValue) {
		String value = getString(name, "");
		if("".equals(value)){
			return defaultValue;
		}
		return Double.parseDouble(value);
	}
	
	@Override
	public BigDecimal getBigDecimal(String name) {
		return getBigDecimal(name, null);
	}

	@Override
	public BigDecimal getBigDecimal(String name, BigDecimal defaultValue) {
		String value = getString(name, "");
		if("".equals(value)){
			return defaultValue;
		}
		return new BigDecimal(value);
	}
	
	@Override
	public Boolean getBoolean(String name) {
		return getBoolean(name, null);
	}
	
	@Override
	public Boolean getBoolean(String name, Boolean defaultValue) {
		String value = getString(name, "");
		if("".equals(value)){
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}

	@Override
	public Date getDate(String name, String format) {
		return getDate(name, format, null);
	}

	@Override
	public Date getDate(String name, String format, Date defaultValue) {
		if(format == null || "".equals(format)){
			return defaultValue;
		}
		
		String timeStr = getString(name, "");
		if ("".equals(timeStr))
			return defaultValue;
		
		Date date = null;
		if (format.length() != timeStr.length())
			format = getTimestampFormat(timeStr);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			date = new Timestamp(sdf.parse(timeStr).getTime());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return date;
	}
	
	private String getTimestampFormat(String value) {
		switch (value.length()) {
		case 4:
			return "yyyy";
		case 6:
			return "yyyyMM";
		case 7:
			return "yyyy-MM";
		case 8:
			return "yyyyMMdd";
		case 10:
			return "yyyy-MM-dd";
		case 13:
			return "yyyy-MM-dd HH";
		case 16:
			return "yyyy-MM-dd HH:mm";
		case 19:
			return "yyyy-MM-dd HH:mm:ss";
		case 21:
			return "yyyy-MM-dd HH:mm:ss.S";
		}
		return null;
	}
	
}