package com.example.fragments;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;

public class Main extends FragmentActivity implements MainFragment.OnButtonClickListener, SettingsFragment.OnSettingsButtonClickListener, StatusFragment.OnStatusButtonClickListener {

    public static final String ARG_ADDRESS = "Address";
    public static final String ARG_LOGIN = "Login";
    public static final String ARG_INTERVAL = "Interval";
    public static final String ARG_DELTA = "Delta";
    public static final String ARG_ISSTARTED = "isRunning";
    public static final String ARG_REFRESHTYPE = "refreshType";
    public static final String ARG_TOSEND = "toSend";

	public static final String APP_PREFERENCES_LOGIN = "Login";
	public static final String APP_PREFERENCES_ADDRESS = "Address";
	public static final String APP_PREFERENCES_INTERVAL = "Interval";
	public static final String APP_PREFERENCES_DELTA = "Delta";
	public static final String APP_PREFERENCES_REFRESHTYPE = "refreshType";
	public static final String APP_PREFERENCES_TOSEND = "roSend";
	public static final String APP_PREFERENCES = "mysettings";
	public SharedPreferences mSettings;
	public String login;
	public String address;
	public int interval;
	public int delta;
	public int refreshType; //0 - distance, 1 - timer
	public boolean toSend;
	public boolean isStarted;
    NmeaBroadcastReceiver br;

	
	static double earthRadius = 6371000.0; // meters
	//double lastLatitude = 61.663856; //North Pole
	//double lastLongitude = 50.815946;
	double lastLatitude = 90; //North Pole
	double lastLongitude = 0;

    public Handler handler;
    public Runnable runnable;
    


	 TextView textLatitude;
	 TextView textLongitude;
	 TextView textInView;
	 TextView textUsed;
	 TextView textRecieved;
	 TextView textSent;
	 TextView textDelta;
     TextView textError;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        Log.v("nmea","Main onCreate, isStated="+address);

