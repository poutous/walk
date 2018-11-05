<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<title>发布/订阅管理</title>
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
							<td class="字段"> 频道名称：</td>
							<td>
							   <input id="channel" name="channel" type="text" class="w260" />
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
       		    <li><a onclick="openMessageDialog();"><img src="${request.contextPath}/component/resources/images/icons/color/18/publish.png"/>发布消息</a></li>
		    </ul>
		</div>
		
		<!-- 数据表 -->
		<div class="w-panel-content">
			<table id="dataGrid" class="easyui-datagrid" url="${request.contextPath}/console/pubsub/queryChannelList" 
				data-options="queryParams:$.walk.getQueryParams('conditionForm'),selectOnCheck:false,frozenAlign:'right'">
				<thead>
					<tr>
						<th data-options="field:'ck', checkbox:true, exportable:false"></th>
						<th data-options="field:'channel',align:'left',tip:true,width:600">频道</th>
						<th data-options="field:'subscriberNum',width:450">订阅者数量</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	
	<!-- 消息发布显示区 -->
	<div id="msgWindow" style="display:none;border: 3px solid #aaaaaa;">
		<div>
			<div id="msgModeRel" class="w-cr-box" style="padding:10px;float:left">
				消息类型：
				<input type="radio" name="msgMode" val="text" style="cursor: pointer;" checked="checked"/>string
				<input type="radio" name="msgMode" val="hex" style="cursor: pointer;"/>hex
			</div>
			<div style="float:right;padding:10px;">
				<a id="saveBtn" href="javascript:void(0)" class="w-btn w-btn-blue-m" onclick="publishMessage();">发布消息</a>
			</div>
			<div style="clear: both;"></div>
		</div>
		<div style="border-top:1px solid #ddd">
			<textarea id="msgContent" style="width:100%;height:390px;resize: none;border:none;" placeholder="请输入消息内容..."></textarea>
		</div>
	</div>
</div>
<script>
var baseUrl = $.walk.ctx + '/console/pubsub';
//页面初始化
$(function(){
});

//发布消息弹框
function openMessageDialog(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何记录！");
		return false;
	}
	seajs.use('$blockui', function(){
		$('#msgContent').val("");
		$.blockUI({message: $('#msgWindow'), css:{top:20,left:'20%',width:600,height:380,cursor:'default',border:'none'}});
		$(".blockOverlay").click($.unblockUI);
	});
}

//发布消息
function publishMessage(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何频道！");
		return false;
	}
	$.walk.confirm("确认向这些频道发布消息吗？", function(ok) {
		if(ok){
		    $.ajax({
				url: baseUrl + "/publishMessage",
				type: "post",
				data: {
					channels: $.walk.checkeds2str(checkeds, 'channel'),
					message: $("#msgContent").val(),
					msgMode: $("#msgModeRel").find("input[name='msgMode']:checked").attr("val")
				},
				success: function(msg) {
					$.walk.alert(msg);
				}
			});
		}
	});
}
</script>
</body>
</html>
