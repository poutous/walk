(function($){
/** trim */
String.prototype.trim = function() {
	return this.replace(/(^\s*)|(\s*$)/g, "");
}
/** starts with */
String.prototype.startsWith = function(prefix) {
	return this.substring(0, prefix.length) == prefix;
}
/** ends width */
String.prototype.endsWith = function(suffix) {
	return this.substring(this.length - suffix.length) == suffix;
}
/** replace all */
String.prototype.replaceAll = function(oldstr, newstr) {
	return this.replace(new RegExp(oldstr,"gm"), newstr);
}

/**
 * 扩展时间类，增加格式化方法
 * 
 * @param fmt 格式，如yyyy-MM-dd hh:mm:ss
 * @returns
 */
Date.prototype.format = function(fmt) {
	var o = { 
		"M+" : this.getMonth()+1,                 //月份 
		"d+" : this.getDate(),                    //日 
		"h+" : this.getHours(),                   //小时 
		"m+" : this.getMinutes(),                 //分 
		"s+" : this.getSeconds(),                 //秒 
		"q+" : Math.floor((this.getMonth()+3)/3), //季度 
		"S"  : this.getMilliseconds()             //毫秒 
	}; 
	if(/(y+)/.test(fmt)) {
		fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
	}
	for(var k in o) {
		if(new RegExp("("+ k +")").test(fmt)){
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length))); 
		}
	}
	return fmt; 
};

//jQuery对象扩展
$.extend({
	//json转换为json 字符串
	object2String : function (object){
		var type = typeof object;
		if ('object' == type){
			if (Array == object.constructor){
				type = 'array';
			} else if (RegExp == object.constructor){
				type = 'regexp';
	   		} else{
				type = 'object';
	   		}
		}
		switch(type){
		case 'undefined':
		case 'unknown':
			return;
		break;
		case 'function':
		case 'boolean':
		case 'regexp':
     		return object.toString();
		break;
		case 'number':
			return isFinite(object) ? object.toString() : 'null';
		break;
		case 'string':
			return '"' + object.replace(/(\\|\")/g,"\\$1").replace(/\n|\r|\t/g,function(){
					var a = arguments[0];                  
					return  (a == '\n') ? '\\n':(a == '\r') ? '\\r':(a == '\t') ? '\\t': "" 
			}) + '"';
     	break;
    	case 'object':
	     	if (object === null) return 'null';
	       	var results = [];
	        for (var property in object) {
	          var value = $.object2String(object[property]);
	          if (value !== undefined)
	            results.push($.object2String(property) + ':' + value);
	        }
			return '{' + results.join(',') + '}';
		break;
		case 'array':
			var results = [];
	        for(var i = 0; i < object.length; i++){
				var value = $.object2String(object[i]);
				if (value !== undefined) results.push(value);
			}
	        return '[' + results.join(',') + ']';
     	break;
	  }
	},
	
	/**
     * 判断是否字符串
     */
	isString : function(obj) {
        return (typeof obj=='string')&&obj.constructor==String; 
    },
    /**
     * 判断是否是数字
     */
    isNumber : function (obj) {
		return (typeof obj == 'number') && obj.constructor == Number;
	},
    /**
     * 判断是否为对象
     */
	isObject : function(obj) {
        return (typeof obj=='object')&&obj.constructor==Object;
    },
	/**
     * 判断是否是json对象
     */
	isJson : function(obj) {
        return typeof (obj) == "object" && Object.prototype.toString.call(obj).toLowerCase() == "[object object]" && !obj.length;
    },
    
	//判断浏览器是否为IE
	isIE : function(){
		return /msie/.test(navigator.userAgent.toLowerCase());
	},
	
    //判断浏览器是否ie8以下
	isIE8Under : function(){
		return this.isIE() && !$.support.opacity && !$.support.style;
	},
	
	//判断浏览器是否ie9以下
	isIE9Under : function(){
		return this.isIE() && (this.isIE8Under() || (!$.support.opacity && $.support.style));
	},
    
	/**
	 * cookie操作
	 * 新建一个cookie：$.cookie('name', 'value');
	 * 新建一个cookie 包括有效期 路径 域名等：$.cookie('name', 'value', {expires: 7, path: '/', domain: 'jquery.com', secure: true});
	 * 删除一个cookie: $.cookie('name', null);
	 *		  
	*/
	cookie : function(name, value, options) {
		if (typeof value != 'undefined') {// name and value given, set cookie
	        options = options || {};
	        if (value === null) {
	            value = '';
	            options.expires = -1;
	        }
	        var expires = '';
	        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
	            var date;
	            if (typeof options.expires == 'number') {
	                date = new Date();
	                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
	            } else {
	                date = options.expires;
	            }
	            expires = '; expires=' + date.toUTCString(); // use expires attribute, max-age is not supported by IE
	        }
	        var path = options.path ? '; path=' + options.path : '';
	        var domain = options.domain ? '; domain=' + options.domain : '';
	        var secure = options.secure ? '; secure' : '';
	        document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
	    } else { // only name given, get cookie
	        var cookieValue = null;
	        if (document.cookie && document.cookie != '') {
	            var cookies = document.cookie.split(';');
	            for (var i = 0; i < cookies.length; i++) {
	                var cookie = $.trim(cookies[i]);
	                // Does this cookie string begin with the name we want?
	                if (cookie.substring(0, name.length + 1) == (name + '=')) {
	                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
	                    break;
	                }
	            }
	        }
	        return cookieValue;
	    }
	},
	
	/**
     * 使低版本浏览器支持placeholder
     */
	initPlaceholder : function(){
		if(this.isIE8Under()){
			var isSupportPlaceholder = ('placeholder' in document.createElement('input'));
			if(!isSupportPlaceholder){
			    $('[placeholder]').focus(function() {
			        var input = $(this);
			        if (input.val() == input.attr('placeholder')) {
			            input.val('');
			            input.removeClass('placeholder');
			        }
			    }).blur(function() {
			        var input = $(this);
			        if (input.val() == '' || input.val() == input.attr('placeholder')) {
			            input.addClass('placeholder');
			            input.val(input.attr('placeholder'));
			        }
			    }).blur();
			};
		}
	}
});

