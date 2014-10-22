
package com.mxh.ftp.server.command;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.mxh.ftp.server.thread.SessionThread;

import android.util.Log;

public class CmdLIST extends CmdAbstractListing implements Runnable {
	// The approximate number of milliseconds in 6 months
	public final static long MS_IN_SIX_MONTHS = 6 * 30 * 24 * 60 * 60*1000 ;
	private String input;
	
	public CmdLIST(SessionThread sessionThread, String input) {
		super(sessionThread, input);
		this.input = input;
	}
	
	public void run() {
		String errString = null;
		
		mainblock: {
			String param = getParameter(input);
			myLog.d("LIST parameter: " + param);
			if(param.startsWith("-")) {
				// Ignore options to list, which start with a dash
				param = "";
			}
			File fileToList = null;
			if(param.equals("")) {
				fileToList = sessionThread.getWorkingDir();
			} else {
				if(param.contains("*")) {
					errString = "550 LIST does not support wildcards\r\n";
					break mainblock;
				}
				fileToList = new File(sessionThread.getWorkingDir(), param);
				if(violatesChroot(fileToList)) {
					errString = "450 Listing target violates chroot\r\n";
					break mainblock;
				}				
			}
			String listing;
			if(fileToList.isDirectory()) {
				StringBuilder response = new StringBuilder();
				errString = listDirectory(response, fileToList);
				if(errString != null) {
					break mainblock;
				}
				listing = response.toString();
			} else {
				listing = makeLsString(fileToList);
				if(listing == null) {
					errString = "450 Couldn't list that file\r\n";
					break mainblock;
				}
			}
			errString = sendListing(listing);
			if(errString != null) {
				break mainblock;
			}
		}	
		
		if(errString != null) {
			sessionThread.writeString(errString);
			myLog.l(Log.DEBUG, "LIST failed with: " + errString);
		} else {
			myLog.l(Log.DEBUG, "LIST completed OK");
		}
		// The success or error response over the control connection will
		// have already been handled by sendListing, so we can just quit now.
	}
	
	// Generates a line of a directory listing in the traditional /bin/ls
	// format.
	protected String makeLsString(File file) {
		StringBuilder response = new StringBuilder();
		
		if(!file.exists()) {
			staticLog.l(Log.INFO, "makeLsString had nonexistent file");
			return null;
		}

		// See Daniel Bernstein's explanation of /bin/ls format at:
		// http://cr.yp.to/ftp/list/binls.html
		// This stuff is almost entirely based on his recommendations.
		
		String lastNamePart = file.getName();
		// Many clients can't handle files containing these symbols
		if(lastNamePart.contains("*") || 
		   lastNamePart.contains("/"))
		{
			staticLog.l(Log.INFO, "Filename omitted due to disallowed character");
			return null;
		} else {
			staticLog.l(Log.DEBUG, "Filename: " + lastNamePart);
		}
			
		
		
		
		if(file.isDirectory()) {
			response.append("drwxr-xr-x 1 owner group");
		} else {
			// todo: think about special files, symlinks, devices
			response.append("-rw-r--r-- 1 owner group");
		}
		
		// The next field is a 13-byte right-justified space-padded file size
		long fileSize = file.length();
		String sizeString = new Long(fileSize).toString();
		int padSpaces = 13 - sizeString.length();
		while(padSpaces-- > 0) {
			response.append(' ');
		}
		response.append(sizeString);
		
		// The format of the timestamp varies depending on whether the mtime
		// is 6 months old
		long mTime = file.lastModified();
		SimpleDateFormat format;
		if(System.currentTimeMillis() - mTime > MS_IN_SIX_MONTHS) {
			// The mtime is less than 6 months ago
			format = new SimpleDateFormat(" MMM dd HH:mm ", Locale.US);
		} else {
			// The mtime is more than 6 months ago
			format = new SimpleDateFormat(" MMM dd  yyyy ", Locale.US);
		}
		response.append(format.format(new Date(file.lastModified())));
		response.append(lastNamePart);
		response.append("\r\n");
		
//		long mTime = file.lastModified();
//		SimpleDateFormat format;
////		if(System.currentTimeMillis() - mTime > MS_IN_SIX_MONTHS) {
////			// The mtime is less than 6 months ago
//			format = new SimpleDateFormat("yyyy-MM-dd");
////		} else {
////			// The mtime is more than 6 months ago
////			format = new SimpleDateFormat(" MMM dd yyyy ");
////		}
//		response.append(format.format(new Date(file.lastModified())));
//		format = new SimpleDateFormat(" hh:mm ");
////		} else {
////			// The mtime is more than 6 months ago
////			format = new SimpleDateFormat(" MMM dd yyyy ");
////		}
//		response.append(format.format(new Date(file.lastModified())));
//		long fileSize = file.length();
//		String sizeString = new Long(fileSize).toString();
//		int padSpaces = 13 - sizeString.length();
//		while(padSpaces-- > 0) {
//			response.append(' ');
//		}
//		response.append(sizeString);
//		response.append(' ');
//		response.append(lastNamePart);
//		response.append("\r\n");
//		Log.e("response of file list", response.toString());
		return response.toString();
	}

}
