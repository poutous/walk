package org.walkframework.restful.model.rsp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import org.walkframework.restful.constant.RspConstants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 通用返回报文信息
 * 
 * @author shf675
 */
@ApiModel(description = "通用返回报文信息")
public class RspInfo<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final static ObjectMapper objectMapper = new ObjectMapper();
	
	@ApiModelProperty(value = "返回代码", required = true, position = 10)
	private Integer rspCode = RspConstants.SUCCESS;
	
	@ApiModelProperty(value = "返回备注", position = 20)
	private String rspDesc;
	
	@ApiModelProperty(value = "返回内容", position = 30)
	private T rspData;
	
	public RspInfo(){
	}
	
	public RspInfo(T rspData){
		this(RspConstants.SUCCESS, null, rspData);
	}
	
	public RspInfo(Integer rspCode, T rspData){
		this(rspCode, null, rspData);
	}
	
	public RspInfo(Integer rspCode, String rspDesc, T rspData){
		this.rspCode = rspCode;
		this.rspDesc = rspDesc;
		this.rspData = rspData;
	}

	public T getRspData() {
		return rspData;
	}

	public RspInfo<T> setRspData(T rspData) {
		this.rspData = rspData;
		return this;
	}

	public Integer getRspCode() {
		return rspCode;
	}

	public RspInfo<T> setRspCode(Integer rspCode) {
		this.rspCode = rspCode;
		return this;
	}

	public String getRspDesc() {
		return rspDesc;
	}

	public RspInfo<T> setRspDesc(String rspDesc) {
		this.rspDesc = rspDesc;
		return this;
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
