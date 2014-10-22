package com.mxh.ftp.datastore;

import java.util.ArrayList;

import com.mxh.ftp.util.Account;
import com.mxh.ftp.util.UserTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private final static String USER_TABLE="ftpServerUserTable";
	private final static int VERSION=2;
	public DatabaseHelper(Context context) {
		super(context, USER_TABLE, null, VERSION);
		// TODO Auto-generated constructor stub
	}

	SQLiteDatabase db;
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		this.db=db;
		db.execSQL("create table user( "+UserTable.userName+" varchar(127) primary key,  "+UserTable.password+" varchar(127)" +
				",  "+UserTable.canRead+" int,  "+UserTable.canWrite+" int,  "+UserTable.canModify+" int,  "+UserTable.canDelete+" int);");
		db.execSQL("insert into  user values('root', null, 1,1,1,1)");//插入root用户，权限都有
//		db.close();
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
//	删除某些行
	public void delete(String table, String whereClause, String[] whereArgs){
		SQLiteDatabase db=this.getWritableDatabase();
		db.delete(table, whereClause, whereArgs);
		db.close();
	}
	
//	编辑某些行
	
	public void edit(String table,ContentValues values, String whereClause, String[] whereArgs){
		SQLiteDatabase db=this.getWritableDatabase();
		db.update(table, values, whereClause, whereArgs);
		db.close();
	}

//	添加
	public void add(String table, String nullColumnHack, ContentValues values){
		SQLiteDatabase db=this.getWritableDatabase();
		db.insert(table, nullColumnHack, values);
		db.close();
	}
	
//	获得用户列表
	public ArrayList<Account> getUserList(){
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cursor=db.query(UserTable.tableName, null, null, null, null, null, null);
		ArrayList<Account> account=new ArrayList<Account>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Account a=new Account();
			a.setUsername(cursor.getString(cursor.getColumnIndex(UserTable.userName)));
			a.setPassword(cursor.getString(cursor.getColumnIndex(UserTable.password)));
			a.setRead(cursor.getInt(cursor.getColumnIndex(UserTable.canRead)));
			a.setWrite(cursor.getInt(cursor.getColumnIndex(UserTable.canWrite)));
			a.setModify(cursor.getInt(cursor.getColumnIndex(UserTable.canModify)));
			a.setDelete(cursor.getInt(cursor.getColumnIndex(UserTable.canDelete)));
			account.add(a);
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return account;
	}

}
