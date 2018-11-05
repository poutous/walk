package org.walkframework.fusioncharts.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.walkframework.fusioncharts.data.ChartItem;
import org.walkframework.fusioncharts.data.FusionChartsResult;
import org.walkframework.fusioncharts.data.TemplateItem;
import org.walkframework.fusioncharts.i18n.FusionChartsMessage;

import com.alibaba.fastjson.JSONObject;

import freemarker.template.Configuration;
import freemarker.template.Template;
public class FusionChartsUtils {
	private static Configuration defaultConfig;
	private static Configuration extendConfig;

	/**
	 * 入口
	 * 
	 * @param chartItemId
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
	public static String lists2FusionChartsJson(String chartItemId, List dataList) throws Exception {
		return lists2FusionChartsJson(chartItemId, dataList, null);
	}
	
	public static String lists2FusionChartsJson(String chartItemId, List dataList, String templateName) throws Exception {
		List<List> dataLists = new ArrayList<List>();
		dataLists.add(dataList);
		FusionChartsResult fusionChartsResult = lists2FusionChartsResult(chartItemId, dataLists, templateName);
		return JSONObject.toJSONString(fusionChartsResult);
	}

	private static FusionChartsResult lists2FusionChartsResult(String chartItemId, List<List> dataLists, String templateName) throws Exception {
		ChartItem chartItem = FusionChartsConfigFactory.getChartItem(chartItemId);
		TemplateItem templateItem = FusionChartsConfigFactory.getTemplate(templateName == null ? chartItem.getTemplateName() : templateName);

		FusionChartsResult fusionChartsResult = new FusionChartsResult();
		String xmlData = lists2FusionChartsXml(chartItem, templateItem, dataLists);
		fusionChartsResult.setXmlData(xmlData);
		fusionChartsResult.setSwfPath(templateItem.getSwfPath());
		return fusionChartsResult;
	}

	private static String lists2FusionChartsXml(ChartItem chartItem, TemplateItem templateItem, List<List> dataLists) throws Exception {
		ChartDataModelMaker modelMaker = chartItem.getModelMaker();
		Map<String, Object> dataModel = modelMaker.getChartDataModel(chartItem, dataLists);
		String xmlStr = null;
		try {
			xmlStr = renderData(templateItem, dataModel);
		} catch (Exception e) {
			throw new Exception(FusionChartsMessage.get("RenderDataError", new Object[] { chartItem.getId() }), e);
		}
		xmlStr = xmlStr.replace("\r", "").replace("\n", "").replace("\t", "");

		return xmlStr;
	}

	private static String renderData(TemplateItem templateItem, Map<String, Object> dataModel) throws Exception {
		StringWriter writer = new StringWriter();
		String templatePath = templateItem.getTemplatePath();

		if (templatePath.matches("default:.+")) {
			String templateFileName = templatePath.substring(8);
			Template template = null;
			try {
				template = getDefaultConfig().getTemplate(templateFileName);
			} catch (IOException e) {
				throw new Exception(FusionChartsMessage.get("GetTemplateError", new Object[] { FusionChartsConfigFactory.getDefaultTmplPath(), templateFileName }), e);
			}
			template.process(dataModel, writer);
		} else if (templatePath.matches("extend:.+")) {
			String templateFileName = templatePath.substring(7);
			Template template = null;
			try {
				template = getExtendConfig().getTemplate(templateFileName);
			} catch (IOException e) {
				throw new Exception(FusionChartsMessage.get("GetTemplateError", new Object[] { FusionChartsConfigFactory.getExtendTmplPath(), templateFileName }), e);
			}
			template.process(dataModel, writer);
		}
		writer.flush();
		return writer.toString();
	}

	private static Configuration getDefaultConfig() {
		if (defaultConfig == null) {
			defaultConfig = new Configuration();
		}
		String defaultTmplPath = FusionChartsConfigFactory.getDefaultTmplPath();
		defaultConfig.setClassForTemplateLoading(FusionChartsUtils.class, defaultTmplPath);
		return defaultConfig;
	}

	private static Configuration getExtendConfig() {
		if (extendConfig == null) {
			extendConfig = new Configuration();
		}
		String extendTmplPath = FusionChartsConfigFactory.getExtendTmplPath();
		extendConfig.setClassForTemplateLoading(FusionChartsUtils.class, extendTmplPath);
		return extendConfig;
	}
}
