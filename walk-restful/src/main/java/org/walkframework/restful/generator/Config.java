package org.walkframework.restful.generator;

import javax.swing.filechooser.FileSystemView;

import org.walkframework.restful.exception.ConfigException;

/**
 * @author wangxin
 * 
 */
public class Config {

	// windows桌面目录路径(例如：C:\Users\Administrator\Desktop)
	public final static String DESKTOP_DIR = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();

	public final static Config DEFAULT = new Config("UTF-8", // charset
			"ModelTemplate.md", // modelTemplateFileName
			"TranslatorTemplate.md", // translatorTemplateFileName
			DESKTOP_DIR + "/excel", // excelFileDir
			"CLASSPATH:../code", // codeDirPath
			"classpath:META-INF/resources/generator/template", // templateDirPath
			"model.req", // reqPackageName
			"model.rsp", // rspPackageName
			"translator", // translatorPackageName
			"org.walkframework.restful.model.req.ReqBody", // reqSuperClassName
			"org.walkframework.restful.model.rsp.RspData", // rspSuperClassName
			"org.walkframework.restful.model.req.PaginationReq", // reqPaginationSuperClassName
			false, // isForceBigDecimals
			false, // override
			false, // showQuickRWMethods
			false); // showMethodComment

	// 模板文件中文编码，默认UTF-8
	private String charset;

	// 模板文件名称
	private String modelTemplateFileName;
	private String translatorTemplateFileName;

	// Excel文件目录
	private String excelFileDir;

	// 生成好的.java文件目录
	private String codeDirPath;

	// 模板文件目录
	private String templateDirPath;

	// 生成后源文件的包名 --- %代表模块名
	private String reqPackageName;
	private String rspPackageName;
	private String translatorPackageName;

	// 父类名称
	private String reqSuperClassName;
	private String rspSuperClassName;
	private String reqPaginationSuperClassName;

	// 是否强制将数据库类型DECIMAL和NUMERIC转换为BigDecimal类型
	private boolean isForceBigDecimals;

	// 是否覆盖源文件
	private boolean override;

	// 打开快速set/get方法
	private boolean showQuickRWMethods;

	// 显示方法注释
	private boolean showMethodComment;

	public Config() {
	}

	private Config(String charset, String modelTemplateFileName, String translatorTemplateFileName, String excelFileDir, String codeDirPath, String templateDirPath, String reqPackageName, String rspPackageName, String translatorPackageName, String reqSuperClassName, String rspSuperClassName, String reqPaginationSuperClassName, boolean isForceBigDecimals, boolean override, boolean showQuickRWMethods, boolean showMethodComment) {
		this.charset = charset;
		this.modelTemplateFileName = modelTemplateFileName;
		this.translatorTemplateFileName = translatorTemplateFileName;
		this.excelFileDir = excelFileDir;
		this.codeDirPath = codeDirPath;
		this.templateDirPath = templateDirPath;
		this.reqPackageName = reqPackageName;
		this.rspPackageName = rspPackageName;
		this.translatorPackageName = translatorPackageName;
		this.reqSuperClassName = reqSuperClassName;
		this.rspSuperClassName = rspSuperClassName;
		this.reqPaginationSuperClassName = reqPaginationSuperClassName;
		this.isForceBigDecimals = isForceBigDecimals;
		this.override = override;
		this.showQuickRWMethods = showQuickRWMethods;
		this.showMethodComment = showMethodComment;
	}

	public String getCharset() {
		return charset;
	}

	/**
	 * 模板文件中文编码
	 * 
	 * @param charset
	 * @return
	 */
	public Config setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	public String getModelTemplateFileName() {
		return modelTemplateFileName;
	}

	/**
	 * Model模板文件名称
	 * 
	 * @param modelTemplateFileName
	 * @return
	 */
	public Config setModelTemplateFileName(String modelTemplateFileName) {
		this.modelTemplateFileName = modelTemplateFileName;
		return this;
	}

	public String getTranslatorTemplateFileName() {
		return translatorTemplateFileName;
	}

	/**
	 * Translator模板文件名称
	 * 
	 * @param translatorTemplateFileName
	 * @return
	 */
	public Config setTranslatorTemplateFileName(String translatorTemplateFileName) {
		this.translatorTemplateFileName = translatorTemplateFileName;
		return this;
	}

	public String getExcelFileDir() {
		return excelFileDir;
	}

	/**
	 * EXCEL文件目录
	 * 
	 * @param excelFileDir
	 * @return
	 */
	public Config setExcelFileDir(String excelFileDir) {
		this.excelFileDir = excelFileDir;
		return this;
	}

	public String getCodeDirPath() {
		return codeDirPath;
	}

	/**
	 * 生成好的.java文件目录位置
	 * 
	 * @param codeDirPath
	 * @return
	 */
	public Config setCodeDirPath(String codeDirPath) {
		this.codeDirPath = codeDirPath;
		return this;
	}

	public String getTemplateDirPath() {
		return templateDirPath;
	}

	/**
	 * 模板文件目录
	 * 
	 * @param templateDirPath
	 * @return
	 */
	public Config setTemplateDirPath(String templateDirPath) {
		this.templateDirPath = templateDirPath;
		return this;
	}

	public boolean isOverride() {
		return override;
	}

