package com.animal.cutecalculate_store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.animal.cutecalculate.MainActivity;
import com.animal.cutecalculate.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class StoreActivity extends Activity {

	private static JSONObject mSettings;
	public static String roleid = "";
	public static int points = 0;
	public static int accUI01 = 1;
	public static int accUI02 = 0;
	public static int setaccUI = 1;

	TextView view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.storelayout);
		// 讀取是否有遊戲帳號
		init(StoreActivity.this);
		
		// 測試
		//points = 5000;
		
		view = (TextView) findViewById(R.id.textpoint);
		view.setText(String.valueOf(points));
	}

	public void backbtn(View v) {
		Intent storeintent = new Intent();
		storeintent.setClass(StoreActivity.this, com.animal.cutecalculate.MainActivity.class);
		StoreActivity.this.startActivity(storeintent);
		StoreActivity.this.finish();
	}

	public void imgbtn01(View v) {
		setaccUI = 1;
		accUI01 = 1;
		writeSettings(MainActivity.getInstance().getApplicationContext());
		Intent storeintent = new Intent();
		storeintent.setClass(StoreActivity.this, com.animal.cutecalculate.MainActivity.class);
		StoreActivity.this.startActivity(storeintent);
		StoreActivity.this.finish();
	}

	public void imgbtn02(View v) {
		
		if (points < 1000) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(StoreActivity.this);
			dialog.setTitle("點數不足");
			dialog.setMessage("狗狗需點數1000認養，請儲值點數");
			dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent storeintent = new Intent();
					storeintent.setClass(StoreActivity.this, com.animal.cutecalculate_shop.ShopActivity.class);
					StoreActivity.this.startActivity(storeintent);
					StoreActivity.this.finish();
				}
			});
			dialog.show();
		} else {
			if(accUI02 == 1){
				setaccUI = 2;
				writeSettings(MainActivity.getInstance().getApplicationContext());
				Intent storeintent = new Intent();
				storeintent.setClass(StoreActivity.this, com.animal.cutecalculate.MainActivity.class);
				StoreActivity.this.startActivity(storeintent);
				StoreActivity.this.finish();
			}
			else{
				setaccUI = 2;
				accUI02 = 1;
				points = points - 1000;
				writeSettings(MainActivity.getInstance().getApplicationContext());
				view.setText(String.valueOf(points));
				Intent storeintent = new Intent();
				storeintent.setClass(StoreActivity.this, com.animal.cutecalculate.MainActivity.class);
				StoreActivity.this.startActivity(storeintent);
				StoreActivity.this.finish();
			}
		}

	}


	private static void readSettings(Context context) {
		File saveFile = new File(context.getFilesDir().getPath() + "/animalcutecalculate.dat");
		if (!saveFile.exists()) {
			try {
				mSettings = new JSONObject();
				mSettings.put("points", 0);
				mSettings.put("roleid", "");
				mSettings.put("accUI01", 0);
				mSettings.put("accUI02", 0);
				mSettings.put("setaccUI", 1);
				saveFile.createNewFile();
				FileOutputStream stream = new FileOutputStream(saveFile);
				stream.write(mSettings.toString().getBytes("UTF-8"));
				stream.flush();
				stream.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				FileInputStream stream = new FileInputStream(saveFile);
				byte[] buffer = new byte[stream.available()];
				stream.read(buffer);
				stream.close();

				mSettings = new JSONObject(new String(buffer, "UTF-8"));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void writeSettings(Context context) {
		try {
			mSettings.put("points", points);
			mSettings.put("roleid", roleid);
			mSettings.put("accUI01", accUI01);
			mSettings.put("accUI02", accUI02);
			mSettings.put("setaccUI", setaccUI);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		File saveFile = new File(context.getFilesDir().getPath() + "/animalcutecalculate.dat");
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(saveFile);
			stream.write(mSettings.toString().getBytes("UTF-8"));
			stream.flush();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void init(Activity act) {
		readSettings(act.getApplicationContext());
		try {
			points = mSettings.getInt("points");
			roleid = mSettings.getString("roleid");
			accUI01 = mSettings.getInt("accUI01");
			accUI02 = mSettings.getInt("accUI02");
			setaccUI = mSettings.getInt("setaccUI");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
