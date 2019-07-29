package org.walkframework.shiro.subject;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

/**
 * 包装Subject
 * @author shf675
 */
public class BaseSubject implements Subject{
	
	/**
	 * 获取Subject对象
	 * @return
	 */
	protected Subject subject(){
		return SecurityUtils.getSubject();
	}
	
	/************ 以下实现subject接口方法****************************************/
	
	public Object getPrincipal() {
		return subject().getPrincipal() == null ? PrincipalHolder.getPrincipal():subject().getPrincipal();
	}
	
	public <V> Callable<V> associateWith(Callable<V> paramCallable) {
		// TODO Auto-generated method stub
		return subject().associateWith(paramCallable);
	}

	public Runnable associateWith(Runnable paramRunnable) {
		// TODO Auto-generated method stub
		return subject().associateWith(paramRunnable);
	}

	
	public void checkPermission(String paramString) throws AuthorizationException {
		// TODO Auto-generated method stub
		subject().checkPermission(paramString);
	}

	
	public void checkPermission(Permission paramPermission) throws AuthorizationException {
		// TODO Auto-generated method stub
		subject().checkPermission(paramPermission);
	}

	
	public void checkPermissions(String... paramArrayOfString) throws AuthorizationException {
		// TODO Auto-generated method stub
		subject().checkPermissions(paramArrayOfString);
	}

	
	public void checkPermissions(Collection<Permission> paramCollection) throws AuthorizationException {
		// TODO Auto-generated method stub
		subject().checkPermissions(paramCollection);
	}

	
	public void checkRole(String paramString) throws AuthorizationException {
		// TODO Auto-generated method stub
		subject().checkRole(paramString);
	}

	
	public void checkRoles(Collection<String> paramCollection) throws AuthorizationException {
		// TODO Auto-generated method stub
		subject().checkRoles(paramCollection);
	}

	
	public void checkRoles(String... paramArrayOfString) throws AuthorizationException {
		// TODO Auto-generated method stub
		subject().checkRoles(paramArrayOfString);
	}

	
	public <V> V execute(Callable<V> paramCallable) throws ExecutionException {
		// TODO Auto-generated method stub
		return subject().execute(paramCallable);
	}

	
	public void execute(Runnable paramRunnable) {
		// TODO Auto-generated method stub
		subject().execute(paramRunnable);
	}

	
	public PrincipalCollection getPreviousPrincipals() {
		// TODO Auto-generated method stub
		return subject().getPreviousPrincipals();
	}

	public PrincipalCollection getPrincipals() {
		// TODO Auto-generated method stub
		return subject().getPrincipals();
	}

	
	public Session getSession() {
		// TODO Auto-generated method stub
		return subject().getSession();
	}

	
	public Session getSession(boolean paramBoolean) {
		// TODO Auto-generated method stub
		return subject().getSession(paramBoolean);
	}

	
	public boolean hasAllRoles(Collection<String> paramCollection) {
		// TODO Auto-generated method stub
		return subject().hasAllRoles(paramCollection);
	}

	
	public boolean hasRole(String paramString) {
		// TODO Auto-generated method stub
		return subject().hasRole(paramString);
	}

	
	public boolean[] hasRoles(List<String> paramList) {
		// TODO Auto-generated method stub
		return subject().hasRoles(paramList);
	}

	
	public boolean isAuthenticated() {
		// TODO Auto-generated method stub
		return subject().isAuthenticated();
	}

	
	public boolean isPermitted(String paramString) {
		// TODO Auto-generated method stub
		return subject().isPermitted(paramString);
	}

	
	public boolean isPermitted(Permission paramPermission) {
		// TODO Auto-generated method stub
		return subject().isPermitted(paramPermission);
	}

	
	public boolean[] isPermitted(String... paramArrayOfString) {
		// TODO Auto-generated method stub
		return subject().isPermitted(paramArrayOfString);
	}

	
	public boolean[] isPermitted(List<Permission> paramList) {
		// TODO Auto-generated method stub
		return subject().isPermitted(paramList);
	}

	
	public boolean isPermittedAll(String... paramArrayOfString) {
		// TODO Auto-generated method stub
		return subject().isPermittedAll(paramArrayOfString);
	}

	
	public boolean isPermittedAll(Collection<Permission> paramCollection) {
		// TODO Auto-generated method stub
		return subject().isPermittedAll(paramCollection);
	}

	
	public boolean isRemembered() {
		// TODO Auto-generated method stub
		return subject().isRemembered();
	}

	
	public boolean isRunAs() {
		// TODO Auto-generated method stub
		return subject().isRunAs();
	}

	
	public void login(AuthenticationToken paramAuthenticationToken) throws AuthenticationException {
		// TODO Auto-generated method stub
		subject().login(paramAuthenticationToken);
	}

	
	public void logout() {
		// TODO Auto-generated method stub
		subject().logout();
	}

	
	public PrincipalCollection releaseRunAs() {
		// TODO Auto-generated method stub
		return subject().releaseRunAs();
	}

	
	public void runAs(PrincipalCollection paramPrincipalCollection) throws NullPointerException, IllegalStateException {
		// TODO Auto-generated method stub
		subject().runAs(paramPrincipalCollection);
	}

}
