package com.mxh.ftp.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

/**
 * ftp客户端各类命令执行函数:
 * 1.连接并登陆
 * 2.获取某个目录的文件列表
 *
 * 4.文件或目录的删除
 * 5.文件或目录的重命名
 * 6.文件或目录的上传
 * 7.文件或目录的下载
 * 8.文件或目录的移动
 * 9.目录的创建
 * @author XiangHang Mi
 *
 */
public class FtpClientCommand {
	private static FTPClient ftpClient=new FTPClient();
	
	public static final int LOG_IN_SUCCEEDED=0;
	public static final int ALREADY_CONNECTED=1;// the client has already connected to the server
	public static final int CONNECTED_WRONG=2;// there is some unknown exceptions or errors when connecting to the server
	public static final int SERVER_REFUSED=3;// the server refuses to be connected
	public final static int LOG_IN_FAILED=4;
	
//	是否已经被服务器认证
	public static boolean isAuthenticated(){
		return ftpClient.isAuthenticated();
	}
	
//	是否已经连接到服务器
	public static boolean isConnected(){
		return ftpClient.isConnected();
	}
	
//	断开服务器连接
	public static void disconnect(){
		try {
			ftpClient.disconnect(false);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void stopDataTransfer(){
		try {
			ftpClient.abortCurrentDataTransfer(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean changeDirectoryUp(){
		try {
			ftpClient.changeDirectoryUp();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		测试连接与登录
		String remoteServer="192.168.137.89";
		int port=2121;
		String userName="anonymous";
		String password="";
		
		switch(FtpClientCommand.ftpClientLogIn(remoteServer, port, userName, password)){
		case LOG_IN_SUCCEEDED:
			System.out.println("log in successfully");
			break;
		default:
			System.out.println("log in failed!");
			break;
		}
		System.out.println(FtpClientCommand.getCurrentDirectory());//测试获取当前目录
		
//		测试修改目录
//		String currentDir=FtpClientCommand.getCurrentDirectory();
//		FtpClientCommand.changeDirectory(currentDir+"/baidu");
//		System.out.println(FtpClientCommand.getCurrentDirectory());
		
//		测试获取文件列表，0为文件，1为目录
//		FTPFile[] fileList=FtpClientCommand.listFiles();
//		if(fileList!=null){
//			for(FTPFile file: fileList){
//				System.out.println("file type:"+file.getType()+" "+file.getName()+" fileSize:"+file.getSize()+" modified date:"+file.getModifiedDate());
//			}
//		}
//		else{
//			System.out.println("failed to get file list!");
//		}
		
//		测试上传文件
		
//		String filePath="src/com/mxh/ftpclient/FtpClientCommand.java";
//		File localFile=new File(filePath);
//		FtpClientCommand.uploadFile(localFile, uploadListener);
		
//		测试下载文件
//		File localFile=new File("img.jpg");
//		FtpClientCommand.downloadFile("DCIM/Camera/IMG.jpg", localFile, downloadListener);
		
//		测试重命名文件或目录
//		FtpClientCommand.renameFileOrDirectory("documents", "MyDocuments");
//		FtpClientCommand.renameFileOrDirectory("klog.txt", "golk.txt");
		
//		测试删除文件或目录功能
		FtpClientCommand.deleteFileOrDirectory("list.txt", 1);
		FtpClientCommand.deleteFileOrDirectory("book", 2);
		
//		测试移动文件或目录
//		FtpClientCommand.moveFileOrDirectory("Vlog.xml", "whodunitbk/Vlog.xml");
//		FtpClientCommand.moveFileOrDirectory("whodunitbk", "baidu/whodunitbk");
		
//		测试建立新的目录
		FtpClientCommand.createDirectory("mixianghang");
	}
	/**
	 * 获得当前目录，如果为null,则表示获取失败或者服务器尚未连接
	 * @return
	 */
	public static String getCurrentDirectory(){
		String dir=null;

			try {
				dir=ftpClient.currentDirectory();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPIllegalReplyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		return dir;
	}
	
	/**
	 * 下载文件，每次只能下载一个，当用户选择多个文件时，由更上一级处理
	 * @param remoteFileName
	 * @param localFile
	 * @param listener
	 * @return
	 */
	public static boolean downloadFile(String  remoteFileName, File localFile, FTPDataTransferListener listener){

			try {
				ftpClient.download(remoteFileName, localFile, listener);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPIllegalReplyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPDataTransferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPAbortedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return true;
	}
	
	
	/**
	 * 上传文件，，每次只能一个
	 * @param localFile
	 * @param listener
	 * @return
	 */
	public static boolean uploadFile(File localFile, FTPDataTransferListener listener){
	
			try {
				ftpClient.upload(localFile, listener);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPIllegalReplyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPDataTransferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPAbortedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
//		以下用于测试
//		try {
//			ftpClient.upload(localFile, listener);
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FTPIllegalReplyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FTPException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FTPDataTransferException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FTPAbortedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return true;
	}
	
	
	/**
	 * 改变当前目录
	 * @param path
	 * @return
	 */
	public static boolean changeDirectory(String path){

			try {
				ftpClient.changeDirectory(path);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPIllegalReplyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
		return true;
	}
	
	/**
	 * 获取当前目录下的文件列表信息
	 * @return
	 */
	public static FTPFile[] listFiles(){
		FTPFile[] fileList=null;
//		try {
//			fileList=ftpClient.list();
//		} catch (IllegalStateException | IOException | FTPIllegalReplyException
//				| FTPException | FTPDataTransferException | FTPAbortedException
//				| FTPListParseException e) {
//			// TODO Auto-generated catch block
//			return null;
//		}
		try {
			fileList=ftpClient.list();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			System.out.println("reply in an illegal way");
			e.printStackTrace();
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPDataTransferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPAbortedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPListParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileList;
	}
	
	/**
	 * 删除服务器上的某个文件或文件夹
	 * @param filePath：文件在服务器上的路径
	 * @param flag:1为文件，2为文件夹
	 * @return
	 */
	public static boolean deleteFileOrDirectory(String filePath, int flag){

			switch(flag){
			case FTPFile.TYPE_FILE://文件
				try {
					ftpClient.deleteFile(filePath);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();return false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();return false;
				} catch (FTPIllegalReplyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();return false;
				} catch (FTPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();return false;
				}
				break;
			case FTPFile.TYPE_DIRECTORY://目录
				try {
					ftpClient.deleteDirectory(filePath);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();return false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();return false;
				} catch (FTPIllegalReplyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();return false;
				} catch (FTPException e) {
					// TODO Auto-generated catch block
					
					e.printStackTrace();return false;
				}
				break;
			default:
				return false;
			}

		return true;
	}
	
	
	/**
	 * 重命名文件或目录
	 * @param oldName
	 * @param newName
	 * @return
	 */
	public static boolean renameFileOrDirectory(String oldName, String newName){
		
			try {
				ftpClient.rename(oldName, newName);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (FTPIllegalReplyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		
		return true;
	}
	
	/**
	 * 移动文件或目录
	 * @param oldPath
	 * @param newPath
	 * @return
	 */
	public static boolean moveFileOrDirectory(String oldPath, String newPath){
//		try {
//			ftpClient.rename(oldPath, newPath);
//		} catch (IllegalStateException | IOException | FTPIllegalReplyException
//				| FTPException e) {
//			// TODO Auto-generated catch block
//			return false;
//		}
		try {
			ftpClient.rename(oldPath, newPath);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	
	/**
	 * 建立新的 目录
	 * @param directoryName
	 * @return
	 */
	public static boolean createDirectory(String directoryName){
	
			try {
				ftpClient.createDirectory(directoryName);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPIllegalReplyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return true;
	}
	
	
	/**
	 *
	 * 连接并尝试登陆服务器
	 * @param remoteServer:服务器Ip地址
	 * @param port：服务器端口
	 * @param userName：登陆的用户名
	 * @param password：登陆的密码
	 * @return
	 */
	public static int ftpClientLogIn(String remoteServer,int port, String userName, String password){
		String[] connectResponse=null;
		try {
			connectResponse=ftpClient.connect(remoteServer, port);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			return FtpClientCommand.ALREADY_CONNECTED;
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return FtpClientCommand.CONNECTED_WRONG;
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			return FtpClientCommand.CONNECTED_WRONG;
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			return FtpClientCommand.SERVER_REFUSED;
		}
		if(connectResponse!=null){
			for(String response:connectResponse)
				System.out.println(response);
		}
		try {
			ftpClient.login(userName, password);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			return FtpClientCommand.CONNECTED_WRONG;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return FtpClientCommand.CONNECTED_WRONG;
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			return FtpClientCommand.CONNECTED_WRONG;
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			return FtpClientCommand.LOG_IN_FAILED;
		}
		return FtpClientCommand.LOG_IN_SUCCEEDED;
	}
	
	/**
	 * 监听上传的listener
	 */
	static FTPDataTransferListener uploadListener=new FTPDataTransferListener(){
		private int transferedSize=0;
		@Override
		public void aborted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void completed() {
			// TODO Auto-generated method stub
			System.out.println("succeed!");
		}

		@Override
		public void failed() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void started() {
			// TODO Auto-generated method stub
			System.out.println("started!");
		}

		@Override
		public void transferred(int arg0) {
			// TODO Auto-generated method stub
			transferedSize+=arg0;
			System.out.println("transfered : "+transferedSize);
		}
		
	};
	
	static FTPDataTransferListener downloadListener=new FTPDataTransferListener(){
		private int transferedSize=0;
		@Override
		public void aborted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void completed() {
			// TODO Auto-generated method stub
			System.out.println("succeed!");
		}

		@Override
		public void failed() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void started() {
			// TODO Auto-generated method stub
			System.out.println("started!");
		}

		@Override
		public void transferred(int arg0) {
			// TODO Auto-generated method stub
			transferedSize+=arg0;
			System.out.println("transfered : "+transferedSize);
		}
		
	};
}
