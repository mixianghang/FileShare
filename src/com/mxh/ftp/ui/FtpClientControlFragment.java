package com.mxh.ftp.ui;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mxh.ftp.ui.FtpContainerActivity.IBackPressedListener;
import com.mxh.ftp.ui.R;
import com.mxh.ftp.util.Defaults;
import com.mxh.ftp.util.FileSortHelper;
import com.mxh.ftp.util.FtpUtil;
import com.mxh.ftp.util.Stack;
import com.mxh.ftp.util.Util;
import com.mxh.ftp.barcode.QRCode;
import com.mxh.ftp.client.FtpClientCommand;
import com.mxh.ftp.client.FtpClientDataTransferThread;
import com.mxh.ftp.encryption.JceProvider;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class FtpClientControlFragment extends Fragment implements IBackPressedListener {
	private int currentSelectedItem=-1;
	private Button startServerButton=null;
	private String currentDirectory="";
	private TextView currentPathTextView;
	public static final int CHOOSE_CERFITICATE=2;
	
	private String fileToMove=null;//要移动的文件或目录
	private String destination=null;
	FileListAdapter  adapter;
	LinearLayout fileListHelper;
	LinearLayout connectServer;
	private Button importCerticateButton;
	private Button connectButton;
	private Button cancelButton;
	private EditText ipAddressEdit;
	private EditText portEdit;
	private EditText usernameEdit;
	private EditText passwordEdit;
	private Button upButton;
	private ListView list;
	
//	以下用于存储当前目录下用户浏览的位置
	int currentSelectedPosition=0;
	Stack positionStack=new Stack(Defaults.MAX_STACK_ITEMS);
	int itemSize=2;
	
	private FileSortHelper sortHelper=new FileSortHelper();
	private Spinner  sortTypeSpinner;
	private CheckBox fileFirstCheckBox;
	private Map<String,FileSortHelper.SortMethod> sortTypeMap=new HashMap<String,FileSortHelper.SortMethod>();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		View view=inflater.inflate(R.layout.fragment_ftp_client_control, container, false);
		LayoutInflater li1 = LayoutInflater.from(getActivity()); 
        View view1 = li1.inflate(R.layout.file_browser_item, null); 
		list=(ListView)view.findViewById(R.id.ftp_client_listview);
		startServerButton=(Button)view.findViewById(R.id.server_status_button);
		startServerButton.setOnClickListener(serverStatusListener);
		
		fileListHelper=(LinearLayout)view.findViewById(R.id.file_list_helper);
		fileListHelper.setVisibility(View.GONE);
		
		sortTypeSpinner=(Spinner)view.findViewById(R.id.sort_type_spinner);
		fileFirstCheckBox=(CheckBox)view.findViewById(R.id.file_first_check);
		
//		初始化spinner并设置监听事件
		ArrayAdapter adapter1 = ArrayAdapter.createFromResource( getActivity(), R.array.sort_type_array, android.R.layout.simple_spinner_item); 
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		sortTypeSpinner.setAdapter(adapter1);
		sortTypeSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);
		Resources rs=getActivity().getResources();
		String[] types=rs.getStringArray(R.array.sort_type_array);
		FileSortHelper.SortMethod[] sortMethods=FileSortHelper.SortMethod.values();
		for(int i=0;i<types.length;i++){
			sortTypeMap.put(types[i], sortMethods[i]);
		}
		
//		设置fileFirstCheckBox的点击事件
		fileFirstCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				sortHelper.setFileFirst(isChecked);
				updateUi();
			}
			
		});
		connectServer=(LinearLayout)view.findViewById(R.id.connect_server);
		connectServer.setVisibility(View.GONE);
		
		importCerticateButton=(Button)connectServer.findViewById(R.id.import_server_certicate_button);
    	connectButton=(Button)connectServer.findViewById(R.id.connect_button);
    	cancelButton=(Button)connectServer.findViewById(R.id.cancel_button);
    	ipAddressEdit=(EditText)connectServer.findViewById(R.id.server_ip_address_edit);
    	portEdit=(EditText)connectServer.findViewById(R.id.server_port_edit);
    	usernameEdit=(EditText)connectServer.findViewById(R.id.server_user_name_edit);
    	passwordEdit=(EditText)connectServer.findViewById(R.id.server_password_edit);
    	importCerticateButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				选择令牌
				 SharedPreferences settings = getActivity().getSharedPreferences(Defaults.getSettingsName(),
							Defaults.getSettingsMode());
					String certificateFolder=settings.getString(FtpClientSettingFragment.DEFAULT_CERTIFICATE_FOLDER, Defaults.certificateDir);
