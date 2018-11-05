package org.walkframework.fusioncharts.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.walkframework.fusioncharts.data.ChartItem;
import org.walkframework.fusioncharts.data.Dataset;

public class DefaultChartDataModelMaker implements ChartDataModelMaker {
	@SuppressWarnings("unchecked")
	public Map<String, Object> getChartDataModel(ChartItem chartItem, List<List> dataLists) throws Exception {
		Map dataModel = new HashMap();

		List datasets = chartItem.getDatasets();
		dataModel.put("caption", chartItem.getCaption());
		dataModel.put("xAxisName", chartItem.getXaxisName());
		dataModel.put("PYAxisName", chartItem.getPyaxisName());
		dataModel.put("SYAxisName", chartItem.getSyaxisName());
		String clickFunction = chartItem.getClickFunciton();
		if ((clickFunction != null) && (!"".equals(clickFunction))) {
			dataModel.put("clickURL", "javascript:" + chartItem.getClickFunciton() + "()");
		}

		if (chartItem.getCategories() != null) {
			dataModel.put("categories", chartItem.getCategories());
		} else if (datasets.size() > 1) {
			List categories = new ArrayList();
			Dataset firstDataset = (Dataset) datasets.get(0);
			List dataList = (List) dataLists.get(firstDataset.getIndex());
			for (int i = 0; i < dataList.size(); i++) {
				String categoryName = ReflectionUtils.getFieldValue(dataList.get(i), firstDataset.getDisplayField()).toString();
				categories.add(categoryName);
			}
			dataModel.put("categories", categories);
		}
		List datasetList = new ArrayList();
		for (int i = 0; i < datasets.size(); i++) {
			Map datasetMap = new HashMap();
			Dataset dataset = (Dataset) datasets.get(i);
			List dataList = (List) dataLists.get(dataset.getIndex());
			datasetMap.put("seriesName", dataset.getSeriesName());
			datasetMap.put("showValues", dataset.getShowValues());
			datasetMap.put("parentYAxis", dataset.getParentYaxis());
			datasetMap.put("renderAs", dataset.getRenderAs());
			datasetMap.put("color", dataset.getColor());
			List setList = new ArrayList();
			for (int j = 0; j < dataList.size(); j++) {
				Map setMap = new HashMap();
				setMap.put("color", dataset.getColor());
				setMap.put("alpha", dataset.getAlpha());
				if ((dataset.getDisplayField() != null) && (!"".equals(dataset.getDisplayField()))) {
					setMap.put("display", ReflectionUtils.getFieldValue(dataList.get(j), dataset.getDisplayField()));
				}
				if ((dataset.getValueField() != null) && (!"".equals(dataset.getValueField()))) {
					Object value = ReflectionUtils.getFieldValue(dataList.get(j), dataset.getValueField());
					value = value == null || "".equals(value) ? "0" : value;
					setMap.put("value", value);
				}
				if ((dataset.getHoverTextField() != null) && (!"".equals(dataset.getHoverTextField()))) {
					setMap.put("hoverText", ReflectionUtils.getFieldValue(dataList.get(j), dataset.getHoverTextField()));
				}
				if ((dataset.getLinkField() != null) && (!"".equals(dataset.getLinkField()))) {
					String[] linkFields = dataset.getLinkField().split(",");
					StringBuilder linkFieldValueStr = new StringBuilder();
					for (int k = 0; k < linkFields.length; k++) {
						Object linkFieldValue = ReflectionUtils.getFieldValue(dataList.get(j), linkFields[k].trim());
						linkFieldValueStr.append(linkFieldValue == null ? "" : "'"+linkFieldValue.toString()+"',");
					}
					linkFieldValueStr.deleteCharAt(linkFieldValueStr.lastIndexOf(","));
					setMap.put("link", "javascript:" + dataset.getLinkFunction() + "(" + linkFieldValueStr.toString() + ")");
				}
				setList.add(setMap);
			}
			datasetMap.put("setList", setList);
			datasetList.add(datasetMap);
		}
		dataModel.put("datasetList", datasetList);
		return dataModel;
	}
}