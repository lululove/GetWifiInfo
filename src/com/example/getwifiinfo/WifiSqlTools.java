package com.example.getwifiinfo;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WifiSqlTools {
	private WifiSqlHelper dbHelper;
	private SQLiteDatabase db;

	public WifiSqlTools(Context context) {
		dbHelper = new WifiSqlHelper(context, "wifi_conf");
		db = dbHelper.getWritableDatabase();
	}

	public long InsertOrModifyItem(WifiItem wifi) {
		long flag = -1;
		String mac = wifi.getBssid();
		WifiItem item = SearchItem(mac);
		if (item == null) {
			flag = insertItem(wifi);
		} else {
			flag = modifyItem(wifi, mac);
		}
		return flag;
	}

	public long insertItem(WifiItem wifi) {
		ContentValues values = new ContentValues();
		values.put(WifiSqlHelper.KEY_SSID, wifi.getSsid());
		values.put(WifiSqlHelper.KEY_MAC, wifi.getBssid());
		values.put(WifiSqlHelper.KEY_PWD, wifi.getPwd());
		values.put(WifiSqlHelper.KEY_CAPABILiTIES, wifi.getEncryption());
		return db.insert(WifiSqlHelper.DB_TABLE, WifiSqlHelper.KEY_ID, values);
	}

	public long modifyItem(WifiItem wifi, String mac) {
		System.out.println("11111221111");
		ContentValues values = new ContentValues();
		values.put(WifiSqlHelper.KEY_SSID, wifi.getSsid());
		values.put(WifiSqlHelper.KEY_MAC, wifi.getBssid());
		values.put(WifiSqlHelper.KEY_PWD, wifi.getPwd());
		values.put(WifiSqlHelper.KEY_CAPABILiTIES, wifi.getEncryption());
		return db.update(WifiSqlHelper.DB_TABLE, values, WifiSqlHelper.KEY_MAC
				+ "=?" , new String[]{mac});
	}

	public WifiItem SearchItem(String mac) {
		Cursor cursor = db.query(WifiSqlHelper.DB_TABLE, null,
				WifiSqlHelper.KEY_MAC+"=?", new String[] { mac }, null, null, null);
		if (cursor != null) {
			WifiItem item;
			while (cursor.moveToNext()) {
				String ssid = cursor.getString(cursor
						.getColumnIndex(WifiSqlHelper.KEY_SSID));
				String bssid = cursor.getString(cursor
						.getColumnIndex(WifiSqlHelper.KEY_MAC));
				String pwd = cursor.getString(cursor
						.getColumnIndex(WifiSqlHelper.KEY_PWD));
				String entry = cursor.getString(cursor
						.getColumnIndex(WifiSqlHelper.KEY_CAPABILiTIES));
				item = new WifiItem(ssid, bssid, pwd, entry);
				Log.i("sql", ssid);
				Log.i("sql", bssid);
				Log.i("sql", pwd);
				Log.i("sql", entry);
				
				return item;
			}

		}
		return null;
	}

	public List<WifiItem> getInfos(WifiItem wifi) {
		List<WifiItem> list = new ArrayList<WifiItem>();
		Cursor cursor = db.query(WifiSqlHelper.DB_TABLE, null, null, null,
				null, null, null);
		if (cursor != null) {
			WifiItem item;
			while (cursor.moveToNext()) {
				String ssid = cursor.getString(cursor
						.getColumnIndex(WifiSqlHelper.KEY_SSID));
				String bssid = cursor.getString(cursor
						.getColumnIndex(WifiSqlHelper.KEY_MAC));
				String pwd = cursor.getString(cursor
						.getColumnIndex(WifiSqlHelper.KEY_PWD));
				String entry = cursor.getString(cursor
						.getColumnIndex(WifiSqlHelper.KEY_CAPABILiTIES));
				item = new WifiItem(ssid, bssid, pwd, entry);
				list.add(item);
			}
		}
		return list;
	}

	public void closeDB() {
		db.close();
	}
}
