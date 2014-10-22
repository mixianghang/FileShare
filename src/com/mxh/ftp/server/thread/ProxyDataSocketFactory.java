/**
 * 
 */
package com.mxh.ftp.server.thread;

import java.net.InetAddress;
import java.net.Socket;

import com.mxh.ftp.util.Globals;

import android.util.Log;

/**
 * @author david
 *
 */
public class ProxyDataSocketFactory extends DataSocketFactory {

	
	private Socket socket;
	private int proxyListenPort;
	ProxyConnector proxyConnector;
	InetAddress clientAddress;
	int clientPort;
	
	public ProxyDataSocketFactory() {
		clearState();
	}
	
	private void clearState() {
		if(socket != null) {
			try {
				socket.close();
			} catch (Exception e) {}
		}
		socket = null;
		proxyConnector = null;
		clientAddress = null;
		proxyListenPort = 0;
		clientPort = 0;
	}

	public InetAddress getPasvIp() {
		ProxyConnector pc = Globals.getProxyConnector();
		if(pc == null) {
			return null;
		}
		return pc.getProxyIp();
	}

//	public int getPortNumber() {
//		if(socket == )
//		return 0;
//	}

	public int onPasv() {
		clearState();
		proxyConnector = Globals.getProxyConnector();
		if(proxyConnector == null) {
			myLog.l(Log.INFO, "Unexpected null proxyConnector in onPasv");
			return 0;
		}
		ProxyDataSocketInfo info = proxyConnector.pasvListen();
		socket = info.getSocket();
		proxyListenPort = info.getRemotePublicPort();
		return proxyListenPort;
	}

	public boolean onPort(InetAddress dest, int port) {
		clearState();
		proxyConnector = Globals.getProxyConnector();
		this.clientAddress = dest;
		this.clientPort = port;
		myLog.d("ProxyDataSocketFactory client port settings stored");
		return true;
	}


	public Socket onTransfer() {
		if(proxyConnector == null) {
			myLog.w("Unexpected null proxyConnector in onTransfer");
			return null;
		}
			
		if(socket == null) {
			// We are in PORT mode (not PASV mode)
			if(proxyConnector == null) {
				myLog.l(Log.INFO, "Unexpected null proxyConnector in onTransfer");
				return null;
			}
			// May return null, that's fine. ProxyConnector will log errors.
			socket = proxyConnector.dataPortConnect(clientAddress, clientPort);
			return socket;
		} else {
			// We are in PASV mode (not PORT mode)
			if(proxyConnector.pasvAccept(socket)) {
				return socket;
			} else {
				myLog.w("proxyConnector pasvAccept failed");
				return null;
			}
		}
	}

}
