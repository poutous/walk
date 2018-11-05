package org.walkframework.base.system.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.system.staticparam.StaticParamUtil;
import org.walkframework.data.util.IData;

/**
 * 从指定表中校验
 * 
 * @author shf675
 * 
 */
public class ValidFromTableValidator implements ConstraintValidator<ValidFromTable, Object> {

	protected final static Common common = SingletonFactory.getInstance(Common.class);

	private String[] tables;

	private boolean allow;

	@Override
	public void initialize(ValidFromTable constraintAnnotation) {
		this.tables = constraintAnnotation.value();
		this.allow = constraintAnnotation.allow();
	}

	@Override
	public boolean isValid(Object v, ConstraintValidatorContext context) {
		String value = v == null ? "" : v.toString();
		//空值直接跳过，如输入项是必选项需配合@NotNull或@NotEmpty或@NotBlank使用
		if(StringUtils.isEmpty(value)){
			return true;
		}
		for (String table : tables) {
			table = StringUtils.trimAllWhitespace(table);
			if (StringUtils.isEmpty(table) || table.indexOf(".") == -1) {
				common.error("注解：" + ValidFromTable.class.getName() + "value设置错误！");
			}

			String[] tbs = table.split("\\.");
			if (tbs.length > 2) {
				common.error("注解：" + ValidFromTable.class.getName() + "value设置错误！");
			}
			
			// 从缓存取
			String key = tbs[0];
			String primaryKey = tbs[1];
			IData<String, Object> cacheData = StaticParamUtil.getCache(key).getValue(StaticParamUtil.getMapCacheKey(value));
			if (cacheData != null && !cacheData.isEmpty()) {
				if (value.equals(cacheData.getString(primaryKey))) {
					return allow;
				}
			}
		}
		return !allow;
	}

}
