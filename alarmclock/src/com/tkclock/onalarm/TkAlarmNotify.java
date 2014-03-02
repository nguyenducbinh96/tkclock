package com.tkclock.onalarm;

import java.util.Calendar;
import java.util.List;

import com.facebook.SessionState;
import com.tkclock.adapters.TkDbAlarmMng;
import com.tkclock.adapters.TkDbAlarmMng.TkDbAlarm;
import com.tkclock.adapters.TkNoficationMng;
import com.tkclock.adapters.TkNoficationMng.NotifycationInfo;
import com.tkclock.dashboard.R;
import com.tkclock.dashboard.TkFragmentActivity;
import com.tkclock.models.TkFacebook;
import com.tkclock.models.TkTextToSpeech;
import com.tkclock.utils.DateTimeUtils;
import com.tkclock.voice.notify_manager.TkNotificationSpeaker;
import com.tkclock.voice.notify_manager.TkVoiceCmd;
import com.tkclock.voice.notify_manager.TkVoiceControlMng;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import static com.tkclock.dashboard.TkApplication.MSG_TYPE_FB_NOTIFICATION;
import static com.tkclock.dashboard.TkApplication.MSG_TYPE_GMAIL_NOTIFICATION;
import static com.tkclock.dashboard.TkApplication.MSG_TYPE_TTS_NOTIFICATION;
import static com.tkclock.adapters.TkDbAlarmMng.KEY_ROWID;

public class TkAlarmNotify extends TkFragmentActivity implements OnClickListener {
	
	public static String COMMAND_NAME_STOP = "stop";
	public static String COMMAND_NAME_NEXT = "next";
	public static String COMMAND_NAME_BACK = "back";
	public static String COMMAND_NAME_REPEAT = "repeat";
	public static String COMMAND_NAME_FACEBOOK = "facebook";
	public static String COMMAND_NAME_MAIL = "mail";
	public static String COMMAND_NAME_NEWS = "news";
	public static String COMMAND_NAME_WEATHER = "weather";
	
	private static final String TAG = "Alarm notify";
	private static int REPEAT_COUNT = 2;
	public static String NOTIFY_ALARM_ID = "alarm_id";
	TextView textResult;
	TkFacebook mFbService;
	TkTextToSpeech mTts;
	
	boolean m_tts_ready;
	boolean m_fb_notification_ready;
	List<String> m_fb_notifcations;
	
	Button m_btn_next, m_btn_back, m_btn_repeat, m_btn_stop;
	TkVoiceCmd m_command_mng;
	TkNotificationSpeaker m_speaker;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_notify);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		textResult = (TextView) findViewById(R.id.textResult);
		m_tts_ready = false;
		m_fb_notification_ready = false;
		m_fb_notifcations = null;

        mFbService = TkFacebook.getService(this);
        mTts = TkTextToSpeech.getEngine(this);

        Bundle data = getIntent().getExtras();
		long alarmId = data.getLong(KEY_ROWID, 0);
        
        TkDbAlarmMng manager = new TkDbAlarmMng(this);
        manager.open();
        
        List<TkDbAlarm> alarms = manager.getAlarms(alarmId);
        manager.close();
        if(alarms.size() != 1) {
        	Log.d(TAG, "Alarm id invalid: " + alarmId);
        	return;
        }
        
        TkDbAlarm alarm = alarms.get(0);
        
        // DO anything when alarm going on here
        Toast.makeText(this, "Alarm: " + DateTimeUtils.date2Str(alarm.m_alarmtime), Toast.LENGTH_LONG).show(); // For example
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(this, notification);
        if(r != null)
        	r.play();
                
        mFbService.setHandler(mResultHandler);
        mTts.setHandler(mResultHandler);
        if(mFbService.isLogin()) {
        	mFbService.requestNotification();
        } else {
        	textResult.setText("Cannot get notification: Logged out");
        }
        
        // Test TTS command
        m_btn_next = (Button) findViewById(R.id.btnNext);
        m_btn_back = (Button) findViewById(R.id.btnBack);
        m_btn_repeat = (Button) findViewById(R.id.btnRepeat);
        m_btn_stop = (Button) findViewById(R.id.btnStop);
        m_btn_next.setOnClickListener(this);
        m_btn_back.setOnClickListener(this);
        m_btn_repeat.setOnClickListener(this);
        m_btn_stop.setOnClickListener(this);
        
        m_command_mng = null;
	}
	
	private Handler mResultHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg.arg1 == MSG_TYPE_FB_NOTIFICATION) {
				Log.d(TAG, "Receive fb notification");
				m_fb_notification_ready = true;
				m_fb_notifcations = mFbService.getNotificationString();
				readNotification();
				
			} else if(msg.arg1 == MSG_TYPE_TTS_NOTIFICATION) {
				Log.d(TAG, "Receive tts notification");
				m_tts_ready = true;
				readNotification();
			}
		}
	};
	
	private void readNotification() {
		if(m_tts_ready == false || m_fb_notification_ready == false)
			return;
		Log.d(TAG, "Start speaking");
		
		// Enable voice command controller when all notifications ready
		m_speaker = new TkNotificationSpeaker(TkAlarmNotify.this, mTts, m_fb_notifcations, null, null, null);
		m_command_mng = new TkVoiceCmd(m_speaker);
		m_speaker.start();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		TkTextToSpeech.shutdown();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mTts.stop();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnNext:
			m_command_mng.processCommand(COMMAND_NAME_NEXT);
			break;
		case R.id.btnBack:
			m_command_mng.processCommand(COMMAND_NAME_BACK);
			break; 
		case R.id.btnRepeat:
			m_command_mng.processCommand(COMMAND_NAME_REPEAT);
			break;
		case R.id.btnStop:
			m_command_mng.processCommand(COMMAND_NAME_STOP);
			break;
		default:
			break;
		}
		
	}
	
}
