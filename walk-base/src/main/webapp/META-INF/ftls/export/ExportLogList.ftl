<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<title>我的导出</title>
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
							<td class="字段"> 导出流水： </td>
							<td>
								<input name="exportId" type="text" class="w180" value="${exportId}" />
							</td>
							<td class="字段"> 导出名称： </td>
							<td>
								<input name="exportName" type="text" class="w180"/>
							</td>
							<td class="字段"> 导出状态：</td>
							<td>
								<select name="exportState" class="w-select2 w180">
									<option value="">全部</option>
									<option value="0">导出中</option>
									<option value="1">导出成功</option>
									<option value="2">导出失败</option>
								</select>
							</td>
						</tr>
						<tr>
							<td class="字段"> 开始日期 :</td>
						    <td>
							   	<input name="beginDate" type="text" class="Wdate w180" onclick="WdatePicker({readOnly:true,isShowClear:true, isShowToday:false, dateFmt:'yyyy-MM-dd'})"/>
						    </td>
						    <td class="字段"> 结束日期 :</td>
						    <td>
								<input name="endDate" type="text" class="Wdate w180" onclick="WdatePicker({readOnly:true,isShowClear:true, isShowToday:false, dateFmt:'yyyy-MM-dd'})"/>
						    </td>
						    <td class="字段"> 导出模式：</td>
							<td>
								<select name="exportMode" class="w-select2 w180">
									<option value="">全部</option>
									<option value="1">同步</option>
									<option value="2" selected="selected">异步</option>
								</select>
							</td>
							<td colspan="10" align="center">
								<a id="queryBtn" class="w-a-btn orange EnterPress" onclick="$.walk.queryList('dataGrid', 'conditionForm');" href="javascript:void(0)">查询</a>
							</td>
						</tr>
					</table>
				</div>
			</form>
		</div>
		
		<!-- 数据表 -->
		<div class="w-panel-content">
			<table id="dataGrid" class="easyui-datagrid"
				   url="${request.contextPath}/common/exportLog/list"
				   data-options="queryParams:$.walk.getQueryParams('conditionForm'),selectOnCheck:false,frozenAlign:'right'">
				<thead>
					<tr>
						<th data-options="field:'ck', checkbox:true, exportable:false"></th>
						<th data-options="field:'LOG_ID',width:230, formatter:operRecord,styler:function(){return 'font-family:新宋体'}">导出流水</th>
						<th data-options="field:'EXPORT_NAME',width:200">文件名称</th>
						<th data-options="field:'EXPORT_STATE_NAME'">导出状态</th>
						<th data-options="field:'EXPORT_MODE_NAME'">导出模式</th>
						<th data-options="field:'TOTAL',formatter:function(val){return val ? val:0;}">总记录数</th>
						<th data-options="field:'FILE_SIZE',width:150,formatter:function(val){return val ? (val/1024).toFixed(2):0;}">导出文件大小(KB)</th>
						<th data-options="field:'CREATE_STAFF',width:120">创建人</th>
						<th data-options="field:'CREATE_TIME',width:150">创建时间</th>
						<th data-options="field:'FINISH_TIME',width:150">导出完成时间</th>
						<th data-options="field:'REMARK',width:120">备注</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</div>
<script>
var baseUrl = $.walk.ctx + '/common/exportLog';
//页面初始化
$(function(){
});

//操作单条记录
function operRecord(val, row){
	if(row.EXPORT_MODE == 2 && row.EXPORT_STATE == 1){
		return '<a href="${request.contextPath}/common/exportLog/downasynfile/'+val+'" title="点击下载">'+val+'</a>';
	} else {
		return val;
	}
}
</script>
</body>
</html>
