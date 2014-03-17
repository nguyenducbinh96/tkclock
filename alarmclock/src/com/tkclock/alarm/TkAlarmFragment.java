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

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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

public class TkAlarmFragment extends Fragment 
implements TkFacebook.OnSessionStateChange, View.OnClickListener {
	LayoutInflater mInflater;
	LinearLayout mAlarmContainer;
	TkDbAlarmMng mDbManager;
	public static String ALARM_TAG = "Alarm tab";
	private static int PICK_RINGTONE = 1;
	private static String ALARM_ID = "alarm_id";
	List<TkDbAlarm> m_alarms = null;
	TkDbAlarm m_cur_setting_alarm = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mInflater = inflater;
		View view = mInflater.inflate(R.layout.alarm_frag_layout, container, false);
		mAlarmContainer = (LinearLayout) view.findViewById(R.id.alarm_container);
		mDbManager = new TkDbAlarmMng(getActivity());
		
		ImageView btnAdd = (ImageView) view.findViewById(R.id.alarm_new);
		
		List<TkDbAlarm> m_alarms = loadAlarm();
		
		for(int i = 0; i < m_alarms.size(); i++) {
			TkDbAlarm alarm = m_alarms.get(i);
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
	
	private void displayAlarm(final TkDbAlarm alarm) {
		View alarm_content = mInflater.inflate(R.layout.alarm_info, mAlarmContainer, false);
		int id = Resources.getSystem().getIdentifier("btn_check_holo_dark", "drawable", "android");
		((CheckBox) alarm_content.findViewById(R.id.alarm_enable)).setButtonDrawable(id);
		((CheckBox) alarm_content.findViewById(R.id.alarm_repeat)).setButtonDrawable(id);
		View seperator = mInflater.inflate(R.layout.layout_seperator, mAlarmContainer, false);
		
		final CheckBox check_enable = (CheckBox) alarm_content.findViewById(R.id.alarm_enable);
		final CheckBox check_repeat = (CheckBox) alarm_content.findViewById(R.id.alarm_repeat);
		final LinearLayout repeat_opt = (LinearLayout) alarm_content.findViewById(R.id.repeat_opt);
		ImageView btnDel = (ImageView) alarm_content.findViewById(R.id.alarm_delete);
		ImageView btnRingtone = (ImageView) alarm_content.findViewById(R.id.alarm_ringtone);
		TextView alarm_time = (TextView) alarm_content.findViewById(R.id.alarm_time);
		
		alarm_time.setText(DateTimeUtils.time2Str(alarm.m_alarmtime));
		check_enable.setChecked(alarm.m_enable);
		check_repeat.setChecked(alarm.m_repeat);
		repeat_opt.setVisibility(alarm.m_repeat ? View.VISIBLE : View.GONE);
		
		alarm_time.setTag(alarm);
		btnDel.setTag(alarm);
		btnRingtone.setTag(alarm);
		
		alarm_time.setOnClickListener(this);
		btnDel.setOnClickListener(this);
		btnRingtone.setOnClickListener(this);
		
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
		mDbManager.close();
		
		// 2. Register new alarm to Android AlarmManager
		TkAlarmManager.setReminder(getActivity(), alarmId, date);
		// 3. Add new alarm to GUI
		// TODO: sort alarm by time
		displayAlarm(alarm);
	}
	
	private void updateAlarm(TkDbAlarm alarm, boolean updateAlarm) {
		mDbManager.open();
		mDbManager.updateAlarm(alarm);
		mDbManager.close();
		if (updateAlarm) {
			TkAlarmManager.setReminder(getActivity(), alarm.m_id, alarm.m_alarmtime);
		}
	}
	
	private TimePickerDialog createTimePicker() {
		final Calendar calendar = Calendar.getInstance();
		TkTimePickerDialog timePicker = new TkTimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Calendar curTime = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.SECOND, 0);
				
				if(curTime.get(Calendar.HOUR_OF_DAY) > calendar.get(Calendar.HOUR_OF_DAY) || 
						curTime.get(Calendar.MINUTE) > calendar.get(Calendar.MINUTE)) {
					calendar.set(Calendar.DAY_OF_MONTH, curTime.get(Calendar.DAY_OF_MONTH) + 1);
				}
				
				addNewAlarm(calendar.getTime());
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
		return timePicker;
	}

	@Override
	public void onSessionStateChange(SessionState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == PICK_RINGTONE) {
			if (resultCode == Activity.RESULT_OK) {
				Uri path = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				
				if(m_cur_setting_alarm != null) {
					m_cur_setting_alarm.m_ringtone = path.toString();
					updateAlarm(m_cur_setting_alarm, false);
				}
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onClick(final View v) {
		final TkDbAlarm alarm = (TkDbAlarm) v.getTag();
		
		switch(v.getId()) {
		
		case R.id.alarm_time:
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(alarm.m_alarmtime);
			TkTimePickerDialog timePicker = new TkTimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
				
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					// TODO Auto-generated method stub
					calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					calendar.set(Calendar.MINUTE, minute);
					calendar.set(Calendar.SECOND, 0);
					alarm.m_alarmtime = calendar.getTime();
					((TextView) v).setText(DateTimeUtils.time2Str(calendar.getTime()));
					
					updateAlarm(alarm, true);
				}
			}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
			
			timePicker.show();
			
			break;
			
		case R.id.alarm_delete:
			// 1. Remove in database
			mDbManager.open();
			boolean result = mDbManager.deleteAlarm(alarm.m_id);
			mDbManager.close();
			
			// 2. Remove in GUI
			if(result) {
				RelativeLayout alarm_content_view = (RelativeLayout) v.getParent();
				int index = mAlarmContainer.indexOfChild(alarm_content_view.findViewById(R.id.alarm_content));
				mAlarmContainer.removeViewAt(index + 1);
				mAlarmContainer.removeViewAt(index);
			}
			
			break;
			
		case R.id.alarm_ringtone:
			m_cur_setting_alarm = alarm;
			Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
	                RingtoneManager.TYPE_ALARM);
	        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
	        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
	        i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(alarm.m_ringtone));
//	        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
	        i.putExtra(ALARM_ID, alarm.m_id);
			startActivityForResult(i, PICK_RINGTONE);
			
			break;
		}
	}
}
