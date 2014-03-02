package com.tkclock.settings;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tkclock.dashboard.R;
import com.tkclock.dashboard.TkFragmentActivity;
import com.tkclock.models.TkFacebook;

import static com.tkclock.dashboard.TkApplication.KEY_PREFERENCE_FACEBOOK_STT;
import static com.tkclock.dashboard.TkApplication.KEY_PREFERENCE_SILENT_TIME;
import static com.tkclock.dashboard.TkApplication.KEY_PREFERENCE_SNOOZE_TIME;

import static com.tkclock.settings.SelectAccDialog.REQ_CODE;

public class TkSettings extends TkFragmentActivity 
	implements SelectAccDialog.NoticeDialogListener {
	
	SharedPreferences mPref;
	SharedPreferences.Editor mPrefEditor;
	
	EditText mSilent_time;
	EditText mSnooze_time;
	
	private static final String CALENDAR_ACCOUNT_SETTING = "account_setting"; 
	SelectAccDialog cal_select_acc_dialog;
	boolean show_dialog = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting_all);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mPrefEditor = mPref.edit();
		
		loadView();
	}
	private void loadView() {
		loadAlarm();
		loadFacebook();
		loadGmail();
		loadCalendar();
	}
	
	private void loadAlarm() {
		// Get pre-config value
		int silent_time = mPref.getInt(KEY_PREFERENCE_SILENT_TIME, 10);
		int snooze_time = mPref.getInt(KEY_PREFERENCE_SNOOZE_TIME, 10);
		
		mSilent_time = (EditText) findViewById(R.id.silent_time);
		mSnooze_time = (EditText) findViewById(R.id.snooze_time);
		
		mSilent_time.setText(Integer.toString(silent_time));
		mSnooze_time.setText(Integer.toString(snooze_time));
	}
	
	private void loadFacebook() {
		// TODO Auto-generated method stub
		boolean fb_state = TkFacebook.getLoginState();
		ToggleButton btnFbState = (ToggleButton) findViewById(R.id.fb_login);
		btnFbState.setChecked(fb_state);
	}
	
	private void loadGmail() {
		// TODO Auto-generated method stub
		
	}
	
	private void loadCalendar() {
		TextView select_acc = (TextView) findViewById(R.id.calendar_select_acc);
		
		select_acc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Open dialog for user to choose account
				cal_select_acc_dialog = new SelectAccDialog();
				cal_select_acc_dialog.show(getSupportFragmentManager(), CALENDAR_ACCOUNT_SETTING);
			}
		});
	}
	
	@Override
	public void finish() {
		super.finish();
		
		// Update new setting value before finish
		int silent_time = Integer.parseInt(mSilent_time.getText().toString());
		int snooze_time = Integer.parseInt(mSnooze_time.getText().toString());
		
		mPrefEditor.putInt(KEY_PREFERENCE_SILENT_TIME, silent_time);
		mPrefEditor.putInt(KEY_PREFERENCE_SNOOZE_TIME, snooze_time);
		
		mPrefEditor.commit();
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		boolean needDisableCalendar = ((SelectAccDialog)dialog).noAccSeleted();
		ToggleButton btnCalendarStt = (ToggleButton) findViewById(R.id.calendar_state);
		btnCalendarStt.setChecked(needDisableCalendar);
	}
	
	@Override
	protected void onResumeFragments() {
		if(show_dialog == true) {
			if (getSupportFragmentManager().findFragmentByTag(CALENDAR_ACCOUNT_SETTING) == null) {
				new SelectAccDialog().show(getSupportFragmentManager(), CALENDAR_ACCOUNT_SETTING);
			}
			show_dialog = false;
		}
	}
	@Override
	public void onDialogStop(boolean change_activity) {
		show_dialog = change_activity;
	}
}
