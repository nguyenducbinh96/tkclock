package com.tkclock.voice.notify_manager;

import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import com.tkclock.models.TkTextToSpeech;
import com.tkclock.utils.StringUtils;

import static com.tkclock.models.TkTextToSpeech.COMPLETE_NORMAL;
import static com.tkclock.models.TkTextToSpeech.COMPLETE_SKIPPED;
import static com.tkclock.models.TkTextToSpeech.COMPLETE_TERMINATED;

import static com.tkclock.dashboard.TkApplication.KEY_PREFERENCE_NOTIFY_ORDER;
import static com.tkclock.dashboard.TkApplication.NOTIFICATION_TYPE_FB;
import static com.tkclock.dashboard.TkApplication.NOTIFICATION_TYPE_MAIL;
import static com.tkclock.dashboard.TkApplication.NOTIFICATION_TYPE_NEWS;
import static com.tkclock.dashboard.TkApplication.NOTIFICATION_TYPE_WEATHER;

import static com.tkclock.models.TkTextToSpeech.TASK_PRIORITY_LOW;

public class TkNotificationSpeaker {
	private static String TAG = "NotificationSpeaker";
	private Object m_lock_setup = new Object(); // for synchronize purpose
	private static Integer SKIP_VALUE = Integer.valueOf(10000); // skip this value when setup
	private static int STATE_SPEAKING = 0;
	private static int STATE_STOPPED = 1;
	private static int STATE_PAUSED = 2;

	public static int DIRECTION_FW = 1;
	public static int DIRECTION_BW = -1;
	
	List<String> m_fb_notification;
	List<String> m_mail_notification;
	List<String> m_news_notification;
	List<String> m_weather_notification;
	
	/* 
	 * Use Integer class instead of int value type for reference purpose
	 *  It allows outside modules (voice commands) to control internal speak thread
	 */
	Integer m_fb_notification_index;
	Integer m_mail_notification_index;
	Integer m_news_notification_index;
	Integer m_weather_notification_index;
	
	Integer m_current_index;
	boolean m_repeat_flag;
	int m_speak_direction;
	private int m_state;
	
	SparseArray<String> m_notification_order;
	
	TkTextToSpeech mTTs;
	Context mContext;
	
	public TkNotificationSpeaker(Context context, TkTextToSpeech tts, 
			List<String> fb_notification, List<String> mail_notification,
			List<String> news_notification, List<String> weather_notification) {
		mContext = context;
		mTTs = tts;
		
		// Init notification info
		m_fb_notification = fb_notification;
		m_mail_notification = mail_notification;
		m_news_notification = news_notification;
		m_weather_notification = weather_notification;
		
		init_control_values();
		m_notification_order = new SparseArray<String>();
		
		// Read share preference to get notification order
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
		String order = prefs.getString(KEY_PREFERENCE_NOTIFY_ORDER, "");
		List<String> notification_orderList = StringUtils.SplitUsingToken(order, null);
		
		for(int i = 0; i < notification_orderList.size(); i++) {
			m_notification_order.put(i, notification_orderList.get(i));
		}	
	}
	
	private void init_control_values() {
		m_speak_direction = DIRECTION_FW;
		m_repeat_flag = false;
		m_state = STATE_STOPPED;
		
		m_fb_notification_index = Integer.valueOf(0);
		m_mail_notification_index = Integer.valueOf(0);
		m_news_notification_index = Integer.valueOf(0);
		m_weather_notification_index = Integer.valueOf(0);
	}
 	
