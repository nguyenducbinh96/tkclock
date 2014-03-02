package com.tkclock.onalarm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public abstract class WakeReminderIntentService extends IntentService {
	abstract void doReminderWork(Intent intent);
	public static final String LOCK_NAME_STATIC="com.TaskReminder.Static";
	private static PowerManager.WakeLock lockStatic = null;
	public static void acquireStaticLock(Context context) {
		getLock(context);
		lockStatic.acquire();
	}
	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {    
			PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
			lockStatic=mgr.newWakeLock(	PowerManager.FULL_WAKE_LOCK | 
										PowerManager.ACQUIRE_CAUSES_WAKEUP ,LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
        }
		return(lockStatic);
	}
    
	public WakeReminderIntentService(String name) {
		super(name);
	}
	@Override
	final protected void onHandleIntent(Intent intent) {
		try {
			doReminderWork(intent);
		} finally {
			getLock(this).release();
		}
	}
	
	public static void acquire(Context ctx) {
        if (lockStatic != null) lockStatic.release();

        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        lockStatic = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "test");
        lockStatic.acquire();
    }

    public static void release() {
        if (lockStatic != null) 
        	lockStatic.release(); 
        
        lockStatic = null;
    }
}