//				启动文件浏览器选择令牌
				Intent intent = new Intent(getActivity().getBaseContext(), FileDialog.class);
	            intent.putExtra(FileDialog.START_PATH, certificateFolder);
	            
	            //can user select directories or not
	            intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
	            intent.putExtra(FileDialog.SELECTION_MODE, 1);
	            
	            //alternatively you can set file filter
	            //intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" });
	            
	            startActivityForResult(intent, CHOOSE_CERFITICATE);
			}
    		
    	});
    	
    	connectButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				连接服务器
				String ipAddress=ipAddressEdit.getText().toString();
				String portString=portEdit.getText().toString();
				String username=usernameEdit.getText().toString();
				String password=passwordEdit.getText().toString();
				if(password==null)
					password="";
				Log.e("info", ipAddress+portString+username+password);
				
				int port=Integer.parseInt(portString==null||"".equals(portString)?"-1":portString);
//				检测ip地址是否正确,必须加转义字符
				String[] ips=ipAddress.split("\\.");
				int isIpAddressRight=0;
				if(ips.length==4){
					for(String ip:ips){
						int temp=-1;
						try{
						temp=Integer.parseInt(ip);
						}
						catch(NumberFormatException e){
							
						}
						if(0<=temp&&temp<=255)
							isIpAddressRight++;
					}
				}
				if(isIpAddressRight!=4){
//					Log.e("ips", ""+i)
					Util.makeToast(getActivity()," the ip address is not right");
					return;
				}
				if(port<0||port>65536){
					Util.makeToast(getActivity()," the port is not right");
					return;
				}
				int result=FtpClientCommand.ftpClientLogIn(ipAddress,port, username, password);
				if(result!=FtpClientCommand.LOG_IN_SUCCEEDED){
					Util.makeToast(getActivity(), "连接失败");
				}
				else{
					updateUi();
					connectServer.setVisibility(View.GONE);
				}
				
				
				
			}
    		
    	});
    	cancelButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				取消连接
				connectServer.setVisibility(View.GONE);
				
			}
    		
    	});
////		测试连接与登录
//		String remoteServer="192.168.137.89";
//		int port=2121;
//		String userName="anonymous";
//		String password="";
//		
//		switch(FtpClientCommand.ftpClientLogIn(remoteServer, port, userName, password)){
//		case FtpClientCommand.LOG_IN_SUCCEEDED:
//			Log.e("ftpclient", "log in successfully");
//			break;
//		default:
//			Log.e("ftpclient", "log in error");
//			break;
//		}
//		String warning=null;
//		if(!FtpClientCommand.isConnected()){
//			warning="请先连接服务器！";
//		}
//		else if(!FtpClientCommand.isAuthenticated()){
//			warning="未通过服务器认证！";
//		}
//		if(warning!=null){
//			new AlertDialog.Builder(getActivity())
//					.setTitle(warning)
//					.setPositiveButton("确定", null)
//					.show();
//			return view;
//		}
		adapter=new FileListAdapter(getActivity());
		list.setAdapter(adapter);
		registerForContextMenu(list);//登记上下文菜单
		list.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				currentSelectedItem=arg2;
				return false;
			}
			
		});
		currentPathTextView=(TextView)view.findViewById(R.id.current_directory);
		currentPathTextView.setText(currentDirectory);
		
//		设置向上按钮及其事件
		upButton=(Button)view.findViewById(R.id.go_up_directory);
		if("/".equals(currentDirectory))
			upButton.setEnabled(false);
		upButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!FtpClientCommand.changeDirectoryUp()){
					Util.makeToast(getActivity(), "change directory failed");
					return;
				}
