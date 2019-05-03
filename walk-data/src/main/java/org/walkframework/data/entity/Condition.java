package org.walkframework.data.entity;

import java.util.List;

import org.walkframework.data.enums.SQLSymbol;

/**
 * 条件对象
 * 
 * @author shf675
 * 
 */
public class Condition {

	private String column;

	private SQLSymbol symbol;

	private Object[] values;

	public Condition(String column) {
		this.column = column;
	}

	public void andEqual(Object value) {
		this.symbol = SQLSymbol.EQUAL;
		this.values = new Object[] { value };
	}

	public void andNotEqual(Object value) {
		this.symbol = SQLSymbol.NOT_EQUAL;
		this.values = new Object[] { value };
	}

	public void andLike(Object value) {
		this.symbol = SQLSymbol.LIKE;
		this.values = new Object[] { value };
	}

	public void andNotLike(Object value) {
		this.symbol = SQLSymbol.NOT_LIKE;
		this.values = new Object[] { value };
	}

	public void andGreater(Object value) {
		this.symbol = SQLSymbol.GREATER;
		this.values = new Object[] { value };
	}

	public void andGreaterEqual(Object value) {
		this.symbol = SQLSymbol.GREATER_EQUAL;
		this.values = new Object[] { value };
	}

	public void andLess(Object value) {
		this.symbol = SQLSymbol.LESS;
		this.values = new Object[] { value };
	}

	public void andLessEqual(Object value) {
		this.symbol = SQLSymbol.LESS_EQUAL;
		this.values = new Object[] { value };
	}

	public void andIsNull() {
		this.symbol = SQLSymbol.IS_NULL;
	}

	public void andIsNotNull() {
		this.symbol = SQLSymbol.NOT_NULL;
	}

	public void andIn(Object... values) {
		this.symbol = SQLSymbol.IN;
		this.values = values[0] != null && values[0] instanceof List ? ((List<?>) values[0]).toArray() : values;
	}

	public void andNotIn(Object... values) {
		this.symbol = SQLSymbol.NOT_IN;
		this.values = values[0] != null && values[0] instanceof List ? ((List<?>) values[0]).toArray() : values;
	}

	public void andBetween(Object value1, Object value2) {
		this.symbol = SQLSymbol.BETWEEN;
		this.values = new Object[] { value1, value2 };
	}

	public void andNotBetween(Object value1, Object value2) {
		this.symbol = SQLSymbol.NOT_BETWEEN;
		this.values = new Object[] { value1, value2 };
	}

	public String getColumn() {
		return column;
	}

	public SQLSymbol getSymbol() {
		return symbol;
	}

	public Object[] getValues() {
		return values;
	}
}
