package org.walkframework.restful.model.rsp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页信息返回报文
 *
 * @author shf675
 */
@ApiModel(description = "分页请求参数")
public class PaginationRsp<E> extends RspData {
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "总数", required = true, position = 10)
	private Long total;
	
	@ApiModelProperty(value = "数据列表", required = true, position = 20)
	private List<E> rows = new ArrayList<E>();

	
	public PaginationRsp(){
	}
	
	public PaginationRsp(Long total){
		this.total = total;
	}
	
	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public List<E> getRows() {
		return rows;
	}
	
	public void addRow(E row) {
		this.rows.add(row);
	}

	public void addRows(List<E> rows) {
		this.rows.addAll(rows);
	}
}
