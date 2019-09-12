<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<title>队列管理</title>
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
							<td class="字段"> 队列名称：</td>
							<td>
							   <input id="queueName" name="queueName" type="text" class="w260" />
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
       		    <li><a onclick="clearQueue();"><img src="${request.contextPath}/component/resources/images/icons/color/18/delete.png"/>批量清空</a></li>
		    </ul>
		</div>
		
		<!-- 数据表 -->
		<div class="w-panel-content">
			<table id="dataGrid" class="easyui-datagrid" url="${request.contextPath}/console/queue/queryQueueList" 
				data-options="queryParams:$.walk.getQueryParams('conditionForm'),selectOnCheck:false">
				<thead>
					<tr>
						<th data-options="field:'ck', checkbox:true, exportable:false"></th>
						<th data-options="field:'queueName',align:'left',tip:true,width:600">队列名称</th>
						<th data-options="field:'queueSize',width:400">元素数量</th>
						<th data-options="field:'oper',formatter:operRecord,width:150">操作区</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</div>
<script>
var baseUrl = $.walk.ctx + '/console/queue';
//页面初始化
$(function(){
});

//操作单条记录
function operRecord(val, row) {
	var operhtml = [];
	operhtml.push("<a onclick=\"manager('" + row.queueName + "')\">元素管理</a>");
	return operhtml.join("");
}

//队列元素管理
function manager(queueName){
	var url = baseUrl + "/go/queueElementManager?queueName=" + queueName;
	$.walk.openUrlDialog(url, "队列元素管理["+queueName+"]", 800, 590, 20, "auto");
}

//批量清空
function clearQueue(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何记录！");
		return false;
	}
	$.walk.confirm("确认清空吗？", function(ok) {
		if(ok){
		    $.ajax({
				url: baseUrl + "/clearQueue",
				type: "post",
				data: {
					queueNames: $.walk.checkeds2str(checkeds, 'queueName')
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
