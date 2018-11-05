package org.walkframework.base.system.tag;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.tools.spring.SpringContextHolder;


/**
 * <p>设置对象到页面上下文，方便获取常量及调用方法</p>
 * 
 * @author mengqk
 * @version 0.1, 2014-9-15
 */
public class SetTag extends BaseTag {
	
	private static final String VALUE_PREFIX = "@"; 

    /** 指定类名或实例或spring定义的service名称
     * 如果是类名或实例或spring定义的service名称必须以@打头，普通值不用
     */
    protected Object value;

    /** 生成实例的别名，用于方法调用等，如只使用成员变量或常量的获取，则此属性可省略 */
    protected String var;

    /** 成员变量和常量的集合，包含对象至顶层类的所有成员 */
    protected String fields;

    /** 是否为不可实例化的类 */
    private boolean isAbstractOrInterface;

    /** 是否由spring进行初始化 */
    private boolean isInstanceBySpring;

    /** 类信息 */
    private Class<?> cls;
    
    /**
     * 执行tag内容，设置相关信息到pageContext.
     */
    public void doTag() {
        boolean isString = this.value instanceof String;
        if (this.value == null) {
            common.error("value can not be empty!");
        }
        if (StringUtils.isBlank(this.var) && StringUtils.isBlank(this.fields)) {
            common.error("method and constant can not both be blank.");
        }
        Object result = this.value;
        if(isString && this.value.toString().startsWith(VALUE_PREFIX)){
        	this.value = this.value.toString().substring(1, this.value.toString().length());
        	result = getInstance();
        }

        if (result == null && !this.isAbstractOrInterface) {
            //common.error("Get instance failed! class name: " + this.value);
            log.warn("Get instance failed! class name: " + this.value);
        }

        if (!StringUtils.isBlank(this.var)) {
            // 默认为pageContext.
            getJspContext().setAttribute(this.var, result);
        }
        if (!StringUtils.isBlank(this.fields)) {
            Map<String, Object> fieldValueMap = new HashMap<String, Object>();
            setFieldValueMap(result, getClass(result), fieldValueMap);
            getJspContext().setAttribute(this.fields, fieldValueMap);
        }
    }

    /**
     * <p>获取value对应的类，由spring代理时，父类才是真正的目标类</p>
     * 
     * @param result 根据value获得的实例化对象
     * @return
     */
    private Class<?> getClass(Object result) {
        Class<?> retCls = null == result ? this.cls : result.getClass();
        if (null == retCls) {
            common.error("Class not found!");
        }

        return this.isInstanceBySpring ? retCls.getSuperclass() : retCls;
    }

    /**
     * <p>强制设置属性值到MAP，方便在EL中获取</p>
     * 
     * @param value 要取值的对象
     * @param cls 对象class
     * @param fieldValueMap 设置进入的MAP
     */
    private void setFieldValueMap(Object value, Class<?> cls, Map<String, Object> fieldValueMap) {
        Field[] fields = cls.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (this.isAbstractOrInterface && !(Modifier.isStatic(field.getModifiers()))) {
                    log.info("I won't load non static fields like {}!", field.getName());
                    continue;
                }
                field.setAccessible(true);
                fieldValueMap.put(field.getName(), field.get(value));

            }
        } catch (Exception e) {
            log.error("Error get field value of object {} for class {}.", value, cls);
            common.error("Error get field value of object.", e);
        }
        log.info("for class {}, result map is: {}.", cls, fieldValueMap);
        Class<?> superClass = cls.getSuperclass();
        if (null != superClass && !superClass.equals(Object.class)) {
            setFieldValueMap(value, superClass, fieldValueMap);
        }
    }

    /**
     * <p>根据给定value值实例化对象</p>
     * 
     * @return 实例化后的对象
     */
    public Object getInstance() {
        Object result = null;
        String classOrService = (String) this.value;
        this.isAbstractOrInterface = false; // 是否可以实例化
        this.isInstanceBySpring = false;
        try {
            this.cls = Class.forName(classOrService);
            this.isAbstractOrInterface = Modifier.isAbstract(this.cls.getModifiers())
                    || Modifier.isInterface(this.cls.getModifiers());
        } catch (ClassNotFoundException e) {
            log.info("Class not found! class name: {}, try spring service.", classOrService);
        }
        // 1、 尝试从spring上下文获取实例
        try {
            if (null == this.cls) {
                result = SpringContextHolder.getBean(classOrService);
            } else if (!this.isAbstractOrInterface) {
                result = SpringContextHolder.getBean(this.cls);
            }
            this.isInstanceBySpring = (null != result); // 此处result获取成功则认定由spring实例化对象
        } catch (Exception e) {
            log.info("Can not load instance from spring context by {}.", classOrService);
        }
        if (null == result && null != this.cls) {
            try {
                if (!this.isAbstractOrInterface) {
                    // 2、 spring上下文获取不到则尝试直接使用无参构造方法获取
                    result = SingletonFactory.getInstance(this.cls); //更新为统一由SingletonFactory管理
                } else {
                    // 3、 接口或抽象类不实例化
                    result = this.cls.cast(null);
                }
            } catch (Exception e1) {
                common.error("New instance of class failed!", e1);
            }
        }

        return result;
    }

    /**
     * <p>获取value</p>
     * 
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * <p>设置value为传入的value</p>
     * 
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * <p>获取var</p>
     * 
     * @return the var
     */
    public String getVar() {
        return var;
    }

    /**
     * <p>设置var为传入的var</p>
     * 
     * @param var the var to set
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * <p>获取fields</p>
     * 
     * @return the fields
     */
    public String getFields() {
        return fields;
    }

    /**
     * <p>设置fields为传入的fields</p>
     * 
     * @param fields the fields to set
     */
    public void setFields(String fields) {
        this.fields = fields;
    }

}
