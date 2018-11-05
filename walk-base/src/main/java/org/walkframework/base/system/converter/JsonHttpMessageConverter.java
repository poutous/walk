package org.walkframework.base.system.converter;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.walkframework.base.system.common.Message;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.base.tools.utils.ExportUtil;
import org.walkframework.data.bean.PageData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

public class JsonHttpMessageConverter extends FastJsonHttpMessageConverter {
	protected final static Logger log = LoggerFactory.getLogger(JsonHttpMessageConverter.class);

	private final static SerializerFeature[] serializerFeatures = new SerializerFeature[] { SerializerFeature.WriteDateUseDateFormat, SerializerFeature.DisableCircularReferenceDetect };

	private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Autowired
	private HttpServletRequest request;

	@Override
	protected void writeInternal(Object obj, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		String text = "{}";
		if (obj instanceof List || obj instanceof PageData) {
			List<?> dataset = obj != null && obj instanceof PageData ? ((PageData<?>) obj).getRows() : (List<?>) obj;
			String actionType = request.getParameter(CommonConstants.ACTION_TYPE);

			// 1、导出
			if (CommonConstants.ACTION_TYPE_EXPORT.equals(actionType)) {
				// 设置导出总数
				request.setAttribute(CommonConstants.EXPORT_TOTAL, (obj != null && obj instanceof PageData ? ((PageData<?>) obj).getTotal() : ((List<?>) obj).size()) + "");
				try {
					HttpServletResponse response = ((ServletServerHttpResponse) outputMessage).getServletResponse();
					String xml = (String) request.getAttribute(CommonConstants.EXPORT_XML_NAME);
					if (!StringUtils.isEmpty(xml)) {
						ExportUtil.exportExcel(request, response, dataset, xml);
					} else {
						String exportName = request.getParameter(CommonConstants.EXPORT_NAME);
						ExportUtil.exportExcel(request, response, dataset, request.getParameter(CommonConstants.HEADER_JSON_NAME), StringUtils.isEmpty(exportName) ? "exportfile" : exportName);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				return;
			}
			// 2、节点展开
			else if (CommonConstants.ACTION_TYPE_EXPAND.equals(actionType)) {
				text = toJson(dataset);
			}
			// 3、其他
			else {
				text = JSON.toJSONString(obj, getFeatures());
			}
		} else {
			text = JSON.toJSONString(obj, getFeatures());
		}

		// jquery.form提交特殊处理(解决IE弹出*.json文件问题)
		if ("true".equals(request.getParameter(Message.JQUERY_FORM_AJAX_REQUEST))) {
			MediaType mediaType = new MediaType(MediaType.TEXT_HTML.getType(), MediaType.TEXT_HTML.getSubtype(), getCharset());
			outputMessage.getHeaders().setContentType(mediaType);
		}

		StreamUtils.copy(text, getCharset(), outputMessage.getBody());
	}

	@Override
	public SerializerFeature[] getFeatures() {
		return serializerFeatures;
	}

	/**
	 * 往http输出流输出字符串文本
	 * 
	 * @param result :
	 *            dataset
	 * @param actionType :
	 *            为pager时，表示分页，加入total和rows进行json的组装
	 */
	@SuppressWarnings("unchecked")
	private static String toJson(List<?> result) {
		if (result != null && result.size() > 0 && result.get(0) instanceof Map) {
			for (int i = 0; i < result.size(); i++) {
				Map data = (Map) result.get(i);
				// 由于框架查出的字段属性默认都设置为大写，但EasyUi内部使用的某些属性是小写，所以要做转换
				if (data.get("STATE") != null) {
					data.put("state", data.get("STATE"));
					data.remove("STATE");
				}
				if (data.get("PARENTID") != null) {
					data.put("parentId", data.get("PARENTID"));
					data.remove("PARENTID");
				}
				if (data.get("ID") != null) {
					data.put("id", data.get("ID"));
					data.remove("ID");
				}
				if (data.get("TEXT") != null) {
					data.put("text", data.get("TEXT"));
					data.remove("TEXT");
				}
				// 其他内部使用的属性需要转换可以继续添加
			}
		}

		if (result != null) {
			SerializeConfig mapping = new SerializeConfig();
			// 日期转换
			mapping.put(Date.class, new SimpleDateFormatSerializer(DATE_FORMAT));
			mapping.put(Timestamp.class, new SimpleDateFormatSerializer(DATE_FORMAT));

			return JSONArray.toJSONString(result, mapping);
		} else {
			return "";
		}
	}
}
