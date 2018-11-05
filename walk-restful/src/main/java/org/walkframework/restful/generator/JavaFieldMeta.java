package org.walkframework.restful.generator;

/**
 * @author wangxin
 *
 */
public class JavaFieldMeta {

	private String name;

	private String javaType;

	private String containAnt;

	private String desc;

	private boolean isReqiure;

	private boolean isComplex;

	private String translatorName;

	private int position;

	public JavaFieldMeta(String name, String javaType, String containAnt, String desc, int position, boolean isReqiure, boolean isComplex, String translatorName) {
		this.name = name;
		this.javaType = javaType;
		this.isReqiure = isReqiure;
		this.isComplex = isComplex;
		this.containAnt = containAnt;
		this.position = position;
		this.desc = desc;
		this.translatorName = translatorName;
	}

	public String getTranslatorName() {
		return translatorName;
	}

	public String getName() {
		return name;
	}

	public String getJavaType() {
		return javaType;
	}

	public boolean isReqiure() {
		return isReqiure;
	}

	public boolean isComplex() {
		return isComplex;
	}

	public String getContainAnt() {
		return containAnt;
	}

	public String getDesc() {
		return desc;
	}

	public int getPosition() {
		return position;
	}

	public String getRWMethodName() {
		return StringUtil.firstCharToUpperCase(this.name);
	}

}
