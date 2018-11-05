package org.walkframework.restful.controller;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import io.swagger.annotations.Api;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import springfox.documentation.RequestHandler;
import springfox.documentation.RequestHandlerKey;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.spring.web.scanners.ApiDescriptionLookup;

import com.google.common.collect.Maps;

/**
 * Swagger V2  解决@Api|@ApiOperation  设置position不能排序问题   
 * 1.spring mvc 加入本控制器 
 * 2.springfox.js 需要做出如下改动
 *  $.get(window.springfox.baseUrl()+"/Swagger2/getApiSorter",function(data){ //START
 *  data = eval('(' + data + ')'); 
 *  var operationsSorterMap = data.operationsSorter;
 *  var apisSorterMap = data.apisSorter;
 *  window.springfox.uiConfig(function(data) {
 *		 window.swaggerUi = new SwaggerUi({ 
 *	      apisSorter: function (e, t) { 
 *	    	 try {
 *				var a = apisSorterMap[e.tag];
 *				var b = apisSorterMap[t.tag];
 *				if (a > b)
 *					return 1;
 *				if (a < b)
 *					return -1;
 *			} catch (e) { }
 *			return e.name.localeCompare(t.name); 
 *	      },
 *	      operationsSorter: function (e, t) { 
 *	    	try {
 *				var a = apisSorterMap[e.path];
 *				var b = apisSorterMap[t.path];
 *				if (a > b)
 *					return 1;
 *				if (a < b)
 *					return -1;
 *			} catch (e) { }
 *			return e.method.localeCompare(t.method);
 *	      },  
 *     //其他...  
 *  }, "text");//END
 *  
 *  @author wangxin
 */
@Controller
@ApiIgnore
@SuppressWarnings("all")
public class ApisSorterController implements ApplicationListener<ContextRefreshedEvent> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String apisSorterJson;

	@Autowired
	private ApiDescriptionLookup apiDescriptionLookup;

	@Autowired
	private JsonSerializer jsonSerializer;

	@Autowired
	private WebMvcRequestHandlerProvider provider;

	@ApiIgnore
	@ResponseBody
	@RequestMapping(value = "swagger/v2/getApiSorter", method = RequestMethod.GET)
	public String getApisSorterJson() {
		return apisSorterJson;
	}

	private void init() {
		try {
			apisSorterJson = String.format("{'apisSorter':%s,'operationsSorter':%s}", getApisSorter(), getOperationSorter());
		} catch (Throwable e) {
			log.error("Swagger Apis排序插件初始化失败！", e);
		}
	}

	private String mapToJson(Map<String, Integer> map) {
		if (map == null || map.size() == 0)
			return null;
		return jsonSerializer.toJson(map).value();
	}

	private String getOperationSorter() throws Exception {
		Map<String, Integer> operationsSorterMap = Maps.newHashMap();
		Field field = ApiDescriptionLookup.class.getDeclaredField("cache");
		field.setAccessible(true);
		Map<RequestHandlerKey, ApiDescription> map = (Map<RequestHandlerKey, ApiDescription>) field.get(apiDescriptionLookup);
		Collection<ApiDescription> values = map.values();
		for (ApiDescription apiDescription : values) {
			String path = apiDescription.getPath();
			int position = apiDescription.getOperations().get(0).getPosition();
			operationsSorterMap.put(path, position);
		}
		return mapToJson(operationsSorterMap);
	}

	private String getApisSorter() throws Exception {
		Map<String, Integer> apisSorterMap = Maps.newHashMap();
		List<RequestHandler> requestHandlers = provider.requestHandlers();
		for (RequestHandler requestHandler : requestHandlers) {
			Class<?> controllerClass = requestHandler.getHandlerMethod().getBeanType();
			Api apiAnnotation = findAnnotation(controllerClass, Api.class);
			if (null != apiAnnotation) {
				apisSorterMap.put(apiAnnotation.tags()[0], apiAnnotation.position());
			}
		}
		return mapToJson(apisSorterMap);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}

}
