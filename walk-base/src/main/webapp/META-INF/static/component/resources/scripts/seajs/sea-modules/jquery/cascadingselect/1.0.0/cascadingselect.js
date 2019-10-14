/**
 * walk框架内部级联下拉列表 
 * 
 * 选项：
 * relyon:        依赖的select组件的名称或ID，名称会默认查询本form内的指定名称select组件，查询不到则按ID查询
 * serviceMethod: 调用的后台方法 
 * valueItem:     加载下拉列表之后option.value，对应查询出列表中的字段名，对应Options标签中的key 
 * textItem:      加载下拉列表之后显示的列表内容，对应查询出列表中的字段名，对应Options标签中的name 
 * defaultValue:  加载下拉列表之后默认选择的选项，对应Options标签中的value 
 * typeId:        同Options标签，查询STATIC表对应的数据 
 * isfull:        同Options标签，选择是否默认添加“全部”下拉选项 
 * lazyInit:      是否延迟加载，判断是在初始化时加载还是在依赖的下拉框组件变化时才加载
 * 
 * @version 1.0.0
 * @author mengqk
 * 
 */
$(function() {
    // 绑定需要级联刷新的下拉菜单
    $("select[relyon]").each(function() {
        var selObj = $(this);
        var relyOn = selObj.attr("relyon");
        var relyOnSelObj = findSelectObject(selObj, relyOn);
        
        if (null == relyOnSelObj || relyOnSelObj.length == 0) {
            return; // 未找到relyon对象则不进行事件绑定
        }
        
        var qryData = {// 保存原始请求数据
                serviceMethod : selObj.attr("serviceMethod"),
                lazyInit : selObj.attr("lazyInit"),
                typeId : selObj.attr("typeId"),
                defaultValue : selObj.attr("defaultValue"),
                valueItem : selObj.attr("valueItem"),
                textItem : selObj.attr("textItem"),
                isfull : selObj.attr("isfull")
        };
        relyOnSelObj.on("change", function() {
            //selObj.attr("disabled", true);
            var realQueryData = $.extend({}, qryData);
            if (realQueryData.serviceMethod && realQueryData.serviceMethod != "") {
                realQueryData.serviceMethod = handleServiceMethod(selObj, $(this), realQueryData.serviceMethod);
            }
            $.ajax({
                url : $.walk.ctx + '/options/tag',
                data : realQueryData,
                type : "POST",
                dataType : "html",
                cache: false,
                success : function(data){
                    //selObj.attr("disabled", false);
                    selObj.html(data);
                    reInitSelect2(selObj);
                },
                error : function() {
                    //selObj.attr("disabled", false);
                }
            });
        });
        
        if (qryData.lazyInit != "true") {
            relyOnSelObj.trigger("change");
        } else if (qryData.isfull != "false") {
            selObj.html("<option value=''>全部</option>");
            reInitSelect2(selObj);
        }
    });
    /**
     * 重新初始化Select2
     */
    function reInitSelect2(selObj) {
        selObj.hasClass("w-select2") && !$.isIE9Under() 
        && seajs.use('$select2', function() {selObj.select2("destroy") && selObj.select2();});
        selObj.trigger("change");
    }
    
    /**
     * 处理serviceMethod，给变量设值
     */
    function handleServiceMethod(selObj, relyOnSelObj, serviceMethod) {
        if (null == serviceMethod || serviceMethod == "" || serviceMethod.indexOf("$") == -1) {
            return serviceMethod;
        }
        try {
            var splitedArr = serviceMethod.split("(");
            var afterHandled = splitedArr[0] + "(";
            splitedArr = splitedArr[1].split(")")[0].split(",");
            for (var idx in splitedArr) {
                var val = $.trim(splitedArr[idx]);
                if (val == "$") {
                    val = relyOnSelObj.val();
                } else if (val.length > 1 && val.charAt(0) == "$") {
                    var finder = val.substring(1);
                    var obj = findSelectObject(selObj, finder);
                    if (obj && obj.length > 0) {
                        val = obj.val();
                    } else {
                        val = "";
                    }
                }
                afterHandled += val;
                afterHandled += ",";
            }
            afterHandled = afterHandled.substring(0, afterHandled.length - 1);
            afterHandled += ")";
            return afterHandled;
        } catch (e) {
        }
        return serviceMethod;
    }
    
    /**
     * 寻找与selObj对象相关的select组件
     */
    function findSelectObject(selObj, finder) {
        var parentForm = selObj.parent("form");
        var relyOnSelObj = null;
        if (parentForm && parentForm.length > 0) {
            relyOnSelObj = parentForm.find("select[name='" + finder + "']");
        }
        
        if (null == relyOnSelObj || relyOnSelObj.length == 0) {
            relyOnSelObj = $("#" + finder);
        }
        
        return relyOnSelObj;
    }
});
