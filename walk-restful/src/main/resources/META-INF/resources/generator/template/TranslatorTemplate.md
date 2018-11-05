<% if(isNotEmpty(root.packageName)){ %>
package ${root.packageName};
<% }%>

import org.springframework.stereotype.Component;
import org.walkframework.restful.translate.RspTranslator;

/**
 * @desc ${root.desc}
 * @author ${root.author}
 * @date ${root.createTime}
 *
 * 本类由工具类RestfulGenerator自动生成
 */
@Component
public class ${root.name} extends RspTranslator {
	
	@Override
	public <T> T translate(Object sourceObject) {
	
		//TODO ${root.name}需要填写属性翻译器具体的实现业务代码 (填写完后清除此行注释)
		
		return (T) null;
	}
}
