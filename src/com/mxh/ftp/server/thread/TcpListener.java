package com.mxh.ftp.server.thread;

import java.net.ServerSocket;
import java.net.Socket;

import com.mxh.ftp.log.MyLog;

import android.util.Log;
/**
 * 负责监听的主服务器线程
 * @author XiangHang Mi
 *
 */
public class TcpListener extends Thread {
	ServerSocket listenSocket;
	FTPServerService ftpServerService;
	MyLog myLog = new MyLog(getClass().getName());
	
	public TcpListener(ServerSocket listenSocket, FTPServerService ftpServerService) {
		this.listenSocket = listenSocket;
		this.ftpServerService = ftpServerService;
	}
	
	public void quit() {
		try {
			listenSocket.close(); // if the TcpListener thread is blocked on accept,
			                      // closing the socket will raise an exception
		} catch (Exception e) {
			myLog.l(Log.DEBUG, "Exception closing TcpListener listenSocket");
		}
	}
	
	public void run() {
		try {
			while(true) {
				
				Socket clientSocket = listenSocket.accept();
				myLog.l(Log.INFO, "New connection, spawned thread");
//				新建一个线程，负责与每个客户进行通信
				SessionThread newSession = new SessionThread(clientSocket,
						new NormalDataSocketFactory(), true);
				newSession.start();
				ftpServerService.registerSessionThread(newSession);
			}
		} catch (Exception e) {
			myLog.l(Log.DEBUG, "Exception in TcpListener");
		}
	}
}
	
