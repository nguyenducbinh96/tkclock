package com.tkclock.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tkclock.models.TkCalendar;
import com.tkclock.utils.StringUtils;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.tkclock.models.TkTextToSpeech.TTS_SPEED_NORMAL;

public class TkApplication extends Application {
	public static int MSG_TYPE_FB_NOTIFICATION = 1;
	public static int MSG_TYPE_GMAIL_NOTIFICATION = 2;
	public static int MSG_TYPE_TTS_NOTIFICATION = 3;
	public static int MSG_RESULT_OK = 1;
	public static int MSG_RESULT_NG = 0;
	
	public static String NOTIFICATION_TYPE_FB = "facebook";
	public static String NOTIFICATION_TYPE_MAIL = "mail";
	public static String NOTIFICATION_TYPE_NEWS = "news";
	public static String NOTIFICATION_TYPE_WEATHER = "weather";

	private boolean mFbLoginState = false;
	
	/* Declare all share preferences here */
	public static String KEY_PREFERENCE_FACEBOOK_STT = "fb_state";
	
	public static String KEY_PREFERENCE_SILENT_TIME = "silent_time";
	public static String KEY_PREFERENCE_SNOOZE_TIME = "snooze_time";
	public static String KEY_PREFERENCE_RINGTONE_TIME = "ringtone_time";
	
	public static String KEY_PREFERENCE_CAL_ACCS = "selected_accs";
	
	public static String KEY_PREFERENCE_NOTIFY_ORDER = "notifcation_order";
	
	public static String KEY_PREFERENCE_TTS_SPEED = "tts_speed";
	
	/* Declare all default setting value here */
	private int default_silent_time = 10; /* 10m */
	private int default_snooze_time = 10; /* 10m */
	private int default_ringtone_time = 1; /* 10m */
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Load default configuration for the first time
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs.edit();
		
		if(!prefs.contains(KEY_PREFERENCE_FACEBOOK_STT)) {
			editor.putBoolean(KEY_PREFERENCE_FACEBOOK_STT, mFbLoginState);
		}
		
		if(!prefs.contains(KEY_PREFERENCE_SILENT_TIME)) {
			editor.putInt(KEY_PREFERENCE_SILENT_TIME, default_silent_time);
		}
		
		if(!prefs.contains(KEY_PREFERENCE_SNOOZE_TIME)) {
			editor.putInt(KEY_PREFERENCE_SNOOZE_TIME, default_snooze_time);
		}
		
		if(!prefs.contains(KEY_PREFERENCE_RINGTONE_TIME)) {
			editor.putInt(KEY_PREFERENCE_RINGTONE_TIME, default_ringtone_time);
		}
		
		// Init notification order
		String[] order = {NOTIFICATION_TYPE_FB, NOTIFICATION_TYPE_MAIL, NOTIFICATION_TYPE_NEWS};
		List<String> notification_orderList = Arrays.asList(order);
		if(!prefs.contains(KEY_PREFERENCE_NOTIFY_ORDER)) {
			editor.putString(KEY_PREFERENCE_NOTIFY_ORDER, StringUtils.list2String(notification_orderList, null));
		}
		
		TkCalendar calendar = new TkCalendar(getApplicationContext());
		String default_accs = StringUtils.list2String(calendar.getAllAccounts("com.google"), ";");
		if(!prefs.contains(KEY_PREFERENCE_CAL_ACCS)) {
			editor.putString(KEY_PREFERENCE_CAL_ACCS, default_accs);
		}
		
		if(!prefs.contains(KEY_PREFERENCE_TTS_SPEED)) {
			editor.putFloat(KEY_PREFERENCE_TTS_SPEED, TTS_SPEED_NORMAL);
		}
		
		editor.commit();
	}

	public void setFbLoginState(boolean state) {
		mFbLoginState = state;
	}
	
	public boolean isFbLogin() {
		return mFbLoginState;
	}
}
