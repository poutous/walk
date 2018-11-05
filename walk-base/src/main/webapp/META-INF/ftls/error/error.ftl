<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl"/>
<link rel="stylesheet" type="text/css" href="${request.contextPath}/component/resources/css/error.css">
<title>错误</title>
</head>
<body>
	<div class="error-wrapper">
		<div class="error-pin"></div>
		<div class="error-code">
			error
			<span>
	            500
			</span>
            <br/>
		</div>
		<h1 id="exception" class="error-heading">
			<#if exception??>
			    ${common.getErrorMessage(exception)}
			<#else>
				${common.getErrorMessage(request.getAttribute("javax.servlet.error.exception"))}
			</#if>
            <br/>
		</h1>
		<span class="btn-span">
			<#if SpringPropertyHolder.getContextProperty('productMode') != 'true'>
			    <a id="linkview" onclick="_clickStackCtrl();" class="e-btn">显示错误</a>
			</#if>
			<a id="linkback" onclick="history.go(-1);" class="e-btn">返回上一页</a>
		</span>
		<#if SpringPropertyHolder.getContextProperty('productMode') != 'true'>
			<!-- 错误详情 -->
			<span id="exceptionTrace" class="exceptionTrace">
				${exceptionTrace}
				<#if exceptionTrace??>
				<#else>
					<#if exception??>
		                ${common.getErrorInfo(common.getStackTrace(exception))}
					<#else>
						${common.getErrorInfo(common.getStackTrace(request.getAttribute("javax.servlet.error.exception")))}
					</#if>
				</#if>
			</span>
		</#if>
	</div>
</body>
<script type="text/javascript">
function _clickStackCtrl() {
	var stackarea = document.getElementById("exceptionTrace");
	var linkview = document.getElementById("linkview");
    if (stackarea.style.display == "inline" || stackarea.style.display == "block") {
    	stackarea.style.display = "none";
    	linkview.innerHTML = "<span>显示错误</span>";
    } else {
    	stackarea.style.display = "block";
    	linkview.innerHTML = "<span>隐藏错误</span>";
	}
}
</script>
</html>