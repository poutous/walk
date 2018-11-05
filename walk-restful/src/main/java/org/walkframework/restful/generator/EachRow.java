package org.walkframework.restful.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.walkframework.restful.exception.ExcelDataException;

/**
 * 遍历行数据
 * 
 * @author wangxin
 */
public class EachRow {

	/**
	 * 节点名 和 行数据 Map
	 */
	private Map<String, Object> rowByNodeNameMap = new LinkedHashMap<String, Object>();

	/**
	 * 类名 和 行数据 Map
	 */
	private Map<String, Row> rowByClassNameMap = new LinkedHashMap<String, Row>();

	public EachRow doForEach(List<Row> rows) {
		for (Row row : rows) {
			if (row.getContain() == null || "".equals(row.getContain()) || !row.getContain().matches("1|\\?|\\*|\\+")) {
				throw new ExcelDataException("第%d行,请指明正确的约束条件:1-一个 ,?-可有可无 ,*-0到多个 ,+一到多个", row.getRowNum());
			}
			forEachRowByNodeName(rowByNodeNameMap, row);
		}
		for (Row row : rows) {
			forEachRowByClassName(rowByClassNameMap, row);
			Row parentRow = findParentRow(rowByNodeNameMap, row);
			row.setParentRow(parentRow);
			if (parentRow != null) {
				parentRow.addSubRow(row);
			}
		}
		// 校验数据是否重复
		Set<Row> set = new HashSet<Row>();
		for (Row row : rows) {
			set.add(row);
		}
		return this;
	}

	/**
	 * 查找父行 原则：父行号小于子行号并且差值最小来确定唯一的父行
	 * 
	 * @param rowByNodeNameMap
	 * @param subRow
	 * @return
	 */
	private Row findParentRow(Map<String, Object> rowByNodeNameMap, Row subRow) {
		String parentNodeName = subRow.getParentNodeName();
		Object obj = rowByNodeNameMap.get(parentNodeName);
		if (obj instanceof Row) {
			return (Row) obj;
		} else {
			@SuppressWarnings("unchecked")
			List<Row> parentRows = (List<Row>) obj;
			int result = -1;
			Row testParentRow = null;
			int subRowNum = subRow.getRowNum();
			if (parentRows == null)
				return null;
			for (Row parentRow : parentRows) {
				int parentRowNum = parentRow.getRowNum();
				int differ = subRowNum - parentRowNum;
				if (result == -1 && differ > 0) {
					result = differ;
				}
				if (differ > 0 && result >= differ) {
					result = differ;
					testParentRow = parentRow;
				}
			}
			return testParentRow;
		}
	}

	private void forEachRowByClassName(Map<String, Row> map, Row row) {
		String nodeName = row.getNodeName();
		String className = row.getClassName();
		if (StringUtil.hasText(nodeName, className)) {
			if (map.containsKey(className)) {
				throw new ExcelDataException("发现第%d行与第%d行,类名都为%s", +map.get(className).getRowNum(), row.getRowNum(), className);
			} else {
				map.put(className, row);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void forEachRowByNodeName(Map<String, Object> map, Row row) {
		String nodeName = row.getNodeName();
		if (StringUtil.hasText(nodeName)) {
			if (map.containsKey(nodeName)) {
				Object value = map.get(nodeName);
				if (value != null) {
					if (value instanceof List) {
						((List<Row>) value).add(row);
					} else if (value instanceof Row) {
						List<Row> list = new ArrayList<Row>();
						list.add((Row) value);
						list.add(row);
						map.put(nodeName, list);
					}
				}
			} else {
				map.put(nodeName, row);
			}
		}
	}

	public Map<String, Object> getRowByNodeNameMap() {
		return rowByNodeNameMap;
	}

	public Map<String, Row> getRowByClassNameMap() {
		return rowByClassNameMap;
	}

}
