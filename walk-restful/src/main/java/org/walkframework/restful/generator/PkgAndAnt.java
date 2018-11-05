package org.walkframework.restful.generator;

import java.util.HashSet;
import java.util.Set;

import org.walkframework.restful.exception.ExcelDataException;

/**
 * 引入的包和注解
 * 
 * @author wangxin
 */
public class PkgAndAnt {

	private String javaType;

	private String validatorAnnotationName;

	private Set<String> importPacks = newSet();

	public String getJavaType() {
		return javaType;
	}

	public String getValidatorAnnotationName() {
		return validatorAnnotationName;
	}

	public Set<String> getImportPacks() {
		return importPacks;
	}

	public PkgAndAnt(Row row, boolean isReq, boolean isForceBigDecimals) {
		init(row, isReq, isForceBigDecimals);
	}

	private void init(Row row, boolean isReq, boolean isForceBigDecimals) {
		JavaType javaType = getJavaType(row, isForceBigDecimals);
		ValidatorAnnotation validatorAnnotation = getValidatorAnnotation(row, javaType, isReq);
		String contain = row.getContain();
		if (contain.matches("\\*|\\+")) {
			if (javaType == JavaType.Model) {
				String className = row.getClassName();
				if (StringUtil.isBlank(className)) {
					throw new ExcelDataException("第%d行,未指定类名", row.getRowNum());
				}
				this.javaType = String.format("List<%s>", className);
			} else {
				this.javaType = String.format("List<%s>", javaType.getJavaType());
			}
			javaType = JavaType.List;
		} else {
			if (javaType == JavaType.Model) {
				String className = row.getClassName();
				if (StringUtil.isBlank(className)) {
					throw new ExcelDataException("第%d行,未指定类名", row.getRowNum());
				}
				this.javaType = className;
			} else {
				this.javaType = javaType.getJavaType();
			}
		}
		addImportPacks(this.importPacks, javaType, validatorAnnotation);
		this.validatorAnnotationName = validatorAnnotation.getAnnotationName();
	}

	private void addImportPacks(Set<String> importPacks, JavaType javaType, ValidatorAnnotation validatorAnnotation) {
		if (validatorAnnotation != null)
			importPacks.add(validatorAnnotation.getImportPkg());
		if (javaType != null)
			importPacks.add(javaType.getImportPack());
	}

	private ValidatorAnnotation getValidatorAnnotation(Row row, JavaType javaType, boolean isReq) {
		if (isReq) {
			String contain = row.getContain();
			if (contain.equals("1")) {
				return (javaType == JavaType.String ? ValidatorAnnotation.NotBlank : ValidatorAnnotation.NotNull);
			}
			if (contain.equals("+")) {
				return ValidatorAnnotation.NotEmpty;
			}
		}
		return ValidatorAnnotation.Non;
	}

	private JavaType getJavaType(Row row, boolean isForceBigDecimals) {
		String type = row.getType().toLowerCase();
		boolean isString = type.equalsIgnoreCase("string");
		boolean isNumber = type.equalsIgnoreCase("number");
		boolean isBoolean = type.equalsIgnoreCase("boolean");
		if (isString) {
			return JavaType.String;
		} else if (isNumber) {
			return getNumberJavaType(row, isForceBigDecimals);
		} else if (isBoolean) {
			return JavaType.Boolean;
		} else {
			return JavaType.Model;
		}
	}

	private JavaType getNumberJavaType(Row row, boolean isForceBigDecimals) {
		String length = row.getLength();
		if (isForceBigDecimals)
			return JavaType.BigDecimal;
		if (length.toLowerCase().matches("^[f|v][\\d][,|\\d]*$")) { // f4.2
			length = length.substring(1);
			if (length.indexOf(",") != -1) {
				// 小数
				int len = Integer.parseInt(length.split(",")[0]); // 长度
				int scale = Integer.parseInt(length.split(",")[1]); // 小数位
				if (scale >= len) {
					throw new ExcelDataException("第%d行,长度定义有误小数位长度不能大于或等于总长度", row.getRowNum());
				}
				if (len > 19) {
					return JavaType.BigDecimal;
				} else {
					return JavaType.Double;
				}
			} else {
				// 整数
				int len = Integer.parseInt(length);
				if (len > 19) {
					return JavaType.BigDecimal;
				} else if (len > 9) {
					return JavaType.Long;
				} else {
					return JavaType.Integer;
				}
			}
		} else {
			throw new ExcelDataException("第%d行,数据长度定义 %s 有误! 请按照规范填写!", row.getRowNum(), row.getLength());
		}
	}

	private enum JavaType {

		Boolean("java.lang.Boolean"),

		Long("java.lang.Long"),

		Integer("java.lang.Integer"),

		Double("java.lang.Double"),

		BigDecimal("java.math.BigDecimal"),

		String("java.lang.String"),

		List("java.util.List"),

		Model(null);

		private final String javaType;

		private JavaType(String javaType) {
			this.javaType = javaType;
		}

		public String getJavaType() {
			if (javaType == null)
				return null;
			return StringUtil.getShortClassName(javaType);
		}

		public String getImportPack() {
			if (javaType == null || javaType.startsWith("java.lang"))
				return null;
			return javaType;
		}

	}

	private enum ValidatorAnnotation {
		NotNull("@NotNull", "javax.validation.constraints.NotNull"),

		NotBlank("@NotBlank", "org.hibernate.validator.constraints.NotBlank"),

		NotEmpty("@NotEmpty", "org.hibernate.validator.constraints.NotEmpty"),

		Non(null, null); // 不用校验

		final String annotationName, importPkg;

		private ValidatorAnnotation(String annotation, String importPack) {
			this.annotationName = annotation;
			this.importPkg = importPack;
		}

		public String getAnnotationName() {
			return annotationName;
		}

		public String getImportPkg() {
			return importPkg;
		}

	}

	/**
	 * 不允许添加null
	 */
	private static <T> Set<T> newSet() {
		return new HashSet<T>() {
			private static final long serialVersionUID = 1L;

			public boolean add(T e) {
				return e == null ? false : super.add(e);
			}
		};
	}

}
