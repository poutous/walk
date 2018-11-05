/**
 * 
 */
package org.walkframework.base.system.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>单例需要继承的抽象类</p>
 * <p>默认调用无参构造方法，特殊需求可重写<code>IMPLEMENT_METHOD</code>成员标识的无参静态方法</p>
 * 
 * @author mengqk
 * @version 0.1, 2014-9-17
 */
public abstract class SingletonFactory {

    /** 抽象锁 */
    private static final Object LOCK = new Object();

    /** 实现方法的名称 */
    protected static final String IMPLEMENT_METHOD = "getInstance";

    /** 实例缓存 */
    private static Map<String, Object> instances = new HashMap<String, Object>();

    /**
     * <p>生成实例</p>
     * 
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> cls) {
        if (!instances.containsKey(cls.getName())) {
            synchronized (LOCK) {
                if (!instances.containsKey(cls.getName())) {
                    T instance = genInstance(cls);
                    instances.put(cls.getName(), instance);
                }
            }
        }
        return (T) instances.get(cls.getName());
    }

    /**
     * <p>函数说明</p>
     */
    @SuppressWarnings("unchecked")
    private static <T> T genInstance(Class<T> cls) {
        T instance = null;
        Method method;
		try {
			method = cls.getDeclaredMethod(IMPLEMENT_METHOD);
			instance = (T) method.invoke(cls);
		}catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
			try {
				instance = (T) cls.newInstance();
			} catch (InstantiationException e1) {
			} catch (IllegalAccessException e1) {
			}
		}
        return instance;
    }
}
