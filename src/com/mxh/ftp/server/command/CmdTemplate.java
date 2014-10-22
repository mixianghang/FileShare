
package com.mxh.ftp.server.command;

import com.mxh.ftp.server.thread.SessionThread;

import android.util.Log;

public class CmdTemplate extends FtpCmd implements Runnable {
	public static final String message = "TEMPLATE!!"; 
	
	public CmdTemplate(SessionThread sessionThread, String input) {
		super(sessionThread, CmdTemplate.class.toString());
	}
	
	public void run() {
		sessionThread.writeString(message);
		myLog.l(Log.INFO, "Template log message");
	}

}
