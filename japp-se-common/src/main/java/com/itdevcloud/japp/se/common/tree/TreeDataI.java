package com.itdevcloud.japp.se.common.tree;

public interface TreeDataI<T> {
	public String getUID(); 
	public void setUID(String uid); 
	public String getParentUID(); 
	public void setParentUID(String parentUID); 
	public T createNewInstance();
	public String toSimpleString();
}
