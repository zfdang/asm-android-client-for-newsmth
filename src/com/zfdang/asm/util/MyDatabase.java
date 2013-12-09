package com.zfdang.asm.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zfdang.asm.util.SQLiteAssetHelper;

public class MyDatabase extends SQLiteAssetHelper {

	private static final String DATABASE_NAME = "qqwry";
	private static final int DATABASE_VERSION = 1;

	public MyDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		// you can use an alternate constructor to specify a database location 
		// (such as a folder on the sd card)
		// you must ensure that this folder is available and you have permission
		// to write to it
		//super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);

	}

	public String getLocation(String ip_int) {

		SQLiteDatabase db = getReadableDatabase();
		
		Cursor result=db.rawQuery("select _id,country from qqwry where _id< " + ip_int + " order by _id desc limit 1", null);  
		result.moveToFirst();  
		String country = result.getString(1);  
		result.close();
		return country;    
	}

}