//				如果是根目录，则无法向上一个目录
//				currentSelectedPosition=positionStack.getItem();
				updateUi();
//				list.setSelection(currentSelectedPosition);
				setPosition();
			}
			
		});
		
//		listview选项的单击事件
		list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				FTPFile file=(FTPFile) adapter.getItem(arg2);
				if(file.getType()==FTPFile.TYPE_DIRECTORY){
					FtpClientCommand.changeDirectory(currentDirectory+"/"+file.getName());
				
					positionStack.addItem(list.getFirstVisiblePosition());
					updateUi();
					
				}
			}
			
		});
		updateUi();
		return view;
	}
	

//	处理排序方法的点击事件
	OnItemSelectedListener spinnerItemSelectedListener=new OnItemSelectedListener(){

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			String typeString=(String) sortTypeSpinner.getItemAtPosition(arg2);
			FileSortHelper.SortMethod sortMethod=sortTypeMap.get(typeString);
			sortHelper.setSortMethog(sortMethod);
			updateUi();
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
//	创建上下文菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
//	    Util.makeToast(getActivity(), "create menu");
	    MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.file_item_context_menu, menu);
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<FTPFile> getFileList(){
		if(FtpClientCommand.isAuthenticated()&&FtpClientCommand.isConnected()){
			FTPFile[] fileList=FtpClientCommand.listFiles();
			ArrayList<FTPFile> lists=new ArrayList<FTPFile>();
			for(FTPFile file1:fileList)
				lists.add(file1);
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
			Collections.sort(lists, sortHelper.getComparator());
			return lists;
		}
		return null;
	}
//	处理上下文菜单的点击事件
	@Override
	public boolean onContextItemSelected(MenuItem item) {
//		首先判断是否已经连接并且获得服务器认证
		String warning=null;
		if(!FtpClientCommand.isConnected()){
			warning="请先连接服务器！";
		}
		else if(!FtpClientCommand.isAuthenticated()){
			warning="未通过服务器认证！";
		}
		
		if(warning!=null){
			new AlertDialog.Builder(getActivity())
					.setTitle(warning)
					.setPositiveButton("确定", null)
					.show();
			return true;
		}
		FTPFile file;
		final AlertDialog alert;
		ProgressBar progress;
		Button cancelButton;
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		LayoutInflater l = LayoutInflater.from(getActivity()); 
		
		switch(item.getItemId()){
		case R.id.file_delete://删除文件
			file=(FTPFile)adapter.getItem(currentSelectedItem);
			String path=file.getName();
			if(!FtpClientCommand.deleteFileOrDirectory(path, file.getType())){
				Util.makeToast(getActivity(), "delete file or directory failed!");
				break;
			}
			Util.makeToast(getActivity(), "delete file or directory successfully!");
			updateUi();
			break;
		case R.id.file_download://下载文件到根目录下
			 file=(FTPFile)adapter.getItem(currentSelectedItem);
			 if(file.getType()!=FTPFile.TYPE_FILE){
				 Util.makeToast(getActivity(), getActivity().getResources().getString(R.string.should_choose_a_file));
				 break;
			 }
			 boolean sdCardExist = Environment.getExternalStorageState() 
		                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在 
			if(!sdCardExist){
				 Util.makeToast(getActivity(), getActivity().getResources().getString(R.string.sdcard_not_exist));
				break;
			}
			SharedPreferences settings = getActivity().getSharedPreferences(Defaults.getSettingsName(),
					Defaults.getSettingsMode());
			String downloadFolder=settings.getString(FtpClientSettingFragment.DEFAULT_DOWNLOAD_FOLDER, Defaults.downloadDir);
			
           File localFile=FtpUtil.createFile(downloadFolder, file.getName());
			builder.setTitle(getActivity().getResources().getString(R.string.download_file)+file);
			LayoutInflater li = LayoutInflater.from(getActivity()); 
            View view = li.inflate(R.layout.progressbar, null); 
            builder.setView(view);
            alert=builder.create();
            progress=(ProgressBar)view.findViewById(R.id.progressBar);
//            这里数值在某些情况下会超，数据量为好几个G的时候，所以改为/1000
            progress.setMax((int) file.getSize());
            Log.e("file size", ""+file.getSize());
            progress.setProgress(0);
            alert.show();
            UploadOrDownloadListener listener=new UploadOrDownloadListener(progress,alert,UploadOrDownloadListener.TYPE_DOWNLOAD);

            //            FtpClientCommand.downloadFile(file.getName(), localFile, listener);
            cancelButton=(Button)view.findViewById(R.id.cancel_button);
            cancelButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					FtpClientCommand.stopDataTransfer();
					alert.dismiss();
				}
            	
            });
            Bundle bundle=new Bundle();
            bundle.putString(FtpClientDataTransferThread.LOCAL_FILE, localFile.getAbsolutePath());
            bundle.putString(FtpClientDataTransferThread.REMOTE_FILE, file.getName());
            FtpClientDataTransferThread thread=new FtpClientDataTransferThread(false, bundle, listener);
            thread.start();
			break;
		case R.id.file_move:
			final int currentSelectedPos=0;
			final Stack posStack=new Stack(Defaults.MAX_STACK_ITEMS);
			fileToMove=((FTPFile) adapter.getItem(currentSelectedItem)).getName();
			LayoutInflater li1 = LayoutInflater.from(getActivity()); 
            View view1 = li1.inflate(R.layout.choose_directory, null); 
            builder.setView(view1);
            
