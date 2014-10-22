

package com.mxh.ftp.server.command;

import com.mxh.ftp.server.thread.SessionThread;

import android.util.Log;
/**
 * 表明支持的编码格式
 * @author XiangHang Mi
 *
 */
public class CmdFEAT extends FtpCmd implements Runnable {
	public static final String message = "TEMPLATE!!"; 
	
	public CmdFEAT(SessionThread sessionThread, String input) {
		super(sessionThread, CmdFEAT.class.toString());
	}
	
	public void run() {
		//sessionThread.writeString("211 No extended features\r\n");
		sessionThread.writeString("211-Features supported\r\n");
		sessionThread.writeString(" UTF8\r\n"); // advertise UTF8 support (fixes bug 14)
		sessionThread.writeString("211 End\r\n");
		myLog.l(Log.DEBUG, "Gave FEAT response");
	}

}
