package com.tkclock.onalarm;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.tkclock.adapters.TkDbAlarmMng;
import com.tkclock.adapters.TkDbAlarmMng.TkDbAlarm;
import com.tkclock.utils.DateTimeUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import static com.tkclock.adapters.TkDbAlarmMng.KEY_ROWID;
import static com.tkclock.adapters.TkDbAlarmMng.KEY_DATE_TIME;

public class OnAlarmReceiver extends BroadcastReceiver{
	
	private static String TAG = "Alarm...";
	@Override
    public void onReceive(Context context, Intent intent) {
		Bundle data = intent.getExtras();
		
		// Check alarm id whether it was removed or not
		TkDbAlarmMng manager = new TkDbAlarmMng(context);
        manager.open();
        
        long alarmId = data.getLong(KEY_ROWID);
        Calendar calender = Calendar.getInstance();
        String time_cur = DateTimeUtils.time2Str(calender.getTime());
        Log.d(TAG, "New alarm fired: id="+ alarmId + " at " + time_cur);
        List<TkDbAlarm> alarms = manager.getAlarms(alarmId);
        manager.close();
        if(alarms.size() != 1) {
        	Log.d(TAG, "Alarm id is not exist or removed: " + alarmId);
        	return;
        } else {
        	String time_set = DateTimeUtils.time2Str(alarms.get(0).m_alarmtime);
        	if(time_set.equals(time_cur) == false) {
        		Log.d(TAG, "Alarm time changed: id=" + alarmId + ", old: " + time_cur + ", new: " + time_set);
            	return;
        	}
	        WakeReminderIntentService.acquireStaticLock(context);
	        
	        Intent i=new Intent(context, TkAlarmNotify.class);
	        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        i.putExtras(data);
	        
	        context.startActivity(i);
	
	        WakeReminderIntentService.release();
        }
    }
}
