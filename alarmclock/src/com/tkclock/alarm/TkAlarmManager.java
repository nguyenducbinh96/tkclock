package com.tkclock.alarm;

import java.util.Calendar;
import java.util.Date;

import com.tkclock.adapters.TkDbAlarmMng;
import com.tkclock.onalarm.OnAlarmReceiver;
import com.tkclock.utils.DateTimeUtils;
import com.tkclock.utils.StringUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TkAlarmManager {

	public static void setReminder(Context context, Long alarmId, Date when) {
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, OnAlarmReceiver.class);
		String strDate = DateTimeUtils.time2Str(when);
		i.putExtra(TkDbAlarmMng.KEY_ROWID, alarmId);
		PendingIntent pi = PendingIntent.getBroadcast(context, (int)(long)alarmId, i, PendingIntent.FLAG_ONE_SHOT);
		
		Log.d("AlarmManager", "new alarm registered: " + alarmId + " " + strDate);
		alarmManager.set(AlarmManager.RTC_WAKEUP, when.getTime(), pi);
	}
}
