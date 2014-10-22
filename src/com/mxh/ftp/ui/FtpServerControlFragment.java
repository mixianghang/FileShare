package com.mxh.ftp.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.mxh.ftp.ui.R;
import com.mxh.ftp.ui.FtpContainerActivity.IBackPressedListener;
import com.mxh.ftp.barcode.QRCode;
import com.mxh.ftp.datastore.DatabaseHelper;
import com.mxh.ftp.encryption.JceProvider;
import com.mxh.ftp.log.MyLog;
import com.mxh.ftp.server.thread.FTPServerService;
import com.mxh.ftp.update.UiUpdater;
import com.mxh.ftp.util.Account;
import com.mxh.ftp.util.Defaults;
import com.mxh.ftp.util.FtpUtil;
import com.mxh.ftp.util.Globals;
import com.mxh.ftp.util.UserAdapter;
import com.mxh.ftp.util.UserTable;
import com.mxh.ftp.util.Util;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ftp服务器的控制窗口
 * @author XiangHang Mi
 *
 */
public class FtpServerControlFragment extends Fragment implements IBackPressedListener  {
	
	
	TextView  wifiStatusTextView;
	ImageView wifiStatusImageView;
	Button    setButton;
	Button    createCertificateButton;
	TextView  serverInfoTextView;
	CheckBox  serverLogCheckBox;
	TextView  serverLogTextView;
	
	private String ipAddress=null;
	private int    port=-1;
	private Account selectedAccount=null;
	
	protected MyLog myLog = new MyLog(this.getClass().getName());//用于记录服务器状态
	
	private int setButtonType=0;//用于设置setButton当前的作用：开启wifi、开启服务器、关闭服务器
	private final int OPEN_WIFI=1;
	private final int OPEN_SERVER=2;
	private final int CLOSE_SERVER=3;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		View view= inflater.inflate(R.layout.fragment_ftp_server_control, container, false);
//		实例化这些控件
		wifiStatusTextView=(TextView)view.findViewById(R.id.wifi_status_textview);
		wifiStatusImageView=(ImageView)view.findViewById(R.id.wifi_status_imageView);
		setButton=(Button)view.findViewById(R.id.set_button);
		serverInfoTextView=(TextView)view.findViewById(R.id.server_info_textview);
		serverLogCheckBox=(CheckBox)view.findViewById(R.id.server_log_checkbox);
		serverLogTextView=(TextView)view.findViewById(R.id.server_log_textview);
		createCertificateButton=(Button)view.findViewById(R.id.create_certificate_button);
//		设置事件监听器
		
		createCertificateButton.setOnClickListener(certificateListener);
		setButton.setOnClickListener(setButttonListener);
		serverLogCheckBox.setOnCheckedChangeListener(serverLogCheckBoxListener);
		
		return view;
	}
	
	
	OnClickListener certificateListener=new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
			builder.setTitle("choose the user for the certificate");
			LayoutInflater l=getActivity().getLayoutInflater();
			View view=l.inflate(R.layout.choose_user, null);
			builder.setView(view);
			final AlertDialog alert=builder.create();
			ListView userList=(ListView)view.findViewById(R.id.user_listview);
			Button newUserButton=(Button)view.findViewById(R.id.add_new_user_button);
			final LinearLayout addUser=(LinearLayout)view.findViewById(R.id.add_user);
			addUser.setVisibility(View.GONE);
			newUserButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					使添加用户的界面显示出来
					addUser.setVisibility(View.VISIBLE);
				}
				
			});
			
//			初始化listview并设置数据
			DatabaseHelper database=new DatabaseHelper(getActivity());
			ArrayList<Account> accounts=database.getUserList();
			final UserAdapter userAdapter=new UserAdapter(getActivity(),accounts);
			userList.setAdapter(userAdapter);
//			设置单击的事件
			
			userList.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					selectedAccount=userAdapter.getAccount(arg2);
					createCertificate();//创建并存储登陆令牌
					alert.dismiss();
				}
				
			});
			
