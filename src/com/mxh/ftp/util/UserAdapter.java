package com.mxh.ftp.util;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mxh.ftp.ui.R;

public class UserAdapter extends BaseAdapter {
	private ArrayList<Account> accounts=null;
	private Context context=null;
	private LayoutInflater inflater=null;
	public UserAdapter(Context context, ArrayList<Account> accounts) {
		super();
		// TODO Auto-generated constructor stub
		this.context=context;
		this.accounts=accounts;
		inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	 @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
		 View view=null;
		 if(convertView!=null){
			 view=convertView;
		 }
		 else{
			 view=inflater.inflate(R.layout.user_item, null);
		 }
		 TextView userNameTextView=(TextView)view.findViewById(R.id.user_name_textview);
		 TextView passwordTextView=(TextView)view.findViewById(R.id.password_textview);
		 CheckBox canReadCheckbox=(CheckBox)view.findViewById(R.id.can_read_checkbox);
		 CheckBox canWriteCheckbox=(CheckBox)view.findViewById(R.id.can_write_checkbox);
		 CheckBox canModifyCheckbox=(CheckBox)view.findViewById(R.id.can_modify_checkbox);
		 CheckBox canDeleteCheckbox=(CheckBox)view.findViewById(R.id.can_delete_checkbox);
		 Account a=accounts.get(position);
		 userNameTextView.setText(a.getUsername());
		 passwordTextView.setText(a.getPassword());
		 if(a.getRead()==1)
		 	canReadCheckbox.setChecked(true);
		 else
			 canReadCheckbox.setChecked(false);
		 if(a.getWrite()==1)
			 canWriteCheckbox.setChecked(true);
		 else
			 canWriteCheckbox.setChecked(false);
		 if(a.getModify()==1)
			 canModifyCheckbox.setChecked(true);
		 else
			 canModifyCheckbox.setChecked(false);
		 if(a.getDelete()==1)
			 canDeleteCheckbox.setChecked(true);
		 else
			 canDeleteCheckbox.setChecked(false);
//		 registerForContextMenu(view);
		 return view;
	 }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return accounts.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
	
	public Account getAccount(int position){
		return accounts.get(position);
	}
//	后台数据更新后，更新adapter
	public void setAccounts(ArrayList<Account> accounts){
		this.accounts=accounts;
	}

}
