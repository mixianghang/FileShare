package com.mxh.ftp.ui;

import java.io.File;

import com.mxh.ftp.ui.R;
import com.mxh.ftp.ui.FtpContainerActivity.IBackPressedListener;
import com.mxh.ftp.util.Defaults;
import com.mxh.ftp.util.Globals;
import com.mxh.ftp.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class FtpServerSettingFragment extends Fragment implements OnClickListener,IBackPressedListener  {
	EditText defaultDirEditText;
	Button 	 dirBrowseButton;
	EditText defaultPortEditText;
	EditText maxConnectionsEditText;
	Button   saveButton;
	
	public static final int REQUEST_SAVE=1;
	final int REQUEST_LOAD=2;
	
	SharedPreferences settings = null;
	public final static String PORTNUM = "portNum";
	public final static String CHROOTDIR = "chrootDir";
	public final static String MAXCONNECTIONS="maxConnections";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		View view= inflater.inflate(R.layout.fragment_ftp_server_setting, container, false);
		defaultDirEditText=(EditText)view.findViewById(R.id.default_dir_edit);
		dirBrowseButton=(Button)view.findViewById(R.id.dir_browse_button);
		defaultPortEditText=(EditText)view.findViewById(R.id.default_port_edit);
		maxConnectionsEditText=(EditText)view.findViewById(R.id.max_connections_edit);
		saveButton=(Button)view.findViewById(R.id.saveButton);
		
//		设置监听器
		dirBrowseButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		
//		初始化数据
		settings = getActivity().getSharedPreferences(Defaults.getSettingsName(),
				Defaults.getSettingsMode());
		int portNumber = settings.getInt(PORTNUM, Defaults.getPortNumber());
		String chroot = settings.getString(CHROOTDIR, Defaults.chrootDir);
		int maxConnections=settings.getInt(MAXCONNECTIONS, Defaults.max_connections);
		
		defaultDirEditText.setText(chroot);
		defaultPortEditText.setText(Integer.toString(portNumber));
		maxConnectionsEditText.setText(Integer.toString(maxConnections));
		return view;
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.saveButton:
//			保存用户修改
			String errString = null;
			int portNum = 0;
			int maxCon=0;
			String portNumberString = defaultPortEditText.getText().toString();
			String chrootDir = defaultDirEditText.getText().toString();
			String maxConnections=maxConnectionsEditText.getText().toString();
//			检查用户输入是否正确
//			check the validation of user's input
			validateBlock: {
				try {
					portNum = Integer.parseInt(portNumberString);
				} catch (Exception e) {
					portNum = 0;
				}
				if(portNum <= 0 || portNum > 65535) {
					errString = getString(R.string.port_validation_error);
					break validateBlock;
				}
				
				if(!(new File(chrootDir).isDirectory())) {
					errString = getString(R.string.chrootDir_validation_error);
					break validateBlock;
				}
				// At least one of the wifi/net listeners must be enabled,
				// otherwise there's nothing for the server to do.
				try{
					maxCon=Integer.parseInt(maxConnections);
				}
				catch(Exception e){
					maxCon=0;
				}
				if(maxCon<=0){
					errString = getString(R.string.set_connections_wrong);
					break validateBlock;
				}
			}
			if(errString != null) {
				AlertDialog dialog =  new AlertDialog.Builder(getActivity()).create();
	        	dialog.setMessage(errString);
	        	dialog.setTitle(getText(R.string.instructions_label));
	        	// This whole mess just adds a do-nothing "OK" button to the dialog
	        	dialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int which) {
	        				return;
	        		} });
	        	dialog.show();
	        	return;
			}
			
//			保存用户的设置
			// Validation was successful, save the settings object
			SharedPreferences settings = getActivity().getSharedPreferences(
					Defaults.getSettingsName(), Defaults.getSettingsMode());
			SharedPreferences.Editor editor = settings.edit();

			editor.putInt(PORTNUM, portNum);
			editor.putString(CHROOTDIR, chrootDir);
			editor.putInt(MAXCONNECTIONS, maxCon);
			File chroot=new File(chrootDir);
			Globals.setChrootDir(chroot);//更新变量
			editor.commit();
			Util.makeToast(getActivity(), "save successfully");
			break;
		case R.id.dir_browse_button:
//			浏览并选择文件夹
			Intent intent = new Intent(getActivity().getBaseContext(), FileDialog.class);
            intent.putExtra(FileDialog.START_PATH, "/sdcard");
            
            //can user select directories or not
            intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
            
            //alternatively you can set file filter
            //intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" });
            
            startActivityForResult(intent, REQUEST_SAVE);
			break;
		}
	}
	@Override
	public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) {

            if (resultCode == Activity.RESULT_OK) {

                    if (requestCode == REQUEST_SAVE) {
                            System.out.println("Saving...");
                    } else if (requestCode == REQUEST_LOAD) {
                            System.out.println("Loading...");
                    }
                    
                    String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                    String errString=null;
                    validateBlock: {
        				
        				
        				if(!(new File(filePath).isDirectory())) {
        					errString = getString(R.string.chrootDir_validation_error);
        					break validateBlock;
        				}
        				// At least one of the wifi/net listeners must be enabled,
        				// otherwise there's nothing for the server to do.
        			
        			}
        			if(errString != null) {
        				AlertDialog dialog =  new AlertDialog.Builder(getActivity()).create();
        	        	dialog.setMessage(errString);
        	        	dialog.setTitle(getText(R.string.instructions_label));
        	        	// This whole mess just adds a do-nothing "OK" button to the dialog
        	        	dialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
        	        		public void onClick(DialogInterface dialog, int which) {
        	        				return;
        	        		} });
        	        	dialog.show();
        	        	return;
        			}
        			defaultDirEditText.setText(filePath);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                 
            }

    }
	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		return false;
	}
}
