package org.walkframework.base.system.tag.resources;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang3.StringUtils;

/**
 * css文件自定义标签 属性值参考http://www.w3school.com.cn/tags/tag_link.asp
 * 
 * @author shf675
 * 
 */
public class CssTag extends BaseResourcesTag {
	
	private final static String RESOURCE_TYPE = "css";

	/**
	 * 定义和用法：charset 属性规定被链接文档的字符编码方式。现代浏览器的默认字符集是 ISO-8859-1。 浏览器支持：几乎没有主流浏览器支持
	 * charset 属性。 HTML5 中不支持。
	 */
	private String charset;

	/**
	 * 定义和用法：href 属性规定被链接文档的位置（URL）。
	 */
	private String href;

	/**
	 * 定义和用法：规定被链接文档中文本的语言。 浏览器支持：几乎没有主流浏览器支持 hreflang 属性。
	 */
	private String hreflang;

	/**
	 * 定义和用法：规定被链接文档将被显示在什么设备上。用于为不同的媒介类型规定不同的样式。 浏览器支持：所有浏览器都支持值为
	 * "screen"、"print" 以及 "all" 的 media 属性。提示：在全屏模式中，Opera 也支持 "projection"
	 * 属性值。
	 */
	private String media;

	/**
	 * 定义和用法：规定当前文档与被链接文档之间的关系。 浏览器支持：只有 rel 属性的 "stylesheet"
	 * 值得到了所有浏览器的支持。其他值只得到了部分地支持。
	 */
	private String rel;

	/**
	 * 定义和用法：属性规定被链接文档与当前文档之间的关系。rev 属性与 rel 属性是相反的。 浏览器支持：几乎没有浏览器支持 rev 属性。
	 * 
	 * HTML5 中不支持。
	 */
	private String rev;

	/**
	 * 定义和用法：属性规定被链接资源的尺寸。只有当被链接资源是图标时 (rel="icon")，才能使用该属性。该属性可接受多个值。值由空格分隔。
	 * 浏览器支持：几乎没有浏览器支持 rev 属性。
	 * 
	 * sizes 属性是 HTML5 中的新属性。
	 */
	private String sizes;

	/**
	 * 定义和用法：target 属性规定在哪个窗口或框架中加载被链接文档。 浏览器支持：几乎没有浏览器支持 target 属性。 
	 * HTML5 中不支持。
	 */
	private String target;

	/**
	 * 定义和用法：规定被链接文档的 MIME 类型。该属性最常见的 MIME 类型是 "text/css"，该类型描述样式表。
	 * 
	 */
	private String type;

	/**
	 * 生成标签
	 * 
	 * @throws IOException
	 */
	public void generateTag() throws IOException {
		JspWriter out = super.getJspContext().getOut();
		
		//尝试从缓存中取
		String resourceKey = getResourceKey(RESOURCE_TYPE, href);
		String resource = getResourcesCache().get(resourceKey);
		if(StringUtils.isNotBlank(resource)){
			//写入浏览器
			out.print(resource);
			return;
		}
		
		//拼装属性
		StringBuilder properties = new StringBuilder();
		appendIfNotEmpty(properties, "type", getValue(type, "text/css"));
		appendIfNotEmpty(properties, "rel", getValue(type, "stylesheet"));
		appendIfNotEmpty(properties, "href", handleUrlVersion(href));
		appendIfNotEmpty(properties, "hreflang", hreflang);
		appendIfNotEmpty(properties, "media", media);
		appendIfNotEmpty(properties, "rev", rev);
		appendIfNotEmpty(properties, "sizes", sizes);
		appendIfNotEmpty(properties, "target", target);
		appendIfNotEmpty(properties, "hreflang", hreflang);
		
		//拼装扩展属性
		String extendProps = getExtendProps();
		if (StringUtils.isNotBlank(extendProps)) {
			properties.append(extendProps).append(" ");
		}

		//生成标签
		StringBuilder tag = new StringBuilder();
		tag.append("<link ").append(properties.toString()).append("/>");
		resource = tag.toString();
		
		//置入缓存
		getResourcesCache().put(resourceKey, resource);
		
		//写入浏览器
		out.print(resource);
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getHreflang() {
		return hreflang;
	}

	public void setHreflang(String hreflang) {
		this.hreflang = hreflang;
	}

	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public String getSizes() {
		return sizes;
	}

	public void setSizes(String sizes) {
		this.sizes = sizes;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
