package org.walkframework.restful.security.right;

import java.util.List;

import org.walkframework.base.system.security.DefaultUrlFilterLoader;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;
import org.walkframework.shiro.bean.UrlFilter;

/**
 * url权限加载
 * 
 * @author shf675
 *
 */
public class StatelessUrlFilterLoader extends DefaultUrlFilterLoader {
	
	private String filterName;
	
	@Override
	public void loadUrlFiltersData() {
		if (!isHasLoaded()) {
			try {
				List<IData<String, Object>> list = getDao().selectList("UrlSQL.loadUrlFiltersData");
				if (list.size() > 0) {
					handleUrlFilters(list);
				}
				setHasLoaded(true);
			} catch (Exception e) {
				log.warn("Not loaded to URL permissions data. The possible reason is that the loadUrlFiltersData statement is not implemented in UrlSQL.xml");
			}
		}
	}
	
	/**
	 * 处理url过滤器
	 * 
	 * @param list
	 */
	private void handleUrlFilters(List<IData<String, Object>> list) {
		IData<String, UrlFilter> urlFilters = new DataMap<String, UrlFilter>();
		for (IData<String, Object> data : list) {
			String urlCode = data.getString("MOD_CODE");
			String urlPath = data.getString("MOD_NAME");
			String rightCode = data.getString("RIGHT_CODE", "");
			UrlFilter urlFilter = urlFilters.get(urlCode);
			if(urlFilter == null) {
				urlFilter = new UrlFilter();
				urlFilter.setUrlCode(urlCode);
				urlFilter.setUrlPath(urlPath);
				urlFilter.setRightCodes(rightCode);
				urlFilters.put(urlCode, urlFilter);
			} else {
				urlFilter.setRightCodes(urlFilter.getRightCodes().concat("|").concat(rightCode));
			}
		}
		
		//加入过滤器链
		for (UrlFilter urlFilter : urlFilters.values()) {
			String url = urlFilter.getUrlPath();
			getChainManager().addToFilterChain(url, getFilterName(), urlFilter.getRightCodes());
		}
	}
	
	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

}
