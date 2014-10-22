

package com.mxh.ftp.server.thread;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.mxh.ftp.log.MyLog;
import com.mxh.ftp.server.command.FtpCmd;
import com.mxh.ftp.util.Account;
import com.mxh.ftp.util.Defaults;
import com.mxh.ftp.util.Globals;
import com.mxh.ftp.util.Util;

import android.util.Log;

public class SessionThread extends Thread {
	protected boolean shouldExit = false;
	
//	监听用户指令的进程
	protected Socket socket;
	protected MyLog myLog = new MyLog(getClass().getName());
	
	
	protected ByteBuffer buffer = ByteBuffer.allocate(Defaults
			.getInputBufferSize());
//	是否使用被动传输模式
	protected boolean pasvMode = false;
	
//	传输数据的格式是否为二进制流
	protected boolean binaryMode = false;
	public Account account = new Account();
	
//	用户是否已经认证
	protected boolean authenticated = false;
	protected File workingDir = Globals.getChrootDir();
	// protected ServerSocket dataServerSocket = null;
//	用于传输数据的socket
	protected Socket dataSocket = null;
	// protected FTPServerService service;
	protected File renameFrom = null;
	// protected InetAddress outDataDest = null;
	// protected int outDataPort = 20; // 20 is the default ftp-data port
//	用于产生dataSocket
	protected DataSocketFactory dataSocketFactory;
	
//	用于向客户端输出数据
	OutputStream dataOutputStream = null;
	private boolean sendWelcomeBanner;
	protected String encoding = Defaults.SESSION_ENCODING;

	/**
	 * Used when we get a PORT command to open up an outgoing socket.
	 * 
	 * @return
	 */
	// public void setPortSocket(InetAddress dest, int port) {
	// myLog.l(Log.DEBUG, "Setting PORT dest to " +
	// dest.getHostAddress() + " port " + port);
	// outDataDest = dest;
	// outDataPort = port;
	// }
	/**
	 * Sends a string over the already-established data socket
	 * 
	 * @param string
	 * @return Whether the send completed successfully
	 */
	public boolean sendViaDataSocket(String string) {
		try {
			byte[] bytes = string.getBytes(encoding);
			myLog.d("Using data connection encoding: " + encoding);
			return sendViaDataSocket(bytes, bytes.length);
		} catch (UnsupportedEncodingException e) {
			myLog.l(Log.ERROR, "Unsupported encoding for data socket send");
			return false;
		}
	}

	public boolean sendViaDataSocket(byte[] bytes, int len) {
		return sendViaDataSocket(bytes, 0, len);
	}

	/**
	 * Sends a byte array over the already-established data socket
	 * 
	 * @param bytes
	 * @param len
	 * @return
	 */
	public boolean sendViaDataSocket(byte[] bytes, int start, int len) {

		if (dataOutputStream == null) {
			myLog.l(Log.ERROR, "Can't send via null dataOutputStream");
			return false;
		}
		if (len == 0) {
			return true; // this isn't an "error"
		}
		try {
			dataOutputStream.write(bytes, start, len);
		} catch (IOException e) {
			myLog.l(Log.INFO, "Couldn't write output stream for data socket");
			myLog.l(Log.INFO, e.toString());
			return false;
		}
		return true;
	}

	/**
	 * Received some bytes from the data socket, which is assumed to already be
	 * connected. The bytes are placed in the given array, and the number of
	 * bytes successfully read is returned.
	 * 
	 * @param bytes
	 *            Where to place the input bytes
	 * @return >0 if successful which is the number of bytes read, -1 if no
	 *         bytes remain to be read, -2 if the data socket was not connected,
	 *         0 if there was a read error
	 */
	
//	从数据socket接收数据
	public int receiveFromDataSocket(byte[] buf) {
		int bytesRead;

		if (dataSocket == null) {
			myLog.l(Log.INFO, "Can't receive from null dataSocket");
			return -2;
		}
		if (!dataSocket.isConnected()) {
			myLog.l(Log.INFO, "Can't receive from unconnected socket");
			return -2;
		}
		InputStream in;
		try {
			in = dataSocket.getInputStream();
			// If the read returns 0 bytes, the stream is not yet
			// closed, but we just want to read again.
			while ((bytesRead = in.read(buf, 0, buf.length)) == 0) {
			}
			if (bytesRead == -1) {
				// If InputStream.read returns -1, there are no bytes
				// remaining, so we return 0.
				return -1;
			}
		} catch (IOException e) {
			myLog.l(Log.INFO, "Error reading data socket");
			return 0;
		}
		return bytesRead;
	}

	/**
	 * Called when we receive a PASV command.
	 * 启动socket server返回系统分配的端口
	 * @return Whether the necessary initialization was successful.
	 */
	public int onPasv() {
		return dataSocketFactory.onPasv();
	}

	/**
	 * Called when we receive a PORT command.
	 * 
	 * @return Whether the necessary initialization was successful.
	 */
	public boolean onPort(InetAddress dest, int port) {
		return dataSocketFactory.onPort(dest, port);
	}

	public InetAddress getDataSocketPasvIp() {
		return dataSocketFactory.getPasvIp();
	}

	// public int getDataSocketPort() {
	// return dataSocketFactory.getPortNumber();
	// }

