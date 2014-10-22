package com.mxh.ftp.ui;

import com.mxh.ftp.ui.R;
import com.mxh.ftp.ui.FtpContainerActivity.IBackPressedListener;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FtpVersionInfoFragment extends Fragment implements IBackPressedListener {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		return inflater.inflate(R.layout.fragment_ftp_version_info, container, false);
	}

	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		return false;
	}
}
