package org.walkframework.fusioncharts.util;

import java.util.List;
import java.util.Map;

import org.walkframework.fusioncharts.data.ChartItem;


public abstract interface ChartDataModelMaker {
	public abstract Map<String, Object> getChartDataModel(ChartItem paramChartItem, List<List> paramList) throws Exception;
}