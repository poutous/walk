package org.walkframework.restful.generator;

/**
 * @author wangxin
 *
 */
public enum JavaFileType {

	RSP, REQ, TRANSLATOR;

	private int sheetAtIndex;
	private String packageName;
	private String templateFileName;
	private static String appendPackageName; // 追加packageName

	private void setClassFileType(int sheetAtIndex, String packageName, String templateFileName) {
		this.sheetAtIndex = sheetAtIndex;
		this.packageName = packageName;
		this.templateFileName = templateFileName;
	}

	public static void init(Config config) {
		String reqPackageName = config.getReqPackageName();
		String rspPackageName = config.getRspPackageName();
		String modelTemplateFileName = config.getModelTemplateFileName();
		String translatorPackageName = config.getTranslatorPackageName();
		String translatorTemplateFileName = config.getTranslatorTemplateFileName();
		REQ.setClassFileType(0, reqPackageName, modelTemplateFileName);
		RSP.setClassFileType(1, rspPackageName, modelTemplateFileName);
		TRANSLATOR.setClassFileType(-1, translatorPackageName, translatorTemplateFileName);
	}

	public String getPackageName() {
		return packageName.replace("%.", (appendPackageName != null ? appendPackageName + "." : ""));
	}

	public int getSheetAtIndex() {
		return sheetAtIndex;
	}

	public String getTemplateFileName() {
		return templateFileName;
	}

	public static void setAppendPackageName(String appendPackageName) {
		JavaFileType.appendPackageName = appendPackageName;
	}

}
