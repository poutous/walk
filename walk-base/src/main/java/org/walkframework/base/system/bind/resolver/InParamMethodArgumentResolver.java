package org.walkframework.base.system.bind.resolver;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.walkframework.base.system.annotation.DataImport;
import org.walkframework.base.tools.utils.FileUtil;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.InParam;
import org.walkframework.data.util.InParamHelper;


/**
 * InParam参数解决器
 *
 * @author liuqf5
 */
public class InParamMethodArgumentResolver extends BaseMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override 
	public boolean supportsParameter(MethodParameter parameter) {
		return InParam.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		InParam<String, Object> inParam = new InParam<String, Object>();
		Iterator<String> fields = webRequest.getParameterNames();
		while (fields.hasNext()) {
			String field = fields.next();
			String[] values = webRequest.getParameterValues(field);
			if (values.length > 1) {
				inParam.put(field, values);
			} else {
				//空字符串认为是null。防止mybatis将空字符串认为0的问题
				if("".equals(values[0])){
					inParam.put(field, null);
				} else {
					inParam.put(field, values[0]);
				}
			}
		}
		common.putFilterRules(inParam);
		
		//处理导入文件
		resolveDataImport(parameter, webRequest, inParam);
		return inParam;
	}
	
	/**
	 * 处理导入文件
	 * 
	 * @param parameter
	 * @param webRequest
	 * @param inParam
	 */
	public void resolveDataImport(MethodParameter parameter, NativeWebRequest webRequest, InParam<String, Object> inParam){
		Object nativeRequest = webRequest.getNativeRequest();
		if(!(nativeRequest instanceof MultipartHttpServletRequest)){
			return ;
		}
		
		Annotation[] annotations = parameter.getMethodAnnotations();
		if(annotations == null || annotations.length == 0){
			return ;
		}
		
		MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest)nativeRequest;
		for (Annotation annotation : annotations) {
			if(DataImport.class.equals(annotation.annotationType())){
				DataImport dataImport = (DataImport)annotation;
				String fileName = dataImport.fileName();
				String xml = dataImport.xml();
				Class<?> type = dataImport.type();
				
				Assert.isTrue(!StringUtils.isEmpty(fileName), "@DataImport: fileName can not be empty!");
				Assert.isTrue(!StringUtils.isEmpty(xml), "@DataImport: xml can not be empty!");
				Assert.isTrue(type != null, "@DataImport: type cannot be set to null!");
				
				if(Map.class.isAssignableFrom(type)){
					type = DataMap.class;
				}
				InParamHelper.putFileList(inParam, fileName, FileUtil.getImportDataset(mRequest.getFile(fileName), xml, type));
			}
		}
	}
}
