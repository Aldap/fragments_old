package com.example.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
	OnSettingsButtonClickListener mCallback;
	
	public interface OnSettingsButtonClickListener {
		public void onSaveClicked();
		public void OnCheckedChangeListenerCallback(RadioGroup group, int checkedId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.settings_fragment, container, false);
		Button buttonSave = (Button) view.findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(new OnClickListener() {
	    	 @Override
	    	 public void onClick(View v) {
	    		 mCallback.onSaveClicked();
	    	 }
	     });
		RadioGroup radioGroupSendType = (RadioGroup) view.findViewById(R.id.radioGroupSendType);
		radioGroupSendType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId){
				mCallback.OnCheckedChangeListenerCallback(group, checkedId);
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
        	EditText loginEdit = (EditText) v.findViewById(R.id.loginEdit);
    		 EditText addressEdit = (EditText)v.findViewById(R.id.AddressEdit);
    		 RadioGroup radioGroupSendType = (RadioGroup) v.findViewById(R.id.radioGroupSendType);
    		  Spinner intervalsList = (Spinner) v.findViewById(R.id.IntervalsList);
    		  Spinner deltasList = (Spinner) v.findViewById(R.id.DeltasList);
    		   TextView textNotification = (TextView) v.findViewById(R.id.textNotification);
    		    Button buttonSave = (Button) v.findViewById(R.id.buttonSave);
    		     CheckBox checkBoxSendData = (CheckBox) v.findViewById(R.id.checkBoxSendData);
    		loginEdit.setText(args.getString(Main.ARG_LOGIN));
    		addressEdit.setText(args.getString(Main.ARG_ADDRESS));
    		if (args.getInt(Main.ARG_REFRESHTYPE) == 0) {
    			radioGroupSendType.check(R.id.radioOnDistance);
    		} else {
    			radioGroupSendType.check(R.id.radioOnTimer);
    		}
    		intervalsList.setSelection(args.getInt(Main.ARG_INTERVAL));
    		deltasList.setSelection(args.getInt(Main.ARG_DELTA));
    		checkBoxSendData.setChecked(args.getBoolean(Main.ARG_TOSEND));
    		if (args.getBoolean(Main.ARG_ISSTARTED)){
    			textNotification.setVisibility(v.VISIBLE);
    		   buttonSave.setClickable(false);
    		} else {
    			textNotification.setVisibility(v.INVISIBLE);
     		   buttonSave.setClickable(true);    			
    		}
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	mCallback = (OnSettingsButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnButtonClickListener");
        }
    }
    
}
