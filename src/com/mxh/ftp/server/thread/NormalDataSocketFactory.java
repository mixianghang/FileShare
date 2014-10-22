package com.mxh.ftp.server.thread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.mxh.ftp.util.Defaults;

import android.util.Log;

public class NormalDataSocketFactory extends DataSocketFactory {

	
	// Listener socket used for PASV mode
	ServerSocket server = null;
	// Remote IP & port information used for PORT mode
	InetAddress remoteAddr;
	int remotePort;
	boolean isPasvMode = true;
	
	public NormalDataSocketFactory() {
		clearState();
	}
		
	
	private void clearState() {
		/**
		 * Clears the state of this object, as if no pasv() or port() had occurred.
		 * All sockets are closed.
		 */
		if(server != null) {
			try {
				server.close();
			} catch (IOException e) {}
		}
		server = null;
		remoteAddr = null;
		remotePort = 0;
		myLog.l(Log.DEBUG, "NormalDataSocketFactory state cleared");
	}
	
//	处理Pasv命令，建立监听数据的serverSocket，并返回系统随机分配的端口
	public int onPasv() {
		clearState();
		try {
			// Listen on any port (port parameter 0)
//			新建一个serverSocket ，端口随机分配
			server = new ServerSocket(0, Defaults.tcpConnectionBacklog);
			myLog.l(Log.DEBUG, "Data socket pasv() listen successful");
			return server.getLocalPort();
		} catch(IOException e) {
			myLog.l(Log.ERROR, "Data socket creation error");
			clearState();
			return 0;
		}
	}

//	Port命令来时，调用
	public boolean onPort(InetAddress remoteAddr, int remotePort) {
		clearState();
		this.remoteAddr = remoteAddr;
		this.remotePort = remotePort;
		return true;
	}
	/*启动数据传输过程，并返回已经建立的socket
	 * 根据当前的模式，启动
	 * Port模式就是对方传过来了对方的端口与地址，我们主动建立socket数据连接
	 * Pasv:则是被动监听某个端口，等待对方发来连接信息
	 */
	public Socket onTransfer() {
		if(server == null) {//表明不是被动模式，要主动建立dataSocket连接
			// We're in PORT mode (not PASV)
			if(remoteAddr == null || remotePort == 0) {
				myLog.l(Log.INFO, "PORT mode but not initialized correctly");
				clearState();
				return null;
			}
			Socket socket;
			try {
				socket = new Socket(remoteAddr, remotePort);
			} catch (IOException e) {
				myLog.l(Log.INFO, 
						"Couldn't open PORT data socket to: " +
						remoteAddr.toString() + ":" + remotePort);
				clearState();
				return null;
			}
			return socket;
		} else {
			// We're in PASV mode (not PORT)
			Socket socket = null;
			try {
//				我们等待对方发起连接
				socket = server.accept();
				myLog.l(Log.DEBUG, "onTransfer pasv accept successful");
			} catch (Exception e) {
				myLog.l(Log.INFO, "Exception accepting PASV socket");
				socket = null;
			}
			clearState();
			return socket;  // will be null if error occurred
		}
	}
	
	/**
	 * Return the port number that the remote client should be informed of (in the body
	 * of the PASV response).
	 * @return The port number, or -1 if error.
	 */
	public int getPortNumber() {
		if(server != null) {
			return server.getLocalPort(); // returns -1 if serversocket is unbound 
		} else {
			return -1;
		}
	}
	
	public InetAddress getPasvIp() {
		//String retVal = server.getInetAddress().getHostAddress();
		return FTPServerService.getWifiIp();
	}
}