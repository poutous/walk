package org.walkframework.fusioncharts.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Element;
import org.walkframework.fusioncharts.util.FusionChartsConfigFactory;


public class TemplateItem {
	private String templateName;
	private String templatePath;
	private String swfPath;

	public String getTemplateName() {
		return this.templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplatePath() {
		return this.templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public String getSwfPath() {
		return this.swfPath;
	}

	public void setSwfPath(String swfPath) {
		this.swfPath = swfPath;
	}

	public static List<TemplateItem> getTemplateListFromElement(Element element) {
		List templateList = new ArrayList();
		for (Iterator i = element.elementIterator(); i.hasNext();) {
			Element item = (Element) i.next();
			if ("template".equals(item.getName())) {
				TemplateItem templateItem = new TemplateItem();
				for (Iterator j = item.elementIterator(); j.hasNext();) {
					Element node = (Element) j.next();
					if ("template-name".equals(node.getName())) {
						templateItem.setTemplateName(node.getTextTrim());
					} else if ("template-path".equals(node.getName())) {
						templateItem.setTemplatePath(node.getTextTrim());
					} else if ("swf-path".equals(node.getName())) {
						templateItem.setSwfPath(FusionChartsConfigFactory.getSwfBasePath()+"/"+node.getTextTrim());
					}
				}
				templateList.add(templateItem);
			}
		}
		return templateList;
	}
}