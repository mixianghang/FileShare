

package com.mxh.ftp.server.command;

import com.mxh.ftp.server.thread.SessionThread;

import android.util.Log;

/**
 * @author XiangHang Mi
 *退出当前的用户会话：包括数据socket与命令socket
 */
public class CmdQUIT extends FtpCmd implements Runnable {
	public static final String message = "TEMPLATE!!"; 
	
	public CmdQUIT(SessionThread sessionThread, String input) {
		super(sessionThread, CmdQUIT.class.toString());
	}
	
	public void run() {
		myLog.l(Log.DEBUG, "QUITting");
		sessionThread.writeString("221 Goodbye\r\n");
		sessionThread.closeSocket();
	}

}
