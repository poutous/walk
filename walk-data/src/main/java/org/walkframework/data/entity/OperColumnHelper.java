package org.walkframework.data.entity;

/**
 * @author shf675
 * 
 */
public abstract class OperColumnHelper {

	public static String getOperColumnProperty(OperColumn operColumn) {
		return operColumn.getOperColumnProperty();
	}
	
	public static String getOperColumn(OperColumn operColumn) {
		return operColumn.getOperColumn();
	}

	public static Class<?> getOperColumnType(OperColumn operColumn) {
		return operColumn.getOperColumnType();
	}

	public static Object getOperColumnValue(OperColumn operColumn) {
		return operColumn.getOperColumnValue();
	}

	public static void cancelCondition(OperColumn operColumn) {
		operColumn.cancelCondition();
	}

	public static String getSort(OperColumn operColumn) {
		return operColumn.getSort();
	}

	public static boolean isCondition(OperColumn operColumn) {
		return operColumn.isCondition();
	}
}
