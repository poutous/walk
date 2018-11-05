package org.walkframework.fusioncharts.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.walkframework.fusioncharts.data.ChartItem;
import org.walkframework.fusioncharts.data.TemplateItem;
import org.walkframework.fusioncharts.i18n.FusionChartsMessage;


/**
 * FusionCharts配置
 * 可在Spring进行配置，如下
 * <!-- Spring加载图表参数配置，将下面这段配置放到Spring配置文件中 -->
 <bean class="org.walkframework.fusioncharts.util.FusionChartsConfigFactory">
 <property name="productMode" value="true"/>	//是否生产模式，非生产模式时修改图表配置文件（除模板配置文件）会立即生效
 <property name="fusionchartsConfigFile" value="fusioncharts/fusioncharts-config.xml"/>//模板文件根路径
 </bean>
 */
public class FusionChartsConfigFactory {
	private static final Logger log = LoggerFactory.getLogger(FusionChartsConfigFactory.class);

	private static final Map<String, ChartItem> chartItems = new LinkedHashMap<String, ChartItem>();

	private static final Map<String, TemplateItem> templates = new LinkedHashMap<String, TemplateItem>();

	private static final Map<String, String> configMap = new HashMap<String, String>();

	private static boolean fusionChartsConfigLoaded = false;

	// 是否生产模式，非生产模式时修改图表配置文件（除模板配置文件）会立即生效
	private static String productMode = "true";

	// fusioncharts配置文件
	private static String fusionchartsConfigFile = "fusioncharts/fusioncharts-config.xml";

	public static void setProductMode(String productMode) {
		FusionChartsConfigFactory.productMode = productMode;
	}

	/**
	 * Spring加载fusioncharts配置文件
	 * 
	 * @param fusionchartsConfigFile
	 * @throws Exception 
	 */
	public static void setFusionchartsConfigFile(String fusionchartsConfigFile) throws Exception {
		FusionChartsConfigFactory.fusionchartsConfigFile = fusionchartsConfigFile;
		if (getFusionchartsConfigFile() != null) {
			try {
				// 加载fusioncharts 参数、模板
				loadFusionChartsConfig(getFusionchartsConfigFile());

				// 加载fusioncharts 图表配置
				loadChartItemsConfig(getBasePath());
				
				fusionChartsConfigLoaded = true;
			} catch (Exception e) {
				log.error(FusionChartsMessage.get("LoadFusionchartsConfigFileError", new Object[] { getFusionchartsConfigFile() }), e);
			}
		}
	}

	/**
	 * 加载fusioncharts 参数、模板
	 * @param configFile
	 * @throws Exception
	 */
	private static void loadFusionChartsConfig(String configFile) throws Exception {
		loadConfig(new PathMatchingResourcePatternResolver().getResource(configFile).getInputStream(), configFile);
	}

	/**
	 * 加载图表配置文件
	 * 
	 * @param loaderBasePath
	 * @throws Exception
	 */
	private static void loadChartItemsConfig(String loaderBasePath) throws Exception {
		try {
			String location = "classpath:" + loaderBasePath + "/**/*.xml";
			ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
			Resource[] source = resourceLoader.getResources(location);
			for (int i = 0; i < source.length; i++) {
				String resource = source[i].getURL().getPath();
				loadConfig(source[i].getURL().openStream(), resource);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}

	/**
	 * 加载配置
	 * 
	 * @param configFile
	 * @throws Exception
	 */
	private static void loadConfig(InputStream is, String configFile) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			log.info("configFile:{}", configFile);
			doc = reader.read(is);
		} catch (Exception e) {
			throw new Exception(FusionChartsMessage.get("FusionChartsConfigFileParseError", new Object[] { configFile }), e);
		}
		Element root = doc.getRootElement();
		for (Iterator i = root.elementIterator(); i.hasNext();) {
			Element item = (Element) i.next();
			if (item.getName().matches("base-path|swf-base-path|default-template-path|extend-template-path")) {
				configMap.put(item.getName(), item.attributeValue("value"));
			} else if ("templates".equals(item.getName())) {
				List<TemplateItem> templateList = TemplateItem.getTemplateListFromElement(item);
				for (int j = 0; j < templateList.size(); j++) {
					String key = ((TemplateItem) templateList.get(j)).getTemplateName();
					templates.put(key, (TemplateItem) templateList.get(j));
				}
			} else if ("chart-item".equals(item.getName())) {
				ChartItem chartItem = ChartItem.getChartItemFromElement(item);
				String key = getKeyPrefix(configFile) + "." + chartItem.getId();
				chartItems.put(key, chartItem);
			} else if ("import".equals(item.getName())) {
				Attribute resource = item.attribute("resource");
				if (resource == null) {
					throw new Exception(FusionChartsMessage.get("FusionChartsConfigResouceNotConfigured", new Object[] { configFile }));
				}
				try {
					loadFusionChartsConfig(resource.getText());
				} catch (Exception e) {
					throw new Exception(FusionChartsMessage.get("LoadFusionchartsConfigFileError", new Object[] { resource.getText() }), e);
				}
			}
		}
	}

