

package com.mxh.ftp.util;

import android.content.Context;
import android.util.Log;
/**
 * 设置一些默认值
 * @author XiangHang Mi
 *
 */
public class Defaults {
	public final static String PASSWORD_CERTIFICATE="tangmi91111135467";
	public final static int MAX_STACK_ITEMS=5;
	public final static int MAX_FRAGMENT=5;
	public final static String IP_ADDRESS="ip_address";
	public final static String    PORT="port";
	public final static String PASSWORD="password";
	public static final String USER_NAME="user_name";
	
	public  static String certificateDir="/sdcard/";//登陆令牌存储的目录
	public  static String downloadDir="/sdcard/";//下载的文件存储的目录
	protected static int inputBufferSize = 256;
	public static int dataChunkSize = 65536;  // do file I/O in 64k chunks 
	protected static int sessionMonitorScrollBack = 10;
	protected static int serverLogScrollBack = 10;
	protected static int uiLogLevel = Defaults.release ? Log.INFO : Log.DEBUG;
	protected static int consoleLogLevel = Defaults.release ? Log.INFO : Log.DEBUG;
	protected static String settingsName = "FileShare";
	//protected static String username = "user";
	//protected static String password = "";
	public static int portNumber = 2121; 
	public static int max_connections=5;
//	protected static int ipRetrievalAttempts = 5;
	public static final int tcpConnectionBacklog = 5;
	public static final String chrootDir = "/sdcard";
	public static final boolean acceptWifi = true;
	public static final boolean acceptNet = false;
	public static final int REMOTE_PROXY_PORT = 2222;
	public static final String STRING_ENCODING = "UTF-8";
	// FTP control sessions should start out in ASCII, according to the RFC.
	// However, many clients don't turn on UTF-8 even though they support it,
	// so we just turn it on by default.
	public static final String SESSION_ENCODING = "UTF-8"; 
	
	// This is a flag that should be true for public builds and false for dev builds
	public static final boolean release = true;
	
//	public static int getIpRetrievalAttempts() {
//		return ipRetrievalAttempts;
//	}

//	public static void setIpRetrievalAttempts(int ipRetrievalAttempts) {
//		Defaults.ipRetrievalAttempts = ipRetrievalAttempts;
//	}

	public static int getPortNumber() {
		return portNumber;
	}

	public static void setPortNumber(int portNumber) {
		Defaults.portNumber = portNumber;
	}

	public static String getSettingsName() {
		return settingsName;
	}

	public static void setSettingsName(String settingsName) {
		Defaults.settingsName = settingsName;
	}

	public static int getSettingsMode() {
		return settingsMode;
	}

	public static void setSettingsMode(int settingsMode) {
		Defaults.settingsMode = settingsMode;
	}

	public static void setServerLogScrollBack(int serverLogScrollBack) {
		Defaults.serverLogScrollBack = serverLogScrollBack;
	}

	protected static int settingsMode = Context.MODE_WORLD_WRITEABLE;
	
	public static int getUiLogLevel() {
		return uiLogLevel;
	}

	public static void setUiLogLevel(int uiLogLevel) {
		Defaults.uiLogLevel = uiLogLevel;
	}

	public static int getInputBufferSize() {
		return inputBufferSize;
	}

	public static void setInputBufferSize(int inputBufferSize) {
		Defaults.inputBufferSize = inputBufferSize;
	}

	public static int getDataChunkSize() {
		return dataChunkSize;
	}

	public static void setDataChunkSize(int dataChunkSize) {
		Defaults.dataChunkSize = dataChunkSize;
	}

	public static int getSessionMonitorScrollBack() {
		return sessionMonitorScrollBack;
	}

	public static void setSessionMonitorScrollBack(
			int sessionMonitorScrollBack) 
	{
		Defaults.sessionMonitorScrollBack = sessionMonitorScrollBack;
	}

	public static int getServerLogScrollBack() {
		return serverLogScrollBack;
	}

	public static void setLogScrollBack(int serverLogScrollBack) {
		Defaults.serverLogScrollBack = serverLogScrollBack;
	}

	public static int getConsoleLogLevel() {
		return consoleLogLevel;
	}

	public static void setConsoleLogLevel(int consoleLogLevel) {
		Defaults.consoleLogLevel = consoleLogLevel;
	}


}
