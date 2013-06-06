package com.example.fragments;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

public class FService extends Service {
	private boolean isRunning = false;
    public LocationManager locManager;
    public LocationListener locListener;
    public GpsStatus.NmeaListener nmeaListener;
    public static final String NMEA = "com.example.service0.CustomService.nmea";
    public static final String NMEA_BROADCAST = "com.example.service0.CustomService.nmea_broadcast";
    public static final String NMEA_ACTION = "com.example.service0.CustomService.nmea_action";

    private HttpClient httpClient;
    public Handler handler;
    public Runnable runnable;

    public String login;
    public String address;
    public int interval;
    public int delta;
    public int refreshType; //0 - distance, 1 - timer
    public boolean toSend;
    boolean timerCycle = false;
    boolean isStarted = false;

    static double earthRadius = 6371000.0; // meters
    //double lastLatitude = 61.663856; //North Pole
    //double lastLongitude = 50.815946;
    double lastLatitude = 90; //North Pole
    double lastLongitude = 0;

    @Override
    public void onCreate(){
        super.onCreate();
        httpClient = CustomHttpClient.getHttpClient();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                timerCycle = true;
                handler.removeCallbacks(runnable);
                handler.postDelayed(this, (10+5*interval)*1000);
            }
        };

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location loc) {}
            @Override
            public void onProviderDisabled(String provider) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onStatusChanged(String provider, int status,Bundle extras) {}
        };
        nmeaListener = new GpsStatus.NmeaListener() {
            public void onNmeaReceived(long timestamp, String nmea) {

                String[] results = nmea.split(",|\\*");
                Bundle bundle = new Bundle();
                bundle.putString("sentenceIdentifier", results[0]);
                try {
                    if (results[0].equals("$GPGGA") || results[0].equals("$GLGGA")) {
                        if (Integer.parseInt(results[6]) > 0) {
                            DoubleCoordinates coordinates = NmeaToDobleCoordinates(results[2],results[3],results[4],results[5]);
                            long distanceDelta = (long)Math.floor(earthRadius*Math.acos(Math.sin(lastLatitude*Math.PI/180)*Math.sin(coordinates.latitude*Math.PI/180) + Math.cos(lastLatitude*Math.PI/180)*Math.cos(coordinates.latitude*Math.PI/180)*Math.cos(lastLongitude*Math.PI/180 - coordinates.longitude*Math.PI/180)));

                                bundle.putLong("distanceDelta", distanceDelta);
                                bundle.putDouble("latitude", coordinates.latitude);
                                bundle.putDouble("longitude", coordinates.longitude);
                                bundle.putString("Recieved", results[1]);
                                if ((timerCycle && (refreshType == 1)) || ((distanceDelta > (5 + delta * 5)) && (refreshType == 0))){
                                    bundle.putString("Sent", results[1]);
                                }

                            if ((timerCycle && (refreshType == 1)) || ((distanceDelta > (5 + delta * 5)) && (refreshType == 0))){
                                if (toSend) {new HttpAsyncTask().execute("http://" + address + "/tracker.php?key=" + login + "&x=" + String.format("%.5f", coordinates.longitude).replace(",",".")  + "&y=" + String.format("%.5f", coordinates.latitude).replace(",","."));}
                                lastLatitude = coordinates.latitude;
                                lastLongitude = coordinates.longitude;
                                if (refreshType == 1) {
                                    timerCycle = false;
                                    handler.postDelayed(runnable, (10 + 5 * interval)*1000);
                                }
                            }
                        } else {
                            bundle.putString("sentenceIdentifier", null);
                        }
                    } else if (results[0].equals("$GPGSV")) {
                            if (results[3]!= null && !results[3].isEmpty()) {
                                bundle.putInt("inView", Integer.parseInt(results[3]));
                            } else {bundle.putInt("inView", 0); }
                    } else if (results[0].equals("$GPGSA")) {
                            int i = 0;
                            while (( i < 12 ) && !results[3 + i].equals("")) {i++;}
                            bundle.putInt("used", i);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                  Intent intent = new Intent(NMEA_BROADCAST);
                 intent.putExtras(bundle);
                sendBroadcast(intent);
            }
        };

    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

	  if (!isRunning) {
	      isRunning=true;
	      Intent i = new Intent(this, Main.class);
	       i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
	      NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
	       builder.setSmallIcon(R.drawable.ic_launcher)
	              .setTicker("Application starting")
	              .setWhen(System.currentTimeMillis())
	              .setContentTitle("Fragments")
	              .setContentText("Running")
	              .setContentIntent(pi);
	      Notification note = builder.build();
	       note.flags|=Notification.FLAG_NO_CLEAR; 
	     startForeground(1337, note);
	    } else {
          if (intent.getExtras()!=null && intent.getExtras().getString(NMEA_ACTION).equals("start") && isStarted == false) {
              Bundle bundle = intent.getExtras();
              login = bundle.getString(Main.ARG_LOGIN);
              address = bundle.getString(Main.ARG_ADDRESS);
              refreshType = bundle.getInt(Main.APP_PREFERENCES_REFRESHTYPE);
              interval = bundle.getInt(Main.ARG_INTERVAL);
              delta = bundle.getInt(Main.ARG_DELTA);
              isStarted = bundle.getBoolean(Main.ARG_ISSTARTED);
              toSend = bundle.getBoolean(Main.ARG_TOSEND);
              if (refreshType == 1) {handler.postDelayed(runnable, (10+5*interval)*1000);}

              locManager.addNmeaListener(nmeaListener);
              locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locListener);
          } else if (intent.getExtras()!=null && intent.getExtras().getString(NMEA_ACTION).equals("stop") && isStarted == true){
              isStarted = false;
              locManager.removeUpdates(locListener);
              locManager.removeNmeaListener(nmeaListener);
          }
      }
	  return(START_NOT_STICKY);
	}

	@Override
	public void onDestroy() {
	  stop();
	}

	@Override
	public IBinder onBind(Intent intent) {
	  return(null);
	}

	private void stop() {
	  if (isRunning) {
	    isRunning=false;
          locManager.removeUpdates(locListener);
          locManager.removeNmeaListener(nmeaListener);
	    stopForeground(true);
	  }
	}

    public class DoubleCoordinates{
        double latitude = 0.0;
        double longitude = 0.0;
        public void SetLatitude(double lat){
            latitude = lat;
        }
        public void SetLongitude(double lon){
            longitude = lon;
        }
    }

    public DoubleCoordinates NmeaToDobleCoordinates(String lat, String NorS, String lon, String EorW){
        DoubleCoordinates dc = new DoubleCoordinates();
        double decLatFull = Double.parseDouble(lat.replace(",","."));
        long decLatDeg = (long)Math.floor(decLatFull/100.0);
        dc.SetLatitude(decLatDeg + (decLatFull - decLatDeg * 100.0) / 60.0);
        if (NorS.equals("S")) { dc.SetLatitude(-dc.latitude); }
        double decLonFull = Double.parseDouble(lon.replace(",","."));
        long decLonDeg = (long)Math.floor(decLonFull/100.0);
        dc.SetLongitude(decLonDeg + (decLonFull - decLonDeg * 100.0) / 60.0);
        if (EorW.equals("W")) { dc.SetLongitude(-dc.longitude); }
        return dc;
    }

    class HttpAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... url) {
            try{
                HttpGet request = new HttpGet(url[0]);
                String page = httpClient.execute(request, new BasicResponseHandler());
            }catch(Exception e){

                Log.e("page", "Error in http connection "+e.toString());

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

        @Override
        protected void onPostExecute(Void unused) {

        }

    }
}
