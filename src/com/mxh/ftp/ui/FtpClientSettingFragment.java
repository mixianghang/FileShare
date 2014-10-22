package com.mxh.ftp.ui;

import java.io.File;

import com.mxh.ftp.ui.FtpContainerActivity.IBackPressedListener;
import com.mxh.ftp.util.Defaults;
import com.mxh.ftp.util.Globals;
import com.mxh.ftp.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class FtpClientSettingFragment extends Fragment implements IBackPressedListener  {
	public static String DEFAULT_DOWNLOAD_FOLDER="default_downlaod_folder";
	public static String DEFAULT_CERTIFICATE_FOLDER="default_certificate_folder";
	private EditText defaultDownloadEdit;
	private EditText defaultCertificateEdit;
	private Button   browse_download_button;
	private Button   browse_certificate_button;
	private Button   saveButton;
	SharedPreferences settings = null;
	
	public static final int DOWNLOAD_BROWSE_REQUEST=1;
	public static final int CERTIFICATE_BROWSE_REQUEST=2;
	@Override
	public void onAttach (Activity activity){
		super.onAttach(activity);
	}
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		View view= inflater.inflate(R.layout.fragment_ftp_client_setting, container, false);
		defaultDownloadEdit=(EditText)view.findViewById(R.id.download_folder_edit);
		defaultCertificateEdit=(EditText)view.findViewById(R.id.certificate_folder_edit);
		browse_download_button=(Button)view.findViewById(R.id.browse_download_button);
		browse_certificate_button=(Button)view.findViewById(R.id.browse_certificate_button);
		saveButton=(Button)view.findViewById(R.id.save_button);
		
//		初始化界面
		initialUi();
		
//		设置监听事件
		saveButton.setOnClickListener(buttonListener);
		this.browse_certificate_button.setOnClickListener(buttonListener);
		this.browse_download_button.setOnClickListener(buttonListener);
		return view;
	}
	
//	监听三个按钮
	OnClickListener buttonListener=new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.browse_certificate_button:
				chooseDir(CERTIFICATE_BROWSE_REQUEST);
				break;
			case R.id.browse_download_button:
//				DOWNLOAD_BROWSE_REQUEST
				chooseDir(DOWNLOAD_BROWSE_REQUEST);
				break;
			case R.id.save_button:
//				保存用户修改
				
				SharedPreferences settings = getActivity().getSharedPreferences(
						Defaults.getSettingsName(), Defaults.getSettingsMode());
				SharedPreferences.Editor editor = settings.edit();
				String downloadFolder=defaultDownloadEdit.getText().toString();
				String certificateFolder=defaultCertificateEdit.getText().toString();
				editor.putString(DEFAULT_CERTIFICATE_FOLDER, certificateFolder);
				editor.putString(DEFAULT_DOWNLOAD_FOLDER, downloadFolder);
				editor.commit();
				Util.makeToast(getActivity(), "save successfully");
				break;
			}
		}
		
	};
	
	@Override
	public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) {

            if (resultCode == Activity.RESULT_OK) {

//                    if (requestCode == REQUEST_SAVE) {
//                            System.out.println("Saving...");
//                    } else if (requestCode == REQUEST_LOAD) {
//                            System.out.println("Loading...");
//                    }
                    
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
        			if(requestCode==DOWNLOAD_BROWSE_REQUEST){
        				defaultDownloadEdit.setText(filePath);
        			}
        			else if(requestCode==CERTIFICATE_BROWSE_REQUEST){
        				defaultCertificateEdit.setText(filePath);
        			}

            } else if (resultCode == Activity.RESULT_CANCELED) {
                 
            }

    }
	 
	public void chooseDir(int requestcode){
		Intent intent = new Intent(getActivity(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, "/sdcard");
        
        //can user select directories or not
        intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
        
        //alternatively you can set file filter
        //intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" });
        
        startActivityForResult(intent, requestcode);
	}
	private void initialUi(){
//		初始化数据
		settings = getActivity().getSharedPreferences(Defaults.getSettingsName(),
				Defaults.getSettingsMode());
		String downloadFolder=settings.getString(DEFAULT_DOWNLOAD_FOLDER, Defaults.downloadDir);
		String certificateFolder=settings.getString(DEFAULT_CERTIFICATE_FOLDER, Defaults.certificateDir);
		defaultDownloadEdit.setText(downloadFolder);
		defaultCertificateEdit.setText(certificateFolder);
		return;
	}
	@Override
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onStart() {
    	super.onStart();
	}
	
	public void onResume() {
    	super.onResume();
	}
	
	public void onPause() {
	    super.onPause();
	}
	
	public void onStop() {
	    super.onStop();
	}
	
	public void onDestroy() {
	    super.onDestroy();
	}
	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		return false;
	}

		
}
