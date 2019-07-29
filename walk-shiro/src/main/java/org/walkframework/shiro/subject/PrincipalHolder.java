package org.walkframework.shiro.subject;

/**
  *  身份信息所用Holder
 * 
 * @author shf675
 */
public abstract class PrincipalHolder {
	
	private static final ThreadLocal<Object> holder = new ThreadLocal<Object>();
	
	public static void setPrincipal(Object principal) {
		holder.set(principal);
    }

    public static Object getPrincipal() {
        return holder.get();
    }
    
    public static void clear(){
    	holder.remove();
    }
}
