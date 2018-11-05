$(function() {
	//初始化顶部菜单
	initTopMenu();
	
	//初始化左侧菜单
	initLeftMenu();
	
	//加载返回顶部组件
	if(!$.isIE8Under()){
		seajs.use('$scrolltop');
	}
	
});

//初始化顶部菜单
function initTopMenu(){
	//绑定顶部菜单点击事件
	$("#nav-top-menu .menu").bind('click', function(){
		//活动效果设置
		var menu = $(this);
		menu.find(".menuName").addClass("active");
		menu.siblings().find(".menuName").removeClass("active");
		
		//绑定左侧菜单
		var targetModule = menu.attr("targetModule");
		$("#nav-mid").find("#" + targetModule).slideDown();
		$("#nav-mid").find("#" + targetModule).siblings(".menuModule").hide();
	});
	
	//添加属性
	$("#nav-mid").find(".menuModule .menuList .menu").each(function(i){
		if(!$(this).attr("menuId")){
			$(this).attr("menuId", "menuId_" + (i+1));
		}
		$(this).attr("title", $.trim($(this).text()));
	});
	
	//绑定退出
	$("#nav-top #logout").bind('click', function(){
		$.walk.confirm('确认退出吗？', function(ok){
			if(ok){
				logout();
			}
		});
	});
}

//初始化左侧导航菜单
function initLeftMenu(){
	var devUrl = $("#develop").attr("devUrl");
	if(devUrl){
		$("#develop").load(devUrl, function(){
			startInitLeftMenu();
		});
	} else {
		startInitLeftMenu();
	}
}

//初始化左侧导航菜单
function startInitLeftMenu(){
	//绑定左侧菜单点击事件
	$("#nav-mid .menuPart .menuList .menu").bind('click', function(){
		$("#nav-mid .menuPart .menuList .menu").removeClass("active");
		$(this).addClass("active");
	});
	
	//展开/收起
	$("#nav-mid .menuPart .title").bind('click', function(){
		var title = $(this);
		var arrow = title.find(".arrow");
		if(arrow.hasClass("up")){
			arrow.removeClass("up").addClass("down");
			title.next(".menuList").slideDown();
		} else if(arrow.hasClass("down")){
			arrow.removeClass("down").addClass("up");
			title.next(".menuList").slideUp();
		}
	});
	
	//添加属性
	$("#nav-mid").find(".menuModule .menuList .menu").each(function(i){
		if(!$(this).attr("menuId")){
			$(this).attr("menuId", "menuId_" + (i+1));
		}
	});
	
	//初始化菜单快捷搜索
	initQuickSearch();
	
	//记录左侧菜单的宽度
	$("#nav-mid-left").attr("width", $("#nav-mid-left").width());
}

//初始化菜单快捷搜索
function initQuickSearch(){
	//注册回车事件
	$(document).keydown(function(event){
		if(event.keyCode == 13){
			quickSearch();
		}
	});
	
	//快捷搜索按钮事件
	$("#quickSearchBtn").click(function(){
		quickSearch();
	});
	
	//快捷搜索输入框绑定事件
	$("#quickSearch").bind('blur', function(){
		var search = $(this);
		if($.trim(search.val()) == ''){
			showTargetModule(targetModule);
		}
	});
}

//显示目标目录
function showTargetModule(){
	var targetModule = $("#nav-top-menu").find(".menuName.active").parent(".menu").attr("targetModule");
	$("#nav-mid").find("#" + targetModule).slideDown();
	$("#nav-mid").find("#" + targetModule).siblings(".menuModule").hide();
}

//快捷搜索
function quickSearch(){
	var value = $("#quickSearch").val();
	if($.trim(value)){
		var searchedMenus = $("#nav-mid-left").find(".menuModule[id!='searchMenuModule']").find(".menuList").find(".menu[title*='" + value + "']");
		$("#searchMenuModule").slideDown();
		$("#searchMenuModule").siblings(".menuModule").hide();
		$("#searchMenuModule").find(".menuList").empty().append(searchedMenus.clone(true));
	} else {
		showTargetModule();
	}
}

//退出
function logout(){
	window.location.href = $.walk.ctx + "/logout";
}