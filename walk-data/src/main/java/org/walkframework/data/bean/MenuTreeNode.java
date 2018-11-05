package org.walkframework.data.bean;

import java.util.ArrayList;
import java.util.List;

import org.walkframework.data.util.IData;

/**
 * 树形菜单
 *
 */
@SuppressWarnings("unchecked")
public class MenuTreeNode {
	
	private IData menuNode;

	private List<MenuTreeNode> children = new ArrayList<MenuTreeNode>();

	public MenuTreeNode(IData menuNode) {
		this.menuNode = menuNode;
	}

	/**
	 * 递归添加节点
	 * 
	 * @param node
	 */
	public void add(MenuTreeNode nextNode) {
		if (this.menuNode.get("MENU_ID").equals(nextNode.getMenuNode().get("PARENT_MENU_ID"))) {
			this.children.add(nextNode);
		} else {
			for (MenuTreeNode tmpNode : children) {
				tmpNode.add(nextNode);
			}
		}
	}

	public IData getMenuNode() {
		return menuNode;
	}

	public void setMenuNode(IData menuNode) {
		this.menuNode = menuNode;
	}

	public List<MenuTreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<MenuTreeNode> children) {
		this.children = children;
	}

}
