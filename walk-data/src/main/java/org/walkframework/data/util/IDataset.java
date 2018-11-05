package org.walkframework.data.util;

import java.util.List;

@SuppressWarnings("unchecked")
public interface IDataset<E> extends List<E> {
	
	/**
	 * Sort order Constants
	 */
	public static final int ORDER_ASCEND = 0;

	public static final int ORDER_DESCEND = 1;

	/**
	 * Key type Constants
	 */
	public static final int TYPE_STRING = 2;

	public static final int TYPE_INTEGER = 3;

	public static final int TYPE_DOUBLE = 4;
	
	/**
	 * get serializable id
	 * @return String
	 */
	public String getSerializableId();
	
	/**
	 * set serializable id
	 * @param serializableId
	 */
	public void setSerializableId(String serializableId);
	
	/**
	 * get serializable path
	 * @return String
	 */
	public String getSerializablePath();
	
	/**
	 * set serializable path
	 * @param serializablePath
	 */
	public void setSerializablePath(String serializablePath);
	
	/**
	 * is serializable
	 * @return boolean
	 */
	public boolean isSerializable();

	/**
	 * set serializable
	 * @param serializable
	 */
	public void setSerializable(boolean serializable);

	/**
	 * is batch serializable
	 * @return boolean
	 */
	public boolean isBatchSerializable();

	/**
	 * set batch serializable
	 * @param batchSerializable
	 */
	public void setBatchSerializable(boolean batchSerializable);

	/**
	 * set batch page size
	 * @return int
	 */
	public int getBatchPageSize();
	
	/**
	 * set batch page size
	 * @param batchPageSize
	 */
	public void setBatchPageSize(int batchPageSize);

	/**
	 * set batch page count
	 * @return int
	 */
	public int getBatchPageCount();
	
	/**
	 * set batch page count
	 * @param batchPageSize
	 */
	public void setBatchPageCount(int batchPageCount);
	
	/**
	 * get object
	 * @param index
	 * @param name
	 * @return Object
	 */
	public Object get(int index, String name);
	
	/**
	 * get object
	 * @param index
	 * @param name
	 * @param def
	 * @return Object
	 */
	public Object get(int index, String name, Object def);
	
	/**
	 * get names
	 * @return String[]
	 */
	public String[] getNames();

	/**
	 * to data
	 * @return IData
	 */
	public IData toData() throws Exception;
	
	
	/**
	 * get count
	 * @return int
	 */
	public int count();
	
	/**
	 * sort single (default ascend)
	 */
	public void sort(String key, int type);

	/**
	 * sort single
	 */
	public void sort(String key, int type, int order);

	/**
	 * sort double (default ascend)
	 */
	public void sort(String key1, int keyType1, String key2, int keyType2);

	/**
	 * sort double
	 */
	public void sort(String key1, int keyType1, int order1, String key2, int keyType2, int order2);
}