package org.walkframework.base.tools.utils;

import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;

import com.alibaba.fastjson.JSONObject;

public class ValidateUtil {
	protected final static Logger log = LoggerFactory.getLogger(ValidateUtil.class);
	protected final static Common common = SingletonFactory.getInstance(Common.class);
	
	public final static String CELL_TYPE_STRING = "1";
	public final static String CELL_TYPE_NUMERIC = "2";
	public final static String CELL_TYPE_DATETIME = "3";
	public final static String VALIDATE_XML_CACHE = "VALIDATE_XML_CACHE";
	public final static String HANDLER_METHOD = "_HANDLER_METHOD";
	
	/**
	 * check length
	 * @param value
	 * @param length
	 * @param desc
	 * @return String
	 */
	public static String checkLength(String value, int length, String desc) {
		if (!"".equals(value) && getLength(value) != length) {
			return desc + "长度必须为" + length + "；";
		}
		return "";
	}
	
	/**
	 * check min length
	 * @param value
	 * @param length
	 * @param desc
	 * @return String
	 */
	public static String checkMinLength(String value, int length, String desc) {
		if (!"".equals(value) && getLength(value) < length) {
			return desc + "最小长度不能低于" + length + "；";
		}
		return "";
	}
	
	/**
	 * check max length
	 * @param value
	 * @param length
	 * @param desc
	 * @return String
	 */
	public static String checkMaxLength(String value, int length, String desc) {
		if (!"".equals(value) && getLength(value) > length) {
			return desc + "最大长度不能超过" + length + "；";
		}
		return "";
	}
	
	/**
	 * check text
	 * @param value
	 * @param desc
	 * @return String
	 */
	public static String checkText(String value, String desc) {
		if ("".equals(value)) {
			return desc + "不能为空；";
		}
		return "";
	}
	
	/**
	 * check numeric
	 * @param value
	 * @param format
	 * @param desc
	 * @return String
	 */
	public static String checkNumeric(String value, String format, String desc) {
		String expression = "[+-]?\\d+";
		String checkdesc = "必须为整数";
		if (format != null && format.indexOf(".") != -1) {
			expression = "[+-]?\\d+(\\.\\d{1," + (format.length() - format.indexOf(".") - 1) + "})?";
			checkdesc = "必须为数字格式(" + format + ")";
		}
		if (!"".equals(value) && !common.isMatches(value, expression)) {
			return desc + checkdesc + "；";
		}
		return "";
	}
	
	/**
	 * check date
	 * @param value
	 * @param format
	 * @param desc
	 * @return String
	 */
	public static String checkDate(String value, String format, String desc) {
		String expression = getDateExpression(format);
		
		if (!"".equals(value) && !common.isMatches(value, expression)) {
			return desc + "必须为时间格式(" + format + ");";
		}
		
		return "";
	}
	
	/**
	 * 根据日期格式获取正则表达式
	 * @param format
	 * @return
	 */
	public static String getDateExpression(String format) {
		String expression = "(\\d{4})(-|\\/)(\\d{2})\\2(\\d{2})";
		
		if ("yyyy-MM-dd".equals(format)) {
			expression = "(\\d{4})(-|\\/)(\\d{2})\\2(\\d{2})";
		} else if ("yyyy-MM-dd HH:mm".equals(format)) {
			expression = "(\\d{4})(-|\\/)(\\d{2})\\2(\\d{2}) (\\d{2}):(\\d{2})";
		} else if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
			expression = "(\\d{4})(-|\\/)(\\d{2})\\2(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})";
		} else if ("HH:mm:ss".equals(format)) {
			expression = "(\\d{2})(:)?(\\d{2})\\2(\\d{2})";
		} else if ("yyyy".equals(format)) {
			expression = "(\\d{4})";
		} else if ("yyyy-MM".equals(format)) {
			expression = "(\\d{4})(-|\\/)(\\d{2})";
		} else if ("HH".equals(format)) {
			expression = "(\\d{2})";
		} else if ("HH:mm".equals(format)) {
			expression = "(\\d{2})(:)?(\\d{2})";
		} else if ("yyyy-MM-dd HH".equals(format)) {
			expression = "(\\d{4})(-|\\/)(\\d{2})\\2(\\d{2}) (\\d{2})";
		}
		
