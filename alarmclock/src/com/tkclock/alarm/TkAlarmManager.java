package com.tkclock.alarm;

import java.util.Calendar;
import java.util.Date;

import com.tkclock.adapters.TkDbAlarmMng;
import com.tkclock.onalarm.OnAlarmReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TkAlarmManager {
	private Context mContext;
	private AlarmManager mAlarmManager;
	
	public TkAlarmManager(Context context) {
		mContext = context; 
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}
	public void setReminder(Long alarmId, Date when) {
		Intent i = new Intent(mContext, OnAlarmReceiver.class);
		i.putExtra(TkDbAlarmMng.KEY_ROWID, alarmId);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);
		
		Log.d("AlarmManager", "new alarm registered: " + alarmId);
		mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTime(), pi);
	}
}
