package com.example.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class StatusFragment extends Fragment {
	OnStatusButtonClickListener mCallback;
	private Button buttonStart;
	private Button buttonStop;
    public Boolean isStarted = null;
	
	final String ARG_ISSTARTED = "isRunning";

	public interface OnStatusButtonClickListener {
		public void onStartClicked();
		public void onStopClicked();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
        Log.v("nmea", "onCrateView");
		View view = inflater.inflate(R.layout.status_fragment, container, false);
		buttonStart = (Button) view.findViewById(R.id.buttonStart);
		buttonStop = (Button) view.findViewById(R.id.buttonStop);
		buttonStart.setOnClickListener(new OnClickListener() {
	    	 @Override
	    	 public void onClick(View v) {
                 isStarted = true;
	    		 buttonStart.setVisibility(v.GONE);
	    		 buttonStop.setVisibility(v.VISIBLE);
	    		 mCallback.onStartClicked();
	    	 }
	     });
		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                isStarted = false;
				buttonStop.setVisibility(v.GONE);
				buttonStart.setVisibility(v.VISIBLE);
				mCallback.onStopClicked();
			}
		});
		return view;
	}
	
	@Override
    
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
        	View v = getView();
        	Button buttonStart = (Button) getView().findViewById(R.id.buttonStart);
        	Button buttonStop = (Button) getView().findViewById(R.id.buttonStop);
            //Log.v("nmea",""+args.getBoolean(ARG_ISSTARTED));
            if (isStarted == null) {isStarted = args.getBoolean(ARG_ISSTARTED);}
        	if (isStarted) {
	    		 buttonStart.setVisibility(v.GONE);
	    		 buttonStop.setVisibility(v.VISIBLE);       		
        	} else {
				buttonStop.setVisibility(v.GONE);
				buttonStart.setVisibility(v.VISIBLE);        		
        	}
        }
    }	
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	mCallback = (OnStatusButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnStatusButtonClickListener");
        }
    }
   /* @Override
    public void onPause(){
        super.onPause();
        Log.v("nmea", "Fragment onPause");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.v("nmea", "Fragment onStop");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v("nmea", "Fragment onDestroy");
    }    */
    @Override
    public void onDetach(){
        super.onDetach();
        isStarted = null;
    }
}
