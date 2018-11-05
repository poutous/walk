package org.walkframework.data.util;

import java.util.List;
import java.util.Map;

/**
 * 请求参数，map结构
 * 解决器：org.walkframework.base.base.system.bind.resolver.InParamMethodArgumentResolver
 *
 * @param <K>
 * @param <V>
 */
public class InParam<K, V> extends DataMap<K, V> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	private Map<String, List> fileList = new DataMap<String, List>();

	@SuppressWarnings("unchecked")
	void putFileList(String fileName, List list) {
		this.fileList.put(fileName, list);
	}

	/**
	 * 获取导入excel文件List
	 * 
	 * @param <E>
	 * @param fileName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> List<E> getFileList(String fileName) {
		return fileList.get(fileName);
	}
}
