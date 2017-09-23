package com.animal.cutecalculate_shop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.animal.cutecalculate.MainActivity;
import com.animal.cutecalculate.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ShopActivity extends Activity {

	private final String GPKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjgBLE763mob3CIfUQDDAt7UVXyuB8rhtZ4sI0HCKsK9LSxg+U3AOt64jcorBivMLJpj0mnDk7LoSF8MbHtStRpDrIVhoHo8aSqYCdvImMjqQR1O94WKkYQTvv7hh97nBRoYFlAvMQW6ancZClVgetk1O8LR5LNrCzWOo67iHpEQ/L4ke56LdQupQ0CCbckQl+cAW7Cz0CbbVszTohDWEfOdyYVLoqsj49vTRsdTPcKDq+4F0R8vEUFxHVSgDh1GhJCN+t/b4yWfnsO/EdTY32Okr4qoMmSYsyfTCvjdRXexk/3or6m4t7LIZZMYApYij4nwSiBOp6W10l6k3x3IRMwIDAQAB";

	private final String TapjoyId = "48919f12-1da4-49c7-89b8-5679890de45f";
	private final String TapjoyAPIKey = "SJGfEh2kSceJuFZ5iQ3kXwEC1Or4N6x9x6skuqTupABGcmCYFk-bplMXjx88";
	private final String NTGServerId = "";

	private final String NTGGameId = "160100004";
	private final String NTGAPIKey = "69319ca350fd43ba";
	private final String TAG = "acc-tag";
	public static String roleid = "";
	public static int points = 0;
	public static int accUI01 = 1;
	public static int accUI02 = 0;
	public static int setaccUI = 1;

	private static JSONObject mSettings;

	TextView view;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shoplayout);

		init(ShopActivity.this);

		view = (TextView) findViewById(R.id.textshoppoint);
		view.setText(String.valueOf(points));
		
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

	public void shopbackbtn(View v) {
		Intent storeintent = new Intent();
		storeintent.setClass(ShopActivity.this, com.animal.cutecalculate.MainActivity.class);
		ShopActivity.this.startActivity(storeintent);
		ShopActivity.this.finish();
	}

	public void shop30(View v) {
		
	}

	public void shop60(View v) {
		
	}

	public void shop150(View v) {
		
	}

	public void shop300(View v) {
		
	}

}
