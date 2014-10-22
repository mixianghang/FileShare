

package com.mxh.ftp.server.command;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

import com.mxh.ftp.log.MyLog;
import com.mxh.ftp.server.thread.SessionThread;
import com.mxh.ftp.util.Globals;

import android.util.Log;

public abstract class FtpCmd implements Runnable {
	protected SessionThread sessionThread;
	protected MyLog myLog;
	protected static MyLog staticLog = new MyLog(FtpCmd.class.toString());
	
	protected static CmdMap[] cmdClasses = {
			new CmdMap("SYST", CmdSYST.class,0),
			new CmdMap("USER", CmdUSER.class,0),
			new CmdMap("PASS", CmdPASS.class,0),
			new CmdMap("TYPE", CmdTYPE.class,0),
			new CmdMap("CWD",  CmdCWD.class,1),
			new CmdMap("PWD",  CmdPWD.class,1),
			new CmdMap("LIST", CmdLIST.class,1),
			new CmdMap("PASV", CmdPASV.class,0),
			new CmdMap("RETR", CmdRETR.class,1),
			new CmdMap("NLST", CmdNLST.class,1),
			new CmdMap("NOOP", CmdNOOP.class,0),
			new CmdMap("STOR", CmdSTOR.class,2),
			new CmdMap("DELE", CmdDELE.class,4),
			new CmdMap("RNFR", CmdRNFR.class,3),
			new CmdMap("RNTO", CmdRNTO.class,3),
			new CmdMap("RMD",  CmdRMD.class,4),
			new CmdMap("MKD",  CmdMKD.class,2),
			new CmdMap("OPTS", CmdOPTS.class,0),
			new CmdMap("PORT", CmdPORT.class,0),
			new CmdMap("QUIT", CmdQUIT.class,0),
			new CmdMap("FEAT", CmdFEAT.class,0),
			new CmdMap("SIZE", CmdSIZE.class,0),
			new CmdMap("CDUP",CmdCDUP.class,1)
	};
	
	public FtpCmd(SessionThread sessionThread, String logName) {
		this.sessionThread = sessionThread;
		myLog = new MyLog(logName);
	}
	
	abstract public void run();
	
	
//	解析客户端命令，并交由不同命令处理器进行处理
	public static void dispatchCommand(SessionThread session, 
	                                      String inputString) {
//		Log.e("the command from "+ session.getSocket().getPort(), inputString+inputString.length());
		String[] strings = inputString.split(" ");
		String unrecognizedCmdMsg = "502 Command not recognized\r\n";
		if(strings == null) {
			// There was some egregious sort of parsing error
			String errString = "502 Command parse error\r\n";
			staticLog.l(Log.INFO, errString);
			session.writeString(errString);
			return;
		}
		if(strings.length < 1) {
			staticLog.l(Log.INFO, "No strings parsed");
			session.writeString(unrecognizedCmdMsg);
			return;
		}
		String verb = strings[0];
		if(verb.length() < 1) {
			staticLog.l(Log.INFO, "Invalid command verb");
			session.writeString(unrecognizedCmdMsg);
			return;
		}
		FtpCmd cmdInstance = null;
		verb = verb.trim();
		verb = verb.toUpperCase();
		if(verb.equals("STOR"));{
			byte[] bytes=inputString.getBytes();
			String temp="";
			for(int i=0;i<bytes.length;i++){
				temp+=bytes[i];
				temp+=" ";
			}
//			Log.e("bytes of command", temp);
		}
		CmdMap cmdMap = null;
		for(int i=0; i<cmdClasses.length; i++) {
			
			if(cmdClasses[i].getName().equals(verb)) {
				// We found the correct command. We retrieve the corresponding
				// Class object, get the Constructor object for that Class, and 
				// and use that Constructor to instantiate the correct FtpCmd 
				// subclass. Yes, I'm serious.
				cmdMap=cmdClasses[i];
				Constructor<? extends FtpCmd> constructor; 
				try {
					constructor = cmdClasses[i].getCommand().getConstructor(
							new Class[] {SessionThread.class, String.class});
				} catch (NoSuchMethodException e) {
					staticLog.l(Log.ERROR, "FtpCmd subclass lacks expected " +
							           "constructor ");
					return;
				}
				try {
					cmdInstance = constructor.newInstance(
							new Object[] {session, inputString});
				} catch(Exception e) {
					staticLog.l(Log.ERROR, 
							"Instance creation error on FtpCmd");
					return;
				}
			}
		}
		if(cmdInstance == null) {
			// If we couldn't find a matching command,
			staticLog.l(Log.DEBUG, "Ignoring unrecognized FTP verb: " + verb);
//			无法识别的指令，向客户端发送相关信息
			session.writeString(unrecognizedCmdMsg);
			return;
		} else if(session.isAuthenticated() 
				|| cmdInstance.getClass().equals(CmdUSER.class)
				|| cmdInstance.getClass().equals(CmdPASS.class)
				|| cmdInstance.getClass().equals(CmdUSER.class))
		{
			boolean canRun=false;
			// Unauthenticated users can run only USER, PASS and QUIT 
			switch(cmdMap.getType()){
			case 0:
				canRun=true;
				break;
			case 1:
				if(session.account.getRead()==1)
					canRun=true;
				break;
			case 2:
				if(session.account.getWrite()==1)
					canRun=true;
				break;
			case 3:
				if(session.account.getModify()==1)
					canRun=true;
				break;
			case 4:
				if(session.account.getDelete()==1)
					canRun=true;
				break;
			}
			if(canRun)
				cmdInstance.run();
			else
				session.writeString("550 you do not have the permission to run the command\r\n");
		} else {
			session.writeString("530 Login first with USER and PASS\r\n");
		}
	}
		
	/**
	 * 被各个命令处理器所使用，用于分离参数
	 * An FTP parameter is that part of the input string that occurs
	 * after the first space, including any subsequent spaces. Also,
	 * we want to chop off the trailing '\r\n', if present.
	 */
	static public String getParameter(String input) {
		if(input == null) {
			return "";
		}
		int firstSpacePosition = input.indexOf(' ');
		if(firstSpacePosition == -1) {
			return "";
		}
		String retString = input.substring(firstSpacePosition+1);
		
		// Remove trailing whitespace
		// todo: trailing whitespace may be significant, just remove \r\n
		retString = retString.replaceAll("\\s+$", "");
		
//		staticLog.l(Log.INFO, "Parsed argument:" + retString);
		return retString; 
	}

	public static File inputPathToChrootedFile(File existingPrefix, String param) {
		File chroot = Globals.getChrootDir();
		if(param.charAt(0) == '/') {
			// The STOR contained an absolute path
			return new File(chroot, param);
		} else {
			// The STOR contained a relative path
			return new File(existingPrefix, param); 
		}
	}
//	检查当前要求的文件目录是否在我们规定的目录范围内
	public boolean violatesChroot(File file) {
		File chroot = Globals.getChrootDir();
		try {
			String canonicalPath = file.getCanonicalPath();
//			Log.e("directory", canonicalPath+"hello"+chroot.toString());
			if(!canonicalPath.startsWith("/mnt"+chroot.toString())) {
				myLog.l(Log.INFO, "Path violated folder restriction, denying");
				myLog.l(Log.DEBUG, "path: " + canonicalPath);
				myLog.l(Log.DEBUG, "chroot: " + chroot.toString());
				return true; // the path must begin with the chroot path
			}
			return false;
		} catch(IOException e) {
			myLog.l(Log.INFO, "Path canonicalization problem");
			return true;  // for security, assume violation
		}
	}
}
