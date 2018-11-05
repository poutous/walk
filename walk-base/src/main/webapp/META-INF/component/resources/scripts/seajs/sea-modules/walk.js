/**
 * walk框架通用函数库
 * 
 * @author shf675
 *
 */
(function($){
$.walk = {
	//工程根路径
	ctx : $("#pageContext").attr("contextName"),
	//当前页面
	page : $("#pageContext").attr("pageName"),
	//页面ID名称
	pageIdName : "pageId",
	//引用页面ID名称
	openerPageIdName : "openerPageId",
	//服务器是否支持combo。在app.properties中指定
	comboAble : ($("#pageContext").attr("comboAble") == true),
	
	//弹出信息框。 默认是从最顶层窗口弹出，但如果写了回调方法则从调用窗口处弹出
	alert : function (msg, typeOrFn, fn){
		//第二个参数可能传入回调函数
		if(typeOrFn && $.isFunction(typeOrFn)){
			fn = typeOrFn;
		}
		//第一个参数可能传入一个json
		if($.isJson(msg) && msg.type != ''){
			typeOrFn = msg.type;
			msg = msg.text;
		} 
		//第一个参数可能传入一个消息字符串 格式：<div><div id='type'>success</div><div id='text'>成功</div></div>
		else{
			try{
				var json = this.parseMessageJSON($(msg));
				if(json.type){
					typeOrFn = json.type;
					msg = json.text;
				}
			}catch(e){}
		}
		var title = "提示";
		if(typeOrFn == "warning"){
			title = "警告";
		} else if(typeOrFn == "success"){
			title = "成功";
		} else if(typeOrFn == "error"){
			title = "错误";
		} else {
			typeOrFn = "info";
		}
		
		//如果有回调函数将会从调用页面处弹出，否则从顶层窗口弹出
		if(fn && $.isFunction(fn)){
			$.messager.alert(title, msg, typeOrFn, fn);
		} else {
			this.getTopWindow().$.messager.alert(title, msg, typeOrFn);
		}
	},
	
	//确认框
	confirm : function (msg, titleOrFn, fn){
		//第二个参数可能传入回调函数
		if(titleOrFn && $.isFunction(titleOrFn)){
			fn = titleOrFn;
			titleOrFn = "确认";
		}
		titleOrFn = titleOrFn ? titleOrFn : "确认";
		$.messager.confirm(titleOrFn, msg, fn);
	},
	
	//对话框
	prompt : function (msg, titleOrFn, fn){
		//第二个参数可能传入回调函数
		if(titleOrFn && $.isFunction(titleOrFn)){
			fn = titleOrFn;
			titleOrFn = "对话框";
		}
		titleOrFn = titleOrFn ? titleOrFn : "对话框";
		$.messager.prompt(titleOrFn, msg, fn);
	},
	
	//进度框
	progress : function (optionsOrMethod){
		$.messager.progress(optionsOrMethod);
	},
	
	//show
	show : function (options){
		$.messager.show(options);
	},
	
	//获取工程最顶端窗口。不用top的原因：可能会有其他工程iframe嵌套
	getTopWindow : function (){
		//默认最多有5级
		var parents = this._getParents();
		for(var i = 0; i < parents.length; i++){
			try{
				if(parents[i].$.walk){
					return parents[i];
				}
			}catch(e){}
		}
	},
	
	//获取父窗口集合
	_getParents : function(){
		return [
			window.parent.parent.parent.parent.parent,
			window.parent.parent.parent.parent,
			window.parent.parent.parent,
			window.parent.parent,
			window.parent,
			window
		];
	},
	
	/**
     * 取当前url参数
     */
    getValueByUrl : function(name, defaultValue, url) {
    	url = !(url) ? window.location.search : url;
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = url.substr(1).match(reg);
        if (r != null) {
            return decodeURIComponent(r[2]);
        }
        return defaultValue;
    },
	
	//获取blockUI配置信息
	getBlockDefaults : function(){
		$.blockUI.defaults = {
			showOverlay : true,
			baseZ: 99999999,
		    css : {
		        padding : 2,
		        left : '20%',
		    	top : '3%',
		        color : '#000',
		        border : 'none',
		        backgroundColor : 'none',
		        cursor : 'default'
		    },
		    overlayCSS : {
		        backgroundColor : '#000',
		        opacity : 0.3,
		        cursor : 'default'
		    }
		};
		return $.blockUI.defaults;
	},
	
	/**
	 * 取随机数
	 */
	getRandomParam : function() {
		var date = new Date();
		return "" + date.getYear() + (date.getMonth() + 1) + date.getDate() + date.getHours() + date.getMinutes() + date.getSeconds() + date.getMilliseconds() + Math.floor(Math.random()*1000000);
	},
	
	/**
	 * 获取请求URL
	 * 调用示例：$.walk.getRequestURL($.walk.ctx + '/example', "queryList", "roleId=1&roleCode=2");
	 * @param baseUrl： 页面访问路径，例如$.walk.ctx + '/example'
	 * @param method：调用方法，例如：queryList
	 * @param params：传入参数，例如：roleId=1&roleCode=2
	 * @return
	 */
	getRequestURL : function(baseUrl, method, params, conditionFormId) {
		if(!baseUrl) return null;
		var url = baseUrl.startsWith(this.ctx) ? baseUrl : (this.ctx + "/" + baseUrl);
		var symbol = baseUrl.indexOf("?") > -1 ? "&":"?";
		if (method != null) url += "/" + method;
		
		if (params) {
			if($.isJson(params)){
				if(conditionFormId){
					$.extend(params, this.getQueryParams(conditionFormId))
				}
				url += symbol + $.param(params) + "&random=" + this.getRandomParam();
			} else {
				url += symbol + params;
				if(conditionFormId){
					url += "&" + $.param(this.getQueryParams(conditionFormId));
				}
				url += "&random=" + this.getRandomParam();
			}
		} else {
			url += symbol + "random=" + this.getRandomParam();
		}
		return url;
	},
	
	/**
	 * 序列化指定区域
	 */
	serializeArea : function(areaId){
		var serializeStr = null;
		if(!areaId){
			alert("请指定区域Id！");
			return;
		}
		var area = $("#" + areaId);
		if(area.size() == 0){
			alert("区域Id[" + areaId + "]不存在！");
			return;
		}
		
		//如果指定区域是表单，则直接序列化
		if(area.is("form")){
			serializeStr = area.serialize()
		} else {
			serializeStr = $.param(area.find("input,select,textarea").serializeArray());
		}
		return serializeStr;
	},
	
	/**
	 * 参数串转json对象
	 */
	param2json : function(param){
		if(!param){
			return {};
		}
		var obj = {};
	    var pairs = param.split('&');
	    var name,value;
	    $.each(pairs, function(i, pair) {
	        pair = pair.split('=');
	        name = decodeURIComponent(pair[0]);
	        if(name){
		        value = decodeURIComponent(pair[1]);
		        obj[name] =  !obj[name] ? value :[].concat(obj[name]).concat(value);//若有多个同名称的参数，则拼接
	        }
	    });
	    return obj;
	},
	
	/**
	 * 将指定form参数转换为json对象
	 */
	getQueryParams : function (conditionAreaId, otherParams){
	    var queryParams = this.param2json(this.serializeArea(conditionAreaId));
	    
	    //扩展额外参数
	    if(otherParams){
	    	if($.isJson(otherParams)){
			    $.extend(queryParams, otherParams)
	    	} else if($.isString(otherParams)){
	    		$.extend(queryParams, this.param2json(otherParams))
	    	}
		}
	    return queryParams;
	},
	
	//获取message信息
	parseMessageJSON : function(resp){
		var type = resp.find("#type").text();
		var text = resp.find("#text").html();
		return {
			type : type,
			text : text,
			isSuccess : type == "success",
			isInfo : type == "info",
			isWarning : type == "warning",
			isError : type == "error"
		};
	},
	
	//释放iframe内存
	clearFrameCache : function(iframe){
		try{
			iframe.src = '';
			iframe.contentWindow.document.write('');
			iframe.contentWindow.close();
			if($.isIE()){
	            CollectGarbage();
	            CollectGarbage();
	        }
		}catch(e){}
	},
	
	//查询
	queryList : function (gridId, conditionAreaIdOrParams, otherParams){
		if(!gridId){
			alert("请指定第一个参数，easyUI表格Id！");
			return;
		}
		var queryParams = {};
		if(conditionAreaIdOrParams){
			if($.isJson(conditionAreaIdOrParams)){
			    $.extend(queryParams, conditionAreaIdOrParams)
	    	} else if($.isString(conditionAreaIdOrParams)){
				queryParams = this.getQueryParams(conditionAreaIdOrParams, otherParams);
	    	}
		}
		
		var opts = {
			queryParams : queryParams,
			pageNumber : 1
		};
		var grid = $("#" + gridId);
		if(grid.hasClass("easyui-datagrid")){
			grid.datagrid(opts);
		} else if(grid.hasClass("easyui-treegrid")){
			grid.treegrid(opts);
		}
	},
	
	//验证表单(form)
	validateForm : function (form){
		var formValidator = form.attr("validXml");
		var validate = $.parseJSON(formValidator);
		seajs.use(['$validate'], function(){
			form.validate({
			    /* 设置验证规则 */
			    rules: validate.rules,
			
			    /* 设置错误信息 */
			    messages: validate.messages,
			
			    /* 设置验证触发事件 */
			    focusInvalid: false,
			    onkeyup: false,
			
			    /* 设置错误信息提示DOM */
			    errorPlacement: function(error, element) {
					error.appendTo(element.parent());
			    }
			});
		});
	},
	
	//模拟ajax方式提交form
	ajaxSubmit : function (formId, action, callbackFunc){
		if(!formId){
			alert("请指定formId！");
			return;
		}
		var form = $("#" + formId);
		if(form.size() == 0){
			//form无id有name也可
			form = $("form[name='" + formId + "']");
			if(form.size() == 0){
				alert("form[" + formId + "]不存在！");
				return;
			}
		}
		//第二个参数可能传入回调函数
		if(action && $.isFunction(action)){
			callbackFunc = action;
			action = form.attr("action");
			if(!action){
				alert("请指定form[" + formId + "]action属性！");
				return;
			}
		}
		//开始提交
		seajs.use(['$form','$validate'], function(){
			var formValidator = form.attr("validXml");
			if(formValidator && !form.valid()) return false;//校验失败返回
			
			form.form('submit', {
				url: action,
				method : "post",
				onSubmit : function(){
					$.walk.showLoading();
					
					//如果后端一直未返回数据，10秒后隐藏遮罩层。
					setTimeout(function(){
						$.walk.hideLoading();
					}, 10000);
					
					//jquery.form请求特殊标识
					if(form.find("#JQUERY_FORM_AJAX_REQUEST").size() == 0){
						form.append("<input id='JQUERY_FORM_AJAX_REQUEST' name='JQUERY_FORM_AJAX_REQUEST' value='true' style='display: none'/>");
					}
				},
				success : function(response) {
					$.walk.hideLoading();
					
					var isMessageMethod = false;
					var resp = response;
					if(resp){
						//后端调用了message.xxx方法
						if(resp.indexOf('_MESSAGE_RESPONSE') > -1){
							isMessageMethod = true;
							resp = $.walk.parseMessageJSON($(resp));
							
							//如果消息类型错误直接弹出错误信息
//							if(resp.isError == true){
//								$.walk.alert(resp);
//								return ;
//							}
						} else {
							//尝试转成json
							try{
								resp = $.parseJSON(resp);
							}catch(e){}
						}
					}
					//如果写了回调函数，逻辑在回调函数里自处理，否则在后端调用了message.xxx方法后前台自动弹出消息。
					if(callbackFunc && $.isFunction(callbackFunc)){
						callbackFunc.call(this, resp);
					} else {
						//后端调用了message.xxx方法后前台自动弹出消息
						if(isMessageMethod){
							$.walk.alert(resp);
						}
					}
				}
			});
		});
	},
	
	/* ajax局部刷新
	 * url: 请求url
	 * params:参数
	 * partids：刷新区域ID，多个以逗号分隔
	 * callbackFunc：回调方法
	*/
	ajaxRefresh : function(url, params, partids, callbackFunc) {
		if(params){
			params._page_name = this.page;
		}
		$.ajax({
			url : url,
			data : params,
			type : "POST",
			dataType : "html",
			cache: false,
			success : function(data){
				if(partids) {
					var parts = partids.split(",");
					for(var i=0; i<parts.length; i++){
						if(parts[i] && parts[i].trim()) {
							var partId = parts[i].trim();
							//初始化jQuery.select2组件
							if($.isIE9Under()){
								$("#"+partId).replaceWith($(data).find("#"+partId));
							} else {
								if($("#"+partId).hasClass("w-select2")){
									seajs.use('$select2',function(){
										$("#"+partId).select2("destroy");
										$("#"+partId).replaceWith($(data).find("#"+partId));
										$("#"+partId).select2();
									});
								} else {
									$("#"+partId).replaceWith($(data).find("#"+partId));
								}
							}
							
						}
					}
				}
				if(callbackFunc && $.isFunction(callbackFunc)){
					callbackFunc.call(this, data);
				}
			}
		});
	},
	
	//显示遮罩层
	showLoading : function (loadingTarget, top, left,iframeSrc){
		seajs.use(['$blockui'], function(){
			$.walk.getBlockDefaults();
			top = top ? top:"50%";
			left = left ? left:"50%";
			iframeSrc = iframeSrc ? iframeSrc:"";
			var opts = {
				message : "<img src='" + $.walk.ctx +"/component/resources/images/loading/loading-4.gif'/>",
				width : "45px",
				css:{
					top : top,
					left : left
				},
				iframeSrc : iframeSrc,
				timeout : 5000
			};
			if (loadingTarget) {// 指定区域显示加载中效果
				opts.css.top = (loadingTarget.height()/2) - 32;//32是loading-4.gif高度的一半
	            loadingTarget.block(opts);
	        } else {// 全页面显示加载中效果
				$.blockUI(opts);
	        }
	        if($.isIE8Under()){
		        //ie6遮罩select
		        $(".blockOverlay").bgiframe();
	        }
		});
	},
	
	//隐藏遮罩层
	hideLoading : function(loadingTarget){
		seajs.use(['$blockui'], function(){
			if (loadingTarget) {// 指定区域隐藏加载中效果
	            loadingTarget.unblock();
	        } else {// 全页面隐藏加载中效果
	            $.unblockUI();
	        }
		});
	},
	//创建iframe
	createDialogFrame : function(scrolling){
		scrolling = scrolling ? scrolling:'no';
		
		//使用iframe池，防止iframe占用内存过高
		var dialogsIframePool = $("#dialogsIframePool");
		if(dialogsIframePool.size() == 0){
			$(document.body).append('<div id="dialogsIframePool" style="display:none"></div>');
			dialogsIframePool = $("#dialogsIframePool");
		}
		var freeFrames = dialogsIframePool.find("iframe[free='true'][scrolling='"+scrolling+"']");
		if(freeFrames.size() == 0){
			dialogsIframePool.append("<div class='dialogframediv' style='display: none;overflow:hidden;'><iframe free='true' class='dialogframe' scrolling='"+scrolling+"' frameborder='0' width='100%' height='100%' allowTransparency='true'></iframe></div>");
			freeFrames = dialogsIframePool.find("iframe[free='true'][scrolling='"+scrolling+"']");
		}
		var iframe = freeFrames.first();
		iframe.attr('free','false');//标记
		return iframe;
	},
	
	//回收iframe
	recoveryDialogFrame : function(iframe){
		this.clearFrameCache(iframe.get(0));
		iframe.attr('free','true');
		iframe.removeAttr($.walk.openerPageIdName);
		iframe.unbind("load");
		$("#dialogsIframePool").append($("<div class='dialogframediv' style='display: none;overflow:hidden;'></div>").append(iframe));
	},
	
	//添加标签页
	openTab : function(subtitle, url, icon){
		return this.openTabById(this.getRandomParam(), subtitle, url, icon);
	},
	
	//指定ID添加标签页
	openTabById : function(subIdOrJson, subtitle, url, icon){
		var subId = subIdOrJson;
		if($.isJson(subIdOrJson)){
			subId = subIdOrJson.subId;
			subtitle = subIdOrJson.subtitle;
			url = subIdOrJson.url;
			icon = subIdOrJson.icon;
		}
		url = url.startsWith(this.ctx) ? url : (this.ctx + "/" + url);
		return $.walk.getTopWindow().addTab(subId, subtitle, url, icon);
	},
	
	//默认关闭当前tab，指定了tabId，则按tabId关闭
	closeTab : function(subId){
		if(subId){
			return $.walk.getTopWindow().closeTabById(subId);
		} else {
			return $.walk.getTopWindow().closeSelectedTab();
		}
	},
	
	//dialog方式打开url
	openUrlDialog : function(urlOrDialogJson, title, width, height, top, scrolling, onloadSuccess){
		var url = urlOrDialogJson;
		if($.isJson(urlOrDialogJson)){
			url = urlOrDialogJson.url;
			title = urlOrDialogJson.title;
			width = urlOrDialogJson.width;
			height = urlOrDialogJson.height;
			top = urlOrDialogJson.top;
			scrolling = urlOrDialogJson.scrolling;
			onloadSuccess = urlOrDialogJson.onloadSuccess;
		}
		url = url.startsWith(this.ctx) ? url : (this.ctx + "/" + url);
		$.walk.getTopWindow().$.walk._openUrlDialog(url, title, width, height, top, scrolling, onloadSuccess, this.getPageId());
	},
	
	//dialog方式打开url
	_openUrlDialog : function(url, title, width, height, top, scrolling, onloadSuccess, pageId){
		var dialogframe = this.createDialogFrame(scrolling);
		var dialogframediv = dialogframe.parent(".dialogframediv");
		dialogframediv.show();
		var win = dialogframediv.window({
			title: title ? title : "Dialog",
			width: (width || width == 0) ? width : 800,
			height: (height || height == 0) ? height : 400,
			top: (top || top == 0) ? top : 80,
			minimizable: false,
			modal: true,
			openAnimation : 'slide',
			closeAnimation : 'slide',
			onOpen : function(){
				//加载效果...
				var loadingTarget = dialogframe.parents(".window");
				$.walk.showLoading(loadingTarget, null, "45%");
				setTimeout(function(){
					$.walk.hideLoading(loadingTarget);
				}, 500);
			},
			onClose : function(){
				//回收iframe
				$.walk.recoveryDialogFrame(dialogframe);
				
				//销毁window
				dialogframediv.window('destroy');
			}
		}).window("hcenter");
		
		//设置引用页面ID及src
		url = this._wrapCas(url);
		url = url.indexOf('?') > -1 ? (url + "&random=" + this.getRandomParam()) :(url + "?random=" + this.getRandomParam());
		dialogframe.attr($.walk.openerPageIdName, pageId);
		dialogframe.attr("src", url);
		dialogframe.unbind("load").bind("load", function(){
			var contents = dialogframe.contents();
			if(pageId){
				contents.find("#pageContext").attr($.walk.openerPageIdName, pageId);
			}
			if(onloadSuccess){
				//第一个对象为iframe窗口对象。第二个为iframe的document的jquery对象。第三个为easyui弹窗window对象
				onloadSuccess(dialogframe.get(0).contentWindow, contents, win);
			}
		});
		
		//返回页面顶部
		this.pageScrollTop();
	},
	
	//关闭弹窗
	closeDialog : function(win){
		var w = win;
		if(!w){
			var parents = this._getParents().reverse();
			for(var i = 0; i < parents.length; i++){
				if(parents[i] && parents[i].$(".window:visible").size() > 0){
					w = parents[i];
					break;
				}
			}
		}
		w.$(".window:visible").last().find('.panel-tool-close').click();
	},
	//处理cas
	_wrapCas : function(url){
		var pageContext = $("#pageContext");
		var fromCas = pageContext.attr("fromCas");
		if(fromCas == 'true'){
			var urlRouteParameterName = pageContext.attr("urlRouteParameterName") ? pageContext.attr("urlRouteParameterName"):'_authenticator';
			url += (url.indexOf('?') > -1 ? '&':'?') + urlRouteParameterName + '=cas';
			
			var casSpecialParameterName = pageContext.attr("casSpecialParameterName");
			var casSpecialParameter = pageContext.attr("casSpecialParameter");
			if(casSpecialParameterName && casSpecialParameter){
				url += (url.indexOf('?') > -1 ? '&':'?') + casSpecialParameterName + '=' + casSpecialParameter;
			}
		}
		return url;
	},
	//datagrid:将选中行的数组转换成以特殊符号分隔的字符串，分隔符默认逗号
	checkeds2str : function(checkeds, idName, divide){
		if(!divide) divide = ",";
		var ids = "";
		$.each(checkeds, function(i, checked){
			ids += eval("checked." + idName) + divide;
		});
		return ids.substr(0, ids.length-1);
	},
	
	//导出当前页表格
	exportData : function (url, queryParams, isAsyn){
		//异步方式导出
		if(isAsyn == true) {
			var promptTitle = "请输入自定义导出文件名称！";
			this.prompt(promptTitle, function(val){
				val = $.trim(val);
				if(!val){
					$.walk.alert(promptTitle, "error");
					return false;
				}
				if(queryParams) {
					//带&符号的字符串参数
					if($.isString(queryParams)){
						queryParams += "&__actionType=asynExport&asynExportName=" + val;
					} 
					//json参数
					else if ($.isJson(queryParams)) {
						queryParams.asynExportName = val;
						queryParams.__actionType = 'asynExport';
					}
				} else {
					queryParams = {asynExportName : val, __actionType : 'asynExport'}
				}
				
				$.ajax({
					type : "POST",
					url : url,
					//提交的数据
					data : queryParams,
					success : function(rsp, textStatus, request) {
						var exportId = request.getResponseHeader("asynExportId");
						if(exportId){
							//提示信息
							$.walk.confirm('导出任务创建成功，流水：' + exportId + '<br>您可以进入菜单“我的导出”中查看导出进度。是否立即进入“我的导出”查看进度？', function(ok){
								if(ok){
									$.walk.openTab("我的导出", $.walk.ctx + "/common/exportLog/toPage?exportId=" + exportId);
								}
							});
						} else {
							$.walk.alert("创建导出任务失败！");
						}
					}
				});
			});
		} 
		//同步方式导出
		else {
			this.confirm("确认导出吗？", function(ok){
				if(ok){
					var hiddenElements = "";
					if (queryParams) {
						//如果是带&符号的字符串参数，转换成json
						if($.isString(queryParams)){
							queryParams = $.walk.param2json(queryParams);
						}
						//判断是否是json
						else if ($.isJson(queryParams)) {
							var _params = $.extend({}, queryParams);
							
							//转换成元素
							for (var key in _params) {
								hiddenElements += '<input type="hidden" name="' + key + '" value="' + _params[key] + '"/>';
							}
						}
					}
					
				    //提交导出
					$('#tempDiv').remove();
					$("<div id='tempDiv'><iframe name='IF_4down'></iframe></div>").appendTo($(document.body)).css('display', 'none').append('<form id="tempForm" action="' + url + '" target="IF_4down" method="post" accept-charset="UTF-8">' + hiddenElements + '<input type="hidden" name="__actionType" value="export"/></form>');
					document.charset = "UTF-8";
					$("#tempForm").submit().remove();
				}
			});
		}
	},
	
	//导出当前页表格
	exportCurrentPage : function (exportName, table){
		this.confirm("确认导出吗？", function(ok){
			if(ok){
			    $("<div id='expTemp'></div>").appendTo($(document.body));
			    $('#expTemp').css({'position': 'absolute','top': '-9000px'});
			    $("#expTemp").append('<iframe name="IF_4down"></iframe>').append('<form id="exportform" action="'+$.walk.ctx+'/fileserver/exportCurrentPage" method="post" target="IF_4down"><input type="hidden" id="pData" name="pData"/><input type="hidden" id="exportName" name="exportName"/></form>');
			    $("#pData").val("<table>" + table.html() + "</table>");
				$("#exportName").val(exportName);
			    $("#exportform").submit().remove();
			    $('#expTemp').remove();
			}
		});
	},
	
	//返回页面顶部
	pageScrollTop : function (){
		this.getTopWindow().$("html, body").animate({scrollTop:"0px"}, 400);
	},
	
	//获取引用本页面的iframe的jquery对象。
	getOpenerIframe : function(){
		var iframe;
		$("iframe", parent.document).each(function(){
			if($(this).get(0).contentWindow == window){
				iframe = $(this);
				return false;
			}
		});
		return iframe;
	},
	
	//获取当前页面ID
	getPageId : function(){
		return $("#pageContext").attr($.walk.pageIdName);
	},
	
	//获取引用页面ID
	getOpenerPageId : function(){
		var openerPageId = $("#pageContext").attr($.walk.openerPageIdName);
		if(!openerPageId){
			openerPageId = this.getOpenerIframe().attr($.walk.openerPageIdName);
		}
		return openerPageId;
	},
	
	//获取创建本窗口的引用窗口
	openerWindow : function(){
		return this.getWindowById(this.getOpenerPageId());
	},
	
	//根据pageId路径获取目标window对象
	getWindowById : function(pageId){
		var topPageId = this.getTopWindow().$.walk.getPageId();
		if(pageId && topPageId && topPageId == pageId){
			return this.getTopWindow();
		}
		return this._getWindowById(this.getTopWindow(), pageId);
	},
	
	//根据pageId路径获取目标window对象
	_getWindowById : function(startWindow, pageId){
		if(!pageId){
			return undefined;
		}
		var targetWin;
		var pageIdArr = pageId.split('.');
		var isFinded = false;
		startWindow.$("iframe").each(function(){
			var tabframe = $(this);
			var pageContext = tabframe.contents().find("#pageContext");
			var pageId = pageContext.attr($.walk.pageIdName) + "";
			if(pageId.indexOf(pageIdArr[0]) > -1){
				isFinded = true;
				targetWin = pageContext.get(0).parentNode.parentNode.parentNode.defaultView;
				return;
			}
		});
		if(isFinded && pageIdArr.length > 1){
			pageIdArr.shift();
			var newPageId = pageIdArr.join('.');
			targetWin = this._getWindowById(targetWin, newPageId);
		}
		return targetWin;
	},
	
	//每个页面标记页面ID
	//生成规则：父页面ID+本页面ID。父页面不包括顶级页面
	_generatePageId : function (){
		var parentPageId;
		if(window.parent != $.walk.getTopWindow()){
			parentPageId = $(window.parent.document).find("#pageContext").attr($.walk.pageIdName);
		}
		var newPageId = "p" + $.walk.getRandomParam();
		var currPageId = parentPageId ? (parentPageId + "." + newPageId):newPageId;
		if(!$("#pageContext").attr($.walk.pageIdName)){
			$("#pageContext").attr($.walk.pageIdName, currPageId);
		}
		
		//记录引用页面
		var topPageId = $.walk.getTopWindow().$.walk.getPageId();
		if(parentPageId && parentPageId != topPageId){
			if(!$("#pageContext").attr($.walk.openerPageIdName)){
				$("#pageContext").attr($.walk.openerPageIdName, parentPageId);
			}
		}
	},
	//比较两个纯json对象是否相等，对象中不能有数组
	comparePlainJSON : function (json1, json2){
		var a = JSON.stringify(json1) == JSON.stringify($.extend($.extend({}, json1), json2));
		var b = JSON.stringify(json2) == JSON.stringify($.extend($.extend({}, json2), json1));
		return a == true && b == true;
	}
};
})(jQuery);

//重置datagrid宽度
function resizeDatagrid(){
	$('.easyui-datagrid,.easyui-treegrid').datagrid('resize');
}