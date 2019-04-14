<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../../common/base/meta.ftl">
<#include "../../common/base/resources.ftl">
<title>流程管理</title>
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
							<td class="字段"> 流程分类：</td>
							<td>
								<select name="category" class="w-select2 w180">
									<option value="">全部</option>
									<#list ParamTranslateUtil.staticlist('MODEL_CATEGORY') as item>
										<option value="${item.dataId}">${item.dataName}</option>
									</#list>
								</select>
							</td>
							<td class="字段"> 模型标识：</td>
							<td>
							   <input name="key" type="text" class="w180" />
							</td>
							<td class="字段"> 模型名称：</td>
							<td>
							   <input name="name" type="text" class="w180" />
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
       		    <li><a onclick="showModelPopWindow(true)"><img src="${request.contextPath}/component/resources/images/icons/color/18/new.png"/>新建模型</a></li>
		    </ul>
		</div>
		
		<!-- 数据表 -->
		<div class="w-panel-content">
			<table id="dataGrid" class="easyui-datagrid" url="${request.contextPath}/act/model/list" 
				data-options="queryParams:$.walk.getQueryParams('conditionForm'),selectOnCheck:false,frozenAlign:'right'">
				<thead data-options="frozen:true">
					<tr>
						<th data-options="field:'oper', width:220, halign:'center', formatter:operRecord, exportable:false">操作区</th>
					</tr>
				</thead>
				<thead>
					<tr>
						<th data-options="field:'ck', checkbox:true, exportable:false"></th>
						<th data-options="field:'id',width:150">模型ID</th>
						<th data-options="field:'key',width:150">模型标识</th>
						<th data-options="field:'name',width:200">模型名称</th>
						<th data-options="field:'categoryName'">流程分类</th>
						<th data-options="field:'description',tip:true,width:300">模型描述</th>
						<th data-options="field:'version'">版本号</th>
						<th data-options="field:'deploymentId'">部署ID</th>
						<th data-options="field:'createTime',width:150">创建时间</th>
						<th data-options="field:'lastUpdateTime',width:150">最后更新时间</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</div>

<!-- 模型新建窗口 -->
<div id="popWindow" style="display: none">
	<div class="find_inbox_list1" style="padding: 20px 10px;">
		<form id="newModelForm" class="w-form" method="post" action="${request.contextPath}/act/model/create" target="_blank">
			<input id="id" name="id" type="hidden"/>
			<table>
				<tr>
					<td class="字段">模型标识: <span class="required">*</span></td>
					<td align="center">
						<input id="key" name="key" type="text" class="w260" />
					</td>
				</tr>
				<tr>
					<td class="字段">模型名称: <span class="required">*</span></td>
					<td align="center">
						<input id="name" name="name" type="text" class="w260"/>
					</td>
				</tr>
				<tr>
					<td class="字段">流程分类: </td>
					<td align="center">
						<select name="category" class="w260">
							<#list ParamTranslateUtil.staticlist('MODEL_CATEGORY') as item>
								<option value="${item.dataId}">${item.dataName}</option>
							</#list>
						</select>
					</td>
				</tr>
				<tr>
					<td class="字段">模型描述: </td>
					<td align="center">
						<textarea id="description" name="description" type="text" class="w260" style="height:80px"></textarea>
					</td>
				</tr>
			</table>
			<div class="w-button-row" style="text-align:center;margin-top: 20px;">
				<a class="w-a-btn orange" id="sureBtn" href="javascript:void(0)">确定</a>
				<a class="w-a-btn blue" onclick="cancel();" href="javascript:void(0)">取消</a>
			</div>
		</form>	
  	</div>
 </div>
<script>
var baseUrl = $.walk.ctx + '/act/model';
//页面初始化
$(function(){
});

//操作单条记录
function operRecord(val, row) {
	var operhtml = [];
	operhtml.push('<a onclick="modify(\'' + encodeURIComponent(JSON.stringify(row)) + '\')">修改</a>');
	operhtml.push('<a href="${request.contextPath}/act/model/copy?id=' + row.id + '" target="_blank" onclick="setTimeout(function(){$(\'#queryBtn\').click();},1000)">复制</a>');
	operhtml.push('<a href="${request.contextPath}/component/modeler.html?modelId=' + row.id + '" target="_blank">设计</a>');
	operhtml.push('<a onclick="deploy(\'' + row.id + '\')">部署</a>');
	operhtml.push('<a href="${request.contextPath}/act/model/export?id=' + row.id + '" target="_blank">导出</a>');
	operhtml.push('<a onclick="del(\'' + row.id + '\')">删除</a>');
	return operhtml.join("");
}

//显示模型新建窗口
function showModelPopWindow(){
	$('#newModelForm').get(0).reset();
	
	$("#popWindow").window({
		title : '新建模型',
		width : 500,
		height : 420,
		top : 20,
		collapsible : false,	
		minimizable : false,
		maximizable : true,
		resizable : false,
		draggable : true,
		modal: true,
		openAnimation : 'slide',
		closeAnimation : 'slide'
	}).show();
	
	$("#sureBtn").unbind("click").bind("click", function(){
		newModel();
	})
}

//新建模型
function newModel(){
	var form = $('#newModelForm');
	
	var key = $.trim(form.find("#key").val());
	var name = $.trim(form.find("#name").val());
	if(!key){
		$.walk.alert("模型标识不能为空！", "error", function(){
			form.find("#key").focus();
		});
		return;
	}
	if(!name){
		$.walk.alert("模型名称不能为空！", "error", function(){
			form.find("#name").focus();
		});
		return;
	}
	setTimeout(function(){
		$('#popWindow').window('close');
		$('#queryBtn').click();
	},1000);
	form.submit();
}

//修改模型
function modify(row){
	row = $.parseJSON(decodeURIComponent(row));
	
	//显示修改窗口
	showModelPopWindow();
	
	var form = $('#newModelForm');
	form.find("#id").val(row.id);
	form.find("#key").val(row.key);
	form.find("#name").val(row.name);
	form.find("#category").val(row.category);
	form.find("#description").val(row.description);
	
	$("#sureBtn").unbind("click").bind("click", function(){
		$.walk.confirm("确认修改吗？", function(ok){
			if(ok){
				$.ajax({
					type: "POST",
					url: baseUrl + "/modify",
					data: form.serialize(),
					success: function(response) {
						$.walk.alert(response, function(){
							$('#popWindow').window('close');
							$('#queryBtn').click();
						});
					}
				});
			
			}
		});
	})
	
}

//部署
function deploy(id){
	$.walk.confirm("确认部署吗？", function(ok){
		if(ok){
			$.ajax({
				type: "POST",
				url: baseUrl + "/deploy",
				data: {
					id: id
				},
				success: function(response) {
					$.walk.alert(response, function(){
						$('#queryBtn').click();
					});
				}
			});
		
		}
	});
}

//删除模型
function del(id){
	$.walk.confirm("确认删除吗？", function(ok){
		if(ok){
			$.ajax({
				type: "POST",
				url: baseUrl + "/delete",
				data: {
					id: id
				},
				success: function(response) {
					$.walk.alert(response, function(){
						$('#queryBtn').click();
					});
				}
			});
		}
	});
}

//取消
function cancel(){
	$('#popWindow').window('close');
	$('#newModelForm').get(0).reset();
}
</script>
</body>
</html>
