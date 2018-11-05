package org.walkframework.shiro.service;

import java.util.List;

/**
 * 用户service
 *
 */
public interface IUserService{
	
    /**
     * 查询用户
     * @return
     * @throws Exception 
     */
    public <T> T findUser(String userId);
	
    /**
     * 查询用户拥有角色列表
     * @return
     * @throws Exception 
     */
    public List<String> findRoles(String userId);
    
    /**
     * 查询用户拥有权限列表
     * @return
     * @throws Exception 
     * @throws Exception
     */
    public List<String> findPermissions(String userId);
    
    /**
     * 查询用户组织
     * @return
     * @throws Exception 
     */
    public <T> T findOrganization(String organizationId);
    
    /**
     * 根据用户得到菜单
     * 
     * @param userId
     * @return
     */
    public List<?> findMenus(String userId, String rootId);
}
