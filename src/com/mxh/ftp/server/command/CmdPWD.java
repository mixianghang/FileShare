

package com.mxh.ftp.server.command;

import java.io.IOException;

import com.mxh.ftp.server.thread.SessionThread;
import com.mxh.ftp.util.Globals;

import android.util.Log;

/**
 * @author XiangHang Mi
 *返回当前工作的目录
 */
public class CmdPWD extends FtpCmd implements Runnable {
	public static final String message = "TEMPLATE!!"; 
	
	public CmdPWD(SessionThread sessionThread, String input) {
		super(sessionThread, CmdPWD.class.toString());
	}
	
	public void run() {
		myLog.l(Log.DEBUG, "PWD executing");
		
		// We assume that the chroot restriction has been applied, and that
		// therefore the current directory is located somewhere within the
		// chroot directory. Therefore, we can just slice of the chroot
		// part of the current directory path in order to get the
		// user-visible path (inside the chroot directory).
		try {
			String currentDir = sessionThread.getWorkingDir().getCanonicalPath();
			currentDir = currentDir.substring(Globals.getChrootDir().
					getCanonicalPath().length());
			// The root directory requires special handling to restore its
			// leading slash
			if(currentDir.length() == 0) {
				currentDir = "/";
			}
			sessionThread.writeString("257 \"" 
								      + currentDir 
								      + "\"\r\n");
		} catch (IOException e) {
			// This shouldn't happen unless our input validation has failed
			myLog.l(Log.ERROR, "PWD canonicalize");
			sessionThread.closeSocket(); // should cause thread termination
		}
		myLog.l(Log.DEBUG, "PWD complete");
	}

}
