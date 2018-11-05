package org.walkframework.data.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public interface IData<K,V> extends Map<K,V>, Serializable {
	
	/**
	 * @param name
	 * @return
	 */
	public V get(Object key);
	
	/**
	 * get object
	 * @param name
	 * @param def
	 * @return Object
	 */
	public V get(Object key, Object def);
	
	/**
	 * get names
	 * @return String[]
	 */
	public String[] getNames();
	
	/**
	 * get string
	 * @param name
	 * @return String
	 */
	public String getString(String name);
	
	/**
	 * get string
	 * @param name
	 * @param defaultValue
	 * @return String
	 */
	public String getString(String name, String defaultValue);
	
	/**
	 * get Short
	 * @param name
	 * @return Short
	 */
	public Short getShort(String name);

	/**
	 * get Short
	 * @param name
	 * @param defaultValue
	 * @return Short
	 */
	public Short getShort(String name, Short defaultValue);
	
	/**
	 * get Integer
	 * @param name
	 * @return Integer
	 */
	public Integer getInteger(String name);

	/**
	 * get Integer
	 * @param name
	 * @param defaultValue
	 * @return Integer
	 */
	public Integer getInteger(String name, Integer defaultValue);
	
	/**
	 * get Long
	 * @param name
	 * @return Long
	 */
	public Long getLong(String name);

	/**
	 * get Long
	 * @param name
	 * @param defaultValue
	 * @return Long
	 */
	public Long getLong(String name, Long defaultValue);
	
	/**
	 * get Float
	 * @param name
	 * @return Float
	 */
	public Float getFloat(String name);
	
	/**
	 * get Float
	 * @param name
	 * @param defaultValue
	 * @return Float
	 */
	public Float getFloat(String name, Float defaultValue);
	
	
	/**
	 * get Double
	 * @param name
	 * @return Double
	 */
	public Double getDouble(String name);
	
	/**
	 * get Double
	 * @param name
	 * @param defaultValue
	 * @return Double
	 */
	public Double getDouble(String name, Double defaultValue);
	
	/**
	 * get BigDecimal
	 * @param name
	 * @param defaultValue
	 * @return double
	 */
	public BigDecimal getBigDecimal(String name);
	
	/**
	 * get BigDecimal
	 * @param name
	 * @param defaultValue
	 * @return double
	 */
	public BigDecimal getBigDecimal(String name, BigDecimal defaultValue);
	
	/**
	 * get Boolean
	 * @param name
	 * @return Boolean
	 */
	public Boolean getBoolean(String name);
	
	/**
	 * get Boolean
	 * @param name
	 * @param defaultValue
	 * @return Boolean
	 */
	public Boolean getBoolean(String name, Boolean defaultValue);
	
	/**
	 * get date
	 * @param name
	 * @param format
	 * @return date
	 */
	public Date getDate(String name, String format);
	
	/**
	 * get date
	 * @param name
	 * @param format
	 * @param defaultValue
	 * @return date
	 */
	public Date getDate(String name, String format, Date defaultValue);
}