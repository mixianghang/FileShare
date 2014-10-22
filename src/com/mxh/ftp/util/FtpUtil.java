package com.mxh.ftp.util;

import java.io.File;
import java.io.IOException;

import com.mxh.ftp.ui.FileDialog;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class FtpUtil {
	public static void makeToast(Context c, String message){
		Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
	}
	
	public static boolean checkIp(String ipAddress){
		String[] ips=ipAddress.split("\\.");
		int isIpAddressRight=0;
		if(ips.length==4){
			for(String ip:ips){
				int temp=-1;
				try{
				temp=Integer.parseInt(ip);
				}
				catch(NumberFormatException e){
					
				}
				if(0<=temp&&temp<=255)
					isIpAddressRight++;
			}
		}
		if(isIpAddressRight==4){
//			Log.e("ips", ""+i)
//			Util.makeToast(getActivity()," the ip address is not right");
			return true;
		}
		return false;
	}
	
	public static boolean checkPort(int port){
		if(port<0||port>65535)
			return false;
		return true;
	}
	
	public static File createFile(String dirPath, String fileName){
		File dir=new File(dirPath);
		dir.mkdirs();
		File file=new File(dir,fileName);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
		
	}
	

}
