package org.walkframework.base.mvc.controller.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.data.util.InParam;
import org.walkframework.fusioncharts.util.FusionChartsUtils;


/**
 * 示例Controller
 *
 */
@RestController
@RequestMapping("/example")
public class ExampleController extends BaseController{
	
	/**
	 * 查询图表
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryChart")
	public String queryChart(InParam<String, Object> inParam) throws Exception {
		List<Map<String, Object>> dataList = getDataList();
		return FusionChartsUtils.lists2FusionChartsJson("example1/exampleChart.ExampleChart", dataList, inParam.getString("templateName"));
	}
	
	/**
	 * 模拟造数据
	 * @return
	 */
	private List<Map<String, Object>> getDataList() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("DEPART_NAME", "长春分公司");
		map1.put("DEPART_ID", "00011");
		map1.put("CHAIN_VALUE", "-7.48%");
		map1.put("PLL_VALUE", "132.%");
		map1.put("CUR_AIM_VALUE", "72");
		map1.put("CUR_VALUE", "95.04");
		list.add(map1);
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("DEPART_NAME", "吉林分公司");
		map2.put("DEPART_ID", "00012");
		map2.put("CHAIN_VALUE", "-8.31%");
		map2.put("PLL_VALUE", "21.33%");
		map2.put("CUR_AIM_VALUE", "100");
		map2.put("CUR_VALUE", "15.36");
		list.add(map2);
		Map<String, Object> map3 = new HashMap<String, Object>();
		map3.put("DEPART_NAME", "延边分公司");
		map3.put("DEPART_ID", "00013");
		map3.put("CHAIN_VALUE", "-15.55%");
		map3.put("PLL_VALUE", "8.14%");
		map3.put("CUR_AIM_VALUE", "50");
		map3.put("CUR_VALUE", "5.86");
		list.add(map3);
		Map<String, Object> map4 = new HashMap<String, Object>();
		map4.put("DEPART_NAME", "四平分公司");
		map4.put("DEPART_ID", "00014");
		map4.put("CHAIN_VALUE", "-21.38%");
		map4.put("PLL_VALUE", "13.29%");
		map4.put("CUR_AIM_VALUE", "60");
		map4.put("CUR_VALUE", "9.57");
		list.add(map4);
		Map<String, Object> map5 = new HashMap<String, Object>();
		map5.put("DEPART_NAME", "通化分公司");
		map5.put("DEPART_ID", "00015");
		map5.put("CHAIN_VALUE", "-55.38%");
		map5.put("PLL_VALUE", "41.92%");
		map5.put("CUR_AIM_VALUE", "90");
		map5.put("CUR_VALUE", "30.18");
		list.add(map5);
		Map<String, Object> map6 = new HashMap<String, Object>();
		map6.put("DEPART_NAME", "白城分公司");
		map6.put("DEPART_ID", "00016");
		map6.put("CHAIN_VALUE", "-21.35%");
		map6.put("PLL_VALUE", "18.78%");
		map6.put("CUR_AIM_VALUE", "150");
		map6.put("CUR_VALUE", "13.52");
		list.add(map6);
		Map<String, Object> map7 = new HashMap<String, Object>();
		map7.put("DEPART_NAME", "辽源分公司");
		map7.put("DEPART_ID", "00017");
		map7.put("CHAIN_VALUE", "44.35%");
		map7.put("PLL_VALUE", "6.82%");
		map7.put("CUR_AIM_VALUE", "80");
		map7.put("CUR_VALUE", "4.91");
		list.add(map7);
		Map<String, Object> map8 = new HashMap<String, Object>();
		map8.put("DEPART_NAME", "松原分公司");
		map8.put("DEPART_ID", "00018");
		map8.put("CHAIN_VALUE", "6.39%");
		map8.put("PLL_VALUE", "10.15%");
		map8.put("CUR_AIM_VALUE", "110");
		map8.put("CUR_VALUE", "7.31");
		list.add(map8);
		Map<String, Object> map9 = new HashMap<String, Object>();
		map9.put("DEPART_NAME", "白山分公司");
		map9.put("DEPART_ID", "00019");
		map9.put("CHAIN_VALUE", "-48.28%");
		map9.put("PLL_VALUE", "6.1%");
		map9.put("CUR_AIM_VALUE", "50");
		map9.put("CUR_VALUE", "4.4");
		list.add(map9);
		return list;
	}
}
