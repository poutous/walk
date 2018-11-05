package org.walkframework.base.system.bind.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.data.bean.Pagination;

/**
 * 分页参数解决器
 * 
 * @author liuqf5
 */
public class PaginationMethodArgumentResolver extends BaseMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int DEFAULT_CURR_PAGE = 1;
	
	//分页尺寸参数名
	private String pageSizeParamName = CommonConstants.PAGINATION_ROWS;
	
	//当前页参数名
	private String pageParamName = CommonConstants.PAGINATION_PAGE;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Pagination.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		// jQuery EasyUI DataGrid
		String actionType = webRequest.getParameter(CommonConstants.ACTION_TYPE);
		if (CommonConstants.ACTION_TYPE_EXPORT.equals(actionType) || CommonConstants.ACTION_TYPE_ASYN_EXPORT.equals(actionType)) {// 同步或异步导出
			return new Pagination(true);
		} else if (CommonConstants.ACTION_TYPE_EXPAND.equals(actionType)) {// 节点展开
			return null;
		}
		
		//通用分页查询对象
		String rows = webRequest.getParameter(getPageSizeParamName());
		String page = webRequest.getParameter(getPageParamName());
		int pageSize = StringUtils.isEmpty(rows) ? DEFAULT_PAGE_SIZE : Integer.parseInt(rows);// 分页数量(每页显示多少条)
		int currPage = StringUtils.isEmpty(page) ? DEFAULT_CURR_PAGE : Integer.parseInt(page);// 当前页
		int start = (currPage - 1) * pageSize;
		start = start < 0 ? 0 : start;
		Pagination pagination = new Pagination();
		pagination.setRange(start, pageSize);
		pagination.setCurrPage(currPage);
		return pagination;
	}

	public String getPageSizeParamName() {
		return pageSizeParamName;
	}

	public void setPageSizeParamName(String pageSizeParamName) {
		this.pageSizeParamName = pageSizeParamName;
	}

	public String getPageParamName() {
		return pageParamName;
	}

	public void setPageParamName(String pageParamName) {
		this.pageParamName = pageParamName;
	}
}
