/*
 seajs相关配置，详见：https://github.com/seajs/seajs/issues/262
*/
seajs.config({
	// Sea.js 的基础路径
	base: 'seajs/sea-modules/',

	// 别名配置：当模块标识很长时，可以使用 alias 来简化。 如果不想在这配置别名可直接写路径，例如seajs.use(['arale/position/1.0.1/position'], function(Position) {//代码});
	// 定义规范：
	// 1、jQuery插件以$开头；
	// 2、非jQuery插件首字母大写
	alias: {
		/********1、jQuery插件开始*********************************************/
		//jQuery
		'$': 'jquery/jquery/jquery',
		
		//jQuery
		'$-debug': 'jquery/jquery/jquery-debug',
		
		//jQuery.extend
		'$-extend': 'jquery/jquery/jquery-extend',
		
		//walk框架通用js
		'$walk': 'walk',
		
		//jQuery.EasyUI 相关插件在jquery/easyui/1.8.1/easyloader.sea.js里配置。 easyloader方式按需加载 http://www.jeasyui.com
		'$easyloader': 'jquery/easyui/1.8.1/easyloader.sea',
		
		//jQuery.form 表单插件，可做ajax提交
		'$form': 'jquery/form/1.3.2/form',
		
		//jQuery.beautify 美化checkboxes, radios
		'$beautify': 'jquery/beautify/1.0.0/beautify',
		
		//jQuery.select2 select组件 http://ivaynberg.github.io/select2/
		'$select2': 'jquery/select2/4.0.5/select2',
		
		//jQuery.cascadingSelect 级联刷新下拉列表插件
		'$cascadingSelect': 'jquery/cascadingselect/1.0.0/cascadingselect',
		
		//jQuery.validate 表单验证框架
		'$validate': 'jquery/validate/1.9.0/validate',
		
		//jQuery.blockUI 遮罩层、弹出框
		'$blockui': 'jquery/blockui/2.57.0/blockui',
		
		//jQuery.tipsy tip提示组件
		'$tipsy': 'jquery/tipsy/1.0.0/tipsy',
		
		//jQuery.jGrowl 系统提示事件
		'$jgrowl': 'jquery/jgrowl/1.3.0/jgrowl',
		
		//jQuery.rightmenu 右键菜单组件
		'$rightmenu': 'jquery/rightmenu/1.0.0/rightmenu',
		
		//jQuery.SuperSlide 左侧导航菜单
		'$superslide': 'jquery/superslide/2.1.1/superslide',
		
		//jQuery.scrolltop 返回顶部组件
		'$scrolltop': 'jquery/scrolltop/1.0.0/scrolltop',
		
		//jQuery.zTree 树插件
		'$zTree': 'jquery/ztree/3.5.17/ztree',
		
		//jQuery.syntaxhighlighter 代码高亮显示。 http://balupton.github.io/jquery-syntaxhighlighter/demo/
		'$syntaxhighlighter': 'jquery/syntaxhighlighter/1.0.0/syntaxhighlighter',
		
		//jQuery.fileinput 单文件上传组件
		'$fileinput': 'jquery/fileinput/1.0.0/fileinput',
		
		//jQuery.ajaxfileupload ajax方式上传文件组件
		'$ajaxfileupload': 'jquery/ajaxfileupload/1.0.0/ajaxfileupload',
		
		/********2、其他插件开始*********************************************/
		//IE下支持的某些属性也可在Firefox/Chrome等浏览器支持
		'Browser': 'extra/browser/1.0.0/browser.patch',
		
		//My97 DatePicker 日期选择控件
		'Wdate': 'extra/wdate/4.8/wdate',
		
		//fusioncharts 图表插件
		'Fusioncharts': 'extra/fusioncharts/3.3.0/fusioncharts',
		
		//json2。兼容JSON.stringify。ie6、7、8不支持JSON.stringify。
		'Json2': 'extra/json2/json2-min.js'
	},

	// 路径配置：当目录比较深，或需要跨目录调用模块时，可以使用 paths 来简化书写
	paths: {
//		//业界精选组件：https://spmjs.org/gallery
//		'gallery': 'gallery/'
	},

	// 映射配置：该配置可对模块路径进行映射修改，可用于路径转换、在线调试等。
	//map: [[ '.js', '-debug.js' ],['http://example.com/js/app/', 'http://localhost/js/app/']],

	// 预加载项：使用 preload 配置项，可以在普通模块加载前，提前加载并初始化好指定模块。
	//preload: [Function.prototype.bind ? '': 'es5-safe', this.JSON ? '': 'json'],
	
	// 变量配置：有些场景下，模块路径在运行时才能确定，这时可以使用 vars 变量来配置。
	vars: {
		'locale': 'zh-cn'
	},

	// 调试模式
	debug: true,

	// 文件编码
	charset: 'utf-8'
});