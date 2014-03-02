package com.tkclock.voice.notify_manager;

/* Singleton class
 * Manage processing voice command received from recognize module
 * State is: updated from voice control thread, 
 * 			 used in main thread
 */
public class TkVoiceControlMng {
	
	// Voice control state
	public static int VOICE_STATE_STOPPED = 1;
	public static int VOICE_STATE_SPEAK_CURTIME = 2;
	public static int VOICE_STATE_SPEAK_WEATHER = 3;
	public static int VOICE_STATE_SPEAK_FACEBOOK = 4;
	public static int VOICE_STATE_SPEAK_EMAIL = 5;
	public static int VOICE_STATE_SPEAK_NEWS = 6;
	
	private int m_state = VOICE_STATE_STOPPED;
	
	private TkNotificationSpeaker m_notification_speaker;
	
	public TkVoiceControlMng(TkNotificationSpeaker speaker) { 
		m_state = VOICE_STATE_STOPPED; 
		m_notification_speaker = speaker;
	}
	
	public void start() {
		m_notification_speaker.start();
	}
	
	public int getState() { 
		return m_state; 
	}
	
	public void updateState(int state) { 
		m_state = state; 
	}
}
