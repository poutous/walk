<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<title>流程导航</title>
</head>
<body>
	<div>
		<!-- 流程图 -->
		<div style="border-bottom: 1px solid #dcdee2;">
			<iframe id="imageIframe" width="100%" height="190" src="${request.contextPath}/component/diagram-viewer/index.html?processDefinitionId=${nodeInfo.procDefId}&processInstanceId=${orderInfo.procInstId}" frameborder="0" scrolling="no" onload="imageDeal(this)"></iframe>
		</div>
		
		<!-- 流程节点页面 -->
		<iframe id="contentIframe" width="100%" height="100%" src="${request.contextPath}/${nodeInfo.pageUrl}" frameborder="0" scrolling="no" onload="iframeResizeInterval(this)"></iframe>
	</div>
</body>
<script>
var baseUrl = $.walk.ctx + '/process';

//页面初始化
$(function(){
});

//设置流程图尺寸
function imageDeal(frame){
	var body = $(frame.contentWindow.document.body);
	body.css("zoom", "0.65");
	body.css("background", "#ffffff");
}

//iframe自适应高度
function iframeResizeInterval(frame){
	setFrameHeight(frame);
	setInterval(function(){
		setFrameHeight(frame);
	}, 1000);
}

//设置frame高度
function setFrameHeight(frame){
	$(frame).height(getFrameContentHeight(frame));
}


//获取Iframe子页面高度
function getFrameContentHeight(frame){
	var body = $(frame.contentWindow.document.body);
	var childrens = body.children();
	if(childrens.length > 1){
		var totalHeight = 0;
		childrens.each(function(){
			totalHeight += $(this).height();
		});
		return totalHeight;
	} else {
		return childrens.height();
	}
}
</script>
</html>