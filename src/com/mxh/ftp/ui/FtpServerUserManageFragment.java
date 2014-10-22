package com.mxh.ftp.ui;

import java.util.ArrayList;

import com.mxh.ftp.ui.R;
import com.mxh.ftp.ui.FtpContainerActivity.IBackPressedListener;
import com.mxh.ftp.datastore.DatabaseHelper;
import com.mxh.ftp.util.Account;
import com.mxh.ftp.util.UserAdapter;
import com.mxh.ftp.util.UserTable;
import com.mxh.ftp.util.Util;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

public class FtpServerUserManageFragment extends Fragment implements IBackPressedListener  {
	ListView userList;
	Button   newUserButton;
	UserAdapter userAdapter;
	private int currentSelectedItem=-1;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		View view=inflater.inflate(R.layout.fragment_ftp_server_user_manage, container, false);
		userList=(ListView)view.findViewById(R.id.user_listview);
		newUserButton=(Button)view.findViewById(R.id.add_new_user_button);
		newUserButton.setOnClickListener(newUserButtonListener);
		return view;
	}
	@Override
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
//		为userList绑定adapter
		DatabaseHelper database=new DatabaseHelper(getActivity());
		ArrayList<Account> accounts=database.getUserList();
		userAdapter=new UserAdapter(getActivity(),accounts);
		userList.setAdapter(userAdapter);
		registerForContextMenu(userList);//为每个item登记上下午菜单
		userList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				currentSelectedItem=arg2;
				return false;
			}
			
		});
	}
	
//	创建上下文菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
//	    Util.makeToast(getActivity(), "create menu");
	    MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.user_item_context_menu, menu);
	}
	
//	处理上下文菜单的点击事件
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.edit_user:
	        	if(currentSelectedItem==-1){
	        		Util.makeToast(getActivity(), "please selecte one item");
	        		return true;
	        	}
	        	final Account a=userAdapter.getAccount(currentSelectedItem);
//	        	Util.makeToast(getActivity(), "edit new user");
	        	LayoutInflater li = LayoutInflater.from(getActivity()); 
	            View view = li.inflate(R.layout.user_dialog, null); 
	           	AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
	        	builder.setView(view);
	        	final AlertDialog alert=builder.create();
//	            初始化编辑窗口的各个值
	            final EditText userNameEdit=(EditText)view.findViewById(R.id.user_name_edittext);
	            final EditText passwordEdit=(EditText)view.findViewById(R.id.password_edittext);
	            final CheckBox canReadCheckbox=(CheckBox)view.findViewById(R.id.can_read_checkbox);
	   		 	final CheckBox canWriteCheckbox=(CheckBox)view.findViewById(R.id.can_write_checkbox);
	   		 	final CheckBox canModifyCheckbox=(CheckBox)view.findViewById(R.id.can_modify_checkbox);
	   		 	final CheckBox canDeleteCheckbox=(CheckBox)view.findViewById(R.id.can_delete_checkbox);
	   		 	Button   saveButton=(Button)view.findViewById(R.id.save_button);
	   		 	Button   cancelButton=(Button)view.findViewById(R.id.cancel_button);
	   		 	final String preUserName=a.getUsername();
	   		 	userNameEdit.setText(a.getUsername());
	   		 	passwordEdit.setText(a.getPassword());
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
	   		 	
//	   		 	设置两个button的监听器
	   		 	saveButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
//						保存用户修改，暂时不做检查
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
						database.edit(UserTable.tableName, values, UserTable.userName+"=?", new String[]{
							preUserName
						});
						
						userAdapter.setAccounts(database.getUserList());
						userAdapter.notifyDataSetChanged();
						alert.dismiss();
						
					}
	   		 		
	   		 	});
	   		 	
	   		 	cancelButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						alert.dismiss();
					}
	   		 		
	   		 	});
	   		 	
	   		 	alert.show();
	            return true;
	        case R.id.delete_user:
//	            Util.makeToast(getActivity(), "delete new user");
	        	if(currentSelectedItem==-1){
	        		Util.makeToast(getActivity(), "please selecte one item");
	        		return true;
	        	}
	        	final Account a1=userAdapter.getAccount(currentSelectedItem);
	        	DatabaseHelper database=new DatabaseHelper(getActivity());
	        	database.delete(UserTable.tableName, UserTable.userName+"=?",new String[]{
	        			
	        	a1.getUsername()});
	        	
	        	userAdapter.setAccounts(database.getUserList());
				userAdapter.notifyDataSetChanged();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	OnClickListener newUserButtonListener=new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
//			新建用户
//			Util.makeToast(getActivity(), "creat new user");
			LayoutInflater li = LayoutInflater.from(getActivity()); 
            View view = li.inflate(R.layout.user_dialog, null); 
           	AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        	builder.setView(view);
        	final AlertDialog alert=builder.create();
//            初始化编辑窗口的各个值
            final EditText userNameEdit=(EditText)view.findViewById(R.id.user_name_edittext);
            final EditText passwordEdit=(EditText)view.findViewById(R.id.password_edittext);
            final CheckBox canReadCheckbox=(CheckBox)view.findViewById(R.id.can_read_checkbox);
   		 	final CheckBox canWriteCheckbox=(CheckBox)view.findViewById(R.id.can_write_checkbox);
   		 	final CheckBox canModifyCheckbox=(CheckBox)view.findViewById(R.id.can_modify_checkbox);
   		 	final CheckBox canDeleteCheckbox=(CheckBox)view.findViewById(R.id.can_delete_checkbox);
   		 	Button   saveButton=(Button)view.findViewById(R.id.save_button);
   		 	Button   cancelButton=(Button)view.findViewById(R.id.cancel_button);
//   		 设置两个button的监听器
		 	saveButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
//					保存用户修改，暂时不做检查
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
					alert.dismiss();
					
				}
		 		
		 	});
		 	
		 	cancelButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					alert.dismiss();
				}
		 		
		 	});
		 alert.show();
		 	
		}
		
	};
	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		return false;
	}
}