//jQuery函数扩展
$.fn.extend({
	outerHTML : function (s) {
	    return (s) ? this.before(s).remove() : $("<p>").append(this.eq(0).clone()).html();
	},
	
	/**
	 * 抖动效果
	 * intShakes：抖动次数
	 * intDistance：抖动距离
	 * intDuration：抖动持续时间
	 * @link http://blog.csdn.net/u011072139/article/details/70208746
	 *		  
	*/
	shake : function (intShakes, intDistance, intDuration) {
	    this.each(function () {
	        var jqNode = $(this);
	        jqNode.css({ position: 'relative' });
	        for (var x = 1; x <= intShakes; x++) {
	            jqNode.animate({ left: (intDistance * -1) }, (((intDuration / intShakes) / 4)))
	            .animate({ left: intDistance }, ((intDuration / intShakes) / 2))
	            .animate({ left: 0 }, (((intDuration / intShakes) / 4)));
	        }
	    });
	    return this;
	}
	
}); 
})(jQuery);

//textarea和select的值clone的时候会丢掉，在此做修正
(function (original) {
  jQuery.fn.clone = function () {
    var result           = original.apply(this, arguments),
        my_textareas     = this.find('textarea').add(this.filter('textarea')),
        result_textareas = result.find('textarea').add(result.filter('textarea')),
        my_selects       = this.find('select').add(this.filter('select')),
        result_selects   = result.find('select').add(result.filter('select'));
    for (var i = 0, l = my_textareas.length; i < l; ++i) $(result_textareas[i]).val($(my_textareas[i]).val());
    for (var i = 0, l = my_selects.length;   i < l; ++i) result_selects[i].selectedIndex = my_selects[i].selectedIndex;
    return result;
  };
}) (jQuery.fn.clone);

//不支持Console对象的浏览器中使用console不会报错
(function() {
    var method;
    var noop = function () {};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});
    while (length--) {
        method = methods[length];
        // Only stub undefined methods.
        if (!console[method]) {
            console[method] = noop;
        }
    }
}());