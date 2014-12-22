package com.example.getwifiinfo;

import java.util.ArrayList;
import java.util.List;

import com.example.getwifiinfo.WifiItem.WifiCipherType;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

public class MainActivity extends Activity implements OnItemClickListener,
		OnClickListener {
	boolean debugOpen = false;
	private Context context;
	private ListView view;
	//private Button scan;
	private WifiAdapter adapter;
	private List<WifiItem> list;
	private WifiManager mWifiManager;
	private WifiStausReceiver receiverWifiStatus;
	private List<ScanResult> wifiList;
	private WifiInfoReceiver receiverWifiInfo;

	private final int DIALOG_SET_WIFI = 0;
	private final int DIALOG_CHECK_WIFI = 1;
	private final int DIALOG_SEND_WIFI = 2;
	private final int DIALOG_OPEN_WIFI = 3;

	private int mListPos;

	private WifiSqlTools tools;
	float down_x;
	float down_y;
	//下拉多少进行刷新
	private int REFRESH_LEN = 100;
	//记录DOWN的位置
	private float pen_down = 0;
	//标志是否为LISTVIEW的最顶
	boolean is_list_top = false;
	Toast toast;
	static SharedPreferences sp;
	public static String PopString = "popstring";
	public static String PROGRESSSOCKET = "progressSocket";
	public static String disPROGRESSSOCKET = "disprogressSocket";
	public static String strSign = "strSign";
	static public ProgressDialog m_pDialog;
	private final static String WIFI_CONFIGUER = "wifiConfiguer";
	private final static String WIFI_NAME = "wifiName";
	private final static String WIFI_POWERWORD = "wifiPowerWord";
	private final static String IP_NAME = "IPname";
	private final static String COM_NAME = "COMname";
	private int neoTime;
	boolean timeFlag = false;
	private final static int NUM_COUNT = 15;
	int count = 0;
	//连接新热点入口
	int scanSwicth = 0;
	private final static int SCAN_NEW_ENTRY = 0;	//扫描新热点
	private final static int CONNECT_NEW_ENTRY = 1; //连接新热点
	//end连接新热点入口
	
	public final static String OPEN_PWD = "NEOWAY13";
	
	//区别向模块发送的内容
	public final static int MOD_MESSAGE = 0;
	public final static int WLAN_MESSAGE = 1;
	
	//
	private  int FIR_CHECK_CONNECT_SLEEP_TIME	= 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		context = this;
		initSP();
		scanSwicth = SCAN_NEW_ENTRY;
		view = (ListView) findViewById(R.id.list);
		//scan = (Button) findViewById(R.id.scan);
		//scan.setOnClickListener(this);
		
		list = new ArrayList<WifiItem>();

		tools = new WifiSqlTools(context);

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
/*
		receiverWifiStatus = new WifiStausReceiver();
		registerReceiver(receiverWifiStatus, new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION));

		receiverWifiInfo = new WifiInfoReceiver();
		registerReceiver(receiverWifiInfo, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
*/
		ScanWifi();

		registerForContextMenu(view);
		registviewevent();
		
		IntentFilter itFilter = new IntentFilter();
		itFilter.addAction(PopString);
		itFilter.addAction(PROGRESSSOCKET);
		itFilter.addAction(disPROGRESSSOCKET);
		registerReceiver(updataReceiver, itFilter);
	}
	
	private void initSP()
	{
		sp = context.getSharedPreferences(WIFI_CONFIGUER, Activity.MODE_PRIVATE);
		//sp.edit().putBoolean(PROPERTY_MAC, true).commit();
		//sp.edit().putString(WIFI_NAME, "W680").commit();//Android35  MEIZU MX2  innos-ap01
		//sp.edit().putString(WIFI_POWERWORD, "123456789").commit();//123456789 80008innos
	}
	
	private boolean checkWifiStatus() {
		if (!mWifiManager.isWifiEnabled()) {
			ShowMyDialog(DIALOG_OPEN_WIFI);

		}
		return mWifiManager.isWifiEnabled();
	}

	private void ScanWifi() {
		displaytoast(R.string.Scan);
		scanSwicth = SCAN_NEW_ENTRY;
		checkWifiStatus();
		mWifiManager.startScan();
		refreshAdapter();
	}

	private void refreshAdapter() {
		adapter = new WifiAdapter(list);
		view.setAdapter(adapter);
	}

	class WifiStausReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (isConnected)
				return;
			list = new ArrayList<WifiItem>();

			wifiList = mWifiManager.getScanResults();
			if (wifiList == null) {
				refreshAdapter();
			} else {
				try {
					Thread.currentThread();
					Thread.sleep(300);
				} catch (Exception e) {
				}
				scanSwicth = SCAN_NEW_ENTRY;
				mWifiManager.startScan();
				wifiList = mWifiManager.getScanResults();

				WifiItem wifi;
				ScanResult result;
				if(debugOpen)
				{
					System.out.println("11wifiList.size()="+wifiList.size());
				}
				for (int i = 0; i < wifiList.size(); i++) {
					wifi = new WifiItem();
					result = wifiList.get(i);
					wifi.setSsid(result.SSID + "");
					wifi.setBssid(result.BSSID + "");
					wifi.setLevel(WifiManager.calculateSignalLevel(
							result.level, 5) + "");
					wifi.setEncryption(result.capabilities);
					list.add(wifi);
				}
				refreshAdapter();
			}

		}
	}

	class WifiInfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if(scanSwicth == SCAN_NEW_ENTRY)
			{
				//System.out.println("isConnected = "+isConnected);
				//if (isConnected)
				//	return;
				list = new ArrayList<WifiItem>();

				wifiList = mWifiManager.getScanResults();

				WifiItem wifi;
				ScanResult result;
				if(debugOpen)
				{
					System.out.println("wifiList.size()="+wifiList.size());
				}

				for (int i = 0; i < wifiList.size(); i++) {
					wifi = new WifiItem();
					result = wifiList.get(i);
					wifi.setSsid(result.SSID + "");
					wifi.setBssid(result.BSSID + "");
					wifi.setLevel(WifiManager.calculateSignalLevel(result.level, 5)
							+ "");
					wifi.setEncryption(result.capabilities);
					list.add(wifi);
				}
				refreshAdapter();
			}
			else if(scanSwicth == CONNECT_NEW_ENTRY)
			{
				scanList = mWifiManager.getScanResults();
				if (wifiList == null || wifiList.size() == 0 || hot == null)  // || isConnected
					return;
				onReceiveNewNetworks(wifiList);
			}
		}
	}

	class WifiAdapter extends BaseAdapter {
		private List<WifiItem> mlist;

		public WifiAdapter(List<WifiItem> mlist) {
			this.mlist = mlist;
		}

		@Override
		public int getCount() {
			return mlist.size();
		}

		@Override
		public Object getItem(int position) {
			return mlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			view = LayoutInflater.from(context).inflate(R.layout.list_item,
					null);
			TextView t_ssid = (TextView) view.findViewById(R.id.ssid);

			ImageView imglevel = (ImageView) view.findViewById(R.id.imgLevel);

			TextView t_bssid = (TextView) view.findViewById(R.id.bssid);
			TextView t_entry = (TextView) view.findViewById(R.id.entry);

			WifiItem wifi = mlist.get(position);
			t_ssid.setText(wifi.getSsid());
			imglevel.setBackgroundResource(getImagelevel(wifi.getLevel()));
			//imglevel.setId(getImagelevel(wifi.getLevel()));
			//t_level.setText(wifi.getLevel());

			t_bssid.setText(wifi.getBssid());
			t_entry.setText(wifi.getEntryInfo());
			return view;
		}
	}
	
	private int getImagelevel(String strid)
	{
		int imgID = R.drawable.wifi0;
		byte id[] = strid.getBytes();
		
		switch(id[0] - '0')
		{
		case 0:
			imgID = R.drawable.wifi0;
			break;
		case 1:
			imgID = R.drawable.wifi1;
			break;
		case 2:
			imgID = R.drawable.wifi2;
			break;
		case 3:
			imgID = R.drawable.wifi3;
			break;
		case 4:
			imgID = R.drawable.wifi4;
			break;
		default:
			break;
		}
		return imgID;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.wifi_item, menu);

		super.onCreateContextMenu(menu, v, menuInfo);
	//	System.out.println("onCreateContextMenuxxxxxxxxxxxxxxxxxxxxxxx");
	}

	private void ShowMyDialog(int id) {
		Builder builder;
		switch (id) {
		case DIALOG_SET_WIFI:
			builder = new AlertDialog.Builder(context)
					.setTitle(getString(R.string.set_wifi_info));
			final EditText epwd = new EditText(context);

			builder.setView(epwd)
					.setPositiveButton(getString(R.string.sure),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									WifiItem item = list.get(mListPos);
									item.setPwd(epwd.getText().toString());
									long flag = tools.InsertOrModifyItem(item);

									SQLToast(flag != -1 ? getString(R.string.sql_success)
											: getString(R.string.sql_error));

									dialog.cancel();
								}

							})
					.setNegativeButton(getString(R.string.no),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});

			builder.create().show();
			break;
		case DIALOG_CHECK_WIFI:

			View mView = LayoutInflater.from(context).inflate(
					R.layout.item_show, null);
			TextView ssid = (TextView) mView.findViewById(R.id.item_ssid);
			TextView bssid = (TextView) mView.findViewById(R.id.item_bssid);
			TextView pwd = (TextView) mView.findViewById(R.id.item_pwd);
			TextView entry = (TextView) mView.findViewById(R.id.item_entry);

			WifiItem item = list.get(mListPos);
			WifiItem wifi = tools.SearchItem(item.getBssid());
			if (wifi != null) {
				item = wifi;
			}

			ssid.setText(item.getSsid() + "");
			bssid.setText(item.getBssid() + "");
			pwd.setText(item.getPwd() + "");
			entry.setText(item.getEntryInfo() + "");

			builder = new AlertDialog.Builder(context)
					.setTitle(getString(R.string.check_wifi_info))
					.setView(mView)
					.setPositiveButton(getString(R.string.sure),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
			builder.create().show();
			break;
		case DIALOG_SEND_WIFI:
			break;
		case DIALOG_OPEN_WIFI:
			
			new AlertDialog.Builder(context)
					.setCancelable(false)
					.setTitle(getString(R.string.warming))
					.setMessage(getString(R.string.checkstatus))
					.setPositiveButton(getString(R.string.sure),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mWifiManager.setWifiEnabled(true);
									Toast.makeText(context,
											getString(R.string.openwifi),
											Toast.LENGTH_SHORT).show();
									//ScanWifi();
									dialog.cancel();
								}
							})
					.setNegativeButton(getString(R.string.no),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).create().show();
			break;
		}

	}

	private void SQLToast(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	WifiItem hot;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		mListPos = menuInfo.position;

		int id = item.getItemId();
		switch (id) {
		case R.id.setwifi:
			ShowMyDialog(DIALOG_SET_WIFI);
			break;
		case R.id.checkwifi:
			ShowMyDialog(DIALOG_CHECK_WIFI);
			break;
		case R.id.sendinfo:
			if(sp.getString(WIFI_NAME, "").length() == 0 /*&& sp.getString(WIFI_POWERWORD, "").length() == 0*/)
			{
				displaytoast(R.string.WIFI_NAME_NO_INIT);
				break;
			}
			
			WifiItem wifi = list.get(mListPos);
			hot = tools.SearchItem(wifi.getBssid());
			scanSwicth = CONNECT_NEW_ENTRY;
			//wifiReceiver = new WifiReceiver();
			//IntentFilter iFilter = new IntentFilter(
			//		WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			//iFilter.setPriority(100);
			//registerReceiver(wifiReceiver, iFilter);
			
			mWifiManager.startScan();
			if (isConnected) {
				try {
					Thread.currentThread();
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				
			}
			
			if (hot == null || hot.getPwd() == null)
			{
				if(wifi.getEntryInfo().equals("no entry") == true)
				{
					hot = new WifiItem(wifi.getSsid(), wifi.getBssid(), OPEN_PWD, null);
				}
				else
				{
					displaytoast(R.string.PopWifiNull);
					break;
				}
			}
			
			timeFlag = false;
			count = 0;
			waittingAnysisty(R.string.WIFI_CONNECT);
			FIR_CHECK_CONNECT_SLEEP_TIME = 0;
			startTimer(1000, MOD_MESSAGE);
			break;
		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
	/*
		switch (id) {
		case R.id.scan:
			ScanWifi();
			break;

		default:
			break;
		}
	*/
	}

	private List<ScanResult> scanList;
	private List<String> passableHotsPot;
	private WifiReceiver wifiReceiver;
	private boolean isConnected = false;

	/* 监听热点变化 */
	private final class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			scanList = mWifiManager.getScanResults();
			if(debugOpen)
			{
				System.out.println("wifiList = " +wifiList);
				System.out.println("hot = " +hot);
				System.out.println("isConnected = " +isConnected);
			}
			if (wifiList == null || wifiList.size() == 0 || hot == null)  // || isConnected
				return;
			onReceiveNewNetworks(wifiList);
		}
	}

	/* 当搜索到新的wifi热点时判断该热点是否符合规格 */
	public void onReceiveNewNetworks(List<ScanResult> wifiList) {

		passableHotsPot = new ArrayList<String>();
		for (ScanResult result : wifiList) {
			
			if ((result.SSID).contains(hot.getSsid())) {
				if(debugOpen)
				{
					System.out.println("SSID=" + hot.getSsid());
					System.out.println("SSID=" + hot.getPwd());
				}
				passableHotsPot.add(result.SSID);
			}
		}
		synchronized (this) {
			connectToHotpot();
		}
	}

	/* 连接到热点 */
	public void connectToHotpot() {
		if (passableHotsPot == null || passableHotsPot.size() == 0)
			return;
		
		WifiConfiguration wifiConfig = this.setWifiParams(passableHotsPot
				.get(0));
		//wifiConfig.wepKeys
		int wcgID = mWifiManager.addNetwork(wifiConfig);
		boolean flag = mWifiManager.enableNetwork(wcgID, true);
		isConnected = flag;
		if(debugOpen)
		{
			System.out.println("connect success? " + flag);
		}
	}

	/* 设置要连接的热点的参数 */
	public WifiConfiguration setWifiParams(String ssid) {
		WifiConfiguration apConfig = new WifiConfiguration();
		//apConfig.SSID = "\"" + ssid + "\"";
		//apConfig.preSharedKey = "\"" + hot.getPwd() + "\"";
		apConfig.SSID = "\"" + sp.getString(WIFI_NAME, "") + "\"";
		apConfig.preSharedKey = "\"" + sp.getString(WIFI_POWERWORD, "") + "\"";
		if(debugOpen)
		{
			System.out.println("apConfig.SSID="+apConfig.SSID);
			System.out.println("apConfig.preSharedKey="+apConfig.preSharedKey);
		}
		apConfig.hiddenSSID = true;
		apConfig.status = WifiConfiguration.Status.ENABLED;
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		apConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		apConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.CCMP);
		apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		
		//解决加密方式WPA1连不上的问题
		apConfig.hiddenSSID = true;   
		apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);   
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);                         
		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);                         
		apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);                    
		apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);                      
        apConfig.status = WifiConfiguration.Status.ENABLED;   

		
		return apConfig;
	}

	protected void onPause() {
		unregisterReceiver(receiverWifiStatus);
		unregisterReceiver(receiverWifiInfo);
		super.onPause();
	}

	protected void onResume() {
		receiverWifiStatus = new WifiStausReceiver();
		registerReceiver(receiverWifiStatus, new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION));

		receiverWifiInfo = new WifiInfoReceiver();
		registerReceiver(receiverWifiInfo, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		tools.closeDB();
		//unregisterReceiver(receiverWifiStatus);
		unregisterReceiver(updataReceiver);
		super.onDestroy();
	}
	
	public boolean onTouchEvent(MotionEvent event) 
    {
    	float x = event.getX();    
        float y = event.getY();
    	switch (event.getAction())
    	{
    	case MotionEvent.ACTION_DOWN:
    		down_x = event.getX();
    		down_y = event.getY();
    		break;
    	case MotionEvent.ACTION_MOVE:
    		break;
    	case MotionEvent.ACTION_UP:
    		if(y - down_y > REFRESH_LEN)
    		{
    			ScanWifi();
    		}
    		break;
    	}
    	return true;    
    }
	
	private void registviewevent()
	{
		view.setOnTouchListener(new OnTouchListener()
    	{

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				float x = arg1.getX();    
		        float y = arg1.getY();
				switch(arg1.getAction())
				{
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_DOWN:
					down_x = arg1.getX();
		    		down_y = arg1.getY();
		    		pen_down = down_y;
					break;
				case MotionEvent.ACTION_UP:
					if(y - down_y > REFRESH_LEN && is_list_top)
		    		{
						ScanWifi();
		    		}
					break;
				}
				return false;
			}
    	});
		
		view.setOnScrollListener(new OnScrollListener()
    	{
    		public void onScrollStateChanged(AbsListView view, int scrollState)
    		{
    			switch (scrollState)
    			{
    			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
    			//	if (m_ListView.getLastVisiblePosition() == (m_ListView.getCount() - 1)) 
    				//判断滚动到顶部
    				if(view.getFirstVisiblePosition() == 0)
    				{
    					is_list_top = true;
    				}
    				else
    				{
    					is_list_top = false;
    				}
    				break;
    			case OnScrollListener.SCROLL_STATE_IDLE:
    				break;
    			}
    		}

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}
    	});
		
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//sendNameAndPWD(arg2);
				mListPos = arg2;
				ShowMyDialog(DIALOG_CHECK_WIFI);
			}
		});
	}
	
	//发送用HU名密码给模块
	private void sendNameAndPWD(int id)
	{
		WifiItem wifi = list.get(id);
		hot = tools.SearchItem(wifi.getBssid());
		
		if (hot == null || hot.getPwd() == null)
		{
			if(wifi.getEntryInfo().equals("no entry") == true)
			{
				hot = new WifiItem(wifi.getSsid(), wifi.getBssid(), OPEN_PWD, null);
			}
			else
			{
				displaytoast(R.string.PopWifiNull);
				return;
			}
		}
		//unregisterReceiver(wifiReceiver);

		//receiverWifiInfo = new WifiInfoReceiver();
		//IntentFilter iFilter = new IntentFilter(
		//		WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		//iFilter.setPriority(100);
		//registerReceiver(receiverWifiInfo, iFilter);
		
		MainSocket socket = new MainSocket(hot.getSsid(), hot.getPwd(), 
											sp.getString(IP_NAME, ""), 
											sp.getString(COM_NAME, ""), this);
	}
	
	 public void displaytoast(int stringID)
	 {
	    if(toast == null)
	    {
	    	toast = Toast.makeText(this, this.getString(stringID), Toast.LENGTH_SHORT);
	    }
	    else
	    {
	    	toast.setText(this.getString(stringID));
	       	toast.setDuration(Toast.LENGTH_SHORT);
	    }
	    	
	   	toast.show();
	  }
	 
	 private BroadcastReceiver updataReceiver = new BroadcastReceiver()
	    {
	    	public void onReceive(Context context, Intent intent)
	    	{
	    		Bundle getResult = intent.getExtras();
	    		if(intent.getAction().equals(PopString))
	    		{
	    			int index = getResult.getInt(strSign);
	    			displaytoast(index);
	    		}
	    		else if(intent.getAction().equals(PROGRESSSOCKET))
	    		{
	    			waittingAnysisty(R.string.Databeingprocessed);
	    		}
	    		else if(intent.getAction().equals(disPROGRESSSOCKET))
	    		{
	    			finishWaitting();
	    		}
	    	}
	    };
	    
	  //进度框
		public void waittingAnysisty(int id)
		{
			// 设置进度条风格，风格为圆形，旋转的
			m_pDialog = new ProgressDialog(this);
			
			m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			// 设置ProgressDialog 标题
			m_pDialog.setTitle(getString(R.string.pleaseWait));
			
			// 设置ProgressDialog 提示信息
			m_pDialog.setMessage(getString(id));

			// 设置ProgressDialog 标题图标
		//	m_pDialog.setIcon(R.drawable.img1);

			// 设置ProgressDialog 的进度条是否不明确
			m_pDialog.setIndeterminate(false);
			
			// 设置ProgressDialog 是否可以按退回按键取消
			m_pDialog.setCancelable(true);
			
			m_pDialog.show();
		}
		
		//进度框消失
		public void finishWaitting()
		{
			if(m_pDialog.isShowing())
			{
				m_pDialog.cancel();
			}
		}
		
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0,0,0, R.string.WifiSetting);
		menu.add(0,1,0, R.string.WLENSetting);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemID = item.getItemId();
		switch(itemID)
		{
		case 0:
			entryChangeNameAndWork();
			break;
		case 1:
			entryWLENParaSetting();
			break;
		default:
			break;
		}
		return true;
	}
	
    public void entryChangeNameAndWork()
	{  //editdiallogview  .inflate 
    	
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View editView = factory.inflate(R.layout.dialwifisetting, null);
    	
    	/*************初始化EDITTEXT的内容*******************/
    	EditText initNameText = (EditText)editView.findViewById(R.id.name);;
		EditText initWorkText = (EditText)editView.findViewById(R.id.workplace);
		initNameText.setText(sp.getString(WIFI_NAME, ""));
		initWorkText.setText(sp.getString(WIFI_POWERWORD, ""));
		/****************************************************/
		
		AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
			.setTitle(getString(R.string.WifiSetting)).setIcon(android.R.drawable.ic_dialog_info)
			.setView(editView)
			.setPositiveButton(getString(R.string.Done), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText nameText;
					EditText workText;
					
					nameText = (EditText)editView.findViewById(R.id.name);
					workText = (EditText)editView.findViewById(R.id.workplace);
					
					sp.edit().putString(WIFI_NAME, nameText.getText().toString()).commit();
					sp.edit().putString(WIFI_POWERWORD, workText.getText().toString()).commit();
				}
			}).setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			}).create();

		ad.show();
	}
    
    private void entryWLENParaSetting()
    {
  //editdiallogview  .inflate 
    	
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View editView = factory.inflate(R.layout.wlanparasetting, null);
    	
    	/*************初始化EDITTEXT的内容*******************/
    	EditText initNameText = (EditText)editView.findViewById(R.id.IPname);;
		EditText initWorkText = (EditText)editView.findViewById(R.id.COMname);
		initNameText.setText(sp.getString(IP_NAME, ""));
		initWorkText.setText(sp.getString(COM_NAME, ""));
		/****************************************************/
		
		AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
			.setTitle(getString(R.string.WLENSetting)).setIcon(android.R.drawable.ic_dialog_info)
			.setView(editView)
			.setPositiveButton(getString(R.string.SAVE), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText nameText;
					EditText workText;
					
					nameText = (EditText)editView.findViewById(R.id.IPname);
					workText = (EditText)editView.findViewById(R.id.COMname);
					
					sp.edit().putString(IP_NAME, nameText.getText().toString()).commit();
					sp.edit().putString(COM_NAME, workText.getText().toString()).commit();
					
					if(!checkIPisRight(nameText.getText().toString()))
					{
						displaytoast(R.string.IP_WRONG);
						entryWLENParaSetting();
						return;
					}
				}
			}).setNegativeButton(getString(R.string.SEND), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText nameText;
					EditText workText;
					
					nameText = (EditText)editView.findViewById(R.id.IPname);
					workText = (EditText)editView.findViewById(R.id.COMname);
					
					sp.edit().putString(IP_NAME, nameText.getText().toString()).commit();
					sp.edit().putString(COM_NAME, workText.getText().toString()).commit();
					
					if(!checkIPisRight(nameText.getText().toString()))
					{
						displaytoast(R.string.IP_WRONG);
						entryWLENParaSetting();
						return;
					}
					
					if(!checkCOMisRight(workText.getText().toString()))
					{
						displaytoast(R.string.PORT_WRONG);
						entryWLENParaSetting();
						return;
					}
					
					sendIPandCOMtoModen();
				}
			}).create();

		ad.show();
	
    }
    
    //check enter ip is right
    private boolean checkIPisRight(String ip)
    {
    	int num = 0;
    	int pointNum = 0;
    	int total = 0;
    			
    	char[] ip_array = ip.toCharArray();
    	for(int i = 0; i < ip.length(); i++)
    	{
    		if(ip_array[i] == '.')
    		{
    			pointNum++;
    			//超过3位数
    			if(num > 3 || num == 0)
    			{
    				System.out.println("超过3位数");
    				return false;
    			}
    			//.超过3个
    			if(pointNum > 3)
    			{System.out.println(".超过3个");
    				return false;
    			}
    			//超过255
    			System.out.println("total="+total);
    			if(total > 255)
    			{
    				System.out.println("超过255");
    				return false;
    			}
    			total = 0;
    			num = 0;
    		}
    		else
    		{
    			num++;
    			switch(num)
    			{
    			case 1:
    				total += (ip_array[i] - '0');
    				break;
    			case 2:
    			case 3:
    				total = (total * 10 + (ip_array[i] - '0'));
    				break;
    			default:
    				break;
    			}
    		}
    	}
    	
    	//超过3位数
		if(num > 3 || num == 0)
		{
			System.out.println("超过3位数");
			return false;
		}
		else if(total > 255)
		{
			System.out.println("超过255");
			return false;
		}
    	
    	if(pointNum == 3)
    	{
    		return true;
    	}
    	else
    	{
    		System.out.println("少3个.");
    		return false;
    	}
    }
    
    /*
     *检测端口号是否为空
    */
    private boolean checkCOMisRight(String com)
    {
    	if(com.length() == 0)
    	{
    		return false;
    	}
    	
    	return true;
    }
    
    private void sendIPandCOMtoModen()
    {
    	if(sp.getString(WIFI_NAME, "").length() == 0 /*&& sp.getString(WIFI_POWERWORD, "").length() == 0*/)
		{
			displaytoast(R.string.WIFI_NAME_NO_INIT);
			return;
		}
		
		scanSwicth = CONNECT_NEW_ENTRY;
		mWifiManager.startScan();
		if (isConnected) {
			try {
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			
		}
		waittingAnysisty(R.string.WIFI_CONNECT);
    	timeFlag = false;
		count = 0;
		FIR_CHECK_CONNECT_SLEEP_TIME = 0;
		startTimer(1000, WLAN_MESSAGE);
    }
    
    private void startTimer(int time, final int flag)
	{
		neoTime = time;
		//timeFlag = false;
		//while(!timeFlag){
		new Thread(){	
			public void run() {
						try {
							if(FIR_CHECK_CONNECT_SLEEP_TIME == 0)
							{
								sleep(neoTime*3);
							}
							else
							{
								sleep(neoTime);
							}
							FIR_CHECK_CONNECT_SLEEP_TIME++;
							
							timeFlag = checkWIFIisConnect();
							if(debugOpen)
							{
								System.out.println("timeFlag ========"+timeFlag);
							}
							
							if(++count > NUM_COUNT || timeFlag)
							{
								//timeFlag = true;
								//return;
								finishWaitting();
							}
							else
							{
								startTimer(1000, flag);
							}
							
							if(timeFlag)
							{
								switch(flag)
								{
								case MOD_MESSAGE:
									sendNameAndPWD(mListPos);
									break;
								case WLAN_MESSAGE:
									MainSocket socket = new MainSocket(sp.getString(IP_NAME, ""), 
																		sp.getString(COM_NAME, ""), 
																		MainActivity.this);
									break;
								default:
									break;
								}
								
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}.start();
			//break;
		//}
	}
    
    private boolean checkWIFIisConnect()
    {
    	ConnectivityManager connManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	if(wifi.isConnected())
    	{
    		return true;
    	}
    	return false;
    }
}
