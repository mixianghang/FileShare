
package com.mxh.ftp.server.command;

import com.mxh.ftp.server.thread.SessionThread;


public class CmdNOOP extends FtpCmd implements Runnable {
	public static final String message = "TEMPLATE!!"; 
	
	public CmdNOOP(SessionThread sessionThread, String input) {
		super(sessionThread, CmdNOOP.class.toString());
	}
	
	public void run() {
		sessionThread.writeString("200 NOOP ok\r\n");
		//myLog.l(Log.INFO, "Executing NOOP, done");
	}

}
