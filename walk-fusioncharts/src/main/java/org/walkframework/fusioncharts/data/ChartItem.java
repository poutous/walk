package org.walkframework.fusioncharts.data;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Element;
import org.walkframework.fusioncharts.i18n.FusionChartsMessage;
import org.walkframework.fusioncharts.util.ChartDataModelMaker;
import org.walkframework.fusioncharts.util.DefaultChartDataModelMaker;

public class ChartItem {
	private String id;
	private String templateName;
	private String caption;
	private String xaxisName;
	private String pyaxisName;
	private String syaxisName;
	private String clickFunciton;
	private List<String> categories;
	private List<Dataset> datasets;
	private ChartDataModelMaker modelMaker;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTemplateName() {
		return this.templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getCaption() {
		return this.caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getXaxisName() {
		return this.xaxisName;
	}

	public void setXaxisName(String xaxisName) {
		this.xaxisName = xaxisName;
	}

	public String getPyaxisName() {
		return this.pyaxisName;
	}

	public void setPyaxisName(String pyaxisName) {
		this.pyaxisName = pyaxisName;
	}

	public String getSyaxisName() {
		return this.syaxisName;
	}

	public void setSyaxisName(String syaxisName) {
		this.syaxisName = syaxisName;
	}

	public List<String> getCategories() {
		return this.categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public ChartDataModelMaker getModelMaker() {
		return this.modelMaker;
	}

	public void setModelMaker(ChartDataModelMaker modelMaker) {
		this.modelMaker = modelMaker;
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public String getClickFunciton() {
		return this.clickFunciton;
	}

	public void setClickFunciton(String clickFunciton) {
		this.clickFunciton = clickFunciton;
	}

	public static ChartItem getChartItemFromElement(Element element) throws Exception {
		ChartItem chartItem = new ChartItem();

		String id = element.attributeValue("id");
		if (id == null) {
			throw new Exception(FusionChartsMessage.get("MissingChartItemId", new Object[0]));
		}
		chartItem.setId(id);
		for (Iterator i = element.elementIterator(); i.hasNext();) {
			Element item = (Element) i.next();

			if ("template-name".equals(item.getName())) {
				chartItem.setTemplateName(item.getTextTrim());
			} else if ("caption".equals(item.getName())) {
				chartItem.setCaption(item.getTextTrim());
			} else if ("click-function".equals(item.getName())) {
				chartItem.setClickFunciton(item.getTextTrim());
			} else if ("x-axis-name".equals(item.getName())) {
				chartItem.setXaxisName(item.getTextTrim());
			} else if ("y-axis".equals(item.getName())) {
				for (Iterator j = item.elementIterator(); j.hasNext();) {
					Element node = (Element) j.next();
					if ("p-y-axis-name".equals(node.getName())) {
						chartItem.setPyaxisName(node.getTextTrim());
					} else if ("s-y-axis-name".equals(node.getName())) {
						chartItem.setSyaxisName(node.getTextTrim());
					}
				}

			} else if ("categories".equals(item.getName())) {
				List categories = new ArrayList();
				for (Iterator j = item.elementIterator(); j.hasNext();) {
					Element node = (Element) j.next();
					if ("category".equals(node.getName())) {
						categories.add(node.attributeValue("name"));
					}
				}
				chartItem.setCategories(categories);
			} else if ("dataset".equals(item.getName())) {
				if (chartItem.getDatasets() != null) {
					throw new Exception(FusionChartsMessage.get("DatasetsNotUnique", new Object[] { chartItem.getId() }));
				}

				List datasets = new ArrayList();
				Dataset dataset = getDatasetFromElement(item);
				datasets.add(dataset);
				chartItem.setDatasets(datasets);
			} else if ("datasets".equals(item.getName())) {
				if (chartItem.getDatasets() != null) {
					throw new Exception(FusionChartsMessage.get("DatasetsNotUnique", new Object[] { chartItem.getId() }));
				}
				List datasets = new ArrayList();
				for (Iterator j = item.elementIterator(); j.hasNext();) {
					Element node = (Element) j.next();
					if ("dataset".equals(node.getName())) {
						Dataset dataset = getDatasetFromElement(node);
						datasets.add(dataset);
					}
				}
				chartItem.setDatasets(datasets);
			} else if ("model-maker".equals(item.getName())) {
				String modelMakerName = item.getTextTrim();
				Object modelMaker = Class.forName(modelMakerName).newInstance();

				if ((modelMaker instanceof ChartDataModelMaker)) {
					chartItem.setModelMaker((ChartDataModelMaker) modelMaker);
				} else {
					throw new Exception(FusionChartsMessage.get("UnsupportedModelMakerType", new Object[] { chartItem.getId(), modelMakerName }));
				}
			}
		}

		if (chartItem.getModelMaker() == null) {
			chartItem.setModelMaker(new DefaultChartDataModelMaker());
		}
		return chartItem;
	}

	private static Dataset getDatasetFromElement(Element element) throws Exception {
		Dataset dataset = new Dataset();
		if (element.attributeValue("index") != null)
			dataset.setIndex(Integer.parseInt(element.attributeValue("index")));
		else {
			dataset.setIndex(0);
		}
		dataset.setSeriesName(element.attributeValue("series-name"));
		dataset.setShowValues(element.attributeValue("show-values"));
		dataset.setParentYaxis(element.attributeValue("parent-y-axis"));
		dataset.setRenderAs(element.attributeValue("render-as"));
		dataset.setLinkFunction(element.attributeValue("link-function"));
		for (Iterator i = element.elementIterator(); i.hasNext();) {
			Element node = (Element) i.next();

			if ("display-field".equals(node.getName())) {
				dataset.setDisplayField(node.getTextTrim());
			}

			if ("hover-text-field".equals(node.getName())) {
				dataset.setHoverTextField(node.getTextTrim());
			}

			if ("value-field".equals(node.getName())) {
				dataset.setValueField(node.getTextTrim());
			}

			if ("link-field".equals(node.getName())) {
				dataset.setLinkField(node.getTextTrim());
			}

			if ("color".equals(node.getName())) {
				dataset.setColor(node.getTextTrim());
			}
			
			if ("alpha".equals(node.getName())) {
				dataset.setAlpha(node.getTextTrim());
			}
		}
		return dataset;
	}
}