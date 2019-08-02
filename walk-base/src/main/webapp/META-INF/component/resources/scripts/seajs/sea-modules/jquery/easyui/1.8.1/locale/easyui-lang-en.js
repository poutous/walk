jQuery._setEasyuiLocalLang = function(){
	if (jQuery.fn.pagination && jQuery.fn.pagination.defaults){
		jQuery.fn.pagination.defaults.beforePageText = 'Page';
		jQuery.fn.pagination.defaults.afterPageText = 'of {pages}';
		jQuery.fn.pagination.defaults.displayMsg = 'Displaying {from} to {to} of {total} items';
	}
	if (jQuery.fn.datagrid && jQuery.fn.datagrid.defaults){
		jQuery.fn.datagrid.defaults.loadMsg = 'Processing, please wait ...';
		//Start:add by shf675 start
		jQuery.fn.datagrid.defaults.pageSize = 10;
		jQuery.fn.datagrid.defaults.pageList = [5,10,20,30,40,50];
		jQuery.fn.datagrid.defaults.pagination = true;
		jQuery.fn.datagrid.defaults.rownumbers = false;
		jQuery.fn.datagrid.defaults.border = 0;//边框
		jQuery.fn.datagrid.defaults.striped = true;//隔行背景色
		jQuery.fn.datagrid.defaults.singleSelect = true;//是否单选,默认单选
		jQuery.fn.datagrid.defaults.remoteSort = false;//是否服务端排序，默认false
		jQuery.fn.datagrid.defaults.fitColumns = false;//自适应宽度，默认false
		//End:add by shf675 end
	}
	if (jQuery.fn.treegrid && jQuery.fn.datagrid && jQuery.fn.treegrid.defaults){
		jQuery.fn.treegrid.defaults.loadMsg = jQuery.fn.datagrid.defaults.loadMsg;
		//Start:add by shf675 start
		jQuery.fn.treegrid.defaults.pageSize = 10;
		jQuery.fn.treegrid.defaults.pageList = [5,10,20,30,40,50];
		jQuery.fn.treegrid.defaults.pagination = true;
		jQuery.fn.treegrid.defaults.rownumbers = false;
		jQuery.fn.treegrid.defaults.border = 0;
		jQuery.fn.treegrid.defaults.striped = true;
		jQuery.fn.treegrid.defaults.singleSelect = true;
		jQuery.fn.treegrid.defaults.remoteSort = false;//是否服务端排序，默认false
		jQuery.fn.treegrid.defaults.fitColumns = false;//自适应宽度，默认false
		jQuery.fn.treegrid.defaults.animate = true;//是否开启节点展开/关闭时动画效果
		//End:add by shf675 end
	}
	if (jQuery.messager && jQuery.messager.defaults){
		jQuery.messager.defaults.ok = 'Ok';
		jQuery.messager.defaults.cancel = 'Cancel';
	}
	jQuery.map(['validatebox','textbox','passwordbox','filebox','searchbox',
			'combo','combobox','combogrid','combotree',
			'datebox','datetimebox','numberbox',
			'spinner','numberspinner','timespinner','datetimespinner'], function(plugin){
		if (jQuery.fn[plugin] && jQuery.fn[plugin].defaults){
			jQuery.fn[plugin].defaults.missingMessage = 'This field is required.';
		}
	});
	if (jQuery.fn.validatebox && jQuery.fn.validatebox.defaults){
		jQuery.fn.validatebox.defaults.rules.email.message = 'Please enter a valid email address.';
		jQuery.fn.validatebox.defaults.rules.url.message = 'Please enter a valid URL.';
		jQuery.fn.validatebox.defaults.rules.length.message = 'Please enter a value between {0} and {1}.';
		jQuery.fn.validatebox.defaults.rules.remote.message = 'Please fix this field.';
	}
	if (jQuery.fn.calendar && jQuery.fn.calendar.defaults){
		jQuery.fn.calendar.defaults.weeks = ['S','M','T','W','T','F','S'];
		jQuery.fn.calendar.defaults.months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
	}
	if (jQuery.fn.datebox && jQuery.fn.datebox.defaults){
		jQuery.fn.datebox.defaults.currentText = 'Today';
		jQuery.fn.datebox.defaults.closeText = 'Close';
		jQuery.fn.datebox.defaults.okText = 'Ok';
	}
	if (jQuery.fn.datetimebox && jQuery.fn.datebox && jQuery.fn.datetimebox.defaults && jQuery.fn.datebox.defaults){
		jQuery.extend(jQuery.fn.datetimebox.defaults,{
			currentText: jQuery.fn.datebox.defaults.currentText,
			closeText: jQuery.fn.datebox.defaults.closeText,
			okText: jQuery.fn.datebox.defaults.okText
		});
	}
}
