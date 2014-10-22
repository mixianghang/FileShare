

package com.mxh.ftp.server.command;

import java.io.File;

import com.mxh.ftp.server.thread.SessionThread;

import android.util.Log;

/**
 * @author XiangHang Mi
 *重命名文件
 */
public class CmdRNFR extends FtpCmd implements Runnable {
	protected String input;

	public CmdRNFR(SessionThread sessionThread, String input) {
		super(sessionThread, CmdRNFR.class.toString());
		this.input = input;
	}
	
	public void run() {
		String param = getParameter(input);
		String errString = null;
		File file = null;
		mainblock: {
			file = inputPathToChrootedFile(sessionThread.getWorkingDir(), param);
			if(violatesChroot(file)) {
				errString = "550 Invalid name or chroot violation\r\n";
				break mainblock;
			}
			if(!file.exists()) {
				errString = "450 Cannot rename nonexistent file\r\n";
			}
		}
		if(errString != null) {
			sessionThread.writeString(errString);
			myLog.l(Log.INFO, "RNFR failed: " + errString.trim());
			sessionThread.setRenameFrom(null);
		} else {
			sessionThread.writeString("350 Filename noted, now send RNTO\r\n");
			sessionThread.setRenameFrom(file);
		}
	}
}
