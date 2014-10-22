package com.mxh.ftp.client;

import java.io.File;
import java.net.Socket;

import com.mxh.ftp.server.thread.NormalDataSocketFactory;
import com.mxh.ftp.server.thread.SessionThread;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

public class FtpClientDataTransferThread extends Thread { 
	private boolean isUpload;
	private Bundle bundle;
	private FTPDataTransferListener listener;
	public static final String LOCAL_FILE="localFile";
	public static final String 	REMOTE_FILE="remoteFile";
	public FtpClientDataTransferThread(boolean isUpload, Bundle bundle, FTPDataTransferListener listener){
		this.isUpload=isUpload;
		this.bundle=bundle;
		this.listener=listener;
		
	}
	public void run() {
		Looper.prepare();
	
		if(isUpload){
			File localFile=new File(bundle.getString(LOCAL_FILE));
			FtpClientCommand.uploadFile(localFile, listener);
		}
		else{
			File localFile=new File(bundle.getString(LOCAL_FILE));
			String remoteFileName=bundle.getString(REMOTE_FILE);
			FtpClientCommand.downloadFile(remoteFileName, localFile, listener);
		}
		Looper.loop();
//		quit();
	}
}
