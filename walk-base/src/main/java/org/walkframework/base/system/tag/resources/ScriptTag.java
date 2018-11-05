package org.walkframework.base.system.tag.resources;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang3.StringUtils;

/**
 * script文件自定义标签
 * 
 * 属性值参考http://www.w3school.com.cn/tags/tag_script.asp
 * 
 * @author shf675
 * 
 */
public class ScriptTag extends BaseResourcesTag {
	
	private final static String RESOURCE_TYPE = "script";

	/**
	 * 指示脚本的 MIME 类型。必选的属性
	 */
	private String type;

	/**
	 * 规定异步执行脚本（仅适用于外部脚本）。
	 */
	private String async;

	/**
	 * 规定在外部脚本文件中使用的字符编码。
	 */
	private String charset;

	/**
	 * 规定是否对脚本执行进行延迟，直到页面加载为止。
	 * 浏览器支持：只有 Internet Explorer 支持 defer 属性。
	 */
	private String defer;

	/**
	 * 规定外部脚本文件的 URL。
	 */
	private String src;

	/**
	 * 规定是否保留代码中的空白。
	 */
	private String xmlSpace;

	/**
	 * 生成标签
	 * 
	 * @throws IOException
	 */
	public void generateTag() throws IOException {
		JspWriter out = super.getJspContext().getOut();
		
		//尝试从缓存中取
		String resourceKey = getResourceKey(RESOURCE_TYPE, src);
		String resource = getResourcesCache().get(resourceKey);
		if(StringUtils.isNotBlank(resource)){
			//写入浏览器
			out.print(resource);
			return;
		}
		
		//拼装属性
		StringBuilder properties = new StringBuilder();
		appendIfNotEmpty(properties, "type", getValue(type, "text/javascript"));
		appendIfNotEmpty(properties, "src", handleUrlVersion(src));
		appendIfNotEmpty(properties, "async", async);
		appendIfNotEmpty(properties, "charset", charset);
		appendIfNotEmpty(properties, "defer", defer);
		appendIfNotEmpty(properties, "xmlSpace", xmlSpace);
		
		//拼装扩展属性
		String extendProps = getExtendProps();
		if (StringUtils.isNotBlank(extendProps)) {
			properties.append(extendProps).append(" ");
		}

		//生成标签
		StringBuilder tag = new StringBuilder();
		tag.append("<script ").append(properties.toString()).append("></script>");
		resource = tag.toString();
		
		//置入缓存
		getResourcesCache().put(resourceKey, resource);
		
		//写入浏览器
		out.print(resource);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAsync() {
		return async;
	}

	public void setAsync(String async) {
		this.async = async;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getDefer() {
		return defer;
	}

	public void setDefer(String defer) {
		this.defer = defer;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getXmlSpace() {
		return xmlSpace;
	}

	public void setXmlSpace(String xmlSpace) {
		this.xmlSpace = xmlSpace;
	}
}
