package org.walkframework.data.util;

import java.util.Comparator;

@SuppressWarnings("unchecked")
public class DataComparator implements Comparator {
	private String key;

	private int keyType;

	private int order;

	public DataComparator(String key, int keyType, int order) {
		this.key = key;
		this.keyType = keyType;
		this.order = order;
	}

	public int compare(Object o1, Object o2) {
		IData data1 = (IData) o1;
		IData data2 = (IData) o2;

		if (order == IDataset.ORDER_ASCEND) {
			if (keyType == IDataset.TYPE_STRING) {
				String value1 = data1.getString(key);
				String value2 = data2.getString(key);
				return value1.compareTo(value2);
			} else if (keyType == IDataset.TYPE_INTEGER) {
				int value1 = data1.getInteger(key, 0);
				int value2 = data2.getInteger(key, 0);
				return value1 < value2 ? -1 : (value1 == value2 ? 0 : 1);
			} else if (keyType == IDataset.TYPE_DOUBLE) {
				double value1 = data1.getDouble(key, 0D);
				double value2 = data2.getDouble(key, 0D);
				return value1 < value2 ? -1 : (value1 == value2 ? 0 : 1);
			}
		} else {
			if (keyType == IDataset.TYPE_STRING) {
				String value1 = data1.getString(key);
				String value2 = data2.getString(key);
				return value2.compareTo(value1);
			} else if (keyType == IDataset.TYPE_INTEGER) {
				int value1 = data1.getInteger(key, 0);
				int value2 = data2.getInteger(key, 0);
				return value1 > value2 ? -1 : (value1 == value2 ? 0 : 1);
			} else if (keyType == IDataset.TYPE_DOUBLE) {
				double value1 = data1.getDouble(key, 0D);
				double value2 = data2.getDouble(key, 0D);
				return value1 > value2 ? -1 : (value1 == value2 ? 0 : 1);
			}
		}
		return 0;
	}
}
