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
							<td class="字段"> key：</td>
							<td>
							   <input id="cacheKey" name="cacheKey" type="text" class="w260" onkeyup="this.value=this.value.trim();"/>
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
       		    <li><a onclick="openAddElementDialog();"><img src="${request.contextPath}/component/resources/images/icons/color/18/new.png"/>新增元素</a></li>
       		    <li><a onclick="setExpireCacheElement();"><img src="${request.contextPath}/component/resources/images/icons/color/18/set.png"/>设置过期时间</a></li>
       		    <li><a onclick="removeCacheElement();"><img src="${request.contextPath}/component/resources/images/icons/color/18/delete.png"/>批量删除</a></li>
		    </ul>
		</div>
		
		<!-- 数据表 -->
		<div class="w-panel-content">
			<table id="dataGrid" class="easyui-datagrid" 
				   url="${request.contextPath}/console/cache/queryCacheElementList"
				   data-options="queryParams:$.walk.getQueryParams('conditionForm'),selectOnCheck:false,frozenAlign:'right'">
				<thead data-options="frozen:true">
					<tr>
						<th data-options="field:'oper', width:150, halign:'center', formatter:operRecord, exportable:false">操作区</th>
					</tr>
				</thead>
				<thead>
					<tr>
						<th data-options="field:'ck', checkbox:true, exportable:false"></th>
						<th data-options="field:'cacheKeyIndex',align:'left',hidden:true">元素key索引</th>
						<th data-options="field:'cacheKey',align:'left',width:770,tip:true">元素key</th>
						<th data-options="field:'cacheTTL',width:150,formatter:function(val){return val ? parseInt(val/1000):'';}">剩余存活时间(秒)</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	
	<!-- 元素值显示区 -->
	<div id="cacheValueWindow" style="display:none;border: 3px solid #aaaaaa;">
		<div>
			<div id="showModeSel" class="w-cr-box" style="padding:10px;float:left">
				<span class="w-tooltip-n" title="仅查看">
					<input type="radio" name="showMode" val="text" style="cursor: pointer;" onclick="showModeSwitch($(this).attr('val'));" checked="checked"/>text
				</span>
				<span class="w-tooltip-n" title="仅查看">
					<input type="radio" name="showMode" val="json" style="cursor: pointer;" onclick="showModeSwitch($(this).attr('val'));"/>json
				</span>
				<span class="w-tooltip-n" title="可修改" style="color:#fd7a00">
					<input type="radio" name="showMode" val="hex" style="cursor: pointer;" onclick="showModeSwitch($(this).attr('val'));"/>hex
				</span>
			</div>
			<div style="padding:10px;float:left;width:400px;text-align:left;word-break: break-all;word-wrap: break-word;">
				值类型：<span id="valueType"></span>
			</div>
			<div style="float:right;padding:10px;">
				<input id="cacheKeyIndex" type="text" style="display:none"/>
				<input id="allowSave" type="text" style="display:none"/>
				<a id="saveBtn" href="javascript:void(0)" class="w-btn w-btn-blue-m" style="display:none" onclick="saveNewVale();">保存新值</a>
			</div>
			<div style="clear: both;"></div>
		</div>
		<div style="border-top:1px solid #ddd">
			<textarea id="valueContent" style="width:100%;height:460px;resize: none;border:none;" readonly="readonly"></textarea>
		</div>
	</div>
	
	<!-- 新元素显示区 -->
	<div id="eleWindow" style="display:none;border: 3px solid #aaaaaa;">
		<div style="border-top:1px solid #ddd;padding-top: 5px;">
			<form class="w-form">
				<table class="eleTable">
					<tr>  
						<td class="字段">元素key：</td>
						<td>
						   <textarea id="eleKey" placeholder="请输入元素key..." style="height:160px;width:380px;resize: none;"></textarea>
						</td>
						<td>
							<div id="eleKeyModeRel" class="w-cr-box" style="padding:15px 0px;">
								<input type="radio" name="eleKeyMode" val="text" style="cursor: pointer;" checked="checked"/>string
								<input type="radio" name="eleKeyMode" val="hex" style="cursor: pointer;"/>hex
							</div>
						</td>
					</tr>
					<tr>  
						<td class="字段">元素value：</td>
						<td>
						   <textarea id="eleValue" placeholder="请输入元素value..." style="height:160px;width:380px;resize: none;"></textarea>
						</td>
						<td>
							<div id="eleValueModeRel" class="w-cr-box" style="padding:15px 0px;">
								<input type="radio" name="eleValueMode" val="text" style="cursor: pointer;" checked="checked"/>string
								<input type="radio" name="eleValueMode" val="hex" style="cursor: pointer;"/>hex
							</div>
						</td>
					</tr>
				</table>
			</form>
		</div>
		<div>
			<div style="padding:10px; text-align: right;">
				<a href="javascript:void(0)" class="w-btn w-btn-blue-m" onclick="addElement();">保存</a>
			</div>
		</div>
	</div>
