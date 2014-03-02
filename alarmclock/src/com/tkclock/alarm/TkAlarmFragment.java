package com.tkclock.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.facebook.SessionState;
import com.tkclock.adapters.TkDbAlarmMng;
import com.tkclock.adapters.TkDbAlarmMng.TkDbAlarm;
import com.tkclock.dashboard.R;
import com.tkclock.models.TkFacebook;
import com.tkclock.utils.DateTimeUtils;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class TkAlarmFragment extends Fragment implements TkFacebook.OnSessionStateChange {
	LayoutInflater mInflater;
	LinearLayout mAlarmContainer;
	TkDbAlarmMng mDbManager;
	public static String ALARM_TAG = "Alarm tab";
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mInflater = inflater;
		View view = mInflater.inflate(R.layout.alarm_frag_layout, container, false);
		mAlarmContainer = (LinearLayout) view.findViewById(R.id.alarm_container);
		mDbManager = new TkDbAlarmMng(getActivity());
		
		ImageView btnAdd = (ImageView) view.findViewById(R.id.alarm_new);
		
		List<TkDbAlarm> alarms = loadAlarm();
		
		for(int i = 0; i < alarms.size(); i++) {
			TkDbAlarm alarm = alarms.get(i);
			displayAlarm(alarm);
		}
		
		btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimePickerDialog newTimerPicker = createTimePicker();
				newTimerPicker.show();
			}
		});
		
		return view;
	}
	
	private void displayAlarm(TkDbAlarm alarm) {
		View alarm_content = mInflater.inflate(R.layout.alarm_info, mAlarmContainer, false);
		View seperator = mInflater.inflate(R.layout.layout_seperator, mAlarmContainer, false);
		
		final CheckBox check_enable = (CheckBox) alarm_content.findViewById(R.id.alarm_enable);
		final CheckBox check_repeat = (CheckBox) alarm_content.findViewById(R.id.alarm_repeat);
		final LinearLayout repeat_opt = (LinearLayout) alarm_content.findViewById(R.id.repeat_opt);
		ImageView btnDel = (ImageView) alarm_content.findViewById(R.id.alarm_delete);
		TextView alarm_time = (TextView) alarm_content.findViewById(R.id.alarm_time);
		
		alarm_time.setText(DateTimeUtils.date2Time(alarm.m_alarmtime));
		check_enable.setChecked(alarm.m_enable);
		check_repeat.setChecked(alarm.m_repeat);
		repeat_opt.setVisibility(alarm.m_repeat ? View.VISIBLE : View.GONE);
		
		check_repeat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				repeat_opt.setVisibility(checked ? View.VISIBLE : View.GONE);
			}
		});
		
		check_enable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

			}
		});
		
		btnDel.setTag(alarm);
		
		btnDel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 1. Remove in database
				mDbManager.open();
				TkDbAlarm alarm = (TkDbAlarm) v.getTag();
				boolean result = mDbManager.deleteAlarm(alarm.m_id);
				mDbManager.close();
				
				// 2. Remove in GUI
				if(result) {
					RelativeLayout alarm_content_view = (RelativeLayout) v.getParent();
					int index = mAlarmContainer.indexOfChild(alarm_content_view.findViewById(R.id.alarm_content));
					mAlarmContainer.removeViewAt(index + 1);
					mAlarmContainer.removeViewAt(index);
				}
			}
		});
		
		mAlarmContainer.addView(alarm_content);
		mAlarmContainer.addView(seperator);		
	}
	private List<TkDbAlarm> loadAlarm() {
		mDbManager.open();
		List<TkDbAlarm> alarms = mDbManager.getAlarms(TkDbAlarmMng.DEFAULT_OPTION);
		mDbManager.close();
		
		return alarms;
	}
	
	private void addNewAlarm(Date date) {
		Log.d(ALARM_TAG, DateTimeUtils.date2Str(date));
		
		// 1. Save new alarm to database
		mDbManager.open();
		TkDbAlarm alarm = new TkDbAlarm();
		alarm.m_repeat = true;
		alarm.m_enable = false;
		alarm.m_alarmtime = date;
		
		long alarmId = mDbManager.createAlarm(alarm);
		
		// 2. Register new alarm to Android AlarmManager
		new TkAlarmManager(getActivity()).setReminder(alarmId, date);
		// 3. Add new alarm to GUI
		// TODO: sort alarm by time
		displayAlarm(alarm);
	}
	
	private TimePickerDialog createTimePicker() {
		final Calendar calendar = Calendar.getInstance();
		TkTimePickerDialog timePicker = new TkTimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.SECOND, 0);
				addNewAlarm(calendar.getTime());
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
		return timePicker;
	}

	@Override
	public void onSessionStateChange(SessionState state) {
		// TODO Auto-generated method stub
		
	}
}
