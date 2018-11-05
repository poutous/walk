package org.walkframework.base.system.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.walkframework.base.system.config.IConfig;
import org.walkframework.base.system.config.TEXTConfig;
import org.walkframework.base.system.config.XMLConfig;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;
import org.walkframework.data.util.InParam;

import com.alibaba.fastjson.JSONArray;

/**
 * 通用类 通过调用SingletonFactory.getInstance(Common.class)获取实例
 * 
 */
public class Common {

	protected static final Logger log = LoggerFactory.getLogger(Common.class);

	private static Common common;

	/**
	 * get instance
	 * 
	 * @return Common
	 */
	public static Common getInstance() {
		if (common == null) {
			common = new Common();
		}
		return common;
	}

	/**
	 * get timestamp format
	 * 
	 * @param value
	 * @return String
	 */
	public String getTimestampFormat(String value) {
		switch (value.length()) {
		case 4:
			return "yyyy";
		case 6:
			return "yyyyMM";
		case 7:
			return "yyyy-MM";
		case 8:
			return "yyyyMMdd";
		case 10:
			return "yyyy-MM-dd";
		case 13:
			return "yyyy-MM-dd HH";
		case 16:
			return "yyyy-MM-dd HH:mm";
		case 19:
			return "yyyy-MM-dd HH:mm:ss";
		case 21:
			return "yyyy-MM-dd HH:mm:ss.S";
		}
		return null;
	}

	/**
	 * get match str
	 * 
	 * @param str
	 * @param regex
	 * @return String
	 */
	@SuppressWarnings("rawtypes")
	public String getMatchStr(String str, String regex) {
		List result = getMatchArray(str, regex);
		return result.size() == 0 ? null : (String) result.get(0);
	}

