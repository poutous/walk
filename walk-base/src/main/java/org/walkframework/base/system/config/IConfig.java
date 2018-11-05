package org.walkframework.base.system.config;

import org.walkframework.data.util.IData;

public interface IConfig {

	/**
	 * get property
	 * @param prop
	 * @return String
	 * @throws Exception
	 */
	public String getProperty(String prop) throws Exception;

	/**
	 * get properties
	 * @param prop
	 * @return IData
	 * @throws Exception
	 */
	public IData getProperties(String prop) throws Exception;

	
}
