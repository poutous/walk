/**
 * FusionCharts处理工具
 * @type
 */
define(function(require, exports, module) {
require('./FusionChartsCore.js');

/**
 * 生成FusionCharts图表
 * 
 * @param {} renderDiv 渲染至哪个div
 * @param {} chartId 渲染后的图表ID
 * @param {}  xmlData xml字符串
 * @param {} swfPath 图表swf文件路径
 * @param {} width 图表宽度
 * @param {} height 图表高度
 * @return {} 图表对象
 */
exports.renderChart = function(renderDiv, chartId, xmlData, swfPath, width, height) {
	var chart = new FusionCharts(swfPath, chartId, width, height, "0", "1");
	chart.setDataXML(xmlData);
	chart.render(renderDiv);
	return chart;
};

/**
* 解析传入json对象或json字符串生成图表
*/
exports.renderChartByJson = function(renderDiv, chartObj, width, height, context) {
	var isJson = typeof (chartObj) == "object" && Object.prototype.toString.call(chartObj).toLowerCase() == "[object object]" && !chartObj.length;
	if(!isJson) {
		chartObj = $.parseJSON(chartObj);
	}
	var chartId = renderDiv + $.walk.getRandomParam();
	var ctx = "";
	try {
		ctx = context ? context : $.walk.ctx;
	}catch(e){}
	
	var jXmlData = $(chartObj.xmlData);
	var jXmlDataDiv = $("<div></div>").append(jXmlData.attr("exportHandler", ctx + jXmlData.attr("exportHandler")));
	
	var chart = new FusionCharts(ctx + chartObj.swfPath, chartId, width, height, "0", "1");
	chart.setDataXML(jXmlDataDiv.html());
	chart.render(renderDiv);
	return chart;
};

/**
* 保存图片
*/
exports.saveAsImage = function(divId){
	var chartId = $("#" + divId + " > OBJECT").attr("id");
    var chart = getChartFromId(chartId);
    if (chart != null){
		chart.saveAsImage();
	}
};

});