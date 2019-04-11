package org.walkframework.activiti.mvc.controller.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.walkframework.activiti.mvc.service.model.ActModelService;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.InParam;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 流程管理Controller
 */
@RestController
@RequestMapping(value = "/act/model")
public class ActModelController extends BaseController {

	@Autowired
	private ActModelService actModelService;

	/**
	 * 模型列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	@RequestMapping(value = "list")
	public Object list(InParam<String, Object> inParam, Pagination pagination) {
		return actModelService.queryList(inParam, pagination);
	}

	/**
	 * 创建模型
	 * 
	 * @param inParam
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "create")
	public void create(InParam<String, Object> inParam, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Model modelData = actModelService.doCreate(inParam);
		response.sendRedirect(request.getContextPath() + "/component/modeler.html?modelId=" + modelData.getId());
	}

	/**
	 * 复制模型
	 * 
	 * @param modelId
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "copy")
	public void copy(String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Model modelData = actModelService.doCopy(id);
		response.sendRedirect(request.getContextPath() + "/component/modeler.html?modelId=" + modelData.getId());
	}

	/**
	 * 部署流程
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "deploy")
	public String deploy(String id) {
		try {
			String processDefinitionId = actModelService.doDeploy(id);
			return message.success("部署成功！流程ID：" + processDefinitionId);
		} catch (Exception e) {
			return message.error("部署失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 修改模型
	 * 
	 * @param inParam
	 * @return
	 */
	@RequestMapping(value = "modify")
	public String modify(InParam<String, Object> inParam) {
		try {
			actModelService.doModify(inParam);
			return message.success("修改成功！模型ID：" + inParam.getString("id"));
		} catch (Exception e) {
			return message.error("修改失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 删除模型
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "delete")
	public String delete(String id) {
		try {
			actModelService.doDelete(id);
			return message.success("删除成功！模型ID：" + id);
		} catch (Exception e) {
			return message.error("删除失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 导出model的xml文件
	 * 
	 * @param id
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "export")
	public void export(String id, HttpServletResponse response) throws Exception {
		JsonNode editorNode = actModelService.getModelEditorNode(id);

		BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(editorNode);
		ByteArrayInputStream in = new ByteArrayInputStream(new BpmnXMLConverter().convertToXML(bpmnModel));
		IOUtils.copy(in, response.getOutputStream());
		String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		response.flushBuffer();
	}

	/**
	 * 查看流程图片
	 * 
	 * @param response
	 * @param processInstanceId
	 */
	@RequestMapping(value = "/image", method = RequestMethod.GET)
	public void image(HttpServletResponse response, @RequestParam String processInstanceId) {
		try {
			InputStream is = actModelService.getDiagram(processInstanceId);
			if (is == null) {
				return;
			}

			response.setContentType("image/png");
			BufferedImage image = ImageIO.read(is);
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "png", out);
			is.close();
			out.close();
		} catch (Exception ex) {
			log.error("查看流程图失败", ex);
		}
	}
}