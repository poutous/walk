package org.walkframework.data.enums;


/**
 * SQL符号定义
 * 
 * @author shf675
 *
 */
public enum SQLSymbol {
	EQUAL(" = "),
	NOT_EQUAL(" <> "),
	LIKE(" LIKE "),
	NOT_LIKE(" NOT LIKE "),
	GREATER(" > "),
	GREATER_EQUAL(" >= "),
	LESS(" < "),
	LESS_EQUAL(" <= "),
	IS_NULL(" IS NULL "), 
	NOT_NULL(" IS NOT NULL "),
	IN(" IN "),
	NOT_IN(" NOT IN "),
	BETWEEN(" BETWEEN "),
	NOT_BETWEEN(" NOT BETWEEN ")
	;
	
	public String value;
	
	SQLSymbol(String value){
		this.value = value;
	}
}
