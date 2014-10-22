

package com.mxh.ftp.server.command;

import java.util.ArrayList;

import com.mxh.ftp.datastore.DatabaseHelper;
import com.mxh.ftp.server.thread.SessionThread;
import com.mxh.ftp.util.Account;
import com.mxh.ftp.util.Defaults;
import com.mxh.ftp.util.Globals;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
/**
 * 检测用户名与密码的正确性
 * @author XiangHang Mi
 *
 */
public class CmdPASS extends FtpCmd implements Runnable {
	String input;
//	private String attempUserName;
	
	public CmdPASS(SessionThread sessionThread, String input) {

		super(sessionThread, CmdPASS.class.toString());
		this.input = input;
	}
	
	public void run() {
		// User must have already executed a USER command to
		// populate the Account object's username
		myLog.l(Log.DEBUG, "Executing PASS");
	
		String attemptPassword = getParameter(input);
		String attemptUsername = sessionThread.account.getUsername();
		if(attemptUsername == null) {
			sessionThread.writeString("503 Must send USER first\r\n");
			return;
		}
		Context ctx = Globals.getContext();
		if(ctx == null) {
			// This will probably never happen, since the global 
			// context is configured by the Service
			myLog.l(Log.ERROR, "No global context in PASS\r\n");
		}
		DatabaseHelper database=new DatabaseHelper(ctx);
		ArrayList<Account> accounts=database.getUserList();

		for(Account a: accounts){
			String userName=a.getUsername();
			String password=a.getPassword();
			Log.e("data",userName+password+attemptUsername+attemptPassword);
			if(attemptPassword==null){
				Log.e("attempt", "null");
			}
//			此处可以对密码进行解密
			if(userName.equals(attemptUsername)){
				if(password!=null&&password.equals(attemptPassword)||(password==null&&(attemptPassword==null||"".equals(attemptPassword)))){
					sessionThread.writeString("230 Access granted\r\n");
					myLog.l(Log.INFO, "User " + attemptUsername + " password verified");
					sessionThread.setAuthenticated(true);
					sessionThread.account=a;
					return;
				}
			}
		}
		try {
			// If the login failed, sleep for one second to foil
			// brute force attacks，防止暴力的密码破解
			Thread.sleep(1000);
		} catch(InterruptedException e) {}
		myLog.l(Log.INFO, "Failed authentication");
		sessionThread.writeString("530 Login incorrect.\r\n");
	}

}