	public static String getKeyPrefix(String configFile) throws Exception {
		if (configFile.startsWith("/")) {
			configFile = configFile.substring(1, configFile.length());
		}
		return configFile.substring(getBasePath().length() + 1, configFile.lastIndexOf(".")).replaceAll("\\\\", "/");
	}

	/**
	 * 获取模板
	 * 
	 * @param templateName
	 * @return
	 * @throws Exception
	 */
	public static TemplateItem getTemplate(String templateName) throws Exception {
		if (getProductMode()) {
			if (templates.isEmpty()) {
				loadFusionChartsConfig(getFusionchartsConfigFile());
			}
		} else {// 开发模式每次调用都加载模板配置
			loadFusionChartsConfig(getFusionchartsConfigFile());
		}

		TemplateItem templateItem = (TemplateItem) templates.get(templateName);
		if (templateItem == null) {
			throw new Exception(FusionChartsMessage.get("TemplateConfigNotFoundError", new Object[] { templateName }));
		}
		return templateItem;
	}

	/**
	 * 获取图表配置
	 * 
	 * @param chartItemId
	 * @return
	 * @throws Exception
	 */
	public static ChartItem getChartItem(String chartItemId) throws Exception {
		if (!isFusionChartsConfigLoaded()) {
			loadFusionChartsConfig(getFusionchartsConfigFile());
		}

		if (getProductMode()) {
			if (chartItems.isEmpty()) {
				loadChartItemsConfig(getBasePath());
			}
		} else {// 开发模式每次调用都加载图表配置
			String xmlPath = getBasePath() + "/" + chartItemId.substring(0, chartItemId.lastIndexOf(".")) + ".xml";
			loadFusionChartsConfig(xmlPath);
		}
		ChartItem chartItem = (ChartItem) chartItems.get(chartItemId);
		if (chartItem == null) {
			throw new Exception(FusionChartsMessage.get("ChartItemConfigNotFoundError", new Object[] { chartItemId }));
		}
		return chartItem;
	}

	public static String getBasePath() throws Exception {
		String basePath = (String) configMap.get("base-path");
		if (basePath.startsWith("/")) {
			basePath = basePath.substring(1, basePath.length());
		}
		if (basePath.endsWith("/")) {
			basePath = basePath.substring(0, basePath.length() - 1);
		}
		return basePath;
	}
	
	public static String getSwfBasePath() {
		return (String) configMap.get("swf-base-path");
	}

	public static String getDefaultTmplPath() {
		return (String) configMap.get("default-template-path");
	}

	public static String getExtendTmplPath() {
		return (String) configMap.get("extend-template-path");
	}

	public static boolean getProductMode() throws Exception {
		return Boolean.valueOf(productMode);
	}

	public static String getFusionchartsConfigFile() throws Exception {
		return fusionchartsConfigFile;
	}

	public static boolean isFusionChartsConfigLoaded() {
		return fusionChartsConfigLoaded;
	}

}
