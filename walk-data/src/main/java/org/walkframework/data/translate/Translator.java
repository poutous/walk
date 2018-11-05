package org.walkframework.data.translate;




/**
 * 翻译器接口
 * 
 * @author shf675
 *
 */
public interface Translator {
	
	/**
	 * 翻译器方法1
	 * 
	 * @param <T>
	 * @param sourceObject
	 * @return
	 */
	<T> T translate(Object sourceObject);
	
	/**
	 * 翻译器方法2
	 * 
	 * @param <T>
	 * @param sourceObject
	 * @param translatedField
	 * @return
	 */
	<T> T translate(Object sourceObject, String translatedField);
	
}
