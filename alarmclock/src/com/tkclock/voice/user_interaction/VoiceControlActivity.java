package com.tkclock.voice.user_interaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.tkclock.dashboard.R;

public class VoiceControlActivity extends Activity implements OnClickListener {

  private TextView mText;

  private SpeechRecognizer sr;

  private static final String TAG = "MyStt3Activity";
  
  // predictor
  private static Predictor predictor;
  
  private static final String commands[] = {"pause", "stop", "dismiss", "turn off", "off", 
    "repeat", "back", "next",
    "facebook", "gmail", "email", "weather", "news"};
  
  private static Set<String> supportedCommands;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.voice_layout);
    Button speakButton = (Button) findViewById(R.id.btn_speak);
    mText = (TextView) findViewById(R.id.textView1);
    speakButton.setOnClickListener(this);
    sr = SpeechRecognizer.createSpeechRecognizer(this);
    sr.setRecognitionListener(new listener());
    
    // init hashing
    supportedCommands = new HashSet<String>();
    for(int i = 0; i < commands.length; i ++) {
      supportedCommands.add(commands[i]);
    }
    
    // init predictor
    predictor = new Predictor();
    
    
//    try {
//      File file = new File(Environment.getExternalStoragePublicDirectory(
//          Environment.DIRECTORY_DOWNLOADS), "pause.db");
//
//      System.out.println("file is here: " + file.getPath());
//      file.createNewFile();
//      //write the bytes in file
//      if(file.exists())
//      {
//           InputStream in = new FileInputStream(file);    
//           BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//           String line = "";
//           while ((line = reader.readLine()) != null) {
//             System.out.println("pause\t" + line);
//           }
//           reader.close();
//           in.close();
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }

  class listener implements RecognitionListener {
    int topK = 3; // just consider the three first results
    
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
      mText.setText("Can't be recognized. Please speak again!");
    }

    public void onResults(Bundle results) {
      Log.d(TAG, "onResults " + results);
      ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      for (int i = 0; i < data.size(); i++) {
        String command = data.get(i);
        Log.d(TAG, "result " + command);
        // check if it is supported?
        if(supportedCommands.contains(command)) {
          mText.setText("Command: " + command);
          return;
        }
      }
      
      // if this is unknown command, try to predict...
      String possibleCommand = null;
      int index = 0;
      while(possibleCommand == null && index < Math.min(data.size(), topK)) {
        possibleCommand = predictor.predictFromSimilarString(data.get(index));
        index++;
      }
      if(possibleCommand != null)
        mText.setText("Command: " + possibleCommand + " (recognized by lsh)");
      else {
        index = 0;
        while(possibleCommand == null && index < Math.min(data.size(), topK)) {
          possibleCommand = predictor.predictFromSimilarSound(data.get(index));
          index++;
        }
        if(possibleCommand != null)
          mText.setText("Command: " + possibleCommand + " (recognized by sound predictor)");
        else
          mText.setText("Can't be recognized. Please speak again!");
      }
      
      
//      try {
//        File file = new File(Environment.getExternalStoragePublicDirectory(
//            Environment.DIRECTORY_DOWNLOADS), "pause.db");
//
//        System.out.println("file is here: " + file.getPath());
//        file.createNewFile();
//        //write the bytes in file
//        if(file.exists())
//        {
//             OutputStream fo = new FileOutputStream(file);    
//             OutputStreamWriter osw = new OutputStreamWriter(fo);
//             osw.write(str + "\n");
//             
//             osw.flush();
//             osw.close();
//             fo.close();
//        }
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
    }

    public void onPartialResults(Bundle partialResults) {
      Log.d(TAG, "onPartialResults");
    }

    public void onEvent(int eventType, Bundle params) {
      Log.d(TAG, "onEvent " + eventType);
    }
  }

  public void onClick(View v) {
    if (v.getId() == R.id.btn_speak) {
    	mText.setText("");
      Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

      intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
      sr.startListening(intent);
      Log.i("111111", "11111111");
    }
  }
}