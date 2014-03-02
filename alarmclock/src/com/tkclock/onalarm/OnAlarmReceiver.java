package com.tkclock.onalarm;

import com.tkclock.adapters.TkDbAlarmMng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import static com.tkclock.onalarm.TkAlarmNotify.NOTIFY_ALARM_ID;

public class OnAlarmReceiver extends BroadcastReceiver{
	
	private static String TAG = "Alarm...";
	@Override
    public void onReceive(Context context, Intent intent) {
		Bundle data = intent.getExtras();
		
//		long alarmId = intent.getExtras().getLong(TkDbAlarmMng.KEY_ROWID);
        WakeReminderIntentService.acquireStaticLock(context);
        
        Intent i=new Intent(context, TkAlarmNotify.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.putExtra(NOTIFY_ALARM_ID, alarmId);
        i.putExtras(data);
        
        context.startActivity(i);

        WakeReminderIntentService.release();
    }
}
