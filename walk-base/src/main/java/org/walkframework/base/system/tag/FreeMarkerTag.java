package org.walkframework.base.system.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.walkframework.base.system.freemarker.FreemarkerStaticModels;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.utils.FreeMarkerUtil;

/**
 * 通用FreeMarker标签
 * 
 * @author shf675
 */
public class FreeMarkerTag extends BaseTag {

	private String path;
	
	private String var;

	private Object value;
	
	public void doTag() throws IOException {
		super.doTag();

		JspWriter out = super.getJspContext().getOut();
		out.print(generateTag());
	}

	public String generateTag() {
		HttpServletRequest request = (HttpServletRequest)((PageContext)getJspContext()).getRequest();
		
		Map<String, Object> dataModel = new HashMap<String, Object>();
		//设置request对象
		dataModel.put("request", request);
		
		//设置静态方法访问
		dataModel.putAll(SpringContextHolder.getBean(FreemarkerStaticModels.class));
		
		//设置自定义参数
		dataModel.put(getVar(), getValue());
		return FreeMarkerUtil.process(getPath(), dataModel, request);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
