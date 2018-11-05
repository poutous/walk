package org.walkframework.restful.generator;

import java.util.ArrayList;
import java.util.List;

import org.walkframework.restful.exception.ExcelDataException;

/**
 * @author shf675
 *
 */
public class Row {
	private final String nodeName;
	private final String parentNodeName;
	private final String contain;
	private final String type;
	private final String length;
	private final String desc;
	private final String className;
	private final int rowNum;
	private Row parentRow;
	private List<Row> subRows;

	public Row getParentRow() {
		return parentRow;
	}

	public void setParentRow(Row parentRow) {
		this.parentRow = parentRow;
	}

	public List<Row> getSubRows() {
		return subRows;
	}

	public boolean IsComplex() {
		// 有类名并且没有子节点的也暂且认为是复杂节点
		return subRows != null || StringUtil.hasText(className);
	}

	public void addSubRow(Row subRow) {
		if (this.subRows == null) {
			this.subRows = new ArrayList<Row>();
		}
		this.subRows.add(subRow);
	}

	public String getClassName() {
		return className;
	}

	public int getRowNum() {
		return rowNum;
	}

	public Row(int rowNum, String nodeName, String parentNodeName, String contain, String type, String length, String desc, String className) {
		this.rowNum = rowNum;
		this.nodeName = nodeName;
		this.parentNodeName = parentNodeName;
		this.contain = contain;
		this.type = type;
		this.length = length;
		this.desc = desc;
		this.className = className;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getParentNodeName() {
		return parentNodeName;
	}

	public String getContain() {
		return contain;
	}

	public String getType() {
		return type;
	}

	public String getLength() {
		return length;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		// 如果节点名和父节点都一样我们可认为数据重复
		if (obj == null || !(obj instanceof Row))
			return false;
		Row row = (Row) obj;
		boolean equal = (this.nodeName.equals(row.getNodeName()) && this.parentRow == row.getParentRow());
		if (equal) {
			throw new ExcelDataException("第%d行,第%d行节点名相同且父节点一致!", row.getRowNum(), rowNum);
		}
		return equal;
	}

	@Override
	// toJSON
	public String toString() {
		return "{ nodeName : '" + nodeName + "', parentNodeName : '" + parentNodeName + "', contain : '" + contain + "', type : '" + type + "', length : '" + length + "', desc : '" + desc + "', className : '" + className + "', rowNum : " + rowNum + " }";
	}

}
