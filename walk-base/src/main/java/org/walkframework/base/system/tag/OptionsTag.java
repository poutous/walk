package org.walkframework.base.system.tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.utils.ParamTranslateUtil;


/**
 * 标签库 options
 * select下拉框的option
 */
public class OptionsTag extends BaseTag {

    /** 要调用的方法名 */
    private String serviceMethod;

    /** 是否需要延迟加载 */
    private String lazyInit;

    /** 加载STATIC表的typeId参数 */
    private String typeId;

    /** options默认选中的value值 */
    private String defaultValue;

    /** 给定List生成options */
    private List<?> list;

    /** 查询出列表中对象的属性名，对应option.value */
    private String valueItem;

    /** 查询出列表中对象的属性名，对应option.text */
    private String textItem;

    /** 是否显示“全部”下拉选项 */
    private String isfull;
    
    /** 全部选项显示文本 */
    private String fullText;
    
	public void doTag() throws IOException {
        super.doTag();
        options();
    }

    /**
     * 取select下拉框的option
     */
    public void options() throws IOException {
        JspWriter out = super.getJspContext().getOut();
        out.print(generateOptions());
    }

    /**
     * <p>根据参数生成options</p>
     * 
     * @return options字符串
     */
    public String generateOptions() {
    	StringBuilder options = new StringBuilder();
        if (isfull == null || Boolean.parseBoolean(isfull)) {// 默认显示-----请选择-----
        	if(fullText != null){
        		options.append("<option value=\"\">" + fullText + "</option>");
        	} else {
        		options.append("<option value=\"\">全部</option>");
        	}
        }
        // 页面初始化时，非ajax请求不生成options
        if (!("true".equals(lazyInit) && !common.isAjaxRequest(common.getContextRequest()))) {

            // 1、 直接给定数据列表，构造options
            if (list != null && valueItem != null && textItem != null) {
                options.append(optionsList(list, valueItem, textItem, defaultValue));
            }
            // 2、 指定方法，获取数据，构造options
            else if (serviceMethod != null && valueItem != null && textItem != null) {
                options.append(optionsMethod(serviceMethod, valueItem, textItem, defaultValue));
            }
            // 3、 使用typeId，获取TD_S_PARAM静态参数表数据，构造options
            else if (typeId != null) {
                options.append(optionsStatic(typeId, defaultValue));
            }
        }

        return options.toString();
    }

    /**
     * <p>给定数据列表，构造options</p>
     * 
     * @param list
     * @param valueItem
     * @param textItem
     * @param defaultValue
     * @return
     */
    @SuppressWarnings({"unchecked" })
    public String optionsList(List list, String valueItem, String textItem, String defaultValue) {
    	StringBuilder str = new StringBuilder();
        if (list == null || list.isEmpty()) {
            return str.toString();
        }
        for (int i = 0; i < list.size(); i++) {
        	Object opValue = null;
        	Object opText = null;
        	Object obj = list.get(i);
            if (obj instanceof Map) {
                Map map = (Map) obj;
                opValue = map.get(valueItem);
                opText =  map.get(textItem);
            } else {
                try {
                    opValue = common.getValueByFieldName(obj, valueItem);
                    opText = common.getValueByFieldName(obj, textItem);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    opValue = null;
                    opText = null;
                }
            }
            String value = stringValue(opValue);
            String text = stringValue(opText);
            str.append("<option value=\"").append(value).append('\"')
                    .append(getIsSelected(defaultValue, value)).append('>')
                    .append(text).append("</option>");
        }

        return str.toString();
    }

    /**
     * <p>指定方法，获取数据，构造options</p>
     * 
     * @param serviceMethod
     * @param valueItem
     * @param textItem
     * @param defaultValue
     * @return
     */
    @SuppressWarnings({"unchecked" })
    public String optionsMethod(String serviceMethod, String valueItem, String textItem, String defaultValue) {
        String serviceName1 = serviceMethod.substring(0, serviceMethod.indexOf("("));
        String serviceName = serviceName1.substring(0, serviceName1.lastIndexOf("."));
        String methodName = serviceName1.substring(serviceName1.lastIndexOf(".") + 1, serviceName1.length());
        String paramsStr = serviceMethod.substring(serviceMethod.indexOf("(") + 1, serviceMethod.indexOf(")"));
        Object[] params = null;
        Class[] types = null;
        if (!"".equals(paramsStr)) {
            String[] paramsArr = paramsStr.split(",");
            params = new Object[paramsArr.length];
            types = new Class[paramsArr.length];
            for (int i = 0; i < paramsArr.length; i++) {
                params[i] = paramsArr[i].trim();
                types[i] = String.class;
            }
        }
        Object service = SpringContextHolder.getBean(serviceName);
        List list = null;
        try {
            list = (List) common.callBean(service, methodName, params, types);
        } catch (Exception e) {
            // log.error(e.getMessage(), e);
        }
        return optionsList(list, valueItem, textItem, defaultValue);
    }

    /**
     * <p>使用typeId，获取TD_S_PARAM静态参数表数据，构造options</p>
     * 
     * @param typeId
     * @param value
     * @return
     */
    public String optionsStatic(String typeId, String defaultValue) {
        return optionsList(ParamTranslateUtil.staticlist(typeId), "dataId", "dataName", defaultValue);
    }

    private static String getIsSelected(String value, String selectValue) {
        String isSelected = "";
        if (value != null) {
        	String[] arr = value.split(",");
        	if(Arrays.asList(arr).contains(selectValue)) {
        		isSelected = " selected=\"selected\"";
        		
        	}
        }
        return isSelected;
    }
    
    private static String stringValue(Object value) {
        return null == value ? "" : value.toString();
    }

    @SuppressWarnings("unchecked")
	public List getList() {
        return list;
    }

    @SuppressWarnings("unchecked")
	public void setList(List list) {
        this.list = list;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getIsfull() {
        return isfull;
    }

    public void setIsfull(String isfull) {
        this.isfull = isfull;
    }

    public String getServiceMethod() {
        return serviceMethod;
    }

    public void setServiceMethod(String serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    public String getLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(String lazyInit) {
        this.lazyInit = lazyInit;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValueItem() {
        return valueItem;
    }

    public void setValueItem(String valueItem) {
        this.valueItem = valueItem;
    }

    public String getTextItem() {
        return textItem;
    }

    public void setTextItem(String textItem) {
        this.textItem = textItem;
    }
    
    public String getFullText() {
		return fullText;
	}

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

}
