package org.walkframework.activiti.mvc.service.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.base.mvc.service.base.BaseService;
import org.walkframework.base.tools.utils.ParamTranslateUtil;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.IData;
import org.walkframework.data.util.InParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 流程模型服务
 * 
 * @author shf675
 */
@Service("actModelService")
public class ActModelService extends BaseService {
	
	private static final String MODEL_CUSTOM_JSON = "customJson";

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	ProcessEngine processEngine;

	@Autowired
	HistoryService historyService;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 模型列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	public PageData<IData<Object, Object>> queryList(InParam<String, Object> inParam, Pagination pagination) {
		ModelQuery modelQuery = repositoryService.createModelQuery().latestVersion().orderByLastUpdateTime().desc();
		if (StringUtils.isNotEmpty(inParam.getString("id"))) {
			modelQuery.modelId(inParam.getString("id"));
		}
		if (StringUtils.isNotEmpty(inParam.getString("name"))) {
			modelQuery.modelName(inParam.getString("name"));
		}
		if (StringUtils.isNotEmpty(inParam.getString("key"))) {
			modelQuery.modelKey(inParam.getString("key"));
		}
		if (StringUtils.isNotEmpty(inParam.getString("category"))) {
			modelQuery.modelCategory(inParam.getString("category"));
		}
		
		List<Model> list = modelQuery.listPage(pagination.getStart(), pagination.getStart() + pagination.getSize());
		List<IData<Object, Object>> retList = new ArrayList<IData<Object, Object>>();
		if(CollectionUtils.isNotEmpty(list)){
			for (Model model : list) {
				IData<Object, Object> m = common.toMap(model);
				m.put("categoryName", ParamTranslateUtil.getTranslateValue(model.getCategory(), "MODEL_CATEGORY"));
				if(StringUtils.isNotEmpty(model.getMetaInfo())){
					JSONObject json = JSON.parseObject(model.getMetaInfo());
					m.put("description", json.getString("description"));
					m.put(MODEL_CUSTOM_JSON, json.getString(MODEL_CUSTOM_JSON));
					
				}
				retList.add(m);
			}
		}
		
		PageData<IData<Object, Object>> pageData = new PageData<IData<Object, Object>>();
		pageData.setTotal(modelQuery.count());
		pageData.setRows(retList);

		return pageData;
	}

	/**
	 * 创建模型
	 * 
	 * @param inParam
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Model doCreate(InParam<String, Object> inParam) throws UnsupportedEncodingException {

		ObjectNode editorNode = objectMapper.createObjectNode();
		editorNode.put("id", "canvas");
		editorNode.put("resourceId", "canvas");
		ObjectNode properties = objectMapper.createObjectNode();
		properties.put("process_author", "walk");
		editorNode.set("properties", properties);
		ObjectNode stencilset = objectMapper.createObjectNode();
		stencilset.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
		editorNode.set("stencilset", stencilset);

		Model modelData = repositoryService.newModel();
		modelData.setKey(inParam.getString("key", ""));
		modelData.setName(inParam.getString("name"));
		modelData.setCategory(inParam.getString("category"));
		modelData.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(modelData.getKey()).count() + 1)));
		
		ObjectNode modelObjectNode = objectMapper.createObjectNode();
		modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, modelData.getName());
		modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
		modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, inParam.getString("description", ""));
		if(StringUtils.isNotEmpty(inParam.getString(MODEL_CUSTOM_JSON, ""))){
			modelObjectNode.set(MODEL_CUSTOM_JSON, getCustomJson(inParam.getString(MODEL_CUSTOM_JSON, "")));
		}
		modelData.setMetaInfo(modelObjectNode.toString());

		repositoryService.saveModel(modelData);
		repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
		return modelData;
	}
	
	/**
	 * 复制模型
	 * 
	 * @param modelId
	 * @return
	 */
	public Model doCopy(String modelId) {
		Model modelData = repositoryService.newModel();
		Model oldModel = repositoryService.getModel(modelId);
		modelData.setName(oldModel.getName() + "-复制");
		modelData.setKey(oldModel.getKey() + "-copy");
		modelData.setCategory(oldModel.getCategory());
		modelData.setMetaInfo(oldModel.getMetaInfo());
		repositoryService.saveModel(modelData);
		repositoryService.addModelEditorSource(modelData.getId(), this.repositoryService.getModelEditorSource(oldModel.getId()));
		repositoryService.addModelEditorSourceExtra(modelData.getId(), this.repositoryService.getModelEditorSourceExtra(oldModel.getId()));
		return modelData;
	}

