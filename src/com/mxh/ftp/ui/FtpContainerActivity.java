package com.mxh.ftp.ui;



import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.mxh.ftp.server.thread.FTPServerService;
import com.mxh.ftp.ui.*;
import com.mxh.ftp.util.Defaults;
import com.mxh.ftp.util.FtpUtil;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;

import android.os.Bundle;
import android.os.Environment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


/**
 * @author XiangHang Mi
 *此类为各个fragment所在的activity
 */
public class FtpContainerActivity extends FragmentActivity  {
	
	Fragment currentFragment=null;
	
	private List<Fragment> fragmentList=new ArrayList<Fragment>();
	
	public Fragment getPreFragment(){
		int preNum=fragmentList.size();
		Log.e("size", ""+preNum);
		if(preNum==0)
			return null;
		Fragment f=fragmentList.get(preNum-1);
		fragmentList.remove(preNum-1);
		return f;
	}
	
	public void addFragment(Fragment f){
		int preNum=fragmentList.size();
		if(preNum==Defaults.MAX_FRAGMENT)
			fragmentList.remove(0);
		fragmentList.add(f);
	}

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ftp_container);
		initialDefaults();
		currentFragment=new FtpServerControlFragment();
		FragmentManager fragmentManager=this.getSupportFragmentManager();
		FragmentTransaction transaction=fragmentManager.beginTransaction();
		transaction.replace(R.id.container, currentFragment);
		transaction.addToBackStack(null);
		
		transaction.commit();
		
	}
	
	void initialDefaults(){
//		应该从用户设置中读取设置，然后更新defaults
		File dir = Environment.getExternalStorageDirectory();
        String path = dir.getAbsolutePath();
        Defaults.certificateDir=path;
        Defaults.downloadDir=path;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_ftp_container, menu);
		return true;
	}
	
	//做退出主程序的处理工作
	private void quit(){
//		FtpUtil.makeToast(getApplicationContext(), "quit the activity!");
		Intent intent = new Intent(this,FTPServerService.class);
		this.stopService(intent);
		finish();
	}
	@Override 
	public boolean onOptionsItemSelected (MenuItem item){
		if(item.getItemId()==R.id.ftp_quit){
			quit();
			return true;
		}
		Resources rs=this.getResources();
		int[] stringIds=new int[]{
				R.string.ftp_server_control,R.string.ftp_server_user_manage,
				R.string.ftp_server_setting, R.string.ftp_client_control, R.string.ftp_client_setting,
				R.string.ftp_version_info, R.string.ftp_instruction
		};
		FragmentMap[] map=new FragmentMap[]{
				new FragmentMap(rs.getString(stringIds[0]),FtpServerControlFragment.class),
				new FragmentMap(rs.getString(stringIds[1]),FtpServerUserManageFragment.class),
				new FragmentMap(rs.getString(stringIds[2]),FtpServerSettingFragment.class),
				new FragmentMap(rs.getString(stringIds[3]),FtpClientControlFragment.class),
				new FragmentMap(rs.getString(stringIds[4]),FtpClientSettingFragment.class),
				new FragmentMap(rs.getString(stringIds[5]),FtpVersionInfoFragment.class),
				new FragmentMap(rs.getString(stringIds[6]),FtpIntroductionFragment.class)
		};
		String itemTitle=(String) item.getTitle();
		Fragment selectedFragment=null;
		for(int i=0;i<map.length;i++){
//			调入与item名称相互对应的fragment，加入到activity中
			if(map[i].getClassName().equals(itemTitle)){
				Constructor<? extends Fragment> constructor;
				try {
					constructor = map[i].getFragmentClass().getConstructor();
				} catch (NoSuchMethodException e) {
					return false;
				}
				try{
					selectedFragment=(Fragment) constructor.newInstance();
				}
				catch(Exception e){
					return false;
				}
				break;
				
			}
		}
		if(selectedFragment==null)
			return false;
		addFragment(currentFragment);
		currentFragment=selectedFragment;
		FragmentManager fragmentManager=this.getSupportFragmentManager();
		FragmentTransaction transaction=fragmentManager.beginTransaction();
		transaction.replace(R.id.container, selectedFragment);
		transaction.addToBackStack(null);
		transaction.commit();
		return true;
		
	}
	
//  处理后退键，调用各个item进行处理，这里提供了一种activity 接收事件，fragment来处理的方式
  @Override
  public void onBackPressed() {
      IBackPressedListener backPressedListener = (IBackPressedListener) currentFragment;
      if (!backPressedListener.onBack()) {
    	  Fragment f=getPreFragment();
    	  if(f!=null){
    		  currentFragment=f;
    	  		FragmentManager fragmentManager=this.getSupportFragmentManager();
    	  		FragmentTransaction transaction=fragmentManager.beginTransaction();
    	  		transaction.replace(R.id.container, currentFragment);
    	  		transaction.commit();
    	  		return ;
    	  }
    	 AlertDialog.Builder builder=new AlertDialog.Builder(this);
      	LayoutInflater li = LayoutInflater.from(this); 
        View view = li.inflate(R.layout.quit_dialog, null); 
        builder.setView(view);
        final AlertDialog alert=builder.create();
        
//       
        Button okButton=(Button)view.findViewById(R.id.ok_button);
        Button cancelButton=(Button)view.findViewById(R.id.cancel_button);
    	okButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alert.dismiss();
				quit();
			}
    		
    	});
    	cancelButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alert.dismiss();
				return;
			}
    		
    	});
    	alert.show();
         
      }
  }

  public interface IBackPressedListener {
      /**
       * 处理back事件。
       * @return True: 表示已经处理; False: 没有处理，让基类处理。
       */
      boolean onBack();
  }
}