//            设置文件列表
            final ListView listview=(ListView) view1.findViewById(R.id.file_list_view);
			ArrayList<FTPFile> lists=getFileList();

			
			final FileListAdapter fileAdapter=new FileListAdapter(getActivity());
			fileAdapter.setFileList(lists);
			listview.setAdapter(fileAdapter);
//			设置当前目录
			final TextView currentPathTextView=(TextView)view1.findViewById(R.id.current_directory);
			currentPathTextView.setText(currentDirectory);
			
//			设置向上按钮及其事件
			final Button upButton=(Button)view1.findViewById(R.id.go_up_directory);
			if(currentDirectory=="/")
				upButton.setEnabled(false);
			upButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(!FtpClientCommand.changeDirectoryUp()){
						Util.makeToast(getActivity(), "change directory failed");
						return;
					}
					ArrayList<FTPFile> lists=getFileList();
					fileAdapter.setFileList(lists);
					String currentDir=FtpClientCommand.getCurrentDirectory();
					currentPathTextView.setText(currentDir);
					listview.smoothScrollToPosition(posStack.getItem()+5);
//					listview.setSelection(posStack.getItem());
//					如果是根目录，则无法向上一个目录
					if("/".equals(currentDir))
						upButton.setEnabled(false);
				}
				
			});
			listview.setCacheColorHint(0);
