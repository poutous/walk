package org.walkframework.data.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回对象
 * 
 * @author shf675
 */
public class PageData<E> implements Serializable {
	private static final long serialVersionUID = -6651530684894722642L;

	//数据总条数
	private long total;

	//当前页码，默认第一页
	private int currPage = 1;

	//每页条数，默认10条
	private int pageSize = 10;

	//数据列表
	private List<E> rows;

	public List<E> getRows() {
		return rows;
	}

	public void setRows(List<E> rows) {
		this.rows = rows;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public int getCurrPage() {
		return currPage;
	}

	public void setCurrPage(int currPage) {
		this.currPage = currPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * 获取总页数
	 * @return
	 */
	public long getTotalPages() {
		return getTotal() % getPageSize() == 0 ? getTotal() / getPageSize() : getTotal() / getPageSize() + 1;
	}
}