</div>
<script>
var baseUrl = $.walk.ctx + '/console/cache';
//页面初始化
$(function(){
});

//显示类型切换
function showModeSwitch(val){
	$('#valueContent').val("加载中...");
	var selectedRow = $("#dataGrid").datagrid("getSelected");
	viewCacheElementValue(selectedRow.cacheKeyIndex);
	
	//保存新值切换
	allowSaveChange(val);
}

//保存新值切换
function allowSaveChange(val){
	$("#saveBtn").hide();
	$("#valueContent").attr("readonly", "readonly");
	if(val == 'hex'){
		$("#saveBtn").show();
		$("#valueContent").removeAttr("readonly").focus();
	}
}

//操作单条记录
function operRecord(val, row) {
	var operhtml = [];
	operhtml.push("<a onclick=\"viewCacheElementValue('" + row.cacheKeyIndex + "')\">元素值管理</a>");
	return operhtml.join("");
}

//查看元素值
function viewCacheElementValue(cacheKeyIndex){
	if($("#cacheValueWindow").is(":hidden")){
		seajs.use('$blockui', function(){
			$('#valueContent').val("加载中...");
			$.blockUI({message: $('#cacheValueWindow'), css:{top:30,left:'20%',width:710,height:450,cursor:'default',border:'none'}});
			$(".blockOverlay").click($.unblockUI);
		});
	}
	
	setTimeout(function(){
		$.ajax({
			url: baseUrl + "/viewCacheElementValue",
			type: "post",
			data: {
				cacheName: $("#cacheName").val(),
				showMode: $("#showModeSel").find("input[name='showMode']:checked").attr("val"),
				cacheKeyIndex: cacheKeyIndex
			},
			success: function(resp) {
				$('#valueContent').val(resp.value);
				$('#valueType').text(resp.type);
				$('#cacheKeyIndex').val(resp.index);
				$('#allowSave').val(resp.allowSave);
			}
		});
	}, 150);
}

//批量设置元素过期时间
function setExpireCacheElement(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	var all = false;
	var msg = "请输入过期时间，单位秒。";
	if (checkeds.length == 0) {
		all = true;
		msg = "不选任何记录表示将设置所有元素的过期时间，" + msg;
	}
	$.walk.prompt(msg, function(expireTime) {
		if(expireTime){
			if(!$.isNumeric(expireTime)){
				$.walk.alert("请输入数字，单位秒！");
				return false;
			}
		    $.ajax({
				url: baseUrl + "/setExpireCacheElement",
				type: "post",
				data: {
					cacheName: $("#cacheName").val(),
					cacheKeyIndexs: $.walk.checkeds2str(checkeds, 'cacheKeyIndex'),
					expireTime: expireTime,
					all : all
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

//新增元素弹出框
function openAddElementDialog(){
	seajs.use('$blockui', function(){
		$('#eleContent').val("");
		$.blockUI({message: $('#eleWindow'), css:{top:30,left:'22%',width:600,height:380,cursor:'default',border:'none'}});
		$(".blockOverlay").click($.unblockUI);
	});
}

//新增元素
function addElement() {
	$.walk.confirm("确认新增吗？", function(ok) {
		if(ok){
		    $.ajax({
				url: baseUrl + "/addElement",
				type: "post",
				data: {
					cacheName: $("#cacheName").val(),
					eleKey: $("#eleKey").val(),
					eleValue: $("#eleValue").val(),
					eleKeyMode: $("#eleKeyModeRel").find("input[name='eleKeyMode']:checked").attr("val"),
					eleValueMode: $("#eleValueModeRel").find("input[name='eleValueMode']:checked").attr("val")
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
function removeCacheElement(){
	var checkeds = $("#dataGrid").datagrid("getChecked");
	if (checkeds.length == 0) {
		$.walk.alert("未选择任何记录！");
		return false;
	}
	$.walk.confirm("确认删除吗？", function(ok) {
		if(ok){
		    $.ajax({
				url: baseUrl + "/removeCacheElement",
				type: "post",
				data: {
					cacheName: $("#cacheName").val(),
					cacheKeyIndexs: $.walk.checkeds2str(checkeds, 'cacheKeyIndex')
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

//保存新值
function saveNewVale(){
	$.walk.confirm("确认设置新值吗？", function(ok) {
		if(ok){
			$.ajax({
				url: baseUrl + "/saveNewVale",
				type: "post",
				data: {
					cacheName: $("#cacheName").val(),
					cacheKeyIndex: $("#cacheKeyIndex").val(),
					newValue: $("#valueContent").val()
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
