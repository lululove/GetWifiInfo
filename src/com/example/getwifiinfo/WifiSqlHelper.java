package com.example.getwifiinfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class WifiSqlHelper extends SQLiteOpenHelper {

	public final static int DB_VERSION = 1;
	public final static String DB_TABLE = "wifi_info";

	public final static String KEY_ID = "_id";
	public final static String KEY_MAC = "bssid";
	public final static String KEY_SSID = "ssid";
	public final static String KEY_PWD = "password";
	public final static String KEY_CAPABILiTIES = "capabilities";

	public WifiSqlHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public WifiSqlHelper(Context context, String name) {
		this(context, name, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String DB_CREATE = "CREATE TABLE " + DB_TABLE + "(" + KEY_ID
				+ " INTEGER PRIMARY KEY," + KEY_SSID + " TEXT," + KEY_MAC
				+ " TEXT," + KEY_PWD + " TEXT," + KEY_CAPABILiTIES + " TEXT)";
		db.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
