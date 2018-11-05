package org.walkframework.fusioncharts.i18n;

public class FusionChartsMessage {
	private static FusionChartsResourceManager rm = new FusionChartsResourceManager("message.FusionChartsMessages");

	public static String get(String messageKey, Object[] values) {
		return rm.get(messageKey, values);
	}
}