        if (savedInstanceState != null) {
            isStarted = savedInstanceState.getBoolean(ARG_ISSTARTED);
            return;
        }

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        	if (mSettings.contains(APP_PREFERENCES_LOGIN)){
        		login = mSettings.getString(APP_PREFERENCES_LOGIN, "");
        	} else {
        		Editor editor = mSettings.edit();
        		login = "";
        		editor.putString(APP_PREFERENCES_LOGIN, login);
        	}
        	if (mSettings.contains(APP_PREFERENCES_ADDRESS)){
        		address = mSettings.getString(APP_PREFERENCES_ADDRESS, "");
        	} else {
        		Editor editor = mSettings.edit();
        		address = "62.182.29.199";
        		editor.putString(APP_PREFERENCES_ADDRESS, address);
        	}
        	if (mSettings.contains(APP_PREFERENCES_INTERVAL)){
        		interval = mSettings.getInt(APP_PREFERENCES_INTERVAL, 0);
        	} else {
        		Editor editor = mSettings.edit();
        		interval = 0;
        		editor.putInt(APP_PREFERENCES_INTERVAL, interval);        		
        	}
        	if (mSettings.contains(APP_PREFERENCES_DELTA)) {
        		delta = mSettings.getInt(APP_PREFERENCES_DELTA, 0);
        	} else {
        		Editor editor = mSettings.edit();
        		delta = 0;
        		editor.putInt(APP_PREFERENCES_DELTA, delta);
        	}
        	if (mSettings.contains(APP_PREFERENCES_REFRESHTYPE)) {
        		refreshType = mSettings.getInt(APP_PREFERENCES_REFRESHTYPE, 0);
        	} else {
        		Editor editor = mSettings.edit();
        		refreshType = 0;
        		editor.putInt(APP_PREFERENCES_REFRESHTYPE, refreshType);        		
        	}
        	if (mSettings.contains(APP_PREFERENCES_TOSEND)) {
        		toSend = mSettings.getBoolean(APP_PREFERENCES_TOSEND, true);
        	} else {
        		Editor editor = mSettings.edit();
        		toSend = true;
        		editor.putBoolean(APP_PREFERENCES_TOSEND, toSend);         		
        	}
		MainFragment mainFragment = new MainFragment();
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.fragment_container, mainFragment);
		fragmentTransaction.commit();
		

        br = new NmeaBroadcastReceiver();
        registerReceiver(br, new IntentFilter(FService.NMEA_BROADCAST));
	    Intent i=new Intent(this, FService.class);
	    startService(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onExitClicked(){
	    stopService(new Intent(this, FService.class));
        finish();
        System.exit(0);		
	}
	
	public void onStatusClicked(){
		StatusFragment statusFragment = new StatusFragment();
		Bundle data = new Bundle();
		data.putBoolean(statusFragment.ARG_ISSTARTED, isStarted);
		statusFragment.setArguments(data);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, statusFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	public void onTasksClicked(){
		TasksFragment tasksFragment = new TasksFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, tasksFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	public void onGPSClicked(){
		GPSFragment gpsFragment = new GPSFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, gpsFragment, "GPS_FRAGMENT");
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	public void onSettingsClicked(){
		SettingsFragment settingsFragment = new SettingsFragment();
		Bundle data = new Bundle();
		data.putString(ARG_LOGIN, login);
		data.putString(ARG_ADDRESS,address);
		data.putInt(APP_PREFERENCES_REFRESHTYPE, refreshType);
		data.putInt(ARG_INTERVAL, interval);
		data.putInt(ARG_DELTA, delta);
		data.putBoolean(ARG_ISSTARTED, isStarted);
		data.putBoolean(ARG_TOSEND, toSend);
		settingsFragment.setArguments(data);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, settingsFragment, "SETTINGS_FRAGMENT");
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	public void onSaveClicked() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		SettingsFragment fragment = (SettingsFragment)fragmentManager.findFragmentByTag("SETTINGS_FRAGMENT");
		View v = fragment.getView();
		EditText loginEdit = (EditText) v.findViewById(R.id.loginEdit);
		EditText addressEdit = (EditText) v.findViewById(R.id.AddressEdit);
		RadioGroup radioGroupSendType = (RadioGroup) v.findViewById(R.id.radioGroupSendType);
		Spinner intervalsList = (Spinner) v.findViewById(R.id.IntervalsList);
		Spinner deltasList = (Spinner) v.findViewById(R.id.DeltasList);
		CheckBox checkBoxSendData = (CheckBox) v.findViewById(R.id.checkBoxSendData);
		login = loginEdit.getText().toString();
		address = addressEdit.getText().toString();
		if (radioGroupSendType.getCheckedRadioButtonId() == R.id.radioOnDistance){
				refreshType = 0;
			} else {
				refreshType = 1;
			}
		interval = intervalsList.getSelectedItemPosition();
		delta = deltasList.getSelectedItemPosition();
		toSend = checkBoxSendData.isChecked();
		
		Editor editor = mSettings.edit();
		editor.putString(APP_PREFERENCES_LOGIN, login);
		editor.putString(APP_PREFERENCES_ADDRESS, address);
		editor.putInt(APP_PREFERENCES_INTERVAL, interval);
		editor.putInt(APP_PREFERENCES_DELTA, delta);
		editor.putBoolean(APP_PREFERENCES_TOSEND, toSend);
		editor.putInt(APP_PREFERENCES_REFRESHTYPE, refreshType);
		editor.commit();
		fragmentManager.popBackStack();
	}
	
	public void onStartClicked() {
		isStarted = true;
        Intent i = new Intent(this, FService.class);
        Bundle data = new Bundle();
            data.putString(ARG_LOGIN, login);
            data.putString(ARG_ADDRESS,address);
            data.putInt(APP_PREFERENCES_REFRESHTYPE, refreshType);
            data.putInt(ARG_INTERVAL, interval);
            data.putInt(ARG_DELTA, delta);
            data.putBoolean(ARG_ISSTARTED, isStarted);
            data.putBoolean(ARG_TOSEND, toSend);
            data.putString(FService.NMEA_ACTION,"start");
        i.putExtras(data);
        startService(i);

	}
	public void onStopClicked() {
		isStarted = false;
        Intent i=new Intent(this, FService.class);
        i.putExtra(FService.NMEA_ACTION,"stop");
        startService(i);
		if (refreshType == 1) {handler.removeCallbacks(runnable);}
	}
	public void OnCheckedChangeListenerCallback(RadioGroup group, int checkedId){
		//Log.v("checkedId", "call");
		FragmentManager fragmentManager = getSupportFragmentManager();
		SettingsFragment fragment = (SettingsFragment)fragmentManager.findFragmentByTag("SETTINGS_FRAGMENT");
		View v =  fragment.getView();
		Spinner IntervalsList = (Spinner) v.findViewById(R.id.IntervalsList);
		Spinner DeltasList = (Spinner) v.findViewById(R.id.DeltasList);
		if (checkedId == R.id.radioOnDistance) {
			IntervalsList.setVisibility(View.INVISIBLE);
			DeltasList.setVisibility(View.VISIBLE);
		} else {
			DeltasList.setVisibility(View.INVISIBLE);
			IntervalsList.setVisibility(View.VISIBLE);
		}
	}

    public class NmeaBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)//this method receives broadcast messages. Be sure to modify AndroidManifest.xml file in order to enable message receiving
        {
            Bundle bundle = intent.getExtras();
            FragmentManager fragmentManager = getSupportFragmentManager();
            GPSFragment fragment = (GPSFragment)fragmentManager.findFragmentByTag("GPS_FRAGMENT");

            if(fragment != null && fragment.isResumed()) {
                if (bundle != null && (bundle.getString("sentenceIdentifier").equals("$GPGGA") || bundle.getString("sentenceIdentifier").equals("$GLGGA"))){
                    View v = fragment.getView();
                    textLatitude = (TextView) v.findViewById(R.id.textLatitude);
                    textLongitude = (TextView) v.findViewById(R.id.textLongitude);
                    textRecieved = (TextView) v.findViewById(R.id.textRecieved);
                    textDelta = (TextView) v.findViewById(R.id.textDelta);

                    textLatitude.setText("Широта: " + String.format("%.5f", bundle.getDouble("latitude")).replace(",","."));
                    textLongitude.setText("Долгота: " + String.format("%.5f", bundle.getDouble("longitude")).replace(",","."));
                    textRecieved.setText("Принято: " + bundle.getString("Recieved"));
                    textDelta.setText("Дельта: "+String.valueOf(bundle.getLong("distanceDelta")));
                    if (bundle.getString("Sent")!=null){
                        textSent = (TextView) v.findViewById(R.id.textSent);
                        textSent.setText("Отправлено: " + bundle.getString("Sent"));
                    }
                } else if (bundle != null && bundle.getString("sentenceIdentifier").equals("$GPGSV")){
                    textInView = (TextView) fragment.getView().findViewById(R.id.textInView);
                    textInView.setText("В зоне видимости: " + bundle.getInt("inView"));
                } else if (bundle != null && bundle.getString("sentenceIdentifier").equals("$GPGSA")){
                    textUsed = (TextView) fragment.getView().findViewById(R.id.textUsed);
                    textUsed.setText("Используется: " +  bundle.getInt("used"));
                }
            }
         }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        //Log.v("nmea","Main onSaveInstanceState");
        savedInstanceState.putBoolean(ARG_ISSTARTED, isStarted);
        // etc.
    }
    @Override
    public void onStop(){
        super.onStop();
        //Log.v("nmea","Main onStop");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        //Log.v("nmea", "Main onDestroy");
    }
}