//			listview选项的单击事件
			listview.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					FTPFile file=(FTPFile) fileAdapter.getItem(arg2);
					if(file.getType()==FTPFile.TYPE_DIRECTORY){
						FtpClientCommand.changeDirectory(FtpClientCommand.getCurrentDirectory()+"/"+file.getName());
						ArrayList<FTPFile> lists=getFileList();
						fileAdapter.setFileList(lists);
						upButton.setEnabled(true);
						currentPathTextView.setText(FtpClientCommand.getCurrentDirectory());
						posStack.addItem(arg2);
					}
				}
				
			});
			final AlertDialog alert1=builder.create();
			Button okButton2=(Button)view1.findViewById(R.id.ok_button);
			Button cancelButton3=(Button)view1.findViewById(R.id.cancel_button);
			okButton2.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					destination=FtpClientCommand.getCurrentDirectory();
					String source=currentDirectory;
					if(FtpClientCommand.moveFileOrDirectory(source+"/"+fileToMove, destination+"/"+fileToMove)){
						Util.makeToast(getActivity(), "move successfully");
						FtpClientCommand.changeDirectory(currentDirectory);
						updateUi();
						
						alert1.dismiss();
				}
				}
			}
				
			);
			
			cancelButton3.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					alert1.dismiss();
				}
			}
				
			);

			
			builder.setTitle("choose the directory to move file into");
			alert1.show();
			break;
		case R.id.file_new_directory://在当前目录下创建新目录
			view=l.inflate(R.layout.new_dir, null);
			final EditText dirNameEdit=(EditText)view.findViewById(R.id.new_dir_edit);
			Button okButton=(Button)view.findViewById(R.id.ok_button);
			Button cancelButton1=(Button)view.findViewById(R.id.cancel_button);
			builder.setTitle("input new directory name");
			builder.setView(view);
			final AlertDialog alert3=builder.create();
			okButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String  newDirName=dirNameEdit.getText().toString();
					if(newDirName==null||"".equals(newDirName)){
						Util.makeToast(getActivity(), "please input dir name");
					}
					else{
						if(FtpClientCommand.createDirectory(newDirName)){
							updateUi();
							Util.makeToast(getActivity(), "create new dir succeefully");
						}
						alert3.dismiss();
					}
				}
				
			});
			cancelButton1.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					alert3.dismiss();
				}
				
			});
			alert3.show();
			break;
		case R.id.file_rename:
			final String oldName=((FTPFile)adapter.getItem(currentSelectedItem)).getName();
			view=l.inflate(R.layout.rename_dialog, null);
			final EditText fileNameEdit=(EditText)view.findViewById(R.id.new_name_edit);
			Button okButton1=(Button)view.findViewById(R.id.ok_button);
			Button cancelButton2=(Button)view.findViewById(R.id.cancel_button);
			builder.setTitle("input a new name");
			builder.setView(view);
			final AlertDialog alert4=builder.create();
			okButton1.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String  newFileName=fileNameEdit.getText().toString();
					if(newFileName==null||"".equals(newFileName)){
						Util.makeToast(getActivity(), "please input a new name");
					}
					else{
						if(FtpClientCommand.renameFileOrDirectory(oldName, newFileName)){
							Util.makeToast(getActivity(), "rename successfully");
							updateUi();
						}
						alert4.dismiss();
					}
				}
				
			});
			
			cancelButton2.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					alert4.dismiss();
				}
				
			});
			alert4.show();
			break;
		case R.id.file_upload:
			Intent intent = new Intent(getActivity().getBaseContext(), FileDialog.class);
            intent.putExtra(FileDialog.START_PATH, "/sdcard");
            
            //can user select directories or not
            intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
            intent.putExtra(FileDialog.SELECTION_MODE, 1);
            
            //alternatively you can set file filter
            //intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" });
            
            startActivityForResult(intent, FtpServerSettingFragment.REQUEST_SAVE);
			break;
		}
		
		return false;
		
	}
	
	@Override
	public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) {

            if (resultCode == Activity.RESULT_OK) {
            	if(requestCode==FtpServerSettingFragment.REQUEST_SAVE){
            	final AlertDialog alert;
            	String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            	File localFile=new File(filePath);
            	if(filePath==null){
            		Util.makeToast(getActivity(), "choose file failed");
            		return;
            	}
            	AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
    			builder.setTitle(getActivity().getResources().getString(R.string.upload_file)+localFile.getName());
    			LayoutInflater li = LayoutInflater.from(getActivity()); 
                View view = li.inflate(R.layout.progressbar, null); 
                builder.setView(view);
                alert=builder.create();
                ProgressBar progress=(ProgressBar)view.findViewById(R.id.progressBar);
//                这里数值在某些情况下会超，数据量为好几个G的时候，所以改为/1000
                progress.setMax((int) localFile.length());
                progress.setProgress(0);
                alert.show();
                UploadOrDownloadListener listener=new UploadOrDownloadListener(progress,alert,UploadOrDownloadListener.TYPE_UPLOAD);
                Bundle bundle=new Bundle();
                bundle.putString(FtpClientDataTransferThread.LOCAL_FILE, localFile.getAbsolutePath());
                FtpClientDataTransferThread thread=new FtpClientDataTransferThread(true, bundle, listener);
                thread.start();
                Button cancelButton=(Button)view.findViewById(R.id.cancel_button);
                cancelButton.setOnClickListener(new OnClickListener(){

    				@Override
    				public void onClick(View arg0) {
    					// TODO Auto-generated method stub
    					FtpClientCommand.stopDataTransfer();
    					alert.dismiss();
    				}
                	
                });
            	}
            	else if(requestCode==CHOOSE_CERFITICATE){
//            		处理获得的令牌
            		String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
//            		Log.e("filePath", filePath);
            	
            		Bitmap bit=null;
            		bit=BitmapFactory.decodeFile(filePath);
            		if(bit!=null){
            			String result=QRCode.decodeImage(bit);
            			if(result!=null){
            				String decodeJasonString=JceProvider.decrypt(result, Defaults.PASSWORD_CERTIFICATE);
            				JSONParser parser=new JSONParser();
            				try {
            				JSONObject object = (JSONObject) parser.parse(decodeJasonString);
            				String ipAddress=(String) object.get(Defaults.IP_ADDRESS);
            				int    port=Integer.parseInt((String)object.get(Defaults.PORT));
            				String userName=(String) object.get(Defaults.USER_NAME);
            				String password=(String) object.get(Defaults.PASSWORD);
//            				Log.e("filePath", ipAddress+port+userName+password);
            				if(FtpUtil.checkIp(ipAddress)&&FtpUtil.checkPort(port)){
//            					读取正确
            					ipAddressEdit.setText(ipAddress);
            					portEdit.setText(""+port);
            					usernameEdit.setText(userName);
            					passwordEdit.setText(password);
            					FtpUtil.makeToast(getActivity(), "get the cerfiticate successfully");
            					return;
            				}
            			} catch (ParseException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            				
            			}
            			}
            		}
            		FtpUtil.makeToast(getActivity(), "get the cerfiticate failed");
            		
            	}
            	
            }
	}
	
	OnClickListener serverStatusListener=new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Button statusButton=(Button)arg0;
			String status=(String) statusButton.getText();
			Resources rs=getActivity().getResources();
			String connectString=rs.getString(R.string.connect_server);
			String disconnectString=rs.getString(R.string.disconnect_server);
			if(status.equals(connectString)){
				connectServer.setVisibility(View.VISIBLE);

			}
			else{
				FtpClientCommand.disconnect();
				updateUi();
			}
			
		}
		
	};
	   public Handler handler = new Handler() {
	    	public void handleMessage(Message msg) {
	    		switch(msg.what) {
	    		case 0:  // We are being told to do a UI update
	    			// If more than one UI update is queued up, we only need to do one.
//	    			如果当队列中有多个消息0，只更新一次UI
	    			removeMessages(0);
	    			updateUi();
	    			break;
	    		case 1:  // We are being told to display an error message
	    			removeMessages(1);
	    		}
	    	
	    	}
	    };
	
