<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<title>WALK开发平台</title>
<link type="text/css" rel="stylesheet" href="${request.contextPath}/static/component/resources/css/nav.css"/>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/nav/tab.js"></script>
<script type="text/javascript" src="${request.contextPath}/static/component/resources/scripts/nav/nav.js"></script>
</head>
<body>
<div class="container">
	<!-- 顶部导航区域 -->
	<div class="nav-top" id="nav-top" >
		<div class="nav-top-info">
   			<div class="left">欢迎来到WALK开发平台</div>
   			<div class="right">
   				<span>部门：${SecurityUtils.getSubject().principal.departName}</span>
   				<span>员工：${SecurityUtils.getSubject().principal.staffName}</span>
   				<span class="pointer"><img src="${request.contextPath}/static/component/resources/images/nav/notice.png"/>公告</span>
   				<span class="pointer" onclick="window.open('https://www.kancloud.cn/shf675/walk')">帮助文档</span>
   				<span class="pointer blue" id="logout">[系统退出]</span>
   			</div>
   		</div>
   		<div class="nav-top-menu">
   			<div class="left">
   				<span class="logo">WALK</span>
				<span class="text">WALK开发平台</span>
   			</div>
			<div class="right" id="nav-top-menu">
				<span class="menu" targetModule="develop"><span class="menuName active">本地开发</span></span>
				<span class="menu" targetModule="documentation"><span class="menuName">文档示例</span></span>
			</div>
   		</div>
	</div>
	
	<!-- 中间区域 -->
	<div class="nav-mid" id="nav-mid">
		<!-- 左侧导航区域 -->
		<div class="nav-mid-left" id="nav-mid-left">
			<div class="search">
			    <input id="quickSearch" type="text" placeholder="菜单快捷搜索..."/>
			    <img id="quickSearchBtn" src="${request.contextPath}/static/component/resources/images/nav/search.png" />
			</div>
			
			<!-- 本地开发 -->
			<div class="menuModule" id="develop" devUrl="${devUrl}"></div>
			
			<!-- 文档示例 -->
			<div class="menuModule" id="documentation" style="display: none">
				<#include "../../common/nav/document-nav.ftl">
			</div>
			
			<!-- 菜单快捷搜索结果 -->
			<div class="menuModule" id="searchMenuModule" style="display: none">
				<div class="menuPart">
				    <div class="title">
					    <div class="icon"><img src="${request.contextPath}/static/component/resources/images/nav/title_0.png"/></div>
				        <span>搜索结果</span>
				        <div class="arrow down"></div>
				    </div>
				    <div class="menuList"></div>
				</div>
			</div>
		</div>
		
		<!-- 右侧内容区域 -->
		<div class="nav-mid-right" id="nav-mid-right">
			<div class="tabs" id="tabs">
		        <div title="<span id='TITLE_HOME_PAGE'>首页</span>" class="home">
					欢迎进入WALK开发平台
		        </div>
		    </div>
		</div>
	</div>
	
	<!-- 底部区域 -->
	<div class="nav-bottom">
		<p class="flinks">
			<a target="_blank" href="javascript:">企业法人营业执照 </a>|
			<a target="_blank" href="javascript:">基础电信业务经营许可证 </a>|
			<a target="_blank" href="javascript:">增值电信业务经营许可证 </a>|
			<a target="_blank" href="javascript:">网络文化经营许可证 </a>
		</p>
		<p>Copyright&copy; 1999-2011 &nbsp; &nbsp; XXXXXX &nbsp; 版权所有</p>
		<p>中华人民共和国增值电信业务经营许可证 经营许可证编号：A2.B1.B2-20090003</p>
	</div>
</div>
</body>
</html>
