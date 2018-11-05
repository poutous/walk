package org.walkframework.restful.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.IData;
import org.walkframework.restful.constant.PaginationConstants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 通用请求报文信息
 * 
 * @author shf675
 */
@ApiModel(description = "通用请求报文信息")
public class ReqInfo<H extends ReqHead, B extends ReqBody> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final static ObjectMapper objectMapper = new ObjectMapper();
	
	protected Common common = SingletonFactory.getInstance(Common.class);
	
	@ApiModelProperty(value = "请求头", required = true, position = 10)
	@Valid
	@NotNull(message = "reqHead节点不能为空")
	private H reqHead;
	
	@ApiModelProperty(value = "请求体", required = true, position = 20)
	@Valid
	@NotNull(message = "reqBody节点不能为空")
	private B reqBody;
	
	//请求头map结构参数
	private IData<Object, Object> headParameters;
	
	//请求体map结构参数
	private IData<Object, Object> bodyParameters;
	
	//分页对象信息
	private Pagination paginationInfo;

	public H getReqHead() {
		return reqHead;
	}

	public void setReqHead(H reqHead) {
		this.reqHead = reqHead;
	}

	public B getReqBody() {
		return reqBody;
	}

	public void setReqBody(B reqBody) {
		this.reqBody = reqBody;
	}
	
	/**
	 * 转换请求头参数为MAP对象
	 * 
	 * @return
	 */
	public IData<Object, Object> headParameters(){
		if(headParameters == null){
			headParameters = common.toMap(getReqHead(), true);
		}
		return headParameters;
	}
	
	/**
	 * 转换请求体参数为MAP对象
	 * 
	 * @return
	 */
	public IData<Object, Object> bodyParameters(){
		if(bodyParameters == null){
			bodyParameters = common.toMap(getReqBody(), true);
		}
		return bodyParameters;
	}
	
	/**
	 * 转换请求头与请求体参数为MAP对象
	 * 
	 * @return
	 */
	public IData<Object, Object> allParameters(){
		IData<Object, Object> allParameters = headParameters();
		if(allParameters != null){
			allParameters.putAll(bodyParameters());
		} else {
			allParameters = bodyParameters();
		}
		return allParameters;
	}
	
	/**
	 * 转换分页对象
	 * @return
	 */
	public Pagination paginationInfo(){
		if(paginationInfo == null){
			Object reqBody = getReqBody();
			if (reqBody instanceof PaginationReq) {
				PaginationReq paginationReq = (PaginationReq) reqBody;
				int pageSize = paginationReq.getPageSize() == null ? PaginationConstants.DEFAULT_PAGE_SIZE : paginationReq.getPageSize().intValue();// 分页数量(每页显示多少条)
				int currPage = paginationReq.getCurrPage() == null ? PaginationConstants.DEFAULT_CURR_PAGE : paginationReq.getCurrPage().intValue();// 当前页
				int start = (currPage - 1) * pageSize;
				start = start < 0 ? 0 : start;
				
				paginationInfo = new Pagination();
				paginationInfo.setRange(start, pageSize);
				paginationInfo.setCurrPage(currPage);
			}
		}
		return paginationInfo;
	}
	
	@Override
	public String toString() {
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return super.toString();
		}
	}
}
