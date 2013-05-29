package com.example.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;



public class MainFragment extends Fragment {
    OnButtonClickListener mCallback;

    public interface OnButtonClickListener {
         public void onExitClicked();
         public void onStatusClicked();
         public void onTasksClicked();
         public void onGPSClicked();
         public void onSettingsClicked();
    }
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
             View v = inflater.inflate(R.layout.main_fragment, container, false);
     Button buttonExit = (Button)v.findViewById(R.id.buttonExit);
     buttonExit.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
        	 mCallback.onExitClicked();
         }
      });
     Button buttonStatus = (Button)v.findViewById(R.id.buttonStatus);
     buttonStatus.setOnClickListener(new OnClickListener() {
    	 @Override
    	 public void onClick(View v) {
    		 mCallback.onStatusClicked();
    	 }
     });
     Button buttonTasks = (Button)v.findViewById(R.id.buttonTasks);
     buttonTasks.setOnClickListener(new OnClickListener() {
    	 @Override
    	 public void onClick(View v) {
    		 mCallback.onTasksClicked();
    	 }
     });
     Button buttonGPS = (Button)v.findViewById(R.id.buttonGPS);
     buttonGPS.setOnClickListener(new OnClickListener() {
    	 @Override
    	 public void onClick(View v) {
    		 mCallback.onGPSClicked();
    	 }
     });
     Button buttonSettings = (Button)v.findViewById(R.id.buttonSettings);
     buttonSettings.setOnClickListener(new OnClickListener() {
    	 @Override
    	 public void onClick(View v) {
    		 mCallback.onSettingsClicked();
    	 }
     });
             return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

    }	
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnButtonClickListener");
        }
    }

}
