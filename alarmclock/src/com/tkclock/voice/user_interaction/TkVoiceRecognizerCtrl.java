package com.tkclock.voice.user_interaction;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import com.tkclock.models.TkTextToSpeech;

import static com.tkclock.models.TkTextToSpeech.TASK_PRIORITY_HIGH;
import static com.tkclock.voice.notify_manager.TkVoiceCmd.COMMAND_OK_STR;
import static com.tkclock.voice.notify_manager.TkVoiceCmd.COMMAND_UNSUPPORTED_STR;

public class TkVoiceRecognizerCtrl implements TkVoiceListener.OnListenerResult {
	private static int MAX_RECOG_RESULT = 5;
	private static String TAG = "TkVoiceRecognizerCtrl";
	TkTextToSpeech m_speaker;
	private SpeechRecognizer m_speech_recog;
	
	private String m_wait_confirm_cmd;
	
	private OnCommandProcessing m_controller;
	
	public interface OnCommandProcessing {
		public String OnCommandResult(String command);
	}
	
	private static final String supported_commands[] = {"pause", "stop", "dismiss", "turn off", "off", 
	    "repeat", "back", "next",
	    "facebook", "gmail", "email", "weather", "news",
	    "yes", "no"};
	
	public TkVoiceRecognizerCtrl(OnCommandProcessing controller, TkTextToSpeech speaker) {
		m_speaker = speaker;
		m_wait_confirm_cmd = "";
		
		m_controller = (OnCommandProcessing) controller;
		m_speech_recog = SpeechRecognizer.createSpeechRecognizer((Context)m_controller);
		m_speech_recog.setRecognitionListener(new TkVoiceListener(this, supported_commands));
	}

	@Override
	public void OnCommandDetected(String command) {
		if (m_controller == null) {
			return;
		} else if(command.length() == 0) {
			Log.d(TAG, "Restart service");
			m_controller.OnCommandResult(command);
			return;
		}
		
		Toast.makeText((Context)m_controller, command, Toast.LENGTH_LONG).show();
		String result = m_controller.OnCommandResult(command);
		
		if (result.equals(COMMAND_OK_STR)) {
			Log.d(TAG, "Command is supported: " + command);
		} else if (result.equals(COMMAND_UNSUPPORTED_STR)) {
			Log.d(TAG, "Command is not supported: " + command);
			// TODO: speak the info to user
			m_speaker.acquire(TASK_PRIORITY_HIGH);
			m_speaker.speak("Command " + command + " is not supported");
			m_speaker.release(TASK_PRIORITY_HIGH);
		} else {
			Log.d(TAG, "Suggest another command: " + command + " --> " + result);
			m_speaker.acquire(TASK_PRIORITY_HIGH);
			m_wait_confirm_cmd = result;
			m_speaker.speak("Do you want to " + result);
			m_speaker.release(TASK_PRIORITY_HIGH);
		}
	}
	
	public void start() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RECOG_RESULT);
		m_speech_recog.startListening(intent);
	}
	public void stop() {
		m_speech_recog.stopListening();
	}
}
