package org.walkframework.restful.generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author wangxin
 *
 */
public class JavaFileMeta {
	private Set<String> importPackageNames = new HashSet<String>();
	private String packageName;
	private String name;
	private String superClassName;
	private String postfix;
	private int sheetAtIndex;
	private List<JavaFieldMeta> fieldMetas;
	private JavaFileType javaFileTypeEnum;
	private String srcFileName;
	private String desc;
	private boolean requireTranslate;
	private boolean showQuickRWMethods;
	private boolean showMethodsComment;
	private String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	private String author = System.getProperty("user.name");

	public JavaFileMeta(Config config, JavaFileType javaFileTypeEnum) {
		this.javaFileTypeEnum = javaFileTypeEnum;
		this.packageName = javaFileTypeEnum.getPackageName();
		this.sheetAtIndex = javaFileTypeEnum.getSheetAtIndex();
		this.showQuickRWMethods = config.isShowQuickRWMethods();
		this.showMethodsComment = config.isShowMethodComment();
		if (javaFileTypeEnum == JavaFileType.REQ) {
			this.superClassName = config.getReqSuperClassName();
		} else {
			this.superClassName = config.getRspSuperClassName();
		}
	}

	public void setFieldMetas(List<JavaFieldMeta> fieldMetas) {
		this.fieldMetas = fieldMetas;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setRequireTranslate(boolean requireTranslate) {
		this.requireTranslate = requireTranslate;
	}

	public void setSrcFileName(String srcFileName) {
		this.srcFileName = srcFileName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRequireTranslate() {
		return requireTranslate;
	}

	public Set<String> getImportPackageNames() {
		return importPackageNames;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getName() {
		return name;
	}

	public String getPostfix() {
		return postfix;
	}

	public int getSheetAtIndex() {
		return sheetAtIndex;
	}

	public List<JavaFieldMeta> getFieldMetas() {
		return fieldMetas;
	}

	public JavaFileType getJavaFileType() {
		return javaFileTypeEnum;
	}

	public String getSrcFileName() {
		return srcFileName;
	}

	public String getDesc() {
		return desc;
	}

	public boolean isShowQuickRWMethods() {
		return showQuickRWMethods;
	}

	public boolean isShowMethodsComment() {
		return showMethodsComment;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public String getAuthor() {
		return author;
	}

	public void addImportPackageName(Set<String> importPagckageNames) {
		if (importPagckageNames != null && importPagckageNames.size() > 0) {
			importPackageNames.addAll(importPagckageNames);
		}
	}

	public void addImportPackageName(String importPagckageName) {
		if (StringUtil.hasText(importPagckageName)) {
			importPackageNames.add(importPagckageName);
		}
	}
}
