
/**
 * 由于List与NList命令执行的操作基本相同，所以这里使用此类作为他们的父类,实现listDirectory sendListing
 * 两个方法,将makeLsString()交由子类来实现
 */
package com.mxh.ftp.server.command;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mxh.ftp.log.MyLog;
import com.mxh.ftp.server.thread.SessionThread;

import android.util.Log;

public abstract class CmdAbstractListing extends FtpCmd {
	protected static MyLog staticLog = new MyLog(CmdLIST.class.toString());
	
	public CmdAbstractListing(SessionThread sessionThread, String input) {
		super(sessionThread, CmdAbstractListing.class.toString());
	}
	
	abstract String makeLsString(File file);
	
/**
 * 将dir目录下所有文件扥信息存入StringBuilder
 * @param response
 * @param dir
 * @return
 */
	public String listDirectory(StringBuilder response, File dir) {
		if(!dir.isDirectory()) {
			return "500 Internal error, listDirectory on non-directory\r\n";
		}
		myLog.l(Log.DEBUG, "Listing directory: " + dir.toString());
		
		// Get a listing of all files and directories in the path
		File[] entries = dir.listFiles();
		myLog.l(Log.DEBUG, "Dir len " + entries.length);
		for(File entry : entries) {
			String curLine = makeLsString(entry);
			if(curLine != null) {
				response.append(curLine);
			}
		}
		return null;
	}

	// 将文件列表信息发给client 通过dataSocket
	protected String sendListing(String listing) {
		if(sessionThread.startUsingDataSocket()) {
			myLog.l(Log.DEBUG, "LIST/NLST done making socket");
		} else {
			sessionThread.closeDataSocket();
			return "425 Error opening data socket\r\n";
		}
		
//		向CommandSocket发送命令
		sessionThread.writeString("150 Beginning transmission\r\n");
		myLog.l(Log.DEBUG, "Sent code 150, sending listing string now");
		if(!sessionThread.sendViaDataSocket(listing)) {
			myLog.l(Log.DEBUG, "sendViaDataSocket failure");
			sessionThread.closeDataSocket();
			return "426 Data socket or network error\r\n";
		}
		sessionThread.closeDataSocket();
		myLog.l(Log.DEBUG, "Listing sendViaDataSocket success");
		sessionThread.writeString("226 Data transmission OK\r\n");
		return null;
	}
}
