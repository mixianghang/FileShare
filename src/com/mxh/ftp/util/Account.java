

package com.mxh.ftp.util;
/**
 * 用于在SessionThread 中存储客户的用户名
 * 在
 * @author XiangHang Mi
 *
 */
public class Account {
	private String username = null;
	private String password=null;
	private int    canRead=0;
	private int    canWrite=0;
	private int    canModify=0;
	private int    canDelete=0;
	
	public String getUsername() {
		return username;
	}
/**
 * 设置用户名
 * 在CmdUser中使用
 * @param username
 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setPassword(String password){
		this.password=password;
	}
	
	public int getWrite(){
		return canWrite;
	}
	public void setWrite(int canWrite){
		this.canWrite=canWrite;
	}
	
	public int getModify(){
		return canModify;
	}
	public void setModify(int canModify){
		this.canModify=canModify;
	}
	public int getRead(){
		return canRead;
	}
	public void setRead(int canRead){
		this.canRead=canRead;
	}
	public int getDelete(){
		return canDelete;
	}
	public void setDelete(int canDelete){
		this.canDelete=canDelete;
	}
	
}
