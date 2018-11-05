package org.walkframework.base.tools.utils;

import com.rits.cloning.Cloner;

/**
 * @ClassName: CloneUtil
 * @Description: 依赖uk.com.robust-it cloningjar包，深拷贝帮助类
 */
public abstract class CloneUtil {
	private static final Cloner cloner = new Cloner();
	
    /**
     * @Title: deepClone
     * @Description: 深拷贝方法，深拷贝一个对象，并返回拷贝的新对象
     * @param sourceObject 源对象
     * @return 深拷贝出来的新对象
     */
    public static <T> T deepClone(T sourceObject) {
        return cloner.deepClone(sourceObject);
    }
    
    /**
     * @Title: fastClone
     * @Description: 快速拷贝对象
     * @param sourceObject 源对象
     * @return 深拷贝出来的新对象
     */
    @SuppressWarnings("unchecked")
	public static <T> T fastClone(T sourceObject) {
    	T dest = (T) cloner.fastCloneOrNewInstance(sourceObject.getClass());
    	cloner.copyPropertiesOfInheritedClass(sourceObject, dest);
        return dest;
    }
    
    /**
     * 将src对象内的属性copy到dest对象中
     * @param <T>
     * @param <E>
     * @param src
     * @param dest
     */
    public static <T, E extends T> void copyPropertiesOfInheritedClass(T src, E dest){
    	cloner.copyPropertiesOfInheritedClass(src, dest);
    }
    
}