	/**
	 * 是否覆盖原有的文件
	 * 
	 * @param override
	 * @return
	 */
	public Config setOverride(boolean override) {
		this.override = override;
		return this;
	}

	public String getReqPackageName() {
		return reqPackageName;
	}

	/**
	 * 请求报文包名 --- % 二级文件夹名
	 * 
	 * @param reqPackageName
	 * @return
	 */
	public Config setReqPackageName(String reqPackageName) {
		this.reqPackageName = reqPackageName;
		return this;
	}

	public String getRspPackageName() {
		return rspPackageName;
	}

	/**
	 * 返回报文包名 --- % 二级文件夹名
	 * 
	 * @param rspPackageName
	 * @return
	 */
	public Config setRspPackageName(String rspPackageName) {
		this.rspPackageName = rspPackageName;
		return this;
	}

	public String getTranslatorPackageName() {
		return translatorPackageName;
	}

	/**
	 * 属性翻译器包名 --- % 二级文件夹名
	 * 
	 * @param translatorPackageName
	 * @return
	 */
	public Config setTranslatorPackageName(String translatorPackageName) {
		this.translatorPackageName = translatorPackageName;
		return this;
	}

	public String getReqSuperClassName() {
		return reqSuperClassName;
	}

	/**
	 * 请求报文父类名称
	 * 
	 * @param reqSuperClassName
	 * @return
	 */
	public Config setReqSuperClassName(String reqSuperClassName) {
		this.reqSuperClassName = reqSuperClassName;
		return this;
	}

	public String getRspSuperClassName() {
		return rspSuperClassName;
	}

	/**
	 * 返回报文父类名称
	 * 
	 * @param rspSuperClassName
	 * @return
	 */
	public Config setRspSuperClassName(String rspSuperClassName) {
		this.rspSuperClassName = rspSuperClassName;
		return this;
	}

	public String getReqPaginationSuperClassName() {
		return reqPaginationSuperClassName;
	}

	public boolean isForceBigDecimals() {
		return isForceBigDecimals;
	}

	/**
	 * 是否强制将数据库类型DECIMAL和NUMERIC转换为BigDecimal类型
	 * 
	 * @param isForceBigDecimals
	 * @return
	 */
	public Config setForceBigDecimals(boolean isForceBigDecimals) {
		this.isForceBigDecimals = isForceBigDecimals;
		return this;
	}

	/**
	 * 请求分页报文父类名称
	 * 
	 * @param reqPaginationSuperClassName
	 * @return
	 */
	public Config setReqPaginationSuperClassName(String reqPaginationSuperClassName) {
		this.reqPaginationSuperClassName = reqPaginationSuperClassName;
		return this;
	}

	public boolean isShowQuickRWMethods() {
		return showQuickRWMethods;
	}

	/**
	 * 生成开发用SET/GET方法
	 * 
	 * @param quickRWMethods
	 * @return
	 */
	public Config setShowQuickRWMethods(boolean showQuickRWMethods) {
		this.showQuickRWMethods = showQuickRWMethods;
		return this;
	}

	public boolean isShowMethodComment() {
		return showMethodComment;
	}

	/**
	 * 打开方法注释
	 * 
	 * @param showMethodComment
	 * @return
	 */
	public Config setShowMethodComment(boolean showMethodComment) {
		this.showMethodComment = showMethodComment;
		return this;
	}

	public Config checkConfig() {
		assertNotNull(reqPackageName, "请配置正确的请求报文包路径Config.setReqPackageName(\"reqPackageName\")");
		assertNotNull(rspPackageName, "请配置正确的返回报文包路径Config.setRspPackageName(\"rspPackageName\")");
		assertNotNull(translatorPackageName, "请配置正确的翻译器包路径Config.setTranslatorPackageName(\"translatorPackageName\")");
		assertNotBlank(translatorTemplateFileName, "请配置正确的模板文件路径Config.setTranslatorTemplateFileName(\"translatorTemplateFileName\")");
		assertNotBlank(modelTemplateFileName, "请配置正确的模板文件路径Config.setModelTemplateFileName(\"modelTemplateFileName\")");
		assertNotBlank(reqSuperClassName, "请配置正确的模板文件路径Config.setReqSuperClassName(\"superClassName\")");
		assertNotBlank(rspSuperClassName, "请配置正确的模板文件路径Config.setRspSuperClassName(\"superClassName\")");
		assertNotBlank(reqPaginationSuperClassName, "请配置正确的模板文件路径Config.setReqPaginationSuperClassName(\"superClassName\")");
		assertNotBlank(templateDirPath, "请配置正确的模板文件路径Config.setTemplateDirPath(\"path\")");
		assertNotBlank(excelFileDir, "请配置正确的EXCEL文档目录路径Config.setExcelFileDir(\"path\")");
		assertNotBlank(codeDirPath, "请配置正确的代码生成路径Config.setCodeDirPath(\"path\")");
		return this;
	}

	private void assertNotBlank(String testStr, String errorInfo) {
		if (StringUtil.isBlank(testStr))
			throw new ConfigException(errorInfo);
	}

	private void assertNotNull(String testStr, String errorInfo) {
		if (testStr == null)
			throw new ConfigException(errorInfo);
	}

}
