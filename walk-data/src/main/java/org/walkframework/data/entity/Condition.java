package org.walkframework.data.entity;

import java.util.List;

import org.walkframework.data.enums.SQLSymbol;

/**
 * @author shf675
 * 
 */
public class Condition {

	private String column;

	private String symbol;

	private Object[] values;

	// true表示使用预处理语句，效率更高。反之表示静态语句
	private boolean pre = true;

	public Condition(String column) {
		this.column = column;

		// 默认为等号
		this.symbol = SQLSymbol.EQUAL.value;
	}

	public void andEqual(Object value) {
		this.symbol = SQLSymbol.EQUAL.value;
		this.values = new Object[] { value };
		this.pre = false;
	}

	public void andNotEqual(Object value) {
		this.symbol = SQLSymbol.NOT_EQUAL.value;
		this.values = new Object[] { value };
		this.pre = false;
	}

	public void andLike(Object value) {
		this.symbol = SQLSymbol.LIKE.value;
		this.values = new Object[] { value };
		this.pre = false;
	}

	public void andNotLike(Object value) {
		this.symbol = SQLSymbol.NOT_LIKE.value;
		this.values = new Object[] { value };
		this.pre = false;
	}

	public void andGreater(Object value) {
		this.symbol = SQLSymbol.GREATER.value;
		this.values = new Object[] { value };
		this.pre = false;
	}

	public void andGreaterEqual(Object value) {
		this.symbol = SQLSymbol.GREATER_EQUAL.value;
		this.values = new Object[] { value };
		this.pre = false;
	}

	public void andLess(Object value) {
		this.symbol = SQLSymbol.LESS.value;
		this.values = new Object[] { value };
		this.pre = false;
	}

	public void andLessEqual(Object value) {
		this.symbol = SQLSymbol.LESS_EQUAL.value;
		this.values = new Object[] { value };
		this.pre = false;
	}

	public void andIsNull() {
		this.symbol = SQLSymbol.IS_NULL.value;
	}

	public void andIsNotNull() {
		this.symbol = SQLSymbol.NOT_NULL.value;
	}

	@SuppressWarnings("unchecked")
	public void andIn(Object... values) {
		this.symbol = SQLSymbol.IN.value;
		this.values = values[0] != null && values[0] instanceof List ? ((List) values[0]).toArray() : values;
		this.pre = false;
	}

	@SuppressWarnings("unchecked")
	public void andNotIn(Object... values) {
		this.symbol = SQLSymbol.NOT_IN.value;
		this.values = values[0] != null && values[0] instanceof List ? ((List) values[0]).toArray() : values;
		this.pre = false;
	}

	public void andBetween(Object value1, Object value2) {
		this.symbol = SQLSymbol.BETWEEN.value;
		this.values = new Object[] { value1, value2 };
		this.pre = false;
	}

	public void andNotBetween(Object value1, Object value2) {
		this.symbol = SQLSymbol.NOT_BETWEEN.value;
		this.values = new Object[] { value1, value2 };
		this.pre = false;
	}

	public String getColumn() {
		return column;
	}

	public String getSymbol() {
		return symbol;
	}

	public Object[] getValues() {
		return values;
	}

	public boolean isPre() {
		return pre;
	}
}
