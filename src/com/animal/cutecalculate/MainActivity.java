package com.animal.cutecalculate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import com.animal.cutecalculate.R;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJGetCurrencyBalanceListener;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJSpendCurrencyListener;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements TJGetCurrencyBalanceListener, TJPlacementListener {

	private static MainActivity mInstance;

	public static MainActivity getInstance() {
		return mInstance;
	}

	private TJPlacement directPlayPlacement;
	private TJPlacement offerwallPlacement;

	private final String TAG = "acc-tag";
	public static String roleid = "";
	public static int points = 0;
	public static int accUI01 = 1;
	public static int accUI02 = 0;
	public static int setaccUI = 1;

	private static JSONObject mSettings;
	// private Button freepoint;

	LinearLayout layoutAd;
	TextView view;
	ImageView animalimg;

	RelativeLayout rlayout;

	TextView recordtext; // 顯示
	TextView resulttext; // 結果
	String recordstring = ""; // 顯示暫存字串
	String resultstring = ""; // 結果暫存字串
	int first = 0; // 初始狀態
	int sub = 0; // 減法次數
	int add = 0; // 加法狀態
	int division = 0; // 除法狀態
	int multiply = 0; // 乘法狀態
	int percentage = 0; // 百分比狀態
	int decimalpoint = 0; // 小數點狀態
	int basicoperation = 0; // 運算狀態(+*/%.)
	int bracket1 = 0; // 上刮號次數
	int bracket2 = 0; // 下刮號次數
	int bracket = 0; // 刮號狀態
	ArrayList<String> operationList; // 字串處理list
	// 分割數字比對用
	String[] mathstring = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "." };
	// 分割運算字元比對用
	String[] operationstring = new String[] { "+", "-", "×", "÷", "%", "(", ")" };

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

	private boolean postRequired() {
		return Looper.myLooper() != this.getMainLooper();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mInstance = this;

		// 讀取是否有遊戲帳號
		init(MainActivity.this);

		// 測試
		// points = 5000;

		animalimg = (ImageView) this.findViewById(R.id.imageView2);

		rlayout = (RelativeLayout) this.findViewById(R.id.rlayout);

		view = (TextView) findViewById(R.id.textcoin);
		view.setText(String.valueOf(points));

		recordtext = (TextView) this.findViewById(R.id.textViewrecord);
		recordtext.setText("");
		resulttext = (TextView) this.findViewById(R.id.textViewresult);
		resulttext.setText("");
		recordstring = "";
		resultstring = "";
		clearstatus();

		switch (setaccUI) {
		case 1:
			rlayout.setBackgroundResource(R.drawable.compute1);
			animalimg.setImageResource(R.drawable.sheep);
			break;

		case 2:
			rlayout.setBackgroundResource(R.drawable.compute2);
			animalimg.setImageResource(R.drawable.dog);
			break;

		}

		Log.d(TAG, "Gameid::");

		connectToTapjoy();
		Tapjoycheckpoint();

		AdBuddiz.setPublisherKey("d44a10ef-e14d-4c46-847e-4ac6d7e355c1");
		AdBuddiz.cacheAds(this.mInstance);
		if (AdBuddiz.isReadyToShowAd(this.mInstance)) { // this = current Activity
			AdBuddiz.showAd(this.mInstance); // showAd will always display an ad
		}
	}

	private void Tapjoycheckpoint() {
		Tapjoy.getCurrencyBalance(new TJGetCurrencyBalanceListener() {
			@Override
			public void onGetCurrencyBalanceResponse(String currencyName, int balance) {
				points = balance;
				writeSettings(MainActivity.getInstance().getApplicationContext());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (view != null) {
							view.setText(String.valueOf(points));
						}
					}
				});
				Log.i(TAG, "getCurrencyBalance returned " + currencyName + ":" + balance);
			}

			@Override
			public void onGetCurrencyBalanceResponseFailure(String error) {
				Log.i(TAG, "getCurrencyBalance error: " + error);
			}
		});
	}

	/**
	 * Wrapper method to call {@link Tapjoy.#spendCurrency(int,
	 * TJSpendCurrencyListener}
	 */
	private void callSpendCurrency(int amount) {
		// Spend virtual currency
		Tapjoy.spendCurrency(amount, new TJSpendCurrencyListener() {
			@Override
			public void onSpendCurrencyResponse(String currencyName, int balance) {
				Log.i(TAG, "getCurrencyBalance returned " + currencyName + ":" + balance);
			}

			@Override
			public void onSpendCurrencyResponseFailure(String error) {
				Log.i(TAG, "error : " + error);
			}
		});
	}

	/**
	 * Attempts to connect to Tapjoy
	 */
	private void connectToTapjoy() {
		// OPTIONAL: For custom startup flags.
		Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
		connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

		// If you are not using Tapjoy Managed currency, you would set your own
		// user ID here.
		// connectFlags.put(TapjoyConnectFlag.USER_ID, "A_UNIQUE_USER_ID");

		// Connect with the Tapjoy server. Call this when the application first
		// starts.
		// REPLACE THE SDK KEY WITH YOUR TAPJOY SDK Key.
		String tapjoySDKKey = "jTMWhYH9TKeSqVkCn8djLgECj1TWUOfiRISn4bH3NyxylWEKDQeiVbTew4xF";

		// NOTE: This is the only step required if you're an advertiser.
		Tapjoy.connect(getApplicationContext(), tapjoySDKKey, connectFlags, new TJConnectListener() {
			@Override
			public void onConnectSuccess() {
				MainActivity.this.onConnectSuccess();
			}

			@Override
			public void onConnectFailure() {
				MainActivity.this.onConnectFail();
			}
		});
	}

	/**
	 * Handles a successful connect to Tapjoy. Pre-loads direct play placement
	 * and sets up Tapjoy listeners
	 */
	public void onConnectSuccess() {

		Log.e(TAG, "Tapjoy connect call Success");
	}

	/**
	 * Handles a failed connect to Tapjoy
	 */
	public void onConnectFail() {
		Log.e(TAG, "Tapjoy connect call failed");
	}

	/**
	 * Notify Tapjoy the start of this activity for session tracking
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Tapjoy.onActivityStart(this);
	}

	/**
	 * Notify Tapjoy the end of this activity for session tracking
	 */
	@Override
	protected void onStop() {
		super.onStop();
		Tapjoy.onActivityStop(this);
		Tapjoycheckpoint();
	}

	private void callShowOffers() {
		// Construct TJPlacement to show Offers web view from where users can
		// download the latest offers for virtual currency.
		offerwallPlacement = new TJPlacement(this, "InsufficientCurrency", new TJPlacementListener() {
			@Override
			public void onRequestSuccess(TJPlacement placement) {

			}

			@Override
			public void onRequestFailure(TJPlacement placement, TJError error) {

			}

			@Override
			public void onContentReady(TJPlacement placement) {
				TapjoyLog.i(TAG, "onContentReady for placement " + placement.getName());

				placement.showContent();
			}

			@Override
			public void onContentShow(TJPlacement placement) {
				TapjoyLog.i(TAG, "onContentShow for placement " + placement.getName());
			}

			@Override
			public void onContentDismiss(TJPlacement placement) {
				TapjoyLog.i(TAG, "onContentDismiss for placement " + placement.getName());
			}

			@Override
			public void onPurchaseRequest(TJPlacement placement, TJActionRequest request, String productId) {
				TapjoyLog.i(TAG, "onPurchaseRequest " + placement.getName() + "productId : " + productId);
			}

			@Override
			public void onRewardRequest(TJPlacement placement, TJActionRequest request, String itemId, int quantity) {
				TapjoyLog.i(TAG,
						"onRewardRequest " + placement.getName() + "itemId : " + itemId + " quantity : " + quantity);
			}
		});
		offerwallPlacement.requestContent();
	}

	// 清除運算按鈕狀態
	public void clearstatus() {
		first = 0; // 初始狀態
		sub = 0; // 減法次數
		add = 0; // 加法狀態
		division = 0; // 除法狀態
		multiply = 0; // 乘法狀態
		percentage = 0; // 百分比狀態
		decimalpoint = 0; // 小數點狀態
		basicoperation = 0; // 運算狀態(+*/%.)
		bracket1 = 0; // 上刮號次數
		bracket2 = 0; // 下刮號次數
		bracket = 0;
	}

	// 已按數字按鈕狀態
	public void digitalstatus() {
		first = 1; // 初始狀態
		sub = 0; // 減法次數
		add = 0; // 加法狀態
		division = 0; // 除法狀態
		multiply = 0; // 乘法狀態
		percentage = 0; // 百分比狀態
		basicoperation = 0; // 運算狀態(+*/%.)
		bracket = 0;
	}

	// 字串處理
	public void stringprocess() {
		recordstring = recordtext.getText().toString();
		char[] recordchars = recordstring.toCharArray();
		operationList = new ArrayList<String>(recordchars.length);

		String cutstring = "";
		for (int a = 0; a < recordchars.length; a++) {
			for (int b = 0; b < mathstring.length; b++) {
				if (String.valueOf(recordchars[a]).equals(mathstring[b])) {
					cutstring += mathstring[b];
				}
			}
			for (int c = 0; c < operationstring.length; c++) {
				if (String.valueOf(recordchars[a]).equals(operationstring[c])) {
					if (cutstring != "") {
						operationList.add(cutstring);
						cutstring = "";
					}
					operationList.add(operationstring[c]);
				}
			}
		}
		if (cutstring != "") {
			operationList.add(cutstring);
			cutstring = "";
		}
		int listchang = operationList.size() - 2;
		for (int a = 0; a < listchang; a++) { // 合併負值
			if (a < operationList.size()) {
				for (int c = 0; c < operationstring.length; c++) {
					for (int d = 0; d < mathstring.length; d++) {
						if (operationList.get(a).equals(operationstring[c]) & operationList.get(a + 1).equals("-")
								& operationList.get(a + 2).equals(mathstring[d])) {
							String merge = "-" + operationList.get(a + 2);
							operationList.set(a + 1, merge);
							operationList.remove(a + 2);
						}
					}
				}
			}
		}
	}

	// 字串運算
	public void stringoperation() {
		for (int all = 0; all < 3; all++) {
			List<String> bracketparallellist;
			int bracket1 = operationList.lastIndexOf("(");
			int bracket2 = 0;
			if (bracket1 != -1) {
				bracketparallellist = new ArrayList<String>(operationList.subList(bracket1, operationList.size()));
				bracket2 = bracketparallellist.indexOf(")");
				if (bracket2 != -1) {
					bracket2 = bracket2 + bracket1;
				}
			}

			List<String> bracketlist; // 括號內字串
			List<String> percentagelist; // 百分比前字串
			String solution = "";
			BigDecimal mathA;
			BigDecimal mathB;
			BigDecimal mathC;
			BigDecimal mathD;
			String mathpercentage = "";
			int add = 0; // 符號+
			int sub = 0; // 符號-
			int multiply = 0; // 符號*
			int division = 0; // 符號/
			int percentage = 0; // 符號%
			if (bracket1 != -1) {
				if (bracket2 != -1 & bracket2 > bracket1) {
					bracket1 = bracket1 + 1;
					bracketlist = new ArrayList<String>(operationList.subList(bracket1, bracket2));
				} else {
					bracket1 = bracket1 + 1;
					bracketlist = new ArrayList<String>(operationList.subList(bracket1, operationList.size()));
				}
				for (int a = 0; a < 2; a++) {
					add = bracketlist.indexOf("+");
					sub = bracketlist.indexOf("-");
					multiply = bracketlist.indexOf("×");
					division = bracketlist.indexOf("÷");
					percentage = bracketlist.indexOf("%");
					if (percentage != -1) {
						if (percentage < 2) {
							mathA = new BigDecimal(bracketlist.get(percentage - 1));
							bracketlist.set(percentage - 1, percentagemethod1(mathA));
							bracketlist.remove(percentage);
						} else {
							if (bracketlist.get(percentage - 2) == "+") {
								if (percentage == 3) {
									mathA = new BigDecimal(bracketlist.get(0));
									mathB = new BigDecimal(operationList.get(0));
									mathC = new BigDecimal(bracketlist.get(percentage - 1));
									mathD = new BigDecimal(percentagemethod2(mathB, mathC));
									mathpercentage = additionmethod(mathA, mathD);
									bracketlist.set(0, mathpercentage);
									for (int z = percentage; z > 0; z--) {
										bracketlist.remove(z);
									}
								} else {
									percentagelist = new ArrayList<String>(bracketlist.subList(0, percentage - 2));
									mathpercentage = percentagestringmethod(percentagelist);

									mathA = new BigDecimal(mathpercentage);
									mathB = new BigDecimal(bracketlist.get(percentage - 1));
									mathC = new BigDecimal(percentagemethod2(mathA, mathB));
									mathpercentage = additionmethod(mathA, mathC);

									bracketlist.set(0, mathpercentage);
									for (int z = percentage; z > 0; z--) {
										bracketlist.remove(z);
									}
								}
							} else {
								if (bracketlist.get(percentage - 2) == "-") {
									if (percentage == 3) {
										mathA = new BigDecimal(bracketlist.get(0));
										mathB = new BigDecimal(operationList.get(0));
										mathC = new BigDecimal(bracketlist.get(percentage - 1));
										mathD = new BigDecimal(percentagemethod2(mathB, mathC));

										mathpercentage = subtractionmethod(mathA, mathD);
										bracketlist.set(0, mathpercentage);
										for (int z = percentage; z > 0; z--) {
											bracketlist.remove(z);
										}
									} else {
										percentagelist = new ArrayList<String>(bracketlist.subList(0, percentage - 2));
										mathpercentage = percentagestringmethod(percentagelist);

										mathA = new BigDecimal(mathpercentage);
										mathB = new BigDecimal(bracketlist.get(percentage - 1));
										mathC = new BigDecimal(percentagemethod2(mathA, mathB));

										mathpercentage = subtractionmethod(mathA, mathC);

										bracketlist.set(0, mathpercentage);
										for (int z = percentage; z > 0; z--) {
											bracketlist.remove(z);
										}
									}

								} else {
									if (bracketlist.get(percentage - 2) == "×") {
										mathA = new BigDecimal(bracketlist.get(percentage - 1));
										bracketlist.set(percentage - 1, percentagemethod1(mathA));
										bracketlist.remove(percentage);
									} else {
										if (bracketlist.get(percentage - 2) == "%") {
											mathA = new BigDecimal(bracketlist.get(percentage - 1));
											bracketlist.set(percentage - 1, percentagemethod1(mathA));
											bracketlist.remove(percentage);
										}
									}

								}

							}
						}

					} else {
						if (multiply != -1 | division != -1) {
							// 不存在的符號給最大值
							if (multiply == -1) {
								multiply = bracketlist.size();
							}
							if (division == -1) {
								division = bracketlist.size();
							}
							// 順位
							// 乘
							if (multiply < division) {
								mathA = new BigDecimal(bracketlist.get(multiply - 1));
								mathB = new BigDecimal(bracketlist.get(multiply + 1));
								solution = multiplymethod(mathA, mathB);
								bracketlist.set(multiply - 1, solution);
								bracketlist.remove(multiply + 1);
								bracketlist.remove(multiply);
								a = 0;
								all = 0;
							} else {
								// 除
								mathA = new BigDecimal(bracketlist.get(division - 1));
								mathB = new BigDecimal(bracketlist.get(division + 1));
								solution = divisionmethod(mathA, mathB);
								bracketlist.set(division - 1, solution);
								bracketlist.remove(division + 1);
								bracketlist.remove(division);
								a = 0;
								all = 0;
							}

						} else {
							if (add != -1 | sub != -1) {
								// 不存在的符號給最大值
								if (add == -1) {
									add = bracketlist.size();
								}
								if (sub == -1) {
									sub = bracketlist.size();
								}
								if (add < sub) {
									// 加
									mathA = new BigDecimal(bracketlist.get(add - 1));
									mathB = new BigDecimal(bracketlist.get(add + 1));
									solution = additionmethod(mathA, mathB);
									bracketlist.set(add - 1, solution);
									bracketlist.remove(add + 1);
									bracketlist.remove(add);
									a = 0;
									all = 0;
								} else {
									// 減
									mathA = new BigDecimal(bracketlist.get(sub - 1));
									mathB = new BigDecimal(bracketlist.get(sub + 1));
									solution = subtractionmethod(mathA, mathB);
									bracketlist.set(sub - 1, solution);
									bracketlist.remove(sub + 1);
									bracketlist.remove(sub);
									a = 0;
									all = 0;
								}
							} else {
								if (bracket2 == -1 | bracket2 < bracket1) {
									bracket2 = operationList.size() - 1;
								}

								if (bracketlist.size() == 1) {
									solution = bracketlist.get(0);
								}

								operationList.set(bracket1 - 1, solution);

								for (int b = bracket2; b >= bracket1; b--) {
									operationList.remove(b);
								}
								a = 2;
								all = 0;
							}
						}
					}

				}
			} else {
				add = operationList.indexOf("+");
				sub = operationList.indexOf("-");
				multiply = operationList.indexOf("×");
				division = operationList.indexOf("÷");
				percentage = operationList.indexOf("%");
				if (percentage != -1) {
					if (percentage < 2) {
						mathA = new BigDecimal(operationList.get(percentage - 1));
						operationList.set(percentage - 1, percentagemethod1(mathA));
						operationList.remove(percentage);
					} else {
						if (operationList.get(percentage - 2) == "+") {
							if (percentage == 3) {
								mathA = new BigDecimal(operationList.get(0));
								mathB = new BigDecimal(operationList.get(percentage - 1));
								mathC = new BigDecimal(percentagemethod2(mathA, mathB));
								mathpercentage = additionmethod(mathA, mathC);

								operationList.set(0, mathpercentage);
								for (int z = percentage; z > 0; z--) {
									operationList.remove(z);
								}
							} else {
								percentagelist = new ArrayList<String>(operationList.subList(0, percentage - 2));
								mathpercentage = percentagestringmethod(percentagelist);

								mathA = new BigDecimal(mathpercentage);
								mathB = new BigDecimal(operationList.get(percentage - 1));
								mathC = new BigDecimal(percentagemethod2(mathA, mathB));
								mathpercentage = additionmethod(mathA, mathC);

								operationList.set(0, String.valueOf(mathpercentage));
								for (int z = percentage; z > 0; z--) {
									operationList.remove(z);
								}
							}
						} else {
							if (operationList.get(percentage - 2) == "-") {
								if (percentage == 3) {
									mathA = new BigDecimal(operationList.get(0));
									mathB = new BigDecimal(operationList.get(percentage - 1));
									mathC = new BigDecimal(percentagemethod2(mathA, mathB));
									mathpercentage = subtractionmethod(mathA, mathC);

									operationList.set(0, String.valueOf(mathpercentage));
									for (int z = percentage; z > 0; z--) {
										operationList.remove(z);
									}
								} else {
									percentagelist = new ArrayList<String>(operationList.subList(0, percentage - 2));
									mathpercentage = percentagestringmethod(percentagelist);

									mathA = new BigDecimal(mathpercentage);
									mathB = new BigDecimal(operationList.get(percentage - 1));
									mathC = new BigDecimal(percentagemethod2(mathA, mathB));
									mathpercentage = subtractionmethod(mathA, mathC);

									operationList.set(0, String.valueOf(mathpercentage));
									for (int z = percentage; z > 0; z--) {
										operationList.remove(z);
									}
								}
							} else {
								if (operationList.get(percentage - 2) == "×") {
									mathA = new BigDecimal(operationList.get(percentage - 1));
									operationList.set(percentage - 1, percentagemethod1(mathA));
									operationList.remove(percentage);
								} else {
									if (operationList.get(percentage - 2) == "%") {
										mathA = new BigDecimal(operationList.get(percentage - 1));
										operationList.set(percentage - 1, percentagemethod1(mathA));
										operationList.remove(percentage);
									}
								}

							}

						}
					}

				} else {
					if (multiply != -1 | division != -1) {
						// 不存在的符號給最大值
						if (multiply == -1) {
							multiply = operationList.size();
						}
						if (division == -1) {
							division = operationList.size();
						}
						// 順位
						// 乘
						if (multiply < division) {
							mathA = new BigDecimal(operationList.get(multiply - 1));
							mathB = new BigDecimal(operationList.get(multiply + 1));
							solution = multiplymethod(mathA, mathB);
							operationList.set(multiply - 1, solution);
							operationList.remove(multiply + 1);
							operationList.remove(multiply);
							all = 0;
						} else {
							// 除
							mathA = new BigDecimal(operationList.get(division - 1));
							mathB = new BigDecimal(operationList.get(division + 1));
							solution = divisionmethod(mathA, mathB);
							operationList.set(division - 1, solution);
							operationList.remove(division + 1);
							operationList.remove(division);
							all = 0;
						}

					} else {
						if (add != -1 | sub != -1) {
							// 不存在的符號給最大值
							if (add == -1) {
								add = operationList.size();
							}
							if (sub == -1) {
								sub = operationList.size();
							}
							if (add < sub) {
								// 加
								mathA = new BigDecimal(operationList.get(add - 1));
								mathB = new BigDecimal(operationList.get(add + 1));
								solution = additionmethod(mathA, mathB);
								operationList.set(add - 1, solution);
								operationList.remove(add + 1);
								operationList.remove(add);
								all = 0;
							} else {
								// 減
								mathA = new BigDecimal(operationList.get(sub - 1));
								mathB = new BigDecimal(operationList.get(sub + 1));
								solution = subtractionmethod(mathA, mathB);
								operationList.set(sub - 1, solution);
								operationList.remove(sub + 1);
								operationList.remove(sub);
								all = 0;
							}
						} else {
							all = 3;
						}
					}
				}

			}
		}
	}

	public String divisionmethod(BigDecimal numberA, BigDecimal numberB) {
		// 除
		if (Double.valueOf(numberB.toString()) == 0) {
			return "0";
		} else {
			String a;
			numberA = numberA.divide(numberB, 10, BigDecimal.ROUND_HALF_UP);
			a = numberA.toString();
			return a;
		}

	}

	public String multiplymethod(BigDecimal numberA, BigDecimal numberB) {
		// 乘
		String a;
		numberA = numberA.multiply(numberB);
		a = numberA.toString();
		return a;
	}

	public String additionmethod(BigDecimal numberA, BigDecimal numberB) {
		// 加
		String a;
		numberA = numberA.add(numberB);
		a = numberA.toString();
		return a;
	}

	public String subtractionmethod(BigDecimal numberA, BigDecimal numberB) {
		// 減
		String a;
		numberA = numberA.subtract(numberB);
		a = numberA.toString();
		return a;
	}

	public String percentagemethod1(BigDecimal numberA) {
		// 百分比(乘除)
		BigDecimal numberB = new BigDecimal("100");
		if (numberA.toString() == "0") {
			return "0";
		} else {
			String a;
			numberA = numberA.divide(numberB, 10, BigDecimal.ROUND_HALF_UP);
			a = numberA.toString();
			return a;
		}
	}

	public String percentagemethod2(BigDecimal numberA, BigDecimal numberB) {
		// 百分比(加減)
		BigDecimal numberC = new BigDecimal("100");
		if (numberA.toString() == "0" | numberB.toString() == "0") {
			return "0";
		} else {
			String a;
			numberA = numberA.multiply(numberB);
			numberA = numberA.divide(numberC, 10, BigDecimal.ROUND_HALF_UP);
			a = numberA.toString();
			return a;
		}
	}

	public String percentagestringmethod(List<String> percentagelist) {
		// 百分比(字串處理)

		int add = 0; // 符號+
		int sub = 0; // 符號-
		int multiply = 0; // 符號*
		int division = 0; // 符號/
		String solution = "";
		BigDecimal mathA;
		BigDecimal mathB;

		for (int a = 0; a < 2; a++) {

			add = percentagelist.indexOf("+");
			sub = percentagelist.indexOf("-");
			multiply = percentagelist.indexOf("×");
			division = percentagelist.indexOf("÷");

			if (multiply != -1 | division != -1) {
				// 不存在的符號給最大值
				if (multiply == -1) {
					multiply = percentagelist.size();
				}
				if (division == -1) {
					division = percentagelist.size();
				}
				// 乘
				if (multiply < division) {
					mathA = new BigDecimal(percentagelist.get(multiply - 1));
					mathB = new BigDecimal(percentagelist.get(multiply + 1));
					solution = multiplymethod(mathA, mathB);
					percentagelist.set(multiply - 1, solution);
					percentagelist.remove(multiply + 1);
					percentagelist.remove(multiply);
					a = 0;
				} else {
					// 除
					mathA = new BigDecimal(percentagelist.get(division - 1));
					mathB = new BigDecimal(percentagelist.get(division + 1));
					solution = divisionmethod(mathA, mathB);
					percentagelist.set(division - 1, solution);
					percentagelist.remove(division + 1);
					percentagelist.remove(division);
					a = 0;
				}

			} else {
				if (add != -1 | sub != -1) {
					// 不存在的符號給最大值
					if (add == -1) {
						add = percentagelist.size();
					}
					if (sub == -1) {
						sub = percentagelist.size();
					}
					if (add < sub) {
						// 加
						mathA = new BigDecimal(percentagelist.get(add - 1));
						mathB = new BigDecimal(percentagelist.get(add + 1));
						solution = additionmethod(mathA, mathB);
						percentagelist.set(add - 1, solution);
						percentagelist.remove(add + 1);
						percentagelist.remove(add);
						a = 0;
					} else {
						// 減
						mathA = new BigDecimal(percentagelist.get(sub - 1));
						mathB = new BigDecimal(percentagelist.get(sub + 1));
						solution = subtractionmethod(mathA, mathB);
						percentagelist.set(sub - 1, solution);
						percentagelist.remove(sub + 1);
						percentagelist.remove(sub);
						a = 0;
					}
				} else {

					if (percentagelist.size() == 1) {
						solution = percentagelist.get(0);
					}
					a = 2;
				}
			}
		}
		return solution;
	}

	// 更換運算中的圖
	public void changanimalimg(int ui, int img) {
		switch (ui) {
		case 1:
			switch (img) {
			case 0:
				animalimg.setImageResource(R.drawable.sheep);
				break;
			case 1:
				animalimg.setImageResource(R.drawable.sheep1);
				break;
			case 2:
				animalimg.setImageResource(R.drawable.sheep2);
				break;
			case 3:
				animalimg.setImageResource(R.drawable.sheep3);
				break;
			}

			break;

		case 2:
			switch (img) {
			case 0:
				animalimg.setImageResource(R.drawable.dog);
				break;
			case 1:
				animalimg.setImageResource(R.drawable.dog1);
				break;
			case 2:
				animalimg.setImageResource(R.drawable.dog2);
				break;
			case 3:
				animalimg.setImageResource(R.drawable.dog3);
				break;
			}

			break;

		}

	}

	// 清除
	public void clearclick(View v) {
		recordtext.setText("");
		resulttext.setText("");
		recordstring = "";
		resultstring = "";
		clearstatus();
		changanimalimg(setaccUI, 0);
	}

	// 除
	public void divisionclick(View v) {
		if (first == 1) {
			if (basicoperation == 0) {
				recordstring = recordtext.getText().toString();
				recordstring = recordstring + "÷";
				recordtext.setText(recordstring);
				basicoperation++;
				decimalpoint = 0;
			} else {
				if (percentage == 1) {
					recordstring = recordtext.getText().toString();
					recordstring = recordstring + "÷";
					recordtext.setText(recordstring);
					basicoperation++;
					decimalpoint = 0;
					percentage = 2;
				}
			}
		}
		changanimalimg(setaccUI, 2);
	}

	// 乘
	public void multiplyclick(View v) {
		if (first == 1) {
			if (basicoperation == 0) {
				recordstring = recordtext.getText().toString();
				recordstring = recordstring + "×";
				recordtext.setText(recordstring);
				basicoperation++;
				decimalpoint = 0;
			} else {
				if (percentage == 1) {
					recordstring = recordtext.getText().toString();
					recordstring = recordstring + "×";
					recordtext.setText(recordstring);
					basicoperation++;
					decimalpoint = 0;
					percentage = 2;
				}
			}
		}
		changanimalimg(setaccUI, 2);
	}

	// 減
	public void subtractionclick(View v) {
		if (first == 0 || basicoperation > 0) {
			if (sub < 1) {
				recordstring = recordtext.getText().toString();
				recordstring = recordstring + "-";
				recordtext.setText(recordstring);
				sub++;
				basicoperation++;
				decimalpoint = 0;
			}
		} else {
			if (sub < 2) {
				recordstring = recordtext.getText().toString();
				recordstring = recordstring + "-";
				recordtext.setText(recordstring);
				sub++;
				basicoperation++;
				decimalpoint = 0;
			}
		}
		changanimalimg(setaccUI, 2);
	}

	// 加
	public void additionclick(View v) {
		if (first == 1) {
			if (basicoperation == 0) {
				recordstring = recordtext.getText().toString();
				recordstring = recordstring + "+";
				recordtext.setText(recordstring);
				basicoperation++;
				decimalpoint = 0;
			} else {
				if (percentage == 1) {
					recordstring = recordtext.getText().toString();
					recordstring = recordstring + "+";
					recordtext.setText(recordstring);
					basicoperation++;
					decimalpoint = 0;
					percentage = 2;
				}
			}
		}
		changanimalimg(setaccUI, 2);
	}

	// 小數點
	public void decimalpointclick(View v) {
		if (first == 1 & percentage != 1) {
			if (decimalpoint == 0) {
				recordstring = recordtext.getText().toString();
				recordstring = recordstring + ".";
				recordtext.setText(recordstring);
				decimalpoint++;
				basicoperation++;
				sub = 2;
				first = 3;
			}
		}
	}

	// 刪除
	public void delclick(View v) {
		if (recordtext.getText().toString() != "") {
			if (recordtext.length() > 1) {
				recordstring = recordtext.getText().toString();
				recordstring = recordstring.substring(0, recordstring.length() - 1);
				recordtext.setText(recordstring);
			} else {
				recordstring = "";
				recordtext.setText(recordstring);
			}
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
		changanimalimg(setaccUI, 1);

	}

	// 等於
	public void equalclick(View v) {
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
		changanimalimg(setaccUI, 3);
	}

	public void sevenclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "7";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void eightclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "8";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void nineclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "9";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void fourclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "4";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void fiveclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "5";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void sixclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "6";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void oneclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "1";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void twoclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "2";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void threeclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "3";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void zeroclick(View v) {
		if (percentage != 1) {
			recordstring = recordtext.getText().toString();
			recordstring = recordstring + "0";
			recordtext.setText(recordstring);
			digitalstatus();
		}
		stringprocess();
		int same = 0;
		if (recordtext.getText().toString() != "") {
			if (operationList.size() > 1) {
				for (int a = 0; a < operationstring.length; a++) {
					int size = operationList.size() - 1;
					if (String.valueOf(operationList.get(size)).equals(operationstring[a])) {
						same = 1;
					}
					if (String.valueOf(operationList.get(size)).equals("%")) {
						same = 0;
					}
				}
			}
			if (same == 0) {
				stringoperation();
				resulttext.setText(operationList.get(0));
			}
		}
	}

	public void shopclick(View v) {
		Intent storeintent = new Intent();
		storeintent.setClass(MainActivity.this, com.animal.cutecalculate_shop.ShopActivity.class);
		MainActivity.this.startActivity(storeintent);
		MainActivity.this.finish();
	}

	public void storeclick(View v) {
		Intent storeintent = new Intent();
		storeintent.setClass(MainActivity.this, com.animal.cutecalculate_store.StoreActivity.class);
		MainActivity.this.startActivity(storeintent);
		MainActivity.this.finish();
	}

	public void freeclick(View v) {
		callShowOffers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onContentDismiss(TJPlacement arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContentReady(TJPlacement arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContentShow(TJPlacement arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPurchaseRequest(TJPlacement arg0, TJActionRequest arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRequestFailure(TJPlacement arg0, TJError arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRequestSuccess(TJPlacement arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRewardRequest(TJPlacement arg0, TJActionRequest arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetCurrencyBalanceResponse(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetCurrencyBalanceResponseFailure(String arg0) {
		// TODO Auto-generated method stub

	}
}