//	更新ui
	@SuppressWarnings("unchecked")
	public void updateUi(){
		Resources rs=getActivity().getResources();
		String connectString=rs.getString(R.string.connect_server);
		String disconnectString=rs.getString(R.string.disconnect_server);
//		断开连接时，并没有更改认证状态
		if(FtpClientCommand.isAuthenticated()&&FtpClientCommand.isConnected()){
			fileListHelper.setVisibility(View.VISIBLE);
			startServerButton.setText(disconnectString);
			currentDirectory=FtpClientCommand.getCurrentDirectory();
			if("/".equals(currentDirectory))
				upButton.setEnabled(false);
			else
				upButton.setEnabled(true);
			 currentPathTextView.setText(currentDirectory);
			FTPFile[] fileList=FtpClientCommand.listFiles();
			ArrayList<FTPFile> lists=new ArrayList<FTPFile>();
			for(FTPFile file:fileList){
				lists.add(file);
			}
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
			Collections.sort(lists,sortHelper.getComparator());
			this.adapter.setFileList(lists);
			adapter.notifyDataSetChanged();
//			Log.e("1", ""+currentSelectedPosition);
//			list.setSelectionFromTop(currentSelected, 4);
//			list.getChildAt(0).getHeight()
//			list.setSelection(currentSelectedPosition);
			
//			list.setSelection(200);
			
			
		}
		else{
			fileListHelper.setVisibility(View.GONE);
			startServerButton.setText(connectString);
			adapter.setFileList(new ArrayList<FTPFile>());
		}
			
	}
	
	class UploadOrDownloadListener implements FTPDataTransferListener{
		public static final int 	TYPE_UPLOAD=1;
		public static final int     TYPE_DOWNLOAD=2;
		private ProgressBar progressBar;
		private AlertDialog dialog;
		private int listenerType=0;
		UploadOrDownloadListener(ProgressBar progress, AlertDialog dialog,int listenerType){
			progressBar=progress;
			this.dialog=dialog;
			this.listenerType=listenerType;
		}
		@Override
		public void aborted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void completed() {
			// TODO Auto-generated method stub
			dialog.dismiss();
			String message;
			if(listenerType==TYPE_UPLOAD)
				Util.makeToast(getActivity(), "上传成功！");
			else
				Util.makeToast(getActivity(), "下载成功！");
			handler.sendEmptyMessage(0);
		}

		@Override
		public void failed() {
			// TODO Auto-generated method stub
			dialog.dismiss();
			if(listenerType==TYPE_UPLOAD)
				Util.makeToast(getActivity(), "上传失败！");
			else
				Util.makeToast(getActivity(), "下载失败！");
		}

		@Override
		public void started() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void transferred(int arg0) {
			// TODO Auto-generated method stub
			progressBar.incrementProgressBy(arg0);
		}
		
	}

	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		if(upButton.getVisibility()==View.VISIBLE&&upButton.isEnabled()){
			if(!FtpClientCommand.changeDirectoryUp()){
				return false;
			}
//			currentSelectedPosition=positionStack.getItem();
//			如果是根目录，则无法向上一个目录
			updateUi();
			setPosition();
//			list.set
			return true;
		}
		return false;
	}
	
