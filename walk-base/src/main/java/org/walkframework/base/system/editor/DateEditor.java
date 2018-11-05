package org.walkframework.base.system.editor;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;


/**
 * spring mvc参数注入时的日期格式化工具类
 *
 */
public class DateEditor extends PropertyEditorSupport {
	private final static Logger log = LoggerFactory.getLogger(DateEditor.class);
	private final static Common common = SingletonFactory.getInstance(Common.class);
	
	private final static Map<String, DateFormat> DATEFORMAT = new HashMap<String, DateFormat>();
	
	private static DateFormat getDateFormat(String text){
		String format = common.getTimestampFormat(text);
		if(DATEFORMAT.get(format) == null){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
			DATEFORMAT.put(format, simpleDateFormat);
			return simpleDateFormat;
		}
		return DATEFORMAT.get(format);
	}

	private DateFormat dateFormat;
	private boolean allowEmpty = true;

	public DateEditor() {
	}

	public DateEditor(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public DateEditor(DateFormat dateFormat, boolean allowEmpty) {
		this.dateFormat = dateFormat;
		this.allowEmpty = allowEmpty;
	}

	/**
	 * Parse the Date from the given text, using the specified DateFormat.
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			// Treat empty String as null value.
			setValue(null);
		} else {
			try {
				if (this.dateFormat != null) {
					setValue(this.dateFormat.parse(text));
				} else {
					setValue(getDateFormat(text).parse(text));
				}
			} catch (ParseException ex) {
				log.error(ex.getMessage(), ex);
				throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Format the Date as String, using the specified DateFormat.
	 */
	@Override
	public String getAsText() {
		Date value = (Date) getValue();
		DateFormat dateFormat = this.dateFormat;
		if (dateFormat == null){
			String defaultformat = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(defaultformat);
			DATEFORMAT.put(defaultformat, simpleDateFormat);
			dateFormat = simpleDateFormat;
			
		}
		return (value != null ? dateFormat.format(value) : "");
	}
}