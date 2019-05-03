/**
 * walk框架ajaxfileupload组件
 * 
 * @author shf675
 *
 */
(function($){
//扩展$.walk 加入ajaxFileUpload相关方法
$.extend($.walk, {
	//附件上传
	ajaxFileUpload: function (fileElement) {
		var _this = this;
		if(!_this.ajaxFileUploadCheck(fileElement)){
			return false;
		};
		
		var fileElementId = fileElement.id;
		//设置name属性
		$(fileElement).attr("name", fileElementId);
		
		$.ajaxFileUpload({
			url : $.walk.ctx + "/ajaxfileupload/upload",
			type: "POST",
			fileElementId: fileElementId,
			data: {
				fileName: fileElementId
			},
			dataType: "text/html",
			secureuri: false,
			success : function(files) {
				document.getElementById(fileElementId).value = '';
				_this.showFileDiv(fileElementId, JSON.parse(files));
			},
			error : function() {
				$.walk.alert("上传文件失败！", "error");
			}
		});
	},

	//显示上传文件
	showFileDiv: function (fileElementId, files) {
		var _this = this;
		var fileItem = '' +
		'<div class="file-item">' +
		'	<input type="text" class="fileId" name="'+fileElementId+'_fileId" style="display: none"/>' +
		'	<img class="folder" src="'+$.walk.ctx+'/component/resources/scripts/seajs/sea-modules/jquery/ajaxfileupload/1.0.0/images/folder.png" alt="" />' +
		'	<span class="file">' +
		'		<a class="fileName" href="javascript:void(0)"></a>' +
		'		<a class="fileSize" href="javascript:void(0)"></a>' +
		'	</span>' +
		'	<img class="download" src="'+$.walk.ctx+'/component/resources/scripts/seajs/sea-modules/jquery/ajaxfileupload/1.0.0/images/download.png" alt="" />' +
		'	<img class="delete" src="'+$.walk.ctx+'/component/resources/scripts/seajs/sea-modules/jquery/ajaxfileupload/1.0.0/images/delete.png" alt="" />' +
		'</div>';
		
		var fe = $("#" + fileElementId);
		var au = fe.parent().parent();
		var fileList = au.find(".files-list");
		if(fileList.size() == 0){
			au.append('<div class="files-list"></div>');
			fileList = au.find(".files-list");
		}
		
		//插入文件
		for (var i = 0; i < files.length; i++) {
			var file = files[i];
			var item = $(fileItem);
			item.find(".fileId").val(file.fileId);
			item.find(".fileName").text(file.fileName).attr("title", file.fileName);
			item.find(".fileSize").text(Math.floor(file.fileSize/1024) + "KB");
			item.find(".download").click(function(){
				_this.ajaxFileUploadDownload(file.fileId);
			});
			item.find(".delete").click(function() {
				_this.ajaxFileUploadDelete($(this).get(0));
			});
			fileList.append(item);
		}
	},
	
	//下载
	ajaxFileUploadDownload: function (fileId){
		window.location.href = $.walk.ctx + '/fileserver/down/' + fileId;
	},

	//删除文件
	ajaxFileUploadDelete: function (obj){
		$.walk.confirm("确认删除吗？", function(ok){
			if(ok) {
				$(obj).parent().remove();
			}
		});
	},
	
	//校验文件类型
	ajaxFileUploadCheck: function (fileElement){
		var _this = this;
		if(!fileElement.id){
			$.walk.alert('未指定组件id属性！', 'error');
			return false;
		}
		
		var files = fileElement.files;
		if (!files || files.length == 0) {
			$.walk.alert('未选择上传文件', 'error');
			return false;
		}
		var types = $(fileElement).parent(".files-btn").find(".fileType").find(".types").text().trim();
		if(!types){
			//未设置文件上传类型限制则直接校验通过
			return true;
		}
		for(var i = 0; i< files.length; i++){
			if(!_this.ajaxFileUploadCheckFileType(files[i], types)) {
				document.getElementById(fileElement.id).value = '';
				return false;
			}
		}
		return true;
	},

	//检查文件类型
	ajaxFileUploadCheckFileType: function (file, types) {
		var reg = new RegExp("\.(" + types + ")$", "gi");
		if (!reg.test(file.name)) {
			$.walk.alert('文件[' + file.name + ']类型受限！', 'error');
			return false;
		}
		return true;
	}
});
})(jQuery);