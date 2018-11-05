package org.walkframework.base.system.validation;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.system.staticparam.StaticParamConstants;
import org.walkframework.base.system.staticparam.StaticParamUtil;
import org.walkframework.data.util.IData;

/**
 * 从td_s_static表校验
 * 
 * @author shf675
 * 
 */
public class ValidFromStaticValidator implements ConstraintValidator<ValidFromStatic, Object> {

	protected final static Common common = SingletonFactory.getInstance(Common.class);

	private String[] typeIds;

	private boolean allow;

	@Override
	public void initialize(ValidFromStatic constraintAnnotation) {
		this.typeIds = constraintAnnotation.value();
		this.allow = constraintAnnotation.allow();
	}

	@Override
	public boolean isValid(Object v, ConstraintValidatorContext context) {
		String value = v == null ? "" : v.toString();
		//空值直接跳过，如输入项是必选项需配合@NotNull或@NotEmpty或@NotBlank使用
		if(StringUtils.isEmpty(value)){
			return true;
		}
		for (String typeId : typeIds) {
			typeId = StringUtils.trimWhitespace(typeId);
			if (StringUtils.isEmpty(typeId)) {
				common.error("注解：" + ValidFromStatic.class.getName() + "value不能为空！");
			}

			//从缓存取
			List<IData<String, Object>> cacheData = StaticParamUtil.getCache(StaticParamConstants.TD_S_STATIC).getValue(StaticParamUtil.getListCacheKey(typeId));
			if (cacheData != null && !cacheData.isEmpty()) {
				for (IData<String, Object> data : cacheData) {
					if (value.equals(data.getString("DATA_ID"))) {
						return allow;
					}
				}
			}
		}
		return !allow;
	}

}
