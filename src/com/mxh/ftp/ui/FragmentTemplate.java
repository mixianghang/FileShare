package com.mxh.ftp.ui;

import com.mxh.ftp.ui.R;
import com.mxh.ftp.update.UiUpdater;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentTemplate extends Fragment {
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
		
		View view= inflater.inflate(R.layout.fragment_ftp_server_control, container, false);
		return view;
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
	
	public void onDestroyView (){
		
	}
	
	public void onDetach (){
		
	}
}
