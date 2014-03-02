package com.tkclock.models;

import java.util.List;

import com.tkclock.utils.StringUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import static com.tkclock.dashboard.TkApplication.MSG_TYPE_TTS_NOTIFICATION;
import static com.tkclock.dashboard.TkApplication.MSG_RESULT_OK;
import static com.tkclock.dashboard.TkApplication.MSG_RESULT_NG;

public class TkTextToSpeech {
	public static int TASK_PRIORITY_HIGH = 1;
	public static int TASK_PRIORITY_NORMAL = 2;
	public static int TASK_PRIORITY_LOW = 3;
	
	public static int COMPLETE_NORMAL = 0; 
	public static int COMPLETE_TERMINATED = 1; // caused by higher priority request
	public static int COMPLETE_RELEASED = 2; // caused by quit application
	public static int COMPLETE_STOPPED = 3; // caused by request stop speak 
	public static int COMPLETE_SKIPPED = 4; // caused by request stop speak 
	
	public static float TTS_SPEED_NORMAL = (float)0.9;
	public static float TTS_SPEED_FAST = (float)1.0;
	public static float TTS_SPEED_SLOW = (float)0.8;
	
	private static int WAIT_LOOP = 5000;
	private static String SENTENCE_DELIMITER = ".,:;";
	
	protected static final String TAG = "Tk Text2Speech";
	private int m_task_priority;
	private static Handler mWaitResHandler = null;
	
	private static TkTextToSpeech mEngine = null;
	private TextToSpeech mTTS = null;
	private float m_speed = TTS_SPEED_NORMAL;
	
	private Object lockSpeak = new Object();
	int m_complete_speak = COMPLETE_NORMAL;
	
	public static TkTextToSpeech getEngine(Context context) {
		if(mEngine == null) {
			mEngine = new TkTextToSpeech(context);
		}
		
		return mEngine;
	}
	
	public static void shutdown() {
		synchronized (mEngine.lockSpeak) {
			Log.d(TAG, "Shutting down");
			mEngine.m_complete_speak = COMPLETE_RELEASED;
		}
		mEngine.mTTS.shutdown();
		mEngine = null;
	}
	
	public int stop() {
		synchronized (lockSpeak) {
			Log.d(TAG, "Stopping");
			m_complete_speak = COMPLETE_STOPPED;
		}
		mTTS.stop();
		return 0;
	}
	
	public int skip() {
		synchronized (lockSpeak) {
			Log.d(TAG, "Skipping");
			m_complete_speak = COMPLETE_SKIPPED;
		}
		
		return 0;
	}
	
	public void setSpeed(float speed) {
		m_speed = speed;
		mTTS.setSpeechRate(m_speed);
	}
	
	private TkTextToSpeech(Context context) {
		m_task_priority = 0;
		mTTS = new TextToSpeech(context, new OnInitListener() {
			
			@Override
			public void onInit(int status) {
				Log.d(TAG, "Init completed");
				mTTS.setSpeechRate(m_speed);
				Message msg = Message.obtain();
				if(status == TextToSpeech.SUCCESS) {
					msg.arg1 = MSG_TYPE_TTS_NOTIFICATION;
					msg.arg2 = MSG_RESULT_OK;
				} else {
					msg.arg1 = MSG_TYPE_TTS_NOTIFICATION;
					msg.arg2 = MSG_RESULT_NG;
				}
				mWaitResHandler.sendMessage(msg);
			}
		});
	}
	
	public void setHandler(Handler handler) {
		mWaitResHandler = handler;
	}
	
	public boolean acquire(int priority) {
		boolean ret = false;
		
		if(m_task_priority == 0) {
			m_task_priority = priority;
			ret = true;
		} else if(priority >= m_task_priority) {
			ret = false;
		} else {
			synchronized (lockSpeak) {
				// Terminate current task due to lower priority
				m_complete_speak = COMPLETE_TERMINATED;
			}
			ret = true;
		}
		
		return ret;
	}
	
	public void release(int priority) {
		stop();
		m_task_priority = 0;
	}
	
	public int speak(String sentence) {
		boolean status = false;
		// Reset
		m_complete_speak = COMPLETE_NORMAL;
		
		do { // Loop until nothing to speak
			List<String> sub_sentences = StringUtils.SplitUsingToken(sentence, SENTENCE_DELIMITER);
			// Loop all parts of sentences.
			for(int i = 0; i < sub_sentences.size(); i++) {
				
				// Wait until the previous sentence completed
				waitSpeak();
				
				// Check request stop before start next sub-sentence
				synchronized (lockSpeak) {
					if(m_complete_speak != COMPLETE_NORMAL) {
						Log.d("TTS", "Stopped: " + m_complete_speak);
						status = false;
						break;
					}
				}
				
				String subString = sub_sentences.get(i);
				Log.d("Speaker", subString);
				mTTS.speak(subString, TextToSpeech.QUEUE_FLUSH, null);
			}
		} while (status == true);
		
		waitSpeak();

		return m_complete_speak;
	}
	
	private void waitSpeak() {
		int wait = 0;
		boolean status = false;
		do {
			wait++;
			status = mTTS.isSpeaking();
			if(status == true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			break;
		} while(wait <= WAIT_LOOP);
	}
}
