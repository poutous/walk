<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<title>静态参数缓存管理</title>
</head>
<body>
<div class="wrapper">
	<div class="w-panel">
		<!-- 查询条件区 -->
		<div class="w-panel-content">
			<form id="conditionForm" class="w-form">
				<div class="find_inbox_list3">
					<table>
						<tr>  
							<td class="字段"> 参数表名称：</td>
							<td>
							   <input id="cacheName" name="cacheName" type="text" class="w260" onkeyup="this.value=this.value.trim();"/>
							</td>
							<td colspan="10" align="right">
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
       		    <li><a onclick="clearCache();"><img src="${request.contextPath}/static/component/resources/images/icons/color/18/delete.png"/>批量清空</a></li>
       		    <li><a onclick="reloadStaticParam();"><img src="${request.contextPath}/static/component/resources/images/icons/color/18/quota.png"/>重新加载</a></li>
		    </ul>
		</div>
		
		<!-- 数据表 -->
		<div class="w-panel-content">
			<table id="dataGrid" class="easyui-datagrid" url="${request.contextPath}/console/staticparam/queryStaticParamCacheList" 
				data-options="queryParams:$.walk.getQueryParams('conditionForm'),selectOnCheck:false">
				<thead>
					<tr>
						<th data-options="field:'ck', checkbox:true, exportable:false"></th>
						<th data-options="field:'cacheName',hidden:true">缓存名称</th>
						<th data-options="field:'showCacheName',align:'left',tip:true,width:600">参数表名称</th>
						<th data-options="field:'cacheSize',width:400">元素数量</th>
						<th data-options="field:'oper',formatter:operRecord,width:150">操作区</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</div>
<script>
var baseUrl = $.walk.ctx + '/console/staticparam';
//页面初始化
$(function(){
});

//操作单条记录
function operRecord(val, row) {
	var operhtml = [];
	operhtml.push("<a onclick=\"manager('" + row.cacheName + "','" + row.showCacheName + "')\">元素管理</a>");
	return operhtml.join("");
}

//元素管理
function manager(cacheName, showCacheName){
	var url = $.walk.ctx + "/console/cache/go/cacheElementManager?cacheName=" + cacheName;
	$.walk.openUrlDialog(url, "元素管理[" + showCacheName + "]", 1100, 625, 5);
}

//批量清空
function clearCache(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何记录！");
		return false;
	}
	$.walk.confirm("确认清空吗？", function(ok) {
		if(ok){
		    $.ajax({
				url: baseUrl + "/clearCache",
				type: "post",
				data: {
					cacheNames: $.walk.checkeds2str(checkeds, 'cacheName')
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

//重新加载
function reloadStaticParam(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何记录！");
		return false;
	}
	$.walk.confirm("确认重新加载吗？", function(ok) {
		if(ok){
			$.walk.showLoading();
		    $.ajax({
				url: baseUrl + "/reloadStaticParam",
				type: "post",
				data: {
					cacheNames: $.walk.checkeds2str(checkeds, 'showCacheName')
				},
				success: function(msg) {
					$.walk.hideLoading();
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