//	设置list 位置
	void setPosition(){
		int index =positionStack.getItem() ;
		View v = list.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();	
		Log.e("", ""+top+"index"+index);
		list.smoothScrollToPosition(index+5);
//		list.setSelectionFromTop(index, top);
	}
}

class FileListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;


    private Context mContext;
    private List<FTPFile> fileList=new ArrayList<FTPFile>();
    public FileListAdapter(Context context){
        super();
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }
    public  void setFileList(List<FTPFile> list){
    	fileList=list;
    	this.notifyDataSetChanged();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.file_browser_item, parent, false);
        }
        
        ImageView fileImage=(ImageView)view.findViewById(R.id.file_image);
        TextView  fileName=(TextView)view.findViewById(R.id.file_name);
        TextView  fileModified=(TextView)view.findViewById(R.id.modified_time);
        TextView  fileSize=(TextView)view.findViewById(R.id.file_size);
        ImageView  fileCheckBox=(ImageView)view.findViewById(R.id.file_checkbox);
        
        FTPFile file=fileList.get(position);
         int type=file.getType();
         String name=file.getName();
         Date   date=file.getModifiedDate();
          long    size=file.getSize();
         
         if(type==FTPFile.TYPE_DIRECTORY){
        	 fileImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.folder));
         }
         else{
        	 fileImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.file));
         }
         
         fileName.setText(name);
         String modifiedTime=new SimpleDateFormat("yyyy-MM-dd:hh:mm").format(date);
         fileModified.setText(modifiedTime);
         if(type==FTPFile.TYPE_DIRECTORY){
        	 fileSize.setText("");
        	 return view;
         }
        
         String sizeString=formatFileSize(size);
         fileSize.setText(sizeString);
        return view;
    }
    public String formatFileSize(long fileS) { 
    	DecimalFormat df = new DecimalFormat("#.00"); 
    	String fileSizeString = ""; 
    	if (fileS < 1024) { 
    	fileSizeString = df.format((double) fileS) + "B"; 
    	} else if (fileS < 1048576) { 
    	fileSizeString = df.format((double) fileS / 1024) + "K"; 
    	} else if (fileS < 1073741824) { 
    	fileSizeString = df.format((double) fileS / 1048576) + "M"; 
    	} else { 
    	fileSizeString = df.format((double) fileS / 1073741824) + "G"; 
    	} 
    	return fileSizeString; 
    	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fileList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return fileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	
}