	/**
	 * 部署模型
	 * 
	 * @throws Exception
	 */
	public String doDeploy(String id) throws Exception {
		String processDefinitionId = "";
		Model modelData = repositoryService.getModel(id);
		BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
		JsonNode editorNode = objectMapper.readTree(repositoryService.getModelEditorSource(modelData.getId()));
		BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
		byte[] bpmnBytes = null;
		try {
			bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
		} catch (Exception e) {
			throw new ActivitiException("设计模型图不正确，检查模型正确性，模型ID=" + id, e);
		}

		String processName = modelData.getName();
		if (!StringUtils.endsWith(processName, ".bpmn20.xml")) {
			processName += ".bpmn20.xml";
		}
		ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
		Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).addInputStream(processName, in).deploy();

		// 设置流程分类
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
		for (ProcessDefinition processDefinition : list) {
			repositoryService.setProcessDefinitionCategory(processDefinition.getId(), modelData.getCategory());
			processDefinitionId = processDefinition.getId();
		}
		if (list.size() == 0) {
			common.error("部署失败，没有流程。");
		}
		
		//保存部署ID
		modelData.setDeploymentId(deployment.getId());
		repositoryService.saveModel(modelData);
		return processDefinitionId;
	}

	/**
	 * 修改模型
	 * 
	 * @param inParam
	 */
	public void doModify(InParam<String, Object> inParam) {
		Model modelData = repositoryService.getModel(inParam.getString("id"));
		if (StringUtils.isNotEmpty(inParam.getString("name"))) {
			modelData.setName(inParam.getString("name"));
		}
		if (StringUtils.isNotEmpty(inParam.getString("category"))) {
			modelData.setCategory(inParam.getString("category"));
		}
		if (StringUtils.isNotEmpty(inParam.getString("description"))) {
			String metaInfo = modelData.getMetaInfo();
			if(StringUtils.isNotEmpty(metaInfo)){
				JSONObject json = JSON.parseObject(metaInfo);
				json.put("description", inParam.getString("description"));
				if(StringUtils.isNotEmpty(inParam.getString(MODEL_CUSTOM_JSON, ""))){
					json.put(MODEL_CUSTOM_JSON, getCustomJson(inParam.getString(MODEL_CUSTOM_JSON, "")));
				}
				modelData.setMetaInfo(json.toJSONString());
			}
		}
		repositoryService.saveModel(modelData);
	}

	/**
	 * 删除模型
	 * 
	 * @param id
	 * @return
	 */
	public void doDelete(String id) {
		repositoryService.deleteModel(id);
	}

	/**
	 * 获取节点信息
	 * 
	 * @throws Exception
	 */
	public JsonNode getModelEditorNode(String id) throws Exception {
		Model modelData = repositoryService.getModel(id);
		return objectMapper.readTree(repositoryService.getModelEditorSource(modelData.getId()));
	}

	/**
	 * 获取流程图片
	 * 
	 * @param processInstanceId
	 * @return
	 */
	public InputStream getDiagram(String processInstanceId) {
		// 获得流程实例
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		String processDefinitionId = StringUtils.EMPTY;
		if (processInstance == null) {
			// 查询已经结束的流程实例
			HistoricProcessInstance processInstanceHistory = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			if (processInstanceHistory == null) {
				return null;
			} else {
				processDefinitionId = processInstanceHistory.getProcessDefinitionId();
			}
		} else {
			processDefinitionId = processInstance.getProcessDefinitionId();
		}

		// 使用宋体
		String fontName = "宋体";
		// 获取BPMN模型对象
		BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
		// 获取流程实例当前的节点，需要高亮显示
		List<String> currentActs = new ArrayList<String>();
		if (processInstance != null) {
			currentActs = runtimeService.getActiveActivityIds(processInstance.getId());
		}

		return processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator().generateDiagram(model, "png", currentActs, new ArrayList<String>(), fontName, fontName, fontName, null, 1.0);
	}
	
	/**
	 * 获取自定义json参数
	 * 
	 * @param customJson
	 * @return
	 */
	private ObjectNode getCustomJson(String customJson){
		ObjectNode node = null;
		if(StringUtils.isNotEmpty(customJson)){
			try {
				node = objectMapper.readValue(customJson, ObjectNode.class);
			} catch (Exception e) {
				throw new ActivitiException(MODEL_CUSTOM_JSON + "需配置成json格式！");
			}
		}
		return node;
	}
}
