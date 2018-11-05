/**
 * --------------------------------------------------------------------
 * jQuery customfileinput plugin
 * Author: Scott Jehl, scott@filamentgroup.com
 * Copyright (c) 2009 Filament Group 
 * licensed under MIT (filamentgroup.com/examples/mit-license.txt)
 * --------------------------------------------------------------------
 * Modified by maimairel (maimairel@yahoo.com) for use in a ThemeForest theme
 * --------------------------------------------------------------------
 */
 
$.fn.customFileInput = function(){
	//apply events and styles for file input element
	return $(this).each(function() {
		var fileInput = $(this)
			.addClass('customfile-input') //add class for CSS
			.mouseover(function(){ upload.addClass('customfile-hover'); })
			.mouseout(function(){ upload.removeClass('customfile-hover'); })
			.focus(function(){
				upload.addClass('customfile-focus'); 
				fileInput.data('val', fileInput.val());
			})
			.blur(function(){ 
				upload.removeClass('customfile-focus');
				$(this).trigger('checkChange');
			 })
			 .bind('disable',function(){
				fileInput.attr('disabled',true);
				upload.addClass('customfile-disabled');
			})
			.bind('enable',function(){
				fileInput.removeAttr('disabled');
				upload.removeClass('customfile-disabled');
			})
			.bind('checkChange', function(){
				if(fileInput.val() && fileInput.val() != fileInput.data('val')){
					fileInput.trigger('change');
				}
			})
			.bind('change',function(){
				//get file name
				var fileName = $(this).val().split(/\\/).pop();
				//get file extension
				var fileExt = 'customfile-ext-' + fileName.split('.').pop().toLowerCase();
				//update the feedback
				uploadFeedback
					.text(fileName) //set feedback text to filename
					.removeClass(uploadFeedback.data('fileExt') || '') //remove any existing file extension class
					.addClass(fileExt) //add file extension class
					.data('fileExt', fileExt) //store file extension for class removal on next change
					.addClass('customfile-feedback-populated'); //add class to show populated state
				//change text of button	
				uploadButton.text('浏览');	
			})
			.click(function(){ //for IE and Opera, make sure change fires after choosing a file, using an async callback
				fileInput.data('val', fileInput.val());
				setTimeout(function(){
					fileInput.trigger('checkChange');
				},100);
			});
			
		//create custom control container
		var upload = $('<div class="customfile"></div>');
		//create custom control button
		var uploadButton = $('<span class="customfile-button" aria-hidden="true">浏览</span>').appendTo(upload);
		//create custom control feedback
		var uploadFeedback = $('<span class="customfile-feedback" aria-hidden="true">请选择文件...</span>').appendTo(upload);
		
		//match disabled state
		if(fileInput.is('[disabled]')){
			fileInput.trigger('disable');
		}
			
		
		//on mousemove, keep file input under the cursor to steal click
		upload
			.mousemove(function(e){
				fileInput.css({
					'left': e.pageX - upload.offset().left - fileInput.outerWidth() + 20, //position right side 20px right of cursor X)
					'top': e.pageY - upload.offset().top - 3
				});
			})
			.insertAfter(fileInput); //insert after the input
		
		fileInput.appendTo(upload);
	});
};

$.fn.initFileInput = function(){
	var fileSource = "<div class='customfile-div'>"+
					 "	<div class='customfile-wapper'></div>"+
					 "	<div class='customfile-add' title='继续添加'></div>"+
					 "	<div class='customfile-delete' title='删除'></div>"+
					 "	<div class='customfile-clear'></div>"+
					 "</div>";
	
	//生成附件上传组件
	$("input[type='file'].w-fileinput").each(function(){
		var uploadfile = $(this);
		uploadfile.attr("w-file-name", uploadfile.attr("name"));
		var fileWidth = uploadfile.width();
		var customfilediv = uploadfile.after(fileSource).next();
		customfilediv.find(".customfile-wapper").append(uploadfile);
		customfilediv.find(".customfile-wapper").width(fileWidth);
		customfilediv.attr("w-file-delete", "false");//不可删除
		if(!uploadfile.attr("add")){
			customfilediv.find(".customfile-add").remove();
		}
		if(!uploadfile.attr("del")){
			customfilediv.find(".customfile-delete").remove();
		}
		uploadfile.customFileInput();
	});
	//绑定删除附件
	$(".customfile-delete").on('click', function() {
		var customfileDiv = $(this).parents(".customfile-div");
		if(customfileDiv.attr("w-file-delete") != "false"){
			customfileDiv.remove();
		} else {
			alert("原始文件不可删除！");
		}
	});
	//绑定添加附件
	$(".customfile-add").on('click', function() {
		var addBtn = $(this);
		var customfilediv = addBtn.parent(".customfile-div");
		var uploadfile = customfilediv.find(".w-fileinput").clone();
		var fileWidth = customfilediv.find(".w-fileinput").width();
		var lastcustomfilediv = customfilediv.siblings(".customfile-div").last();
		if(customfilediv.next(".customfile-div").size() == 0){
			lastcustomfilediv = customfilediv;
		}
		var originalFileName = uploadfile.attr("w-file-name");
		var fileNum = $(".w-fileinput[w-file-name='"+originalFileName+"']").size();
		var newFileName = originalFileName + "_" + (fileNum + 1);
		if(uploadfile.attr("maxNum") && fileNum >= parseInt(uploadfile.attr("maxNum"))){
			alert("超出最大文件数量！");
			return false;
		}
		
		//console.log(newFileName);
		uploadfile.attr("id", newFileName);
		uploadfile.attr("name", newFileName);
		var newcustomfilediv = $(fileSource);
		newcustomfilediv.find(".customfile-wapper").append(uploadfile);
		newcustomfilediv.find(".customfile-wapper").width(fileWidth);
		lastcustomfilediv.after(newcustomfilediv);
		uploadfile.customFileInput();
	});
};