	/**
	 * get match array
	 * 
	 * @param str
	 * @param regex
	 * @return List
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" })
	public List getMatchArray(String str, String regex) {
		List result = new ArrayList();

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			result.add(matcher.group());
		}

		return result;
	}

	/**
	 * is matches
	 * 
	 * @param str
	 * @param regex
	 * @return boolean
	 */
	public boolean isMatches(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	/**
	 * trim prefix
	 * 
	 * @param str
	 * @param suffix
	 * @return String
	 */
	public String trimPrefix(String str, String prefix) {
		return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
	}

	/**
	 * trim suffix
	 * 
	 * @param str
	 * @param suffix
	 * @return String
	 */
	public String trimSuffix(String str, String suffix) {
		return str.endsWith(suffix) ? str.substring(0, str.length() - 1) : str;
	}

	/**
	 * get str by array
	 * 
	 * @param array
	 * @return String
	 */
	public String getStrByArray(String[] array) {
		return getStrByArray(array, ",");
	}

	/**
	 * get str by array
	 * 
	 * @param array
	 * @param split
	 * @return String
	 */
	public String getStrByArray(String[] array, String split) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			str.append(array[i] + (i != array.length - 1 ? split : ""));
		}
		return str.toString();
	}

	/**
	 * encode charset
	 * 
	 * @param charSet
	 * @return String
	 * @throws Exception
	 */
	public String encodeCharset(String charSet) {
		String set = null;
		try {
			set = new String(charSet.getBytes("GBK"), "ISO8859_1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return set;
	}

	/**
	 * decode charset
	 * 
	 * @param charSet
	 * @return String
	 * @throws Exception
	 */
	public String decodeCharset(String charSet) {
		String set = null;
		try {
			set = new String(charSet.getBytes("ISO8859_1"), "GBK");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return set;
	}

	/**
	 * encode timestamp
	 * 
	 * @param timeStr
	 * @return Timestamp
	 * @throws Exception
	 */
	public Date encodeTimestamp(String timeStr) {
		if (timeStr == null || "".equals(timeStr)) {
			return null;
		}
		String format = getTimestampFormat(timeStr);
		if (StringUtils.isEmpty(format)) {
			return null;
		}
		return encodeTimestamp(format, timeStr);
	}

	/**
	 * encode timestamp
	 * 
	 * @param format
	 * @param timeStr
	 * @return Timestamp
	 * @throws Exception
	 *             modified by caom on 08.7.28, check timeStr is null
	 */
	public Date encodeTimestamp(String format, String timeStr) {
		Date date = null;
		if (null == timeStr || "".equals(timeStr))
			return null;
		if (format.length() != timeStr.length())
			format = getTimestampFormat(timeStr);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			date = new Timestamp(sdf.parse(timeStr).getTime());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return date;
	}

	/**
	 * decode timestamp
	 * 
	 * @param format
	 * @param timeStr
	 * @return String
	 * @throws Exception
	 */
	public String decodeTimeStr(String format, String timeStr) {
		Date time = encodeTimestamp(format, timeStr);
		return decodeTimestamp(format, time);
	}

	/**
	 * decode timestamp
	 * 
	 * @param format
	 * @param time
	 * @return String
	 * @throws Exception
	 */
	public String decodeTimestamp(String format, Date time) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(time);
	}

	/**
	 * get current time
	 * 
	 * @return Timestamp
	 * @throws Exception
	 */
	public Date getCurrentTime() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * get sys time
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String getSysTime() {
		return decodeTimestamp("yyyy-MM-dd HH:mm:ss", new Timestamp(System.currentTimeMillis()));
	}

	/**
	 * get sys date
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String getSysDate() {
		return decodeTimestamp("yyyy-MM-dd", new Timestamp(System.currentTimeMillis()));
	}

	/**
	 * get last day
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String getLastDay() {
		return getLastDay(getSysDate());
	}

	/**
	 * get last day
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String getLastDay(String timestr) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(encodeTimestamp(timestr));
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		return dateformat.format(cal.getTime());
	}

	/**
	 * get prev day by curr date
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String getPrevDayByCurrDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);

		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		return dateformat.format(cal.getTime());
	}

	/**
	 * getPrevMonthFirstDay
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String getPrevMonthFirstDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 1);

		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		return dateformat.format(cal.getTime());
	}

	/**
	 * format decimal
	 * 
	 * @param format
	 *            <"#.##(mentisia lack ignore)、0.00(appoint mentisia，lack add
	 *            0>"
	 * @param decimal
	 * @return String
	 * @throws Exception
	 */
	public String formatDecimal(String format, double decimal) {
		DecimalFormat df = new DecimalFormat(format);
		return df.format(decimal);
	}

	/**
	 * get class resource
	 * 
	 * @param file
	 * @return URL
	 * @throws Exception
	 */
	public URL getClassResource(String file) {
		URL url = this.getClass().getClassLoader().getResource(file);
		if (url == null)
			error("file " + file + " not exist!");
		return url;
	}

	/**
	 * get char length
	 * 
	 * @param value
	 * @return String
	 */
	public int getCharLength(String value) {
		char[] chars = value.toCharArray();

		int charlen = 0;
		for (int i = 0; i < chars.length; i++) {
			if ((int) chars[i] > 0x80) {
				charlen += 2;
			} else {
				charlen += 1;
			}
		}

		return charlen;
	}

	/**
	 * get char length
	 * 
	 * @param value
	 * @param length
	 * @return String
	 */
	public int getCharLength(String value, int length) {
		char[] chars = value.toCharArray();
		if (chars.length < length)
			length = chars.length;

		int charidx = 0, charlen = 0;
		while (charlen < length) {
			if ((int) chars[charidx] > 0x80) {
				charlen += 2;
			} else {
				charlen += 1;
			}
			charidx++;
		}

		return charidx;
	}

	/**
	 * get array by coding str
	 * 
	 * @param namestr
	 * @param encodestr
	 * @return List
	 * @throws Exception
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" })
	public List getArrayByCodingStr(String namestr, String encodestr) {
		String[] encodename = namestr.split(",");

		int tablen = 4, rowlen = 4, collen = 3;
		int rows = Integer.parseInt(encodestr.substring(tablen, tablen + rowlen));
		String content = encodestr.substring(tablen + rowlen + collen);

		List dataset = new ArrayList();
		for (int i = 0; i < rows; i++) {
			Map data = new HashMap();
			for (int j = 0; j < encodename.length; j++) {
				// log.debug("------------------------"+content.substring(0,
				int namelen = Integer.parseInt(content.substring(0, 4));
				content = content.substring(4);
				int vallen = getCharLength(content, namelen);
				String value = content.substring(0, vallen);
				content = content.substring(vallen);

				data.put(encodename[j], value);
			}
			dataset.add(data);
		}

		return dataset;
	}

	/**
	 * put data by entity
	 * 
	 * @param entity
	 * @return Map
	 * @throws Exception
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" })
	public Map putDataByEntity(Object entity) throws Exception {
		Map data = new HashMap();
		Method[] methods = entity.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			String methodName = methods[i].getName();
			if (methodName.startsWith("get")) {
				String paramName = methodName.substring(3, methodName.length());
				data.put(paramName, methods[i].invoke(entity, null));
			}
		}
		return data;
	}

	/**
	 * put entity by data
	 * 
	 * @param entity
	 * @param data
	 * @return Object
	 * @throws Exception
	 */
	@SuppressWarnings( { "rawtypes" })
	public Object putEntityByData(Object entity, Map data) throws Exception {
		Method[] methods = entity.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String methodName = method.getName();
			if (methodName.startsWith("set")) {
				Class methodType = method.getParameterTypes()[0];
				Object[] params = new Object[1];

				String paramName = methodName.substring(3, methodName.length()).toUpperCase();
				Object paramValue = data.get(paramName);
				if (paramValue == null)
					continue;

				params[0] = paramValue;

				if (String.class.isAssignableFrom(methodType)) {
					params[0] = paramValue;
				} else if (Date.class.isAssignableFrom(methodType)) {
					params[0] = encodeTimestamp((String) paramValue);
				} else if (Long.class.isAssignableFrom(methodType)) {
					params[0] = new Long(paramValue.toString());
				} else if (Integer.class.isAssignableFrom(methodType)) {
					params[0] = new Integer(paramValue.toString());
				} else if (Short.class.isAssignableFrom(methodType)) {
					params[0] = new Short(paramValue.toString());
				} else if (Double.class.isAssignableFrom(methodType)) {
					params[0] = new Double(paramValue.toString());
				} else if (Float.class.isAssignableFrom(methodType)) {
					params[0] = new Float(paramValue.toString());
				} else if (Boolean.class.isAssignableFrom(methodType)) {
					params[0] = new Boolean(paramValue.toString());
				}

				method.invoke(entity, params);
			}
		}
		return entity;
	}

	/**
	 * call bean
	 * 
	 * @param class_name
	 * @param method_name
	 * @param params
	 * @return Object
	 * @throws Exception
	 */
	public Object callBean(String class_name, String method_name) throws Exception {
		return callBean(class_name, method_name, null, null);
	}

	/**
	 * call bean
	 * 
	 * @param pd
	 * @param class_name
	 * @param method_name
	 * @param params
	 * @return Object
	 * @throws Exception
	 */
	public Object callBean(String class_name, String method_name, Object[] params) throws Exception {
		return callBean(class_name, method_name, params, null);
	}

	/**
	 * call bean
	 * 
	 * @param class_name
	 * @param method_name
	 * @param params
	 * @param types
	 * @return Object
	 * @throws Exception
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" })
	public Object callBean(String class_name, String method_name, Object[] params, Class[] types) throws Exception {
		Class cls = Class.forName(class_name);
		Object instance = cls.newInstance();
		if (types == null)
			types = new Class[params.length];

		if (params != null && types != null) {
			for (int i = 0; i < types.length; i++) {
				if (types[i] == null)
					types[i] = params[i].getClass();
			}
		}
		Method method = cls.getMethod(method_name, types);
		return method.invoke(instance, params);
	}

	/**
	 * call bean
	 * 
	 * @param class_name
	 * @param method_name
	 * @param params
	 * @param types
	 * @return Object
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Object callBean(Object instance, String method_name, Object[] params, Class[] types) throws Exception {
		if (params != null && types != null) {
			for (int i = 0; i < types.length; i++) {
				if (types[i] == null)
					types[i] = params[i].getClass();
			}
		}

		Method method = instance.getClass().getMethod(method_name, types);
		return method.invoke(instance, params);
	}

	/**
	 * get property
	 * 
	 * @param prop
	 * @return String
	 */
	public String getProperty(String prop) {
		return getProperty(prop, null);
	}

	/**
	 * get property
	 * 
	 * @param prop
	 * @param defval
	 * @return String
	 */
	public String getProperty(String prop, String defval) {
		return getProperty("application.xml", prop, defval);
	}

	/**
	 * get property
	 * 
	 * @param file
	 * @param prop
	 * @param defval
	 * @return String
	 */
	public String getProperty(String file, String prop, String defval) {
		ICache cache = getCacheManager().getICache("COM_CONFIG_COLLECT");
		if (cache != null && cache.get(prop) != null) {
			return cache.getValue(prop);
		}

		String result = null;
		try {
			IConfig config = new XMLConfig(file);
			result = config.getProperty(prop);
		} catch (Exception e) {
			if (defval == null) {
				error("application config " + prop + " reading error!", e);
			} else {
				result = defval;
			}
		} finally {
			if (result == null && defval != null) {
				result = defval;
			}
			if (result != null) {
				if (cache != null) {
					cache.put(prop, result);
				}
			}
		}

		return result;
	}

	/**
	 * get properties
	 * 
	 * @param prop
	 * @return Map
	 */
	@SuppressWarnings( { "rawtypes" })
	public IData getProperties(String prop) {
		return getProperties("application.xml", prop);
	}

	/**
	 * get property
	 * 
	 * @param file
	 * @param prop
	 * @return Map
	 */
	@SuppressWarnings( { "rawtypes" })
	public IData getProperties(String file, String prop) {
		ICache cache = getCacheManager().getICache("COM_CONFIG_COLLECT");
		if (cache != null && cache.get(prop) != null) {
			return cache.getValue(prop);
		}

		try {
			IConfig config = new XMLConfig(file);
			IData result = config.getProperties(prop);

			if (cache != null) {
				cache.put(prop, result);
			}

			return result;
		} catch (Exception e) {
			error("application config " + prop + " reading error!", e);
		}

		return null;
	}

	/***************************************************************************
	 * throw error
	 * 
	 * @param message
	 * @throws RuntimeException
	 */
	public void error(String message) throws RuntimeException {
		RuntimeException exception = new RuntimeException(message);
		log.error(message);
		throw exception;
	}

	/***************************************************************************
	 * throw error
	 * 
	 * @param e
	 * @throws RuntimeException
	 */
	public void error(Exception e) throws RuntimeException {
		RuntimeException exception = new RuntimeException(e);
		log.error(e.getMessage(), exception);
		throw exception;
	}

	/***************************************************************************
	 * throw error
	 * 
	 * @param message
	 * @param e
	 * @throws RuntimeException
	 */
	public void error(String message, Exception e) throws RuntimeException {
		RuntimeException exception = new RuntimeException(message, e);
		log.error(message, exception);
		throw exception;
	}

	/**
	 * get values
	 * 
	 * @param value
	 * @return String[]
	 */
	public String[] getValues(Object value) {
		if (value == null)
			return new String[] {};
		if (value instanceof String[]) {
			return (String[]) value;
		} else {
			return new String[] { (String) value };
		}
	}

	/**
	 * get hit info
	 * 
	 * @param prop
	 * @return String
	 * @throws Exception
	 */
	public String getHintInfo(String prop) throws Exception {
		ICache cache = getCacheManager().getICache("SYS_COLUMNS_CACHE");
		if (cache != null && cache.get(prop) != null) {
			return cache.getValue(prop);
		}

		IConfig config = new TEXTConfig(prop.startsWith("component.") ? "hint_wadelib.txt" : "hint.txt");
		String result = config.getProperty(prop);
		if (result != null) {
			String tmp = URLDecoder.decode(result, "GBK");
			result = tmp;
		}
		if (cache != null) {
			cache.put(prop, result);
		}
		return result;
	}

	/**
	 * 从spring上下文中获取缓存管理器
	 * 
	 * @return
	 */
	public ICacheManager getCacheManager() {
		String springCacheManager = SpringPropertyHolder.getContextProperty("cacheManagerName", "springCacheManager");
		return SpringContextHolder.getBean(springCacheManager, ICacheManager.class);
	}

	/**
	 * to chinese money
	 * 
	 * @param money
	 * @return
	 * @throws Exception
	 */
	public String toChineseMoney(String money) {
		if (money == null)
			return null;
		String prefix = money.startsWith("-") ? "负" : "";
		money = money.replaceAll("-", "");
		String[] part = money.split("\\.");
		String newchar = "";
		for (int i = part[0].length() - 1; i >= 0; i--) {
			if (part[0].length() > 10) {
				error("位数过大，无法计算");
			}
			String tmpnewchar = "";
			char perchar = part[0].charAt(i);
			switch (perchar) {
			case '0':
				tmpnewchar = "零" + tmpnewchar;
				break;
			case '1':
				tmpnewchar = "壹" + tmpnewchar;
				break;
			case '2':
				tmpnewchar = "贰" + tmpnewchar;
				break;
			case '3':
				tmpnewchar = "叁" + tmpnewchar;
				break;
			case '4':
				tmpnewchar = "肆" + tmpnewchar;
				break;
			case '5':
				tmpnewchar = "伍" + tmpnewchar;
				break;
			case '6':
				tmpnewchar = "陆" + tmpnewchar;
				break;
			case '7':
				tmpnewchar = "柒" + tmpnewchar;
				break;
			case '8':
				tmpnewchar = "捌" + tmpnewchar;
				break;
			case '9':
				tmpnewchar = "玖" + tmpnewchar;
				break;
			}
			switch (part[0].length() - i - 1) {
			case 0:
				tmpnewchar = tmpnewchar + "元";
				break;
			case 1:
				if (perchar != 0)
					tmpnewchar = tmpnewchar + "拾";
				break;
			case 2:
				if (perchar != 0)
					tmpnewchar = tmpnewchar + "佰";
				break;
			case 3:
				if (perchar != 0)
					tmpnewchar = tmpnewchar + "仟";
				break;
			case 4:
				tmpnewchar = tmpnewchar + "万";
				break;
			case 5:
				if (perchar != 0)
					tmpnewchar = tmpnewchar + "拾";
				break;
			case 6:
				if (perchar != 0)
					tmpnewchar = tmpnewchar + "佰";
				break;
			case 7:
				if (perchar != 0)
					tmpnewchar = tmpnewchar + "仟";
				break;
			case 8:
				tmpnewchar = tmpnewchar + "亿";
				break;
			case 9:
				tmpnewchar = tmpnewchar + "拾";
				break;
			}
			newchar = tmpnewchar + newchar;
		}
		if (money.indexOf(".") != -1) {
			if (part[1].length() > 2) {
				log.warn("小数点之后只能保留两位,系统将自动截断");
				part[1] = part[1].substring(0, 2);
			}
			for (int i = 0; i < part[1].length(); i++) {
				String tmpnewchar = "";
				char perchar = part[1].charAt(i);
				switch (perchar) {
				case '0':
					tmpnewchar = "零" + tmpnewchar;
					break;
				case '1':
					tmpnewchar = "壹" + tmpnewchar;
					break;
				case '2':
					tmpnewchar = "贰" + tmpnewchar;
					break;
				case '3':
					tmpnewchar = "叁" + tmpnewchar;
					break;
				case '4':
					tmpnewchar = "肆" + tmpnewchar;
					break;
				case '5':
					tmpnewchar = "伍" + tmpnewchar;
					break;
				case '6':
					tmpnewchar = "陆" + tmpnewchar;
					break;
				case '7':
					tmpnewchar = "柒" + tmpnewchar;
					break;
				case '8':
					tmpnewchar = "捌" + tmpnewchar;
					break;
				case '9':
					tmpnewchar = "玖" + tmpnewchar;
					break;
				}
				if (i == 0)
					tmpnewchar = tmpnewchar + "角";
				if (i == 1) {
					tmpnewchar = (part[1].charAt(0) == '0' ? "零" : "") + tmpnewchar + "分";
				}
				newchar = newchar + tmpnewchar;
			}
		}

		while (newchar.indexOf("零零") != -1)
			newchar = newchar.replaceAll("零零", "零");
		newchar = newchar.replaceAll("零亿", "亿");
		newchar = newchar.replaceAll("亿万", "亿");
		newchar = newchar.replaceAll("零万", "万");
		if (!newchar.startsWith("零元"))
			newchar = newchar.replaceAll("零元", "元");
		newchar = newchar.replaceAll("零角", "");
		newchar = newchar.replaceAll("零分", "");
		if (newchar.charAt(newchar.length() - 1) == '元' || newchar.charAt(newchar.length() - 1) == '角')
			newchar = newchar + "整";
		return prefix + newchar;
	}

	/**
	 * reflect invoke
	 * 
	 * @param bean
	 * @param funcName
	 * @param params
	 * @param types
	 * @return Object
	 * @throws Exception
	 */
	@SuppressWarnings( { "rawtypes" })
	public Object reflectInvoke(Object bean, String funcName, Object[] params, Class[] types) throws Exception {
		Object ret = null;
		Class[] Params = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			if (types != null && types[i] != null)
				Params[i] = types[i];
			else if (params[i] != null)
				Params[i] = params[i].getClass();
		}
		Method method = bean.getClass().getMethod(funcName, Params);

		ret = method.invoke(bean, params);

		return ret;
	}

	/**
	 * get partition id
	 * 
	 * @param id
	 * @return String
	 */
	public String getPartitionId(String id) {
		return getPartitionId(id, 4);
	}

	/**
	 * get partition id
	 * 
	 * @param id
	 * @param length
	 * @return String
	 */
	public String getPartitionId(String id, int length) {
		return String.valueOf(Long.parseLong(id) % (int) Math.pow(10, length));
	}

	/**
	 * equalsNVL
	 * 
	 * @param obj1
	 * @param obj2
	 * @return boolean
	 */
	public boolean equalsNVL(Object obj1, Object obj2) {
		if ((obj1 == null) && (obj2 == null)) {
			return true;
		}
		if ((obj1 != null) && (obj2 != null) && obj1.equals(obj2)) {
			return true;
		}
		return false;
	}

	/**
	 * hashCodeNVL
	 * 
	 * @param o
	 * @return int
	 */
	public int hashCodeNVL(Object o) {
		if (o == null) {
			return 0;
		}
		return o.hashCode();
	}

	public String getStackTrace(Object e) {
		return getStackTrace((Throwable) e, 0);
	}

	public String getStackTrace(Throwable e, int maxLength) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String str = sw.toString();
		if (maxLength == 0)
			return str;

		int charLength = getCharLength(str, maxLength);
		return str.substring(0, charLength);
	}

	/**
	 * <p>
	 * 根据异常信息获取错误说明。
	 * </p>
	 * 
	 * @param e
	 * @return
	 */
	public String getErrorMessage(Object e) {
		String msg = "";
		if (e instanceof Throwable) {
			msg = ((Throwable) e).getMessage();
			if (null == e || StringUtils.isBlank(msg)) { // 没有错误信息或者是运行时抛出的程序bug，不暴露到外界
				return "出错了！:(";
			}
		}
		int messageSplit = -1;
		if ((messageSplit = msg.lastIndexOf("Exception:")) == -1) {
			return msg;
		}
		return msg.substring(messageSplit + 10);
	}

	/**
	 * 获取obj对象fieldName的Field
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public Field getFieldByFieldName(Object obj, String fieldName) {
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
			}
		}
		return null;
	}

	/**
	 * 获取obj对象fieldName的属性值
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	public Object getValueByFieldName(Object obj, String fieldName) throws Exception {
		String methodName = "get" + toUpperCaseFirstOne(fieldName);
		try {
			obj.getClass().getMethod(methodName, null);
		} catch (Exception e) {
			return null;
		}
		return this.callBean(obj, methodName, null, null);
	}

	/**
	 * 设置obj对象fieldName的属性值
	 * 
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	public void setValueByFieldName(Object obj, String fieldName, Object value) throws Exception {
		String methodName = "set" + toUpperCaseFirstOne(fieldName);
		Field field = null;
		try {
			field = obj.getClass().getDeclaredField(fieldName);
			obj.getClass().getMethod(methodName, new Class[] { field.getType() });
		} catch (Exception e) {
			return;
		}

		this.callBean(obj, methodName, new Object[] { value }, new Class[] { field.getType() });
	}

	/**
	 * 首字母转小写
	 * 
	 * @param s
	 * @return
	 */
	public String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * 首字母转大写
	 * 
	 * @param s
	 * @return
	 */
	public String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	public String getUniqeName() {
		return String.valueOf(System.currentTimeMillis()) + Math.abs(new Random().nextInt());
	}

	/**
	 * 判断类是否是String或基本类型包装类
	 * 
	 * @param clazz
	 * @return
	 */
	public boolean isBasicType(Class<?> clazz) {
		if (String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)) {
			return true;
		}
		return false;
	}

	/**
	 * Java去除字符串中的空格,回车,换行符,制表符
	 * 
	 * @param str
	 * @return
	 */
	public String replaceBlank(String str) {
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(str);
		return m.replaceAll("");
	}

	/**
	 * 解码url参数
	 * 
	 * @param param
	 * @param charset
	 * @return
	 */
	public String transParamDecode(String param, String charset) {
		if (param == null) {
			return null;
		}
		try {
			return URLDecoder.decode(param, charset).trim();
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * spring环境中获取request对象
	 * 
	 * @return
	 */
	public HttpServletRequest getContextRequest() {
		ServletRequestAttributes ra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = ra.getRequest();
		return request;
	}

	/**
	 * 分割一个大list为几个小list
	 * 
	 * @param list
	 * @param sub
	 *            每个list数量
	 * @return
	 */
	@SuppressWarnings( { "rawtypes" })
	public List[] getSubLists(List list, int sub) {
		List[] lists = null;
		if (list.size() > 0) {
			int subNums = list.size() / sub > 0 ? ((list.size() / sub) + (list.size() % sub == 0 ? 0 : 1)) : 1;
			lists = new List[subNums];
			for (int i = 0; i < subNums; i++) {
				List subList = null;
				if (i != subNums - 1) {
					subList = list.subList(i * sub, (i + 1) * sub);
				} else {
					subList = list.subList(i * sub, list.size());
				}
				lists[i] = subList;
			}
		}
		return lists;
	}

	/**
	 * isAjaxRequest:判断请求是否为Ajax请求
	 * 
	 * @param request
	 *            请求对象
	 * @return boolean
	 */
	public boolean isAjaxRequest(HttpServletRequest request) {
		String header = request.getHeader("X-Requested-With");
		boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(header) ? true : false;
		return isAjax;
	}

	/**
	 * 获取错误信息
	 * 
	 * @param throwable
	 * @return
	 */
	public String getErrorInfo(String errorInfo) {
		if (StringUtils.isBlank(errorInfo)) {
			return "";
		}
		if (errorInfo.startsWith("java.lang.RuntimeException:")) {
			errorInfo = errorInfo.substring("java.lang.RuntimeException:".length());
		}
		return errorInfo.replaceAll("\\n", "<br/>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
	}

	/**
	 * 生成随机数
	 * 
	 * @param length
	 * @return
	 */
	public String getRandomString(int length) { // length表示生成字符串的长度
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * 获取登录IP地址
	 * 
	 * @param request
	 * @return
	 */
	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip != null && ip.indexOf(",") > -1 && ip.length() > 8) {
			ip = ip.substring(0, ip.indexOf(","));
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 获取请求参数，map
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public InParam<String, Object> getInParam(HttpServletRequest request) {
		InParam<String, Object> inParam = new InParam<String, Object>();
		Enumeration<String> fields = request.getParameterNames();
		while (fields.hasMoreElements()) {
			String field = fields.nextElement();
			String[] values = request.getParameterValues(field);
			if (values.length > 1) {
				inParam.put(field, values);
			} else {
				// 空字符串认为是null。防止mybatis将空字符串认为0的问题
				if ("".equals(values[0])) {
					inParam.put(field, null);
				} else {
					inParam.put(field, values[0]);
				}
			}
		}

		putFilterRules(inParam);
		return inParam;
	}

	/**
	 * 获取以group分组的参数 默认不截取 如一组参数为condition_userName、condition_age、password，
	 * getGroupInParam("condition")只会获取以condition开头的参数。
	 * 
	 * @param group
	 * @return
	 */
	public InParam<String, Object> getGroupInParam(InParam<String, Object> inParam, String group) {
		return getGroupInParam(inParam, group, false);
	}

	/**
	 * 获取以group分组的参数 istrim参数来确定是否截取
	 * 
	 * @param group
	 * @param istrim
	 * @return
	 */
	public InParam<String, Object> getGroupInParam(InParam<String, Object> inParam, String group, boolean istrim) {
		InParam<String, Object> element = new InParam<String, Object>();
		String[] names = inParam.getNames();
		for (int i = 0; i < names.length; ++i) {
			if (names[i].startsWith(group + "_")) {
				element.put((istrim) ? names[i].substring((group + "_").length()) : names[i], inParam.get(names[i]));
			}
		}
		return element;
	}

	/**
	 * 将过滤字段放入paramData easyui 表格过滤使用
	 */
	public void putFilterRules(InParam<String, Object> inParam) {
		String filterRules = inParam.getString("filterRules");
		if (!StringUtils.isEmpty(filterRules)) {
			JSONArray arr = JSONArray.parseArray(filterRules);
			if (arr.size() > 0) {
				String filterField = String.valueOf(arr.getJSONObject(0).get("field"));
				String filterFieldValue = String.valueOf(arr.getJSONObject(0).get("value"));
				inParam.put(filterField, filterFieldValue);
			}
		}
	}

	/**
	 * 对象转Map
	 * 
	 * @param object
	 * @return
	 */
	public IData<Object, Object> toMap(Object object) {
		return toMap(object, false);
	}

	/**
	 * 对象转Map
	 * 
	 * @param object
	 * @param isToString：基本类型及包装类型是否转为String
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IData<Object, Object> toMap(Object object, boolean isToString) {
		String format = "yyyy-MM-dd HH:mm:ss";
		String valueName = "value";
		IData<Object, Object> map = new DataMap<Object, Object>();
		if (object == null) {
			return null;
		} else if (Date.class.isAssignableFrom(object.getClass())) {
			map.put(valueName, isToString ? this.decodeTimestamp(format, (Date) object) : object);
			return map;
		} else if (Comparable.class.isAssignableFrom(object.getClass())) {
			map.put(valueName, isToString ? object.toString() : object);
			return map;
		} else if (Map.class.isAssignableFrom(object.getClass())) {
			if (!isToString) {
				map.putAll((Map) object);
				return map;
			}
			Set<Map.Entry<Object, Object>> entrySet = ((Map) object).entrySet();
			for (Map.Entry<Object, Object> entry : entrySet) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				Object newValue = value;
				if (value != null) {
					if (Date.class.isAssignableFrom(value.getClass())) {
						newValue = this.decodeTimestamp(format, (Date) value);
					} else if (Comparable.class.isAssignableFrom(value.getClass())) {
						newValue = newValue.toString();
					}
				}
				map.put(key, newValue);
			}
			return map;
		} else {
			MetaObject meta = SystemMetaObject.forObject(object);
			String[] propertyNames = meta.getGetterNames();
			for (String propertyName : propertyNames) {
				Object value = getValueFromMeta(meta, propertyName);
				if (value != null) {
					if (Date.class.isAssignableFrom(value.getClass())) {
						value = isToString ? this.decodeTimestamp(format, (Date) value) : value;
					} else if (Comparable.class.isAssignableFrom(value.getClass())) {
						value = isToString ? value.toString() : value;
					}
				}
				map.put(propertyName, value);
			}
		}
		return map;
	}

	/**
	 * 从meta中取值
	 * 
	 * @param meta
	 * @param propertyName
	 * @return
	 */
	public Object getValueFromMeta(MetaObject meta, String propertyName) {
		try {
			return meta.getValue(propertyName);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 反射取值
	 * 
	 * @param <T>
	 * @param sourceObject
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(Object sourceObject, String name) {
		try {
			MetaObject targetMeta = SystemMetaObject.forObject(sourceObject);
			return (T) targetMeta.getValue(name);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * lpad
	 * 
	 * @param str
	 * @param size
	 * @param padStr
	 * @return
	 */
	public String lpad(String str, int size, String padStr) {
		String val = StringUtils.leftPad(str, size, padStr);
		if (size > 0 && val.length() > size) {
			val = val.substring(val.length() - size, val.length());
		}
		return val;
	}

	/**
	 * rpad
	 * 
	 * @param str
	 * @param size
	 * @param padStr
	 * @return
	 */
	public String rpad(String str, int size, String padStr) {
		String val = StringUtils.rightPad(str, size, padStr);
		if (size > 0 && val.length() > size) {
			val = val.substring(val.length() - size, val.length());
		}
		return val;
	}
}