	private int speakInfo(List<String> notification, Integer start_index) {
		if(start_index < 0 || start_index >= notification.size())
			return COMPLETE_NORMAL;
		
		int speakResult = 0;
		Integer notification_size = Integer.valueOf(notification.size());
		m_current_index = start_index;
		String sentence = "";
		for(; start_index >= 0 && m_current_index>= 0 && m_current_index < notification_size;) {
			// Lock to speak only
			synchronized (m_lock_setup) {
				Log.d("Speak", 	"current index: " + m_current_index + 
								" start_index: " + start_index + 
								" direction: " + m_speak_direction);
				sentence = notification.get(m_current_index);
				Log.d("NotificationSpeaker", sentence);
				speakResult = mTTs.speak(sentence);
				if(speakResult != COMPLETE_SKIPPED && 
						speakResult != COMPLETE_NORMAL) {
					Log.d(TAG, "Terminated: " + speakResult);
					return speakResult;
				} else {
					Log.d(TAG, speakResult == COMPLETE_SKIPPED ? "Skipped" : "Completed");
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Lock to update index
			synchronized (m_lock_setup) {
				Log.d(TAG, "calculate next index");
				m_current_index += m_repeat_flag ? 0 : m_speak_direction;
				m_repeat_flag = false;
			}
		}
		
		return COMPLETE_NORMAL;
	}
	
	public void start() {
		mTTs.acquire(TASK_PRIORITY_LOW);
		init_control_values();
		speak();
	}
	
	public void restart() {
		mTTs.acquire(TASK_PRIORITY_LOW);
		speak();
	}
	
	private void speak() {
		Thread speakThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				List<String> notification = null;
				Integer index = 0;
				String notification_type = "";
				int completed_result = COMPLETE_NORMAL;
				for(int i = 0; i < m_notification_order.size(); i++) {
					notification_type = m_notification_order.get(i);
					if(notification_type.contains(NOTIFICATION_TYPE_FB)) {
						notification = m_fb_notification;
						index = m_fb_notification_index;
					} else if(notification_type.contains(NOTIFICATION_TYPE_MAIL)) {
						notification = m_mail_notification;
						index = m_mail_notification_index;
					} else if(notification_type.contains(NOTIFICATION_TYPE_NEWS)) {
						notification = m_news_notification;
						index = m_news_notification_index;
					} else if(notification_type.contains(NOTIFICATION_TYPE_WEATHER)) {
						notification = m_weather_notification;
						index = m_weather_notification_index;
					} 
					
					completed_result = speakInfo(notification, index);
					
					if(completed_result != COMPLETE_SKIPPED || 
							completed_result != COMPLETE_NORMAL) {
						m_state = completed_result == COMPLETE_TERMINATED ? STATE_PAUSED : STATE_STOPPED;
						return;
					}
				}
			}
		});
		m_state = STATE_SPEAKING;
		speakThread.start();
	}
	
	public void stop() {
		Thread stopThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				mTTs.stop();
				setup(-1, -1, -1, -1);
				Log.d("Stopped", "" + m_fb_notification_index + " "
									+ m_mail_notification_index + " "
									+ m_news_notification_index + " "
									+ m_weather_notification_index);
			}
		});
		
		stopThread.start();
	}
	
	public void next() {
		Thread nextThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// Skip the current sentence
				mTTs.skip();
				synchronized (m_lock_setup) {
					m_speak_direction = DIRECTION_FW;
					Log.d("Next", "current index: " + m_current_index);
				}
			}
		});
		
		nextThread.start();
		if(m_state == STATE_PAUSED) {
			restart();
		}
	}
	
	public void back() {
		Thread backThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// Skip the current sentence
				mTTs.skip();
				synchronized (m_lock_setup) {
					m_speak_direction = DIRECTION_BW;
					Log.d("Back", "current index: " + m_current_index);
				}
			}
		});
		
		backThread.start();
		if(m_state == STATE_PAUSED) {
			restart();
		}
	}
	
	public void repeat() {
		Thread repeatThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// Skip the current sentence
				mTTs.skip();
				synchronized (m_lock_setup) {
					m_repeat_flag = true;
					Log.d("Repeat", "current index: " + m_current_index);
				}
			}
		});
		
		repeatThread.start();
		if(m_state == STATE_PAUSED) {
			restart();
		}
	}
	
	/*
	 * Setting what notification spoken.
	 * Called by Voice commands
	 */
	public void setup(int fb_index, int mail_index, int news_index, int weather_index) {
		synchronized (m_lock_setup) {
			Log.d(TAG, "setup");
			if(fb_index != SKIP_VALUE.intValue()) {
				m_fb_notification_index = Integer.valueOf(fb_index);
			}
			
			if(mail_index != SKIP_VALUE.intValue()) {
				m_mail_notification_index = Integer.valueOf(mail_index);
			}
			
			if(news_index != SKIP_VALUE.intValue()) {
				m_news_notification_index = Integer.valueOf(news_index);
			}
			
			if(weather_index != SKIP_VALUE.intValue()) {
				m_weather_notification_index = Integer.valueOf(weather_index);
			}
		}
	}
}
