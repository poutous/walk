<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<link type="text/css" rel="stylesheet" href="${request.contextPath}/component/resources/css/login.css"/>
<title>WALK开发平台</title>
</head>
<body>
<div class="login-container">
	<div class="site-nav">
		<div class="site-nav-bd">
			<div class="sit-l f2 left">WALK开发平台</div>
		</div>
	</div>
	<div class="login-box">
		<div class="login-aside">
			<div class="o-box-up"></div>
			<div class="o-box-down">
				<div class="error-box">${error}</div>
				<form class="loginForm" action="${request.contextPath}/formlogin" method="POST">
					<div class="fm-item">
						<label for="logonId" class="form-label">登录</label>
						<input type="text" class="i-text i-user" placeholder="账号" name="username" autocomplete="off"/>
						<input type="password" class="i-text i-pwd" placeholder="密码" name="password"/>
					</div>
					<div class="fm-item">
						<input type="button" value="登 录" class="btn-login f2"/>
					</div>
				</form>
			</div>
		</div>
	</div>
	<div style="clear: both;"></div>
	<div class="footer">
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
<script type="text/javascript">
//页面初始化
$(function(){
	//注册登录按钮点击事件
	$(".btn-login").click(function(){
		login();
	});
	
	//注册回车事件
	$(document).keydown(function(event){
		if(event.keyCode == 13){
			login();
		}
	});
	
	//高度自适应
	$(".login-box").height(($(".login-box").width()/2.833)+10);
});	
	
//登录
function login(){
	var usrname = $.trim($(".i-user").val());
	if(!usrname){
		$(".error-box").html("请输入账号。");
		return false;
	}
	var pwd = $(".i-pwd").val();
	if(!pwd){
		$(".error-box").html("请输入密码。");
		return false;
	}
	$(".btn-login").val("登录中...");
	$('.loginForm').submit();
}
</script>
</html>