	/**
	 * Will be called by (e.g.) CmdSTOR, CmdRETR, CmdLIST, etc. when they are
	 * about to start actually doing IO over the data socket.
	 * 建立datasocket
	 * @return
	 */
	public boolean startUsingDataSocket() {
		try {
			dataSocket = dataSocketFactory.onTransfer();
			if (dataSocket == null) {
				myLog.l(Log.INFO,
						"dataSocketFactory.onTransfer() returned null");
				return false;
			}
			dataOutputStream = dataSocket.getOutputStream();
			return true;
		} catch (IOException e) {
			myLog.l(Log.INFO,
					"IOException getting OutputStream for data socket");
			dataSocket = null;
			return false;
		}
	}

	public void quit() {
		myLog.d("SessionThread told to quit");
		closeSocket();
	}
//数据读取存取完毕后，关闭，在Nlist\List\retr\stor四个命令中使用
	public void closeDataSocket() {
		myLog.l(Log.DEBUG, "Closing data socket");
		if (dataOutputStream != null) {
			try {
				dataOutputStream.close();
			} catch (IOException e) {
			}
			dataOutputStream = null;
		}
		if (dataSocket != null) {
			try {
				dataSocket.close();
			} catch (IOException e) {
			}
		}
		dataSocket = null;
	}

	protected InetAddress getLocalAddress() {
		return socket.getLocalAddress();
	}

	static int numNulls = 0;
	/*
	 * 接受客户端的指令，并执行
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		myLog.l(Log.INFO, "SessionThread started");

		if(sendWelcomeBanner) {
			String serverNameVersion = "nothing!";
			writeString("220 SwiFTP " + Util.getVersion() + " ready\r\n");
		}
		// Main loop: read an incoming line and process it
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()), 8192); // use 8k buffer
			while (true) {
//				循环读取客户的命令
				String line;
				line = in.readLine(); // will accept \r\n or \n for terminator
				if (line != null) {
//					写入监视对话的日志
					FTPServerService.writeMonitor(true, line);
//					Log.e("input", line);
					myLog.l(Log.DEBUG, "Received line from client: " + line);
//					解析并执行指令
					FtpCmd.dispatchCommand(this, line);
				} else {
					myLog.i("readLine gave null, quitting");
					break;
				}
			}
		} catch (IOException e) {
			myLog.l(Log.INFO, "Connection was dropped");
		}
		closeSocket();
	}

	/**
	 * A static method to check the equality of two byte arrays, but only up to
	 * a given length.
	 */
	public static boolean compareLen(byte[] array1, byte[] array2, int len) {
		for (int i = 0; i < len; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}
		return true;
	}

//	关闭命令socket
	public void closeSocket() {
		if (socket == null) {
			return;
		}
		try {
			socket.close();
		} catch (IOException e) {}
	}

//	在很多命令中被调用，用于向CommandSocket中写入回复
	public void writeBytes(byte[] bytes) {
		try {
			// socket.write(ByteBuffer.wrap(bytes)); // from old SocketChannel
			// impln.
			BufferedOutputStream out = new BufferedOutputStream(socket
					.getOutputStream(), Defaults.dataChunkSize);
			out.write(bytes);
			out.flush();
		} catch (IOException e) {
			myLog.l(Log.INFO, "Exception writing socket");
			closeSocket();
			return;
		}
	}

	public void writeString(String str) {
		FTPServerService.writeMonitor(false, str);
//		Log.e("response in command socket in Session Thread writeString", str);
		byte[] strBytes;
		try {
			strBytes = str.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			myLog.e("Unsupported encoding: " + encoding);
			strBytes = str.getBytes();
		}
		writeBytes(strBytes);
	}

	public Socket getSocket() {
		return socket;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public boolean isPasvMode() {
		return pasvMode;
	}

	public SessionThread(Socket socket, DataSocketFactory dataSocketFactory,
			boolean sendWelcomeBanner) {
		this.socket = socket;
		this.dataSocketFactory = dataSocketFactory;
		this.sendWelcomeBanner = sendWelcomeBanner;
	}

	static public ByteBuffer stringToBB(String s) {
		return ByteBuffer.wrap(s.getBytes());
	}

	public boolean isBinaryMode() {
		return binaryMode;
	}

	public void setBinaryMode(boolean binaryMode) {
		this.binaryMode = binaryMode;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		if (authenticated) {
			myLog.l(Log.INFO, "Authentication complete");
		}
		this.authenticated = authenticated;
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		try {
			this.workingDir = workingDir.getCanonicalFile().getAbsoluteFile();
		} catch (IOException e) {
			myLog.l(Log.INFO, "SessionThread canonical error");
		}
	}

	/*
	 * public FTPServerService getService() { return service; }
	 * 
	 * public void setService(FTPServerService service) { this.service =
	 * service; }
	 */

	public Socket getDataSocket() {
		return dataSocket;
	}

	public void setDataSocket(Socket dataSocket) {
		this.dataSocket = dataSocket;
	}

	// public ServerSocket getServerSocket() {
	// return dataServerSocket;
	// }

	public File getRenameFrom() {
		return renameFrom;
	}

	public void setRenameFrom(File renameFrom) {
		this.renameFrom = renameFrom;
	}
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
