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
				<input id="queueName" name="queueName" type="hidden" value="${queueName}" />
			</form>
		</div>
		<!-- 工具栏 -->
		<div class="w-panel-toolbar">
       		<ul>
       		    <li><a onclick="openOfferDialog();"><img src="${request.contextPath}/component/resources/images/icons/color/18/new.png"/>新元素入队</a></li>
       		    <li><a onclick="removeQueueElement();"><img src="${request.contextPath}/component/resources/images/icons/color/18/delete.png"/>批量删除</a></li>
		    </ul>
		</div>
		
		<!-- 数据表 -->
		<div class="w-panel-content">
			<table id="dataGrid" class="easyui-datagrid" 
				   url="${request.contextPath}/console/queue/queryQueueElementList"
				   data-options="queryParams:$.walk.getQueryParams('conditionForm'),selectOnCheck:false,frozenAlign:'right'">
				<thead data-options="frozen:true">
					<tr>
						<th data-options="field:'oper', width:150, halign:'center', formatter:operRecord, exportable:false">操作区</th>
					</tr>
				</thead>
				<thead>
					<tr>
						<th data-options="field:'ck', checkbox:true, exportable:false"></th>
						<th data-options="field:'queueIndex',align:'left',width:300,hidden:true">队列元素索引</th>
						<th data-options="field:'queueContent',align:'left',width:620,tip:true">队列元素</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	
	<!-- 元素值显示区 -->
	<div id="queueValueWindow" style="display:none;border: 3px solid #aaaaaa;">
		<div>
			<div id="showModeSel" class="w-cr-box" style="padding:10px;float:left">
				<input type="radio" name="showMode" val="text" style="cursor: pointer;" onclick="showModeSwitch($(this).attr('val'));" checked="checked"/>text
				<input type="radio" name="showMode" val="json" style="cursor: pointer;" onclick="showModeSwitch($(this).attr('val'));"/>json
				<input type="radio" name="showMode" val="hex" style="cursor: pointer;" onclick="showModeSwitch($(this).attr('val'));"/>hex
			</div>
			<div style="padding:10px;float:left;width:390px;text-align:left;word-break: break-all;word-wrap: break-word;">
				值类型：<span id="valueType"></span>
			</div>
			<div style="clear: both;"></div>
		</div>
		<div style="border-top:1px solid #ddd">
			<textarea id="valueContent" style="width:100%;height:390px;resize: none;border:none;" readonly="readonly"></textarea>
		</div>
	</div>
	
	<!-- 新元素显示区 -->
	<div id="eleWindow" style="display:none;border: 3px solid #aaaaaa;">
		<div>
			<div id="eleModeRel" class="w-cr-box" style="padding:10px;float:left">
				元素类型：
				<input type="radio" name="eleMode" val="text" style="cursor: pointer;" checked="checked"/>string
				<input type="radio" name="eleMode" val="hex" style="cursor: pointer;"/>hex
			</div>
			<div style="float:right;padding:10px;">
				<a id="saveBtn" href="javascript:void(0)" class="w-btn w-btn-blue-m" onclick="offerQueueElement();">确认</a>
			</div>
			<div style="clear: both;"></div>
		</div>
		<div style="border-top:1px solid #ddd">
			<textarea id="eleValue" style="width:100%;height:390px;resize: none;border:none;" placeholder="请输入元素值..."></textarea>
		</div>
	</div>
</div>
<script>
var baseUrl = $.walk.ctx + '/console/queue';
//页面初始化
$(function(){
});

//显示类型切换
function showModeSwitch(val){
	$('#valueContent').val("加载中...");
	var selectedRow = $("#dataGrid").datagrid("getSelected");
	viewQueueElementValue(selectedRow.queueIndex);
}

//操作单条记录
function operRecord(val, row) {
	var operhtml = [];
	operhtml.push("<a onclick=\"viewQueueElementValue('" + row.queueIndex + "')\">查看</a>");
	return operhtml.join("");
}

//查看元素值
function viewQueueElementValue(queueIndex){
	if($("#queueValueWindow").is(":hidden")){
		seajs.use('$blockui', function(){
			$('#valueContent').val("加载中...");
			$.blockUI({message: $('#queueValueWindow'), css:{top:20,left:'12%',width:600,height:380,cursor:'default',border:'none'}});
			$(".blockOverlay").click($.unblockUI);
		});
	}
	
	setTimeout(function(){
		$.ajax({
			url: baseUrl + "/viewQueueElementValue",
			type: "post",
			data: {
				queueName: $("#queueName").val(),
				showMode: $("#showModeSel").find("input[name='showMode']:checked").attr("val"),
				queueIndex: queueIndex
			},
			success: function(resp) {
				$('#valueContent').val(resp.value);
				$('#valueType').text(resp.type);
			}
		});
	}, 150);
}

//新元素入队弹出框
function openOfferDialog(){
	seajs.use('$blockui', function(){
		$('#eleContent').val("");
		$.blockUI({message: $('#eleWindow'), css:{top:20,left:'12%',width:600,height:380,cursor:'default',border:'none'}});
		$(".blockOverlay").click($.unblockUI);
	});
}

//新元素入队
function offerQueueElement() {
	$.walk.confirm("确认入队吗？", function(ok) {
		if(ok){
		    $.ajax({
				url: baseUrl + "/offerQueueElement",
				type: "post",
				data: {
					queueName: $("#queueName").val(),
					eleValue: $("#eleValue").val(),
					eleMode: $("#eleModeRel").find("input[name='eleMode']:checked").attr("val")
				},
				success: function(msg) {
					$.walk.alert(msg, function(){
						window.location.reload();
					});
				}
			});
		}
	});
}

//批量删除
function removeQueueElement(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何记录！");
		return false;
	}
	$.walk.confirm("确认删除吗？", function(ok) {
		if(ok){
		    $.ajax({
				url: baseUrl + "/removeQueueElement",
				type: "post",
				data: {
					queueName: $("#queueName").val(),
					queueIndexs: $.walk.checkeds2str(checkeds, 'queueIndex')
				},
				success: function(msg) {
					$.walk.alert(msg, function(){
						$.walk.queryList('dataGrid', 'conditionForm');
					});
				}
			});
		}
	});
}
</script>
</body>
</html>
