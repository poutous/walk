package org.walkframework.fusioncharts.data;

public class Dataset {
	private int index;
	private String seriesName;
	private String showValues;
	private String parentYaxis;
	private String renderAs;
	private String displayField;
	private String hoverTextField;
	private String valueField;
	private String color;
	private String linkFunction;
	private String linkField;
	private String alpha;

	public String getAlpha() {
		return alpha;
	}

	public void setAlpha(String alpha) {
		this.alpha = alpha;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getSeriesName() {
		return this.seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public String getShowValues() {
		return this.showValues;
	}

	public void setShowValues(String showValues) {
		this.showValues = showValues;
	}

	public String getParentYaxis() {
		return this.parentYaxis;
	}

	public void setParentYaxis(String parentYaxis) {
		this.parentYaxis = parentYaxis;
	}

	public String getRenderAs() {
		return this.renderAs;
	}

	public void setRenderAs(String renderAs) {
		this.renderAs = renderAs;
	}

	public String getDisplayField() {
		return this.displayField;
	}

	public void setDisplayField(String displayField) {
		this.displayField = displayField;
	}

	public String getHoverTextField() {
		return this.hoverTextField;
	}

	public void setHoverTextField(String hoverTextField) {
		this.hoverTextField = hoverTextField;
	}

	public String getValueField() {
		return this.valueField;
	}

	public void setValueField(String valueField) {
		this.valueField = valueField;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getLinkFunction() {
		return this.linkFunction;
	}

	public void setLinkFunction(String linkFunction) {
		this.linkFunction = linkFunction;
	}

	public String getLinkField() {
		return this.linkField;
	}

	public void setLinkField(String linkField) {
		this.linkField = linkField;
	}
}
