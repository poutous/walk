define(function(require){
require('./jquery.validate.min.js');

//添加正则方法
$.validator.addMethod("regex", function(value, element, param){
    var regex = new RegExp(param);
    return this.optional(element) || regex.test(value);
},"格式错误，请重新输入！");
});