package org.walkframework.base.system.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;
import org.walkframework.base.mvc.service.common.AbstractBaseService;
import org.walkframework.data.bean.MenuTreeNode;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;
import org.walkframework.shiro.service.IUserService;

/**
 * 默认的用户信息服务
 * 
 * 需在工程的sql目录下定义UserSQL.xml文件，并按需实现用到的sql语句，如不满足需要可继承DefaultUserService类重写
 *
 */
public class DefaultUserService extends AbstractBaseService implements IUserService {

	/**
	 * 查询用户
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T findUser(String userId) {
		IData<String, Object> param = new DataMap<String, Object>();
		param.put("userId", userId);
		param.put("currentTime", dao().getDialect().getToDate(common.getSysTime()));
		return (T) dao().selectOne("UserSQL.findUser", param);
	}

	/**
	 * 查询用户拥有角色列表
	 * @return
	 * @throws Exception 
	 */
	@Override
	public List<String> findRoles(String userId) {
		return dao().selectList("UserSQL.findRoles", userId);
	}

	/**
	 * 查询用户拥有权限列表
	 * @return
	 * @throws Exception 
	 * @throws Exception
	 */
	@Override
	public List<String> findPermissions(String userId) {
		return dao().selectList("UserSQL.findPermissions", userId);
	}

	/**
	 * 查询用户组织
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOrganization(String organizationId) {
		return (T) dao().selectOne("UserSQL.findOrganization", organizationId);
	}

	/**
	 * 根据用户得到菜单
	 * 
	 * @param userId 用户Id
	 * @param rootId 根节点（点击顶部菜单进行异步刷新）
	 * @return
	 */
	@Override
	public List<MenuTreeNode> findMenus(String userId, String rootId) {
		IData<String, Object> param = new DataMap<String, Object>();
		param.put("userId", userId);
		param.put("rootId", rootId);
		List<IData<String, Object>> list = dao().selectList("UserSQL.findMenus", param);
		if (!CollectionUtils.isEmpty(list)) {
			list = deleteTrunkNodeWithNoLeaf(list);
		}
		
		if (CollectionUtils.isEmpty(list)) {
			return new ArrayList<MenuTreeNode>();
		}
		
		MenuTreeNode rootNode = new MenuTreeNode(list.get(0));
		for (IData<String, Object> data : list) {
			rootNode.add(new MenuTreeNode(data));
		}
		List<MenuTreeNode> menus = rootNode.getChildren();
		return menus;
	}
	
	/**
	 * 删除没有叶子节点的树干节点
	 * 
	 * @param menus
	 */
	protected List<IData<String, Object>> deleteTrunkNodeWithNoLeaf(List<IData<String, Object>> list){
		//所有的树干节点
		Map<String, IData<String, Object>> allTrunkNodes = new LinkedHashMap<String, IData<String, Object>>();
		
		//叶子节点上级树干节点
		Set<String> useTrunkNodes = new HashSet<String>();
		
		//叶子节点
		List<IData<String, Object>> leafNodes = new ArrayList<IData<String, Object>>();
		
		//list是经过排序的，遍历一次即可
		for (IData<String, Object> data : list) {
			if("0".equals(data.getString("NODE_TYPE"))){
				allTrunkNodes.put(data.getString("MENU_ID"), data);
			} else {
				leafNodes.add(data);
				
				//递归添加树干节点
				addToUseTrunkNodes(allTrunkNodes, useTrunkNodes, data);
			}
		}
		
		//从所有的树干节点中删除用不到的树干节点，保留原有顺序
		Map<String, IData<String, Object>> cleanNodes = new LinkedHashMap<String, IData<String, Object>>(allTrunkNodes);
		for (Map.Entry<String, IData<String, Object>> entry : allTrunkNodes.entrySet()) {
			if(!useTrunkNodes.contains(entry.getKey())){
				cleanNodes.remove(entry.getKey());
			}
		}

		//合并
		leafNodes.addAll(0, cleanNodes.values());
		return leafNodes;
	}
	
	/**
	 * 递归添加树干节点
	 * 
	 * @param allTrunkNodes
	 * @param useTrunkNodes
	 * @param node
	 */
	protected void addToUseTrunkNodes(Map<String, IData<String, Object>> allTrunkNodes, Set<String> useTrunkNodes, IData<String, Object> node) {
		String parentMenuId = node.getString("PARENT_MENU_ID");
		if(allTrunkNodes.containsKey(parentMenuId)){
			useTrunkNodes.add(parentMenuId);
			addToUseTrunkNodes(allTrunkNodes, useTrunkNodes, allTrunkNodes.get(parentMenuId));
		}
	}
}