		return expression;
	}
/*	public static String getDateExpression(String format) {
		String expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2})";
		
		if ("yyyy-MM-dd".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2})";
		} else if ("yyyy-MM-dd HH:mm".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2}):(\\d{1,2})";
		} else if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2}):(\\d{1,2}):(\\d{1,2})";
		} else if ("HH:mm:ss".equals(format)) {
			expression = "(\\d{1,2})(:)?(\\d{1,2})\\2(\\d{1,2})";
		} else if ("yyyy".equals(format)) {
			expression = "(\\d{1,4})";
		} else if ("yyyy-MM".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})";
		} else if ("HH".equals(format)) {
			expression = "(\\d{1,2})";
		} else if ("HH:mm".equals(format)) {
			expression = "(\\d{1,2})(:)?(\\d{1,2})";
		} else if ("yyyy-MM-dd HH".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2})";
		}
		
		return expression;
	}
*/	
	/**
	 * check data source
	 * @param value
	 * @param datasrc
	 * @param desc
	 * @return String
	 */
	public static String checkDataSource(String value, String datasrc, String desc){
		String translateValue = ParamTranslateUtil.getTranslateValue(value, datasrc);
		
		if (translateValue == null) {
			return desc + "(" + value + ")无法匹配;";
		}
		
		return "";
	}
	
	/**
	 * verify cell 1、改造Validate.verifyCell(导入校验，nullable="yes"并且设置了datasrc属性，实际value如果为空则不进行datasrc的校验)
	 * 			   2、加入正则表达式验证
	 * @param bd
	 * @param cell
	 * @param value
	 * @return String
	 */
	public static String verifyCell(Element cell, String value){
		if(value == null) value = "";
		StringBuilder error = new StringBuilder();

		String type = cell.getAttributeValue("type");
		String desc = cell.getAttributeValue("desc");
		String nullable = cell.getAttributeValue("nullable");
		String equsize = cell.getAttributeValue("equsize");
		String minsize = cell.getAttributeValue("minsize");
		String maxsize = cell.getAttributeValue("maxsize");
		String minvalue = cell.getAttributeValue("minvalue");
		String maxvalue = cell.getAttributeValue("maxvalue");
		String format = cell.getAttributeValue("format");
		String datasrc = cell.getAttributeValue("datasrc");
		String regex = cell.getAttributeValue("regex");
		String regexDesc = cell.getAttributeValue("regexdesc");
		if (nullable != null && "no".equals(nullable)) {
			error.append(ValidateUtil.checkText(value, desc));
		}
		if (equsize != null) {
			error.append(ValidateUtil.checkLength(value, Integer.parseInt(equsize), desc));
		}
		if (minsize != null) {
			error.append(ValidateUtil.checkMinLength(value, Integer.parseInt(minsize), desc));
		}
		if (maxsize != null) {
			error.append(ValidateUtil.checkMaxLength(value, Integer.parseInt(maxsize), desc));
		}
		if (minvalue != null) {
			error.append(checkMinValue(value, minvalue, desc));
		}
		if (maxvalue != null) {
			error.append(checkMaxValue(value, maxvalue, desc));
		}
		if (ValidateUtil.CELL_TYPE_DATETIME.equals(type)) {
			if (format != null) {
				error.append(ValidateUtil.checkDate(value, format, desc));
			}
		}
		if (ValidateUtil.CELL_TYPE_NUMERIC.equals(type)) {
			error.append(ValidateUtil.checkNumeric(value, format, desc));
		}
		if (datasrc != null) {
			error.append(checkDataSource(value, datasrc, desc, nullable));
		}
		if (regex != null) {
			error.append(checkRegex(regex, value, desc, regexDesc));
		}
		return error.toString();
	}
	
	/**
	 * 检查值是否符合正则表达式
	 * 
	 * @param regex
	 * @param value
	 * @param desc
	 * @param regexDesc
	 * @return
	 */
	public static String checkRegex(String regex, String value, String desc, String regexDesc) {
		if (!"".equals(regex) && !"".equals(value) && !common.isMatches(value, regex)) {
			return desc + "格式错误"+(regexDesc==null||"".equals(regexDesc)?"":",正确格式:"+regexDesc)+"；";
		}
		return "";
	}
	
	/**
	 * 检查最小值
	 * 
	 * @param value
	 * @param length
	 * @param desc
	 * @return
	 */
	public static String checkMinValue(String value, String minvalue, String desc) {
		if (!"".equals(value) && Double.parseDouble(value) < Double.parseDouble(minvalue)) {
			return desc + "不能小于" + minvalue + "；";
		}
		return "";
	}
	
	/**
	 * 检查最大值
	 * 
	 * @param value
	 * @param length
	 * @param desc
	 * @return
	 */
	public static String checkMaxValue(String value, String maxvalue, String desc) {
		if (!"".equals(value) && Double.parseDouble(value) > Double.parseDouble(maxvalue)) {
			return desc + "不能大于" + maxvalue + "；";
		}
		return "";
	}

	/**
	 * check data source 改造Validate.checkDataSource(导入校验，nullable="yes"并且设置了datasrc属性，实际value如果为空则不进行datasrc的校验)
	 * @param value
	 * @param datasrc
	 * @param desc
	 * @return String
	 */
	public static String checkDataSource(String value, String datasrc, String desc, String nullable){
		if ((nullable == null || "yes".equals(nullable)) && (value == null || "".equals(value))) {
			//(导入校验，nullable="yes"并且设置了datasrc属性，实际value如果为空则不进行datasrc的校验)
		} else {
			String translateValue = ParamTranslateUtil.getTranslateValue(value, datasrc);
			if (translateValue == null) {
				return desc + "(" + value + ")无法匹配;";
			}
		}
		return "";
	}
	
	/**
	 * 验证表单
	 * @param xml
	 * @param validObj
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String validateForm(String xml, Object validObj) {
		StringBuilder errors = new StringBuilder();
		try {
			if(validObj == null){
				return null;
			}
			Element validateConfig = getValidateConfig(xml);
			List<?> elements = validateConfig.getChildren();
			for (int i = 0; i < elements.size(); i++) {
				Element element = ((Element) elements.get(i));
				String elementName = element.getAttributeValue("name");
				Object value = validObj instanceof Map ? ((Map)validObj).get(elementName):common.getValueByFieldName(validObj, elementName);
				String elementValue = value == null ? "" : value.toString();
				String verifyResult = ValidateUtil.verifyCell(element, elementValue);
				if(!StringUtils.isEmpty(verifyResult)){
					errors.append(verifyResult);
				}
			}
			//自定义方法校验
			String method = validateConfig.getAttributeValue("method");
			if(method != null){
				errors.append(checkMethod(method, validObj));
			}
			if(!StringUtils.isEmpty(errors.toString())){
				errors.insert(0, "校验失败：");
			}
		} catch (Exception e) {
			common.error(e);
		}
		return errors.toString();
	}
	
	/**
	 * 加载校验xml文件
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static Element getValidateConfig(String xml) throws Exception {
		xml = SpringPropertyHolder.getContextProperty("validate.validatesDirectory","validates/") + xml;
		
		String productMode = SpringPropertyHolder.getContextProperty("productMode");
		ICache cache = ((ICacheManager)SpringContextHolder.getBean("springCacheManager")).getICache("VALIDATE_XML_CACHE");
		if("true".equals(productMode) && cache != null){
			return cache.getValue(xml);
		}
		Document document = new SAXBuilder().build(common.getClassResource(xml).toString());
		Element element = document.getRootElement();
		if ("true".equals(productMode) && cache != null) {
			cache.put(xml, element);
		}
		return element;
	}
	
	/**
	 * 自定义方法校验
	 * @param value
	 * @param desc
	 * @param validObj
	 * @return
	 */
	public static String checkMethod(String method, Object validObj) {
		String serviceName = method.substring(0, method.lastIndexOf("."));
		String methodName = method.substring(method.lastIndexOf(".") + 1, method.length());
		Object service = SpringContextHolder.getBean(serviceName);
		String error = "";
		try {
			Class<?> type = validObj instanceof Map ? Map.class : validObj.getClass();
			Object result = common.callBean(service, methodName, new Object[] { validObj }, new Class[] { type });
			error = result == null ? "" : result.toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return error;
	}
	
	/**
	 * 将验证xml转换成json，供客户端校验
	 * 
	 * @param xml
	 * @return
	 */
	public static String validXml(String xml) {
		JSONObject validate = new JSONObject();
		JSONObject rules = new JSONObject();
		JSONObject messages = new JSONObject();
		try {
			List<?> elements = ValidateUtil.getValidateConfig(xml).getChildren();
			for (int i = 0; i < elements.size(); i++) {
				JSONObject rule = new JSONObject();
				JSONObject message = new JSONObject();
				Element element = ((Element) elements.get(i));
				String name = element.getAttributeValue("name");
				String type = element.getAttributeValue("type");
				String desc = element.getAttributeValue("desc");
				String nullable = element.getAttributeValue("nullable");
				String equsize = element.getAttributeValue("equsize");
				String minsize = element.getAttributeValue("minsize");
				String maxsize = element.getAttributeValue("maxsize");
				String minvalue = element.getAttributeValue("minvalue");
				String maxvalue = element.getAttributeValue("maxvalue");
				String format = element.getAttributeValue("format");
				String regex = element.getAttributeValue("regex");
				String regexDesc = element.getAttributeValue("regexdesc");
				if (nullable != null && "no".equals(nullable)) {
					rule.put("required", true);
					message.put("required", desc + "不能为空");
				}
				if (equsize != null) {
					int eqsize = Integer.parseInt(equsize);
					rule.put("rangelength", new Integer[]{eqsize, eqsize});
					message.put("rangelength", desc + "长度必须为" + equsize);
				}
				if (minsize != null) {
					rule.put("minlength", minsize);
					message.put("minlength", desc + "最小长度不能低于" + minsize);
				}
				if (maxsize != null) {
					rule.put("maxlength", maxsize);
					message.put("maxlength", desc + "最大长度不能超过" + maxsize);
				}
				if (minvalue != null) {
					rule.put("min", minvalue);
					message.put("min", desc + "不能小于" + minvalue);
				}
				if (maxvalue != null) {
					rule.put("max", maxvalue);
					message.put("max", desc + "不能大于" + maxvalue);
				}
				if (ValidateUtil.CELL_TYPE_DATETIME.equals(type)) {
					if (format != null) {
						rule.put("regex", convert2jsRegex(ValidateUtil.getDateExpression(format)));
						message.put("regex", desc + "必须为时间格式(" + format + ")");
					} else {
						rule.put("dateISO", true);
						message.put("dateISO", "必须输入正确格式的日期(ISO)");
					}
				}
				if (ValidateUtil.CELL_TYPE_NUMERIC.equals(type)) {
					if (format == null) {
						rule.put("digits", true);
						message.put("digits", desc + "必须为整数");
					} else {
						String expression = "[+-]?\\d+(\\.\\d{1," + (format.length() - format.indexOf(".") - 1) + "})?";
						rule.put("regex", convert2jsRegex(expression));
						message.put("regex", desc + "必须为数字格式(" + format + ")");
					}
				}
				if (regex != null) {
					rule.put("regex", convert2jsRegex(regex));
					message.put("regex", desc + "格式错误" + (regexDesc == null || "".equals(regexDesc) ? "" : "，正确格式:" + regexDesc));
				}
				rules.put(name, rule);
				messages.put(name, message);
			}
			validate.put("rules", rules);
			validate.put("messages", messages);
			//System.out.println("validate:" + validate.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return validate.toString();
	}
	
	
	/**
	 * java正则转换为js正则
	 * 
	 * @param regex
	 * @return
	 */
	public static String convert2jsRegex(String regex){
		String prefix = regex.startsWith("^") ? "" : "^";
		String suffix = regex.endsWith("$") ? "" : "$";
		return prefix + regex + suffix;
	}
	
	/**
     * get length
     * @param value
     * @return int
     */
    public static int getLength(String value) {
		int length = 0;
		
		char[] chars = value.toCharArray();
    	for (int i=0; i<chars.length; i++) {
			if (((int) chars[i]) > 0x80) {
				length += 2;
			} else {
				length += 1;
			}
		}
		
		return length;
    }
}