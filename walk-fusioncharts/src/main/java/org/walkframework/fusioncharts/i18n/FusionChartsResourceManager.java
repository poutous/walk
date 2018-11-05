package org.walkframework.fusioncharts.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionChartsResourceManager {
	private static final Logger log = LoggerFactory.getLogger(FusionChartsResourceManager.class);

	private ResourceBundle resource = null;

	public FusionChartsResourceManager(String resouceFullPath) {
		try {
			this.resource = ResourceBundle.getBundle(resouceFullPath);
		} catch (Exception e) {
			log.error("Load i18n resource " + resouceFullPath + " error", e);
		}
		log.info("dazzle i18n resources init finish");
	}

	public String get(String messageKey, Object[] values) {
		String message = this.resource.getString(messageKey);
		if (message != null) {
			return MessageFormat.format(message, values);
		}
		return null;
	}
}