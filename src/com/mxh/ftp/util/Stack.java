package com.mxh.ftp.util;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class Stack {
	List<Integer> preSelected;
	private int stackSize;
	public Stack(int stackSize){
		preSelected=new ArrayList<Integer>();
		this.stackSize=stackSize;
	}
	
	public void addItem(int i){
		
		int num=preSelected.size();
		if(num==stackSize)
			preSelected.remove(0);
		preSelected.add(i);
		
	}
	
	public int getItem(){
		int num=preSelected.size();
		if(num==0)
			return 0;
		int i=preSelected.get(num-1);
		preSelected.remove(num-1);
		return i;
	}

}
