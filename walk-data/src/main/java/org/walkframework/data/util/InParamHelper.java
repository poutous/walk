package org.walkframework.data.util;

import java.util.List;


/**
 * InParam帮助类
 * 
 * @author shf675
 *
 */
public abstract class InParamHelper {
	
	@SuppressWarnings("unchecked")
	public static void putFileList(InParam inparam, String fileName, List list) {
		inparam.putFileList(fileName, list);
	}
}
