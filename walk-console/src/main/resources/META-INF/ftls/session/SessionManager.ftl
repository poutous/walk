<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<title>缓存元素管理</title>
<style>
.btn-disable{cursor: not-allowed;}
</style>
</head>
<body>
<div class="wrapper">
	<div class="w-panel">
		<!-- 查询条件区 -->
		<div class="w-panel-content">
			<form id="conditionForm" class="w-form">
				<input id="cacheName" name="cacheName" type="hidden" value="${cacheName}" />
				<div class="find_inbox_list3">
					<table>
						<tr>  
							<td class="字段"> 账号： </td>
							<td>
							   <input id="account" name="account" type="text" class="w260" onkeyup="this.value=this.value.trim();"/>
							</td>
							<td class="字段"> 会话ID：</td>
							<td>
							   <input id="sessionId" name="sessionId" type="text" class="w260" onkeyup="this.value=this.value.trim();"/>
							</td>
							<td colspan="10" align="center">
								<a id="queryBtn" class="w-a-btn orange EnterPress" onclick="$.walk.queryList('dataGrid', 'conditionForm');" href="javascript:void(0)">查询</a>
							</td>
						</tr>
					</table>
				</div>
			</form>
		</div>
		
		<!-- 工具栏 -->
		<div class="w-panel-toolbar">
       		<ul>
       		    <li><a onclick="forceLogout();"><img src="${request.contextPath}/component/resources/images/icons/color/18/delete.png"/>强制下线</a></li>
       		    <li><a onclick="setSessionTimeout();"><img src="${request.contextPath}/component/resources/images/icons/color/18/set.png"/>设置会话时长</a></li>
		    </ul>
		</div>
		
		<!-- 数据表 -->
		<div class="w-panel-content">
			<table id="dataGrid" class="easyui-datagrid"
				   url="${request.contextPath}/console/session/querySessionList"
				   data-options="queryParams:$.walk.getQueryParams('conditionForm'),selectOnCheck:false,frozenAlign:'right'">
				<thead>
					<tr>
						<th data-options="field:'ck', checkbox:true, exportable:false"></th>
						<th data-options="field:'account',width:150">账号</th>
						<th data-options="field:'host',width:120">请求服务器</th>
						<th data-options="field:'lastAccessTime',width:150">最近访问时间</th>
						<th data-options="field:'startTimestamp',width:150">会话创建时间</th>
						<th data-options="field:'sessionTTL',width:150,formatter:function(val){return val ? parseInt(val/1000):'';}">剩余会话时长(秒)</th>
						<th data-options="field:'timeout',width:150,formatter:function(val){return val ? parseInt(val/1000):'';}">超时时长(秒)</th>
						<th data-options="field:'sessionId',width:400,tip:true">会话ID</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</div>
<script>
var baseUrl = $.walk.ctx + '/console/session';
//页面初始化
$(function(){
});

//设置会话时长
function setSessionTimeout(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何记录！");
		return false;
	}
	$.walk.prompt("请输入会话时长，单位秒。", function(timeout) {
		if(timeout){
			if(!$.isNumeric(timeout)){
				$.walk.alert("请输入数字，单位秒！");
				return false;
			}
		    $.ajax({
				url: baseUrl + "/setSessionTimeout",
				type: "post",
				data: {
					sessionIds: $.walk.checkeds2str(checkeds, 'sessionId'),
					timeout: timeout
				},
				success: function(msg) {
					$.walk.alert(msg, function(){
						$("#queryBtn").click();
					});
				}
			});
		}
	});
}

//强制下线
function forceLogout(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何记录！");
		return false;
	}
	$.walk.confirm("确认强制下线吗？", function(ok) {
		if(ok){
		    $.ajax({
				url: baseUrl + "/forceLogout",
				type: "post",
				data: {
					sessionIds: $.walk.checkeds2str(checkeds, 'sessionId')
				},
				success: function(msg) {
					$.walk.alert(msg, function(){
						$("#queryBtn").click();
					});
				}
			});
		}
	});
}
</script>
</body>
</html>
