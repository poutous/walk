//datatables默认设置
$.extend($.fn.dataTable.defaults, {
		sDom:'ftlipr',		//自定义布局 l:每行显示的记录数;f:搜索框;t:表格;i:表格信息;p:分页条;r:加载时的进度条
		bServerSide : true,	//是否从服务器端获取数据 ajax
		bProcessing : false,	//是否显示正在处理中
		bJQueryUI : false, 	//可以添加 jqury的ui theme  需要添加css
		bSort : false, 		//是否支持排序功能
		bFilter : false,		//是否启用客户端过滤功能
		bAutoWidth : true,	//是否自动适应宽度
		bPaginate : true, 	//是否启用分页
		sPaginationType : "full_numbers",//指定分页器风格，插件默认是"two_button"
		bInfo : true,			//是否显示 当前显示 ? 到 ? 条，共 ? 条记录
		bLengthChange : true,	//是否显示每页显示 ? 条记录下拉框
		aLengthMenu : [[5, 10, 20, 50, 100, -1], [5, 10, 20, 50, 100, "所有"]],//定义每页显示数据数量
		iDisplayLength : 10,	//默认每页展示个数设置，为aLengthMenu中的数值
		oLanguage :  {		//改变分页语言
			sLengthMenu : "_MENU_",
			sZeroRecords : "对不起，查询不到任何相关数据",
			sInfo : "显示 _START_ 到 _END_ 条, 共 _TOTAL_ 记录",
			sInfoEmpty : "找不到相关数据",
			sInfoPostFix : "",
			sInfoFiltered : "",
			sProcessing : "正在加载中...",
			sSearch : "快速搜索",
			sUrl : "",
			oPaginate : {
			    sFirst :    " 首页 ",
			    sPrevious : " 上页 ",
			    sNext :     " 下页 ",
			    sLast :     " 尾页 "
			},
			oAria: {
				sSortAscending:  "asc",
				sSortDescending: "des"
			}
		}
});

//获取查询条件 返回json格式
function getDataTablesQueryParams(conditionFormId, aoData, otherParams){
	var param = $("#" + conditionFormId).serialize() + "&" + $.param(aoData);
	if(otherParams) param += ($.isJson(otherParams) ? $.param(otherParams) : otherParams);
	return param;
}

//获取服务器数据
function queryServerData(dataTableId, queryUrl, queryParams, fnCallback){
	$.ajax({
		url: queryUrl,
        data: queryParams,
        type: "POST",
        dataType: "json",
        success: function(data) {
			fnCallback(data);
			
			//去掉可能出现的undefined
			$(".dataTables_info").html($(".dataTables_info").html().replaceAll("undefined",""));
			$(".dataTables_paginate").find("a").each(function(){
				$(this).text($(this).text().replaceAll("undefined",""));
			});
			//$(".dataTables_processing").hide();
			
			//绑定导出
			var exportBtn = $("#"+dataTableId).attr("exportId");
			if(exportBtn){
				$("#"+exportBtn).bind("click", function(){
					var tag = queryUrl.indexOf('?') > -1 ? '' : '?';
					var exportUrl = queryUrl + tag + queryParams + "&__actionType=export";
					$.walk.exportTableData(dataTableId, exportUrl);
				});
			}
		}
	});
}

