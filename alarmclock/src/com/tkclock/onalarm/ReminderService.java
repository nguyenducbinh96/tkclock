package com.tkclock.onalarm;

import com.tkclock.adapters.TkDbAlarmMng;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ReminderService extends WakeReminderIntentService{
	public ReminderService() {
        super("ReminderService");
    }
	@Override
    void doReminderWork(Intent intent) {
         Long rowId = intent.getExtras().getLong(TkDbAlarmMng.KEY_ROWID);
         //  Status bar notification Code Goes here. 
         Log.d("Reminder Service", "create OK" + rowId.toString());
         Toast.makeText(this, "alarm wake up", Toast.LENGTH_LONG).show();
   }
}
