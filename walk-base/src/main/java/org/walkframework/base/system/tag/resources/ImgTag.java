package org.walkframework.base.system.tag.resources;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang3.StringUtils;

/**
 * 图片文件自定义标签 属性值参考http://www.w3school.com.cn/tags/tag_img.asp
 * 
 * @author shf675
 * 
 */
public class ImgTag extends BaseResourcesTag {
	
	private final static String RESOURCE_TYPE = "img";

	/**
	 * 规定图像的替代文本。
	 */
	private String alt;

	/**
	 * 规定显示图像的 URL。
	 */
	private String src;

	/**
	 * 不推荐使用。规定如何根据周围的文本来排列图像。
	 */
	private String align;

	/**
	 * 不推荐使用。定义图像周围的边框。
	 */
	private String border;
	
	/**
	 * 设置图像的宽度。
	 * 
	 */
	private String width;

	/**
	 * 定义图像的高度。
	 */
	private String height;

	/**
	 * 不推荐使用。定义图像左侧和右侧的空白。
	 * 
	 */
	private String hspace;

	/**
	 * 将图像定义为服务器端图像映射。
	 * 
	 */
	private String ismap;

	/**
	 * 指向包含长的图像描述文档的 URL。
	 */
	private String longdesc;

	/**
	 * 将图像定义为客户器端图像映射。
	 * 
	 */
	private String usemap;
	
	/**
	 * 不推荐使用。定义图像顶部和底部的空白。
	 * 
	 */
	private String vspace;
	

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
		appendIfNotEmpty(properties, "src", handleUrlVersion(src));
		appendIfNotEmpty(properties, "alt", alt);
		appendIfNotEmpty(properties, "align", align);
		appendIfNotEmpty(properties, "border", border);
		appendIfNotEmpty(properties, "width", width);
		appendIfNotEmpty(properties, "height", height);
		appendIfNotEmpty(properties, "hspace", hspace);
		appendIfNotEmpty(properties, "vspace", vspace);
		appendIfNotEmpty(properties, "longdesc", longdesc);
		appendIfNotEmpty(properties, "ismap", ismap);
		appendIfNotEmpty(properties, "usemap", usemap);
		
		//拼装扩展属性
		String extendProps = getExtendProps();
		if (StringUtils.isNotBlank(extendProps)) {
			properties.append(extendProps).append(" ");
		}

		//生成标签
		StringBuilder tag = new StringBuilder();
		tag.append("<img ").append(properties.toString()).append("/>");
		resource = tag.toString();
		
		//置入缓存
		getResourcesCache().put(resourceKey, resource);
		
		//写入浏览器
		out.print(resource);
	}


	public String getAlt() {
		return alt;
	}


	public void setAlt(String alt) {
		this.alt = alt;
	}


	public String getSrc() {
		return src;
	}


	public void setSrc(String src) {
		this.src = src;
	}


	public String getAlign() {
		return align;
	}


	public void setAlign(String align) {
		this.align = align;
	}


	public String getBorder() {
		return border;
	}


	public void setBorder(String border) {
		this.border = border;
	}


	public String getWidth() {
		return width;
	}


	public void setWidth(String width) {
		this.width = width;
	}


	public String getHeight() {
		return height;
	}


	public void setHeight(String height) {
		this.height = height;
	}


	public String getHspace() {
		return hspace;
	}


	public void setHspace(String hspace) {
		this.hspace = hspace;
	}


	public String getIsmap() {
		return ismap;
	}


	public void setIsmap(String ismap) {
		this.ismap = ismap;
	}


	public String getLongdesc() {
		return longdesc;
	}


	public void setLongdesc(String longdesc) {
		this.longdesc = longdesc;
	}


	public String getUsemap() {
		return usemap;
	}


	public void setUsemap(String usemap) {
		this.usemap = usemap;
	}


	public String getVspace() {
		return vspace;
	}


	public void setVspace(String vspace) {
		this.vspace = vspace;
	}
}
