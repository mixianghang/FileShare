package com.mxh.ftp.server.thread;

import java.net.InetAddress;
import java.net.Socket;

import com.mxh.ftp.log.MyLog;


abstract public class DataSocketFactory {
	

	protected MyLog myLog = new MyLog(getClass().getName());
	

	abstract public boolean onPort(InetAddress dest, int port);
	

	abstract public int onPasv();


	abstract public Socket onTransfer();

	abstract public InetAddress getPasvIp();
}

