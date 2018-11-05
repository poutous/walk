package org.walkframework.base.system.config;

import java.io.InputStream;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.walkframework.base.system.common.Common;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.DatasetList;
import org.walkframework.data.util.IData;
import org.walkframework.data.util.IDataset;


public class XMLConfig implements IConfig {

	public static final String XML_FORMAT_CONTENT = "CONTENT";
	public static final String XML_FORMAT_ATTRIBUTE = "ATTRIBUTE";
	
	private Element root;

	/**
	 * construct function
	 * @param file
	 * @throws Exception
	 */
	public XMLConfig(String file) throws Exception {
		root = new SAXBuilder().build(Common.getInstance().getClassResource(file).toString()).getRootElement();
	}
	
	
	
	/**
	 * construct function
	 * @param in
	 * @throws Exception
	 */
	public XMLConfig(InputStream in) throws Exception {
		root = new SAXBuilder().build(in).getRootElement();
	}
	
	/**
	 * get child
	 * for example:subsys\sale\addr
	 * @param prop
	 * @return String
	 * @throws Exception
	 */
	protected Element getChild(String prop) throws Exception {
		Element element = root;
		if (prop == null) return element;
		
		String[] nodes = prop.split("/");
		for (int i=0; i<nodes.length; i++) {
			element = element.getChild(nodes[i]);
		}
		
		return element;
	}
	
	/**
	 * get property
	 * for example:subsys\sale\addr
	 * @param prop
	 * @return String
	 * @throws Exception
	 */
	public String getProperty(String prop) throws Exception {
		Element element = root;
		
		String[] nodes = prop.split("/");
		for (int i=0; i<nodes.length; i++) {
			if (i == nodes.length - 1) break;
			element = element.getChild(nodes[i]);
		}
		
		return element.getAttributeValue(nodes[nodes.length - 1]);
	}
	
	/**
	 * get properties
	 * for example:subsys\*\addr or database\eparchy
	 * @param prop
	 * @return IData
	 * @throws Exception
	 */
	public IData getProperties(String prop) throws Exception {
		IData properties = new DataMap();
		
		if (prop.indexOf("*") == -1) {
			Element element = getChild(prop);
			List attrs = element.getAttributes();
			for (int i=0; i<attrs.size(); i++) {
				Attribute attr = (Attribute) attrs.get(i);
				properties.put(attr.getName(), attr.getValue());
			}
		} else {
			Element element = root;
			String[] nodes = prop.split("/");
			for (int i=0; i<nodes.length; i++) {
				if ("*".equals(nodes[i])) {
					List children = element.getChildren();
					for (int j=0; j<children.size(); j++) {
						Element child = (Element) children.get(j);
						String childName = child.getName();
						String childValue = child.getAttributeValue(nodes[i + 1]);
						properties.put(childName, childValue);
					}
					break;
				} else {
					element = element.getChild(nodes[i]);
				}
			}
		}
		
		return properties;
	}
	
	/**
	 * get dataset
	 * @return IDataset
	 * @throws Exception
	 */
	public IDataset getDataset() throws Exception {
		return getDataset(null, 0, XML_FORMAT_CONTENT);
	}
	
	/**
	 * get dataset
	 * @param prop
	 * @return IDataset
	 * @throws Exception
	 */
	public IDataset getDataset(String prop) throws Exception {
		return getDataset(prop, 0, XML_FORMAT_CONTENT);
	}
	
	/**
	 * get dataset
	 * @param prop
	 * @param format
	 * @return IDataset
	 * @throws Exception
	 */
	public IDataset getDataset(String prop, String format) throws Exception {
		return getDataset(prop, 0, format);
	}
	
	/**
	 * get dataset
	 * @param prop
	 * @param index
	 * @param format
	 * @return IDataset
	 * @throws Exception
	 */
	public IDataset getDataset(String prop, int index, String format) throws Exception {
		IDataset dataset = new DatasetList();
		
		Element element = getChild(prop);
		List rows = element.getChildren();
		
		for (int i=0; i<index; i++) {
			rows = ((Element) rows.get(i)).getChildren();
		}
		
		for (int i=0; i<rows.size(); i++) {
			IData data = new DataMap();
			
			Element row = (Element) rows.get(i);
			
			if (XML_FORMAT_CONTENT.equals(format)) {
				List cells = row.getChildren();
				for (int j=0; j<cells.size(); j++) {
					Element cell = (Element) cells.get(j);
					data.put(cell.getName(), cell.getValue());
				}
			}
			
			if (XML_FORMAT_ATTRIBUTE.equals(format)) {
				List cells = row.getAttributes();
				for (int j=0; j<cells.size(); j++) {
					Attribute cell = (Attribute) cells.get(j);
					data.put(cell.getName(), cell.getValue());
				}
			}
			
			dataset.add(data);
		}
		
		return dataset;
	}
	
}