//			设置添加用户模块
			 final EditText userNameEdit=(EditText)view.findViewById(R.id.user_name_edittext);
	            final EditText passwordEdit=(EditText)view.findViewById(R.id.password_edittext);
	            final CheckBox canReadCheckbox=(CheckBox)view.findViewById(R.id.can_read_checkbox);
	   		 	final CheckBox canWriteCheckbox=(CheckBox)view.findViewById(R.id.can_write_checkbox);
	   		 	final CheckBox canModifyCheckbox=(CheckBox)view.findViewById(R.id.can_modify_checkbox);
	   		 	final CheckBox canDeleteCheckbox=(CheckBox)view.findViewById(R.id.can_delete_checkbox);
	   		 	Button   saveButton=(Button)view.findViewById(R.id.save_button);
	   		 	Button   cancelButton=(Button)view.findViewById(R.id.cancel_button);
//	   		 设置两个button的监听器
			 	saveButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
//						保存用户修改，暂时不做检查
						Account a=new Account();
						a.setUsername(userNameEdit.getText().toString());
						a.setPassword(passwordEdit.getText().toString());
						if(canReadCheckbox.isChecked()){
							a.setRead(1);
						}
						else
							a.setRead(0);
						if(canWriteCheckbox.isChecked()){
							a.setWrite(1);
						}
						else
							a.setWrite(0);
						if(canModifyCheckbox.isChecked()){
							a.setModify(1);
						}
						else
							a.setModify(0);
						if(canDeleteCheckbox.isChecked()){
							a.setDelete(1);
						}
						else
							a.setDelete(0);
						ContentValues values=new ContentValues();
						values.put(UserTable.userName, a.getUsername());
						values.put(UserTable.password, a.getPassword());
						values.put(UserTable.canRead, a.getRead());
						values.put(UserTable.canWrite, a.getWrite());
						values.put(UserTable.canModify, a.getModify());
						values.put(UserTable.canDelete, a.getDelete());
						DatabaseHelper database=new DatabaseHelper(getActivity());
						database.add(UserTable.tableName,null, values);
						
						userAdapter.setAccounts(database.getUserList());
						userAdapter.notifyDataSetChanged();
//						alert.dismiss();
						addUser.setVisibility(View.GONE);
						
					}
			 		
			 	});
			 	
			 	cancelButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						addUser.setVisibility(View.GONE);
					}
			 		
			 	});
			 alert.show();
		}
		
	};
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		Context myContext = Globals.getContext();
		if(myContext == null) {
			myContext = getActivity().getApplicationContext();
			if(myContext == null) {
				throw new NullPointerException("Null context!?!?!?");
			}
			Globals.setContext(myContext);
		}
	}
	
