<% if(isNotEmpty(root.packageName)){ %>
package ${root.packageName};
<% }%>

<% for(importPackageName in root.importPackageNames){ %>
import ${importPackageName};
<% } %>
<% var isRsp = root.sheetAtIndex == 1; %>

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import ${root.superClassName};

<% if(root.requireTranslate){%>
import org.walkframework.data.translate.Translate;
import org.walkframework.data.translate.TranslateEnable;
<% } %>

/**
 * @desc ${root.srcFileName}${isRsp ? '返回':'请求'}报文${root.desc}
 * @author ${root.author}
 * @date ${root.createTime}
 *
 * 本类由工具类RestfulGenerator自动生成
 */
<% if(root.requireTranslate){%>
@TranslateEnable
<% } %>
@ApiModel(description = "${root.srcFileName}${isRsp ? '返回':'请求'}报文${root.desc}") 
public class ${root.name} extends ${@org.walkframework.restful.generator.StringUtil.getShortClassName(root.superClassName)} {

	private static final long serialVersionUID = 1L;
<% var fieldMetas=root.fieldMetas; %>
<% for(fieldMeta in fieldMetas){ 
	var javaType = fieldMeta.javaType;
	var name = fieldMeta.name;
	var isReqiure = fieldMeta.isReqiure;
	var isComplex = fieldMeta.isComplex;
	var translatorName = fieldMeta.translatorName;
%>

<%if(isReqiure && isNotEmpty(fieldMeta.containAnt)){%>
	${fieldMeta.containAnt} 
	<% } %>
	<%if(isNotEmpty(translatorName)){%>
	@Translate(translator=${translatorName}.class) 
	<% } %>
	@ApiModelProperty(value = "${fieldMeta.desc}", required = ${isReqiure}, position = ${fieldMeta.position}0)
	private ${javaType} ${name};
<% } %>
<% for(fieldMeta in fieldMetas){ 
	var javaType=fieldMeta.javaType;
	var methodName=@fieldMeta.getRWMethodName();
	var name=fieldMeta.name;
%>

	<%if(root.showMethodsComment){%>
	/**
	 * ${fieldMeta.desc}
	 */
	<% } %>
	public void set${methodName}(${javaType} ${name}){
		this.${name} = ${name};
	}
	
	<%if(root.showMethodsComment){%>
	/**
	 * ${fieldMeta.desc}
	 */
	<% } %>
	public ${javaType} get${methodName}(){
		return ${name};
	}
<% } %>

}

<% if(root.showQuickRWMethods && isRsp) { %>
/********************************************  Quick Setter/Getter Methods	******************************************

<% for(fieldMeta in fieldMetas){ 
	var javaType=fieldMeta.javaType;
	var methodName=@fieldMeta.getRWMethodName();
	var columnName = @org.walkframework.restful.generator.StringUtil.camel2Underline(methodName);
%>
	 <% if(!fieldMeta.isComplex){ %>
rsp.set${methodName}(map.get${javaType}("${columnName}"));
	 <% } %>
<% } %>

********************************************  Quick Setter/Getter Methods	*******************************************/


<% } %>
