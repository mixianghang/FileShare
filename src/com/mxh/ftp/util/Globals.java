

package com.mxh.ftp.util;

import java.io.File;

import com.mxh.ftp.server.thread.ProxyConnector;

import android.content.Context;

/**
 * @author XiangHang Mi
 *设置一些公共的变量
 */
public class Globals {
	private static Context context;
	private static String lastError;
	private static File chrootDir = null;
	private static ProxyConnector proxyConnector = null;
	private static int wifiStatus=0;
	
	public static int getWifiStatus(){
		return wifiStatus;
	}
	public static void setWifiStatus(int status){
		wifiStatus=status;
	}
	public static ProxyConnector getProxyConnector() {
		if(proxyConnector != null) {
			if(!proxyConnector.isAlive()) {
				return null;
			}
		}
		return proxyConnector;
	}

	public static void setProxyConnector(ProxyConnector proxyConnector) {
		Globals.proxyConnector = proxyConnector;
	}

	public static File getChrootDir() {
		return chrootDir;
	}

	public static void setChrootDir(File chrootDir) {
		if(chrootDir.isDirectory()) {
			Globals.chrootDir = chrootDir;
		}
	}

	public static String getLastError() {
		return lastError;
	}

	public static void setLastError(String lastError) {
		Globals.lastError = lastError;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		if(context != null) { 
			Globals.context = context;
		}
	}
	
}
