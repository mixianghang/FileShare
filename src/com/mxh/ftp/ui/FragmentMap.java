package com.mxh.ftp.ui;

import android.support.v4.app.Fragment;

/**
 * 此类用于各个fragment与对应的字符串 的存储
 * @author XiangHang Mi
 *
 */
public class FragmentMap {
	private Class<? extends Fragment> c;
	private String className;
	
	public FragmentMap(String className, Class<? extends Fragment> c){
		this.className=className;
		this.c=c;
	}
	
	public boolean setFragmentClass(Class<? extends Fragment> c){
		this.c=c;
		return true;
	}
	
	public Class<? extends Fragment> getFragmentClass(){
		return c;
	}
	
	public boolean setClassName(String className){
		this.className=className;
		return true;
	}
	
	public String getClassName(){
		return className;
	}
}
