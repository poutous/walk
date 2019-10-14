/**
 * 使用seajs进行加载 服务器如果支持combo，会减少请求次数
 *
 * @author shf675
 */
(function($) {
	//本地语言配置
	var locales = {
		"en": "easyui-lang-en.js",
		"zh_CN": "easyui-lang-zh_CN.js"
	};
	//插件配置
	var modules = {
		draggable: {
            js: "jquery.draggable.js"
        },
        droppable: {
            js: "jquery.droppable.js"
        },
        resizable: {
            js: "jquery.resizable.js"
        },
        linkbutton: {
            js: "jquery.linkbutton.js",
            css: "linkbutton.css"
        },
        progressbar: {
            js: "jquery.progressbar.js",
            css: "progressbar.css"
        },
        tooltip: {
            js: "jquery.tooltip.js",
            css: "tooltip.css"
        },
        pagination: {
            js: "jquery.pagination.js",
            css: "pagination.css",
            dependencies: ["linkbutton"]
        },
        datagrid: {
            js: "jquery.datagrid.js",
            css: "datagrid.css",
            dependencies: ["panel", "resizable", "linkbutton", "pagination"]
        },
        treegrid: {
            js: "jquery.treegrid.js",
            css: "tree.css",
            dependencies: ["datagrid"]
        },
        propertygrid: {
            js: "jquery.propertygrid.js",
            css: "propertygrid.css",
            dependencies: ["datagrid"]
        },
        datalist: {
            js: "jquery.datalist.js",
            css: "datalist.css",
            dependencies: ["datagrid"]
        },
        panel: {
            js: "jquery.panel.js",
            css: "panel.css"
        },
        window: {
            js: "jquery.window.js",
            css: "window.css",
            dependencies: ["resizable", "draggable", "panel"]
        },
        dialog: {
            js: "jquery.dialog.js",
            css: "dialog.css",
            dependencies: ["linkbutton", "window"]
        },
        messager: {
            js: "jquery.messager.js",
            css: "messager.css",
            dependencies: ["linkbutton", "dialog", "progressbar"]
        },
        layout: {
            js: "jquery.layout.js",
            css: "layout.css",
            dependencies: ["resizable", "panel"]
        },
        form: {
            js: "jquery.form.js"
        },
        menu: {
            js: "jquery.menu.js",
            css: "menu.css"
        },
        tabs: {
            js: "jquery.tabs.js",
            css: "tabs.css",
            dependencies: ["panel", "linkbutton"]
        },
        menubutton: {
            js: "jquery.menubutton.js",
            css: "menubutton.css",
            dependencies: ["linkbutton", "menu"]
        },
        splitbutton: {
            js: "jquery.splitbutton.js",
            css: "splitbutton.css",
            dependencies: ["menubutton"]
        },
        switchbutton: {
            js: "jquery.switchbutton.js",
            css: "switchbutton.css"
        },
        accordion: {
            js: "jquery.accordion.js",
            css: "accordion.css",
            dependencies: ["panel"]
        },
        calendar: {
            js: "jquery.calendar.js",
            css: "calendar.css"
        },
        textbox: {
            js: "jquery.textbox.js",
            css: "textbox.css",
            dependencies: ["validatebox", "linkbutton"]
        },
        passwordbox: {
            js: "jquery.passwordbox.js",
            css: "passwordbox.css",
            dependencies: ["textbox"]
        },
        filebox: {
            js: "jquery.filebox.js",
            css: "filebox.css",
            dependencies: ["textbox"]
        },
        radiobutton:{
        	js: "jquery.radiobutton.js",
        	css: "radiobutton.css"
        },
        checkbox:{
        	js: "jquery.checkbox.js",
        	css: "checkbox.css"
        },
        sidemenu:{
        	js: "jquery.sidemenu.js",
        	css: "sidemenu.css",
        	dependencies: ["accordion", "tree", "tooltip"]
        },
        combo: {
            js: "jquery.combo.js",
            css: "combo.css",
            dependencies: ["panel", "textbox"]
        },
        combobox: {
            js: "jquery.combobox.js",
            css: "combobox.css",
            dependencies: ["combo"]
        },
        combotree: {
            js: "jquery.combotree.js",
            dependencies: ["combo", "tree"]
        },
        combogrid: {
            js: "jquery.combogrid.js",
            dependencies: ["combo", "datagrid"]
        },
        combotreegrid: {
            js: "jquery.combotreegrid.js",
            dependencies: ["combo", "treegrid"]
        },
        tagbox:{
        	js: "jquery.tagbox.js",
        	dependencies: ["combobox"]
        },
        validatebox: {
            js: "jquery.validatebox.js",
            css: "validatebox.css",
            dependencies: ["tooltip"]
        },
        numberbox: {
            js: "jquery.numberbox.js",
            dependencies: ["textbox"]
        },
        searchbox: {
            js: "jquery.searchbox.js",
            css: "searchbox.css",
            dependencies: ["menubutton", "textbox"]
        },
        spinner: {
            js: "jquery.spinner.js",
            css: "spinner.css",
            dependencies: ["textbox"]
        },
        numberspinner: {
            js: "jquery.numberspinner.js",
            dependencies: ["spinner", "numberbox"]
        },
        timespinner: {
            js: "jquery.timespinner.js",
            dependencies: ["spinner"]
        },
        tree: {
            js: "jquery.tree.js",
            css: "tree.css",
            dependencies: ["draggable", "droppable"]
        },
        datebox: {
            js: "jquery.datebox.js",
            css: "datebox.css",
            dependencies: ["calendar", "combo"]
        },
        datetimebox: {
            js: "jquery.datetimebox.js",
            dependencies: ["datebox", "timespinner"]
        },
        slider: {
            js: "jquery.slider.js",
            dependencies: ["draggable"]
        },
		dgfilter: {
			js: "jquery.datagrid-filter.js",
			dependencies: ["treegrid"]
		},
		detailview: {
           js: "jquery.datagrid-detailview.js",
           dependencies: ["datagrid"]
        },
        treesearch: {
			js: "jquery.tree-search.js",
			dependencies: ["tree"]
		},
        bgiframe: {
        	js: "jquery.bgiframe.js"
        }
	};
	//easyloader
	window.easyloader = {
		base: "jquery/easyui/1.8.1/",
		theme: "bootstrap",//默认主题设置为bootstrap
		locale: "zh_CN",//默认语言设置为中文
		locales: locales,
		modules: modules,
		css: true,
		timeout: 2000,
		load: function(_1f, _20) {
			if (/\.css$/i.test(_1f)) {
				if (/^http/i.test(_1f)) {
					seajs.use(_1f, _20);
				} else {
					seajs.use(seajs.data.base + base + _1f, _20);
				}
			} else {
				if (/\.js$/i.test(_1f)) {
					if (/^http/i.test(_1f)) {
						seajs.use(_1f, _20);
					} else {
						seajs.use(seajs.data.base + base + _1f, _20);
					}
				} else {
					_load(_1f, _20);
				}
			}
		},
		onProgress: function(_21) {},
		onLoad: function(_22) {}
	};
	
	//加载资源
	function _load(_18, _19) {
		var jss = [];
		var csss = [];
		var customBase = window.easyloader.customBase;
		var customIcon = window.easyloader.customIcon;
		var base = window.easyloader.base;
		var theme = window.easyloader.theme;
		var locale = window.easyloader.locale;
		var locales = window.easyloader.locales;
		var modules = window.easyloader.modules;
		
		//本地语言
		var localeJs = locales[locale];
		jss.push((localeJs && localeJs.startsWith('/') ? customBase:base) + "locale/" + (localeJs.startsWith('/') ? "":"/") + localeJs);
		//图标
		csss.push((customIcon == true ? customBase:base) + "themes/icon.css");
		
		if (typeof _18 == "string") {
			add(_18);
		} else {
			for (var i = 0; i < _18.length; i++) {
				add(_18[i]);
			}
		}
		
		//服务器如果支持combo则一次性加载，否则按顺序递归加载
		if($.walk.comboAble == true){
			//css一次性加载
			seajs.use(csss, function(){
				seajs.use(jss, function(){
					loaded();
				});
			});
		} else {
			//逐个加载
			function oneByOneLoad(js){
				seajs.use(js, function(){
					jss.shift();
					if(jss.length > 0){
						oneByOneLoad(jss[0]);
					} else {
						//一次性加载css
						seajs.use(csss, function(){
							loaded();
						});
					}
				});
			};
			//开始加载
			oneByOneLoad(jss[0]);
		}
		
		function add(_1b) {
			if (!modules[_1b]) {
				return;
			}
			var d = modules[_1b]["dependencies"];
			if (d) {
				for (var i = 0; i < d.length; i++) {
					add(d[i]);
				}
			}
			var css = modules[_1b]["css"];
			if(css){
				css = (css.startsWith('/') ? customBase:base) + "themes/" + theme + (css.startsWith('/') ? "":"/") + css
				if($.inArray(css, csss) == -1){
					csss.push(css);
				}
			}
			var js = modules[_1b]["js"];
			if(js){
				js = (js.startsWith('/') ? customBase:base) + "plugins" + (js.startsWith('/') ? "":"/") + js;
				if($.inArray(js, jss) == -1){
					jss.push(js);
				}
			}
		};
		
		//加载完毕动作
		function loaded(){
			$._setEasyuiLocalLang();
			if (_19) {
				_19();
			}
			easyloader.onLoad(_18);
		};
	};
	
	//初始化
	$(function() {
        var d = $("<div style=\"position:absolute;top:-1000px;width:100px;height:100px;padding:5px\"></div>").appendTo("body");
        $._boxModel = d.outerWidth() != 100;
        d.remove();
        d = $("<div style=\"position:fixed\"></div>").appendTo("body");
        $._positionFixed = (d.css("position") == "fixed");
        d.remove();
        if (window.easyloader && $.parser.auto) {
            $.parser.parse();
        }
    });

//jQuery.easyui 调用时自动加载相应资源 可以不再用easyloader.load方法进行组件的加载
$.fn.extend({
	draggable: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('draggable', function(){ths.draggable(method, opts);});});},
	droppable: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('droppable', function(){ths.droppable(method, opts);});});},
	resizable: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('resizable', function(){ths.resizable(method, opts);});});},
	linkbutton: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('linkbutton', function(){ths.linkbutton(method, opts);});});},
	progressbar: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('progressbar', function(){ths.progressbar(method, opts);});});},
	pagination: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('pagination', function(){ths.pagination(method, opts);});});},
	datagrid: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('datagrid', function(){ths.datagrid(method, opts);});});},
	treegrid: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('treegrid', function(){ths.treegrid(method, opts);});});},
	propertygrid: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('propertygrid', function(){ths.propertygrid(method, opts);});});},
	datalist: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('datalist', function(){ths.datalist(method, opts);});});},
	panel: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('panel', function(){ths.panel(method, opts);});});},
	window: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('window', function(){ths.window(method, opts);});});},
	dialog: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('dialog', function(){ths.dialog(method, opts);});});},
	layout: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('layout', function(){ths.layout(method, opts);});});},
	form: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('form', function(){ths.form(method, opts);});});},
	menu: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('menu', function(){ths.menu(method, opts);});});},
	tabs: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('tabs', function(){ths.tabs(method, opts);});});},
	menubutton: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('menubutton', function(){ths.menubutton(method, opts);});});},
	splitbutton: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('splitbutton', function(){ths.splitbutton(method, opts);});});},
	switchbutton: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('switchbutton', function(){ths.switchbutton(method, opts);});});},
	accordion: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('accordion', function(){ths.accordion(method, opts);});});},
	calendar: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('calendar', function(){ths.calendar(method, opts);});});},
	textbox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('textbox', function(){ths.textbox(method, opts);});});},
	passwordbox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('passwordbox', function(){ths.passwordbox(method, opts);});});},
	filebox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('filebox', function(){ths.filebox(method, opts);});});},
	iRadiobutton: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('radiobutton', function(){ths.iRadiobutton(method, opts);});});},
	iCheckbox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('checkbox', function(){ths.iCheckbox(method, opts);});});},
	iSidemenu: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('sidemenu', function(){ths.iSidemenu(method, opts);});});},
	tagbox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('tagbox', function(){ths.tagbox(method, opts);});});},
	combo: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('combo', function(){ths.combo(method, opts);});});},
	combobox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('combobox', function(){ths.combobox(method, opts);});});},
	combotree: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('combotree', function(){ths.combotree(method, opts);});});},
	combogrid: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('combogrid', function(){ths.combogrid(method, opts);});});},
	combotreegrid: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('combotreegrid', function(){ths.combotreegrid(method, opts);});});},
	validatebox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('validatebox', function(){ths.validatebox(method, opts);});});},
	numberbox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('numberbox', function(){ths.numberbox(method, opts);});});},
	searchbox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('searchbox', function(){ths.searchbox(method, opts);});});},
	spinner: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('spinner', function(){ths.spinner(method, opts);});});},
	numberspinner: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('numberspinner', function(){ths.numberspinner(method, opts);});});},
	timespinner: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('timespinner', function(){ths.timespinner(method, opts);});});},
	tree: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('tree', function(){ths.tree(method, opts);});});},
	datebox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('datebox', function(){ths.datebox(method, opts);});});},
	datetimebox: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('datetimebox', function(){ths.datetimebox(method, opts);});});},
	slider: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('slider', function(){ths.slider(method, opts);});});},
	bgiframe: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('bgiframe', function(){ths.bgiframe(method, opts);});});},
	treesearch: function(method, opts) {return this.each(function(){var ths = $(this);easyloader.load('treesearch', function(){ths.treesearch(method, opts);});});}
});	
//messager
$.messager = {
	show: function(options){easyloader.load('messager', function(){$.messager.show(options);});},
	alert: function(title, msg, icon, fn){easyloader.load('messager', function(){$.messager.alert(title, msg, icon, fn);});},
	confirm: function(title, msg, fn){easyloader.load('messager', function(){$.messager.confirm(title, msg, fn);});},
	prompt: function(title, msg, fn){easyloader.load('messager', function(){$.messager.prompt(title, msg, fn);});},
	progress: function(options){easyloader.load('messager', function(){$.messager.progress(options);});}
}
//parser
$.parser={auto:true,emptyFn:function(){},onComplete:function(_b){},plugins:window.easyloader.modules,parse:function(_c){var aa=[];var plugins=[];$.each($.parser.plugins,function(plugin){var r=$(".easyui-"+plugin+"[auto!='false']",_c);if(r.length>0){aa.push({name:plugin,jq:r});plugins.push(plugin)}});if(aa.length>0&&window.easyloader){easyloader.load(plugins,function(){for(var i=0;i<aa.length;i++){var _f=aa[i].name;var jq=aa[i].jq;jq.each(function(){$(this)[_f]($.data(this,"options")||{})})}$.parser.onComplete.call($.parser,_c)})}else{$.parser.onComplete.call($.parser,_c)}},parseValue:function(_10,_11,_12,_13){ _13=_13||0; var v=$.trim(String(_11||"")); var _14=v.substr(v.length-1,1); if(_14=="%"){ v=parseFloat(v.substr(0,v.length-1)); if(_10.toLowerCase().indexOf("width")>=0){ _13+=_12[0].offsetWidth-_12[0].clientWidth; v=Math.floor((_12.width()-_13)*v/100); }else{ _13+=_12[0].offsetHeight-_12[0].clientHeight; v=Math.floor((_12.height()-_13)*v/100); } }else{ v=parseInt(v)||undefined; } return v; },parseOptions:function(_15,_16){var t=$(_15);var _17={};var s=$.trim(t.attr("data-options"));if(s){if(s.substring(0,1)!="{"){s="{"+s+"}"}_17=(new Function("return "+s))()}$.map(["width","height","left","top","minWidth","maxWidth","minHeight","maxHeight"],function(p){var pv=$.trim(_15.style[p]||"");if(pv){if(pv.indexOf("%")==-1){pv=parseInt(pv);if(isNaN(pv)){pv=undefined}}_17[p]=pv}});if(_16){var _18={};for(var i=0;i<_16.length;i++){var pp=_16[i];if(typeof pp=="string"){_18[pp]=t.attr(pp)}else{for(var _19 in pp){var _1a=pp[_19];if(_1a=="boolean"){_18[_19]=t.attr(_19)?(t.attr(_19)=="true"):undefined}else{if(_1a=="number"){_18[_19]=t.attr(_19)=="0"?0:parseFloat(t.attr(_19))||undefined}}}}}$.extend(_17,_18)}return _17}};
//easyui
$.easyui={indexOfArray:function(a,o,id){for(var i=0,_1=a.length;i<_1;i++){if(id==undefined){if(a[i]==o){return i}}else{if(a[i][o]==id){return i}}}return -1},removeArrayItem:function(a,o,id){if(typeof o=="string"){for(var i=0,_2=a.length;i<_2;i++){if(a[i][o]==id){a.splice(i,1);return}}}else{var _3=this.indexOfArray(a,o);if(_3!=-1){a.splice(_3,1)}}},addArrayItem:function(a,o,r){var _4=this.indexOfArray(a,o,r?r[o]:undefined);if(_4==-1){a.push(r?r:o)}else{a[_4]=r?r:o}},getArrayItem:function(a,o,id){var _5=this.indexOfArray(a,o,id);return _5==-1?null:a[_5]},forEach:function(_6,_7,_8){var _9=[];for(var i=0;i<_6.length;i++){_9.push(_6[i])}while(_9.length){var _a=_9.shift();if(_8(_a)==false){return}if(_7&&_a.children){for(var i=_a.children.length-1;i>=0;i--){_9.unshift(_a.children[i])}}}}};
//other func
$.fn._outerWidth=function(_1b){if(_1b==undefined){if(this[0]==window){return this.width()||document.body.clientWidth}return this.outerWidth()||0}return this._size("width",_1b)};
$.fn._outerHeight=function(_1c){if(_1c==undefined){if(this[0]==window){return this.height()||document.body.clientHeight}return this.outerHeight()||0}return this._size("height",_1c)};
$.fn._scrollLeft=function(_1d){if(_1d==undefined){return this.scrollLeft()}else{return this.each(function(){$(this).scrollLeft(_1d)})}};
$.fn._propAttr = $.fn.prop || $.fn.attr;
$.fn._size=function(_1e,_1f){if(typeof _1e=="string"){if(_1e=="clear"){return this.each(function(){$(this).css({width:"",minWidth:"",maxWidth:"",height:"",minHeight:"",maxHeight:""})})}else{if(_1e=="fit"){return this.each(function(){_20(this,this.tagName=="BODY"?$("body"):$(this).parent(),true)})}else{if(_1e=="unfit"){return this.each(function(){_20(this,$(this).parent(),false)})}else{if(_1f==undefined){return _21(this[0],_1e)}else{return this.each(function(){_21(this,_1e,_1f)})}}}}}else{return this.each(function(){_1f=_1f||$(this).parent();$.extend(_1e,_20(this,_1f,_1e.fit)||{});var r1=_22(this,"width",_1f,_1e);var r2=_22(this,"height",_1f,_1e);if(r1||r2){$(this).addClass("easyui-fluid")}else{$(this).removeClass("easyui-fluid")}})}function _20(_23,_24,fit){if(!_24.length){return false}var t=$(_23)[0];var p=_24[0];var _25=p.fcount||0;if(fit){if(!t.fitted){t.fitted=true;p.fcount=_25+1;$(p).addClass("panel-noscroll");if(p.tagName=="BODY"){$("html").addClass("panel-fit")}}return{width:($(p).width()||1),height:($(p).height()||1)}}else{if(t.fitted){t.fitted=false;p.fcount=_25-1;if(p.fcount==0){$(p).removeClass("panel-noscroll");if(p.tagName=="BODY"){$("html").removeClass("panel-fit")}}}return false}}function _22(_26,_27,_28,_29){var t=$(_26);var p=_27;var p1=p.substr(0,1).toUpperCase()+p.substr(1);var min=$.parser.parseValue("min"+p1,_29["min"+p1],_28);var max=$.parser.parseValue("max"+p1,_29["max"+p1],_28);var val=$.parser.parseValue(p,_29[p],_28);var _2a=(String(_29[p]||"").indexOf("%")>=0?true:false);if(!isNaN(val)){var v=Math.min(Math.max(val,min||0),max||99999);if(!_2a){_29[p]=v}t._size("min"+p1,"");t._size("max"+p1,"");t._size(p,v)}else{t._size(p,"");t._size("min"+p1,min);t._size("max"+p1,max)}return _2a||_29.fit}function _21(_2b,_2c,_2d){var t=$(_2b);if(_2d==undefined){_2d=parseInt(_2b.style[_2c]);if(isNaN(_2d)){return undefined}if($._boxModel){_2d+=_2e()}return _2d}else{if(_2d===""){t.css(_2c,"")}else{if($._boxModel){_2d-=_2e();if(_2d<0){_2d=0}}t.css(_2c,_2d+"px")}}function _2e(){if(_2c.toLowerCase().indexOf("width")>=0){return t.outerWidth()-t.width()}else{return t.outerHeight()-t.height()}}}};
})(jQuery);
/*other*/
(function($){var _2f=null;var _30=null;var _31=false;function _32(e){if(e.touches.length!=1){return}if(!_31){_31=true;dblClickTimer=setTimeout(function(){_31=false},500)}else{clearTimeout(dblClickTimer);_31=false;_33(e,"dblclick")}_2f=setTimeout(function(){_33(e,"contextmenu",3)},1000);_33(e,"mousedown");if($.fn.draggable.isDragging||$.fn.resizable.isResizing){e.preventDefault()}}function _34(e){if(e.touches.length!=1){return}if(_2f){clearTimeout(_2f)}_33(e,"mousemove");if($.fn.draggable.isDragging||$.fn.resizable.isResizing){e.preventDefault()}}function _35(e){if(_2f){clearTimeout(_2f)}_33(e,"mouseup");if($.fn.draggable.isDragging||$.fn.resizable.isResizing){e.preventDefault()}}function _33(e,_36,_37){var _38=new $.Event(_36);_38.pageX=e.changedTouches[0].pageX;_38.pageY=e.changedTouches[0].pageY;_38.which=_37||1;$(e.target).trigger(_38)}if(document.addEventListener){document.addEventListener("touchstart",_32,true);document.addEventListener("touchmove",_34,true);document.addEventListener("touchend",_35,true)}})(jQuery);
