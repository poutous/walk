<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl"/>
<link rel="stylesheet" type="text/css" href="${request.contextPath}/component/resources/css/error.css">
<title>您访问的页面不存在...</title>
</head>
<body>
	<div class="error-wrapper">
		<div class="error-pin"></div>
		<div class="error-code">
			error <span>404</span>
		</div>
		<h1 class="error-heading">您访问的页面不存在...</h1>
		<p><a onclick="history.go(-1);" class="e-btn">返回上一页</a></p>
	</div>
</body>
</html>