//	创建登陆令牌，并保存到默认目录
	private void createCertificate(){
		  Map<String, Object> obj=new LinkedHashMap();
		  obj.put(Defaults.IP_ADDRESS, ipAddress);
		  obj.put(Defaults.PORT, ""+port);
		  obj.put(Defaults.USER_NAME, selectedAccount.getUsername());
		  obj.put(Defaults.PASSWORD, selectedAccount.getPassword());
		  String jsonString=this.createJsonString(obj);
		  String encryptedJasonString=JceProvider.encrypt(jsonString, Defaults.PASSWORD_CERTIFICATE);
		  Bitmap bit=null;
		  SharedPreferences settings = getActivity().getSharedPreferences(Defaults.getSettingsName(),
					Defaults.getSettingsMode());
			String certificateFolder=settings.getString(FtpClientSettingFragment.DEFAULT_CERTIFICATE_FOLDER, Defaults.certificateDir);
			
		  try {
			bit=QRCode.encodeAsBitmap(getActivity(),encryptedJasonString, BarcodeFormat.QR_CODE);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  if(bit!=null){
//			  Log.e("decode", QRCode.decodeImage(bit));
//			  保存
			  if(storeImage(selectedAccount.getUsername(),certificateFolder,bit))
				  Util.makeToast(getActivity(), "已经生成令牌，保存于"+certificateFolder);
			  else
				  Util.makeToast(getActivity(), "生成令牌失败"); 
		  }
		  else{
			  Util.makeToast(getActivity(), "生成令牌失败");
		  }
	}
	
	private boolean storeImage(String fileName, String path,Bitmap bit){
		 boolean sdCardExist = Environment.getExternalStorageState() 
	                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在 
		if(!sdCardExist){
			 Util.makeToast(getActivity(), getActivity().getResources().getString(R.string.sdcard_not_exist));
			return false;
		}
		
		File file = new File(path);
		  if (!file.exists()) {
		   file.mkdirs();
		  }
		  File imagefile = new File(file, fileName + ".png");
		  if(imagefile.exists()){
			  imagefile.delete();
		  }
		  if (!imagefile.exists()) {
		   try {
		    imagefile.createNewFile();
		    FileOutputStream fos = new FileOutputStream(imagefile);
		    bit.compress(Bitmap.CompressFormat.PNG, 100, fos);
		    fos.flush();
		    fos.close();
		   } catch (Exception e) {
		    e.printStackTrace();
		    return false;
		   }
		  } else {
		   return false;
		  }
		return true;
	}
	
//	  生成jason字符串，根据输入的hashmap
	  private String createJsonString(Map<String, Object> obj){
		  StringWriter out = new StringWriter();
		   try {
			JSONValue.writeJSONString(obj, out);
			return out.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		  
	  }
	
	@Override
	public void onStart() {
    	super.onStart();
//    	当有需要更新界面的操作发生时，通知此handler
		UiUpdater.registerClient(handler);
		updateUi();
    }
	
	public void onResume() {
    	super.onResume();
    
//        因为在onPaused解除了登记，这里重新登记
        UiUpdater.registerClient(handler);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(wifiReceiver, 
				filter);
		updateUi();
		// Register to receive wifi status broadcasts
		myLog.l(Log.DEBUG, "Registered for wifi updates");
		
//		登记监听wifi状态的变化
		getActivity().registerReceiver(wifiReceiver, 
				new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }

    /* Whenever we lose focus, we must unregister from UI update messages from
     * the FTPServerService, because we may be deallocated.
     */
    public void onPause() {
    	super.onPause();
		UiUpdater.unregisterClient(handler);
		myLog.l(Log.DEBUG, "Unregistered for wifi updates");
		getActivity().unregisterReceiver(wifiReceiver);
    }
    
    public void onStop() {
    	super.onStop();
    	UiUpdater.unregisterClient(handler);
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	UiUpdater.unregisterClient(handler);
//    	Context context = getActivity().getApplicationContext();
//		Intent intent = new Intent(context,	FTPServerService.class);
//		context.stopService(intent);
    }
    
//	setButton的监听器
	OnClickListener setButttonListener= new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			Util.makeToast(getActivity(), "setButton  clicked!");
			switch(setButtonType){
			case OPEN_WIFI:{
//				开启wifi
				startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
				updateUi();//不能保证用户已经开启wifi了，所以要大面积更新
				break;
			}
			case OPEN_SERVER:{
//				开启服务器
				Log.e("server starting", "server starting");
				Context context = getActivity().getApplicationContext();
	    		Intent intent = new Intent(context,	FTPServerService.class);
	    		Log.e("server starting", "server starting1");
	    		context.startService(intent);
	    		
	    		break;
			}
			case CLOSE_SERVER:{
//				关闭服务器
				Context context = getActivity().getApplicationContext();
	    		Intent intent = new Intent(context,	FTPServerService.class);
	    		context.stopService(intent);
	    		setButtonType=OPEN_SERVER;
	    		setButton.setText(R.string.start_server);
	    		updateUi();
	    		break;
			}
			}
		}
		
	};
	
//	onServerLogCheckBox的监听器
	OnCheckedChangeListener serverLogCheckBoxListener= new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			if(isChecked)
				serverLogTextView.setVisibility(View.VISIBLE);
			else
				serverLogTextView.setVisibility(View.GONE);
//			Util.makeToast(getActivity(), "serverlogCheckBox status changed :"+isChecked);
		}
		
	};
	
    /**
     * 被handler调用，更新Ui
     */
    public void updateUi() {
    	WifiManager wifiMgr = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
    	int wifiState = wifiMgr.getWifiState();
    	ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    	if (mWifi.isConnected()) {
    	    // Do whatever
    		wifiState=WifiManager.WIFI_STATE_ENABLED;
    	}
    	else
    		wifiState=WifiManager.WIFI_STATE_DISABLED;
    	Globals.setWifiStatus(wifiState);
//    	Log.e("server starting", "update ui");
//    	根据wifi及ftp服务器是否运行的状态，设置相关Ui的显示
    	createCertificateButton.setVisibility(View.GONE);
    	switch(wifiState) {
    	case WifiManager.WIFI_STATE_ENABLED:
    		wifiStatusTextView.setText(wifiMgr.getConnectionInfo().getSSID());
    		wifiStatusImageView.setBackgroundResource(R.drawable.wifi_open);
        	if(FTPServerService.isRunning()) {
        		createCertificateButton.setVisibility(View.VISIBLE);
        		serverInfoTextView.setVisibility(View.VISIBLE);
        		setButton.setText(R.string.stop_server);
        		this.setButtonType=this.CLOSE_SERVER;
        		InetAddress address =  FTPServerService.getWifiIp();
            	if(address != null) {
            		ipAddress=address.getHostAddress();
            		port=FTPServerService.getPort();
            		serverInfoTextView.setText("ftp://" + ipAddress + 
        		               ":" + port + "/");
            	} else {
            		myLog.l(Log.VERBOSE, "Null address from getServerAddress()", true);
            		serverInfoTextView.setText(R.string.cant_get_url);
            	}
            	
        	}
        	else{
        		serverInfoTextView.setVisibility(View.GONE);
        		setButton.setText(R.string.start_server);
        		this.setButtonType=this.OPEN_SERVER;
        	}
    		break;
    	case WifiManager.WIFI_STATE_DISABLED:
    		//wifiButton.setText(R.string.enable_wifi);
    		wifiStatusTextView.setText(R.string.no_wifi);
    		wifiStatusImageView.setBackgroundResource(R.drawable.wifi_close);
    		setButton.setText(R.string.open_wifi);
    		this.setButtonType=this.OPEN_WIFI;
    		serverInfoTextView.setVisibility(View.GONE);
    		break;
    	default:
    		break;
    	}

    	// Manage the visibility and text of the "last error" display
    	// and popup a dialog box, if there has been an error
/**
 * 展示上一次的错误，并弹出窗口
 */
    	String errString;
    	if((errString = Globals.getLastError()) != null) {
    		Globals.setLastError(null);  // Clear the error condition after retrieving
//    		lastErrorText.setText(errString);
//    		lastErrorText.setVisibility(View.VISIBLE);
//    		TextView lastErrorLabel = (TextView)findViewById(R.id.last_error_label);
//    		lastErrorLabel.setVisibility(View.VISIBLE);
    		
    		//TextView textView = new TextView(this);
        	//textView.setText(R.string.error_dialog_text);
        	
        	AlertDialog dialog =  new AlertDialog.Builder(getActivity()).create();
        	CharSequence text = getText(R.string.error_dialog_text);
        	String str = text.toString().replace("%%%Replace_Here%%%", errString);
        	//text = text + "\n\n" + getText(R.string.the_error_was) + "\n\n" 
        	//	+ errString;
        	dialog.setMessage(str);
        	dialog.setTitle(getText(R.string.error_dialog_label));
        	dialog.setButton(getText(R.string.ok), ignoreDialogListener);
        	dialog.show();
        	
    	}
    	
    	
    	//展示服务器日志
    	if(serverLogCheckBox.isChecked()) {
    		// If the server log is visible, then retrieve the contents
    		// from the FTPServerService
    		serverLogTextView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    		List<String> lines = FTPServerService.getServerLogContents();
    		//Log.d("", "Got " + lines.size() + " lines from server");
			int size = Defaults.getServerLogScrollBack();
    		serverLogTextView.setMinLines(size);
    		serverLogTextView.setMaxLines(size);
    		String showText = "";
    		for(String line : lines) {
    			showText = showText + line + "\n";  
    		}
    		serverLogTextView.setText(showText);
    	} else {
    		serverLogTextView.setHeight(1);
    	}
    }
    
    DialogInterface.OnClickListener ignoreDialogListener = 
        	new DialogInterface.OnClickListener() 
        {
        	public void onClick(DialogInterface dialog, int which) {
        	}
        };
        
        public Handler handler = new Handler() {
        	public void handleMessage(Message msg) {
        		switch(msg.what) {
        		case 0:  // We are being told to do a UI update
        			// If more than one UI update is queued up, we only need to do one.
//        			如果当队列中有多个消息0，只更新一次UI
        			removeMessages(0);
        			updateUi();
        			break;
        		case 1:  // We are being told to display an error message
        			removeMessages(1);
        		}
        	
        	}
        };
	
//      接受wifi状态的改变
        BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        	public void onReceive(Context ctx, Intent intent) {
            	myLog.l(Log.DEBUG, "Wifi status broadcast received");
        		updateUi();
        	}
        };
@Override
public boolean onBack() {
	// TODO Auto-generated method stub
	return false;
}
}
