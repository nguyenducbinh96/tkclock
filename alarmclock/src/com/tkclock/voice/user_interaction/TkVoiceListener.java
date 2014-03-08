package com.tkclock.voice.user_interaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class TkVoiceListener implements RecognitionListener {
	private static final String TAG = "TkVoiceListener";
	int topK = 3; // just consider the three first results
	// predictor
	private static Predictor m_predictor;

	private static Set<String> m_supportedCommands;
	
	private OnListenerResult m_controller;
	
	public interface OnListenerResult {
		public void OnCommandDetected(String command);
	}

	public TkVoiceListener(OnListenerResult controller, String[] commands) {
		m_supportedCommands = new HashSet<String>();
	    for(int i = 0; i < commands.length; i ++) {
	    	m_supportedCommands.add(commands[i]);
	    }
	    
	    m_controller = controller;
	    m_predictor = new Predictor();
	}

	public void onReadyForSpeech(Bundle params) {
		Log.d(TAG, "onReadyForSpeech");
	}

	public void onBeginningOfSpeech() {
		Log.d(TAG, "onBeginningOfSpeech");
	}

	public void onRmsChanged(float rmsdB) {
		Log.d(TAG, "onRmsChanged");
	}

	public void onBufferReceived(byte[] buffer) {
		Log.d(TAG, "onBufferReceived");
	}

	public void onEndOfSpeech() {
		Log.d(TAG, "onEndofSpeech");
	}

	public void onError(int error) {
		Log.d(TAG, "error " + error);
		m_controller.OnCommandDetected("");
	}

	public void onResults(Bundle results) {
		Log.d(TAG, "onResults " + results);
		ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		for (int i = 0; i < data.size(); i++) {
			String command = data.get(i);
			Log.d(TAG, "result: " + command);
			// check if it is supported?
			if(m_supportedCommands.contains(command)) {
				m_controller.OnCommandDetected(command);
				return;
			}
		}

		// if this is unknown command, try to predict...
		String possibleCommand = null;
		int index = 0;
		while(possibleCommand == null && index < Math.min(data.size(), topK)) {
			possibleCommand = m_predictor.predictFromSimilarString(data.get(index));
			index++;
		}
		if(possibleCommand != null) {
			//        mText.setText("Command: " + possibleCommand + " (recognized by lsh)");
			m_controller.OnCommandDetected(possibleCommand);
		} else {
			index = 0;
			while(possibleCommand == null && index < Math.min(data.size(), topK)) {
				possibleCommand = m_predictor.predictFromSimilarSound(data.get(index));
				index++;
			}
			if(possibleCommand != null) {
				//          mText.setText("Command: " + possibleCommand + " (recognized by sound predictor)");
			}else {
				//          mText.setText("Can't be recognized. Please speak again!");
			}
		}
	}

	public void onPartialResults(Bundle partialResults) {
		Log.d(TAG, "onPartialResults");
	}

	public void onEvent(int eventType, Bundle params) {
		Log.d(TAG, "onEvent " + eventType);
	}
}
