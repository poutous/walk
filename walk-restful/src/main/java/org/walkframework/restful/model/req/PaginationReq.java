package org.walkframework.restful.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import org.walkframework.restful.constant.PaginationConstants;

/**
 * 分页请求报文
 * 需要分页时继承本类，反之直接继承BaseReq
 * 
 * @author shf675
 *
 */
@ApiModel(description = "分页信息")
public class PaginationReq extends ReqBody {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "当前页，默认第一页", example = "1", position = 999998)
	@NotNull
	private Integer currPage;

	@ApiModelProperty(value = "分页数量，默认返回10页", example = "10", position = 999999)
	@NotNull
	@Max(value = PaginationConstants.MAX_PAGE_SIZE)
	private Integer pageSize;

	public Integer getCurrPage() {
		return currPage;
	}

	public void setCurrPage(Integer currPage) {
		this.currPage = currPage;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
}
