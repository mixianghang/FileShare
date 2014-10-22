
package com.mxh.ftp.log;

import com.mxh.ftp.server.thread.FTPServerService;
import com.mxh.ftp.util.Defaults;
import com.mxh.ftp.util.Globals;

import android.util.Log;

/**
 * @author XiangHang Mi
 *根据log等级，向不同的地方输出日志
 */
public class MyLog {
	protected String tag;
	
	public MyLog(String tag) {
		this.tag = tag;
	}
//	根据等级,划分记录归属
	public void l(int level, String str, boolean sysOnly) {
		synchronized (MyLog.class) {
			str = str.trim();
			if(level == Log.ERROR) {
				Globals.setLastError(str);
			}
			if(level >= Defaults.getConsoleLogLevel()) {
				Log.println(level,tag, str);
			}
			if(!sysOnly) { 
//				在server log中显示
				if(level >= Defaults.getUiLogLevel()) {
					FTPServerService.log(level, str);
				}
			}
		}
	}
	
	public void l(int level, String str) {
		l(level, str, false);
	}
	
	public void e(String s) {
		l(Log.ERROR, s, false);
	}
	public void w(String s) {
		l(Log.WARN, s, false);
	}
	public void i(String s) {
		l(Log.INFO, s, false);
	}
	public void d(String s) {
		l(Log.DEBUG, s, false);
	}
}
