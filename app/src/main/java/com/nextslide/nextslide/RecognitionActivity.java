package com.nextslide.nextslide;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class RecognitionActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        final String TAG = "RecognitionActivity";
        final TextView mTextView = (TextView) findViewById(R.id.speechText);

        //TODO: Display name of presentation
        //TODO: Recieve string/resource associations
        //TODO: Display image if strings match

        Log.d(TAG,"Creating SpeechRecognizer");
        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Log.d(TAG,"SpeechRecognizer created.");

        Log.d(TAG,"Creating RecognizerIntent");
        final Intent mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        Log.d(TAG,"RecognizerIntent created.");

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG,"Began speech recognition");

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG,"Ended speech recognition");
            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                mSpeechRecognizer.stopListening();
                mSpeechRecognizer.startListening(mRecognizerIntent);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                String totalstring = "";
                List<String> listOfWords = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if(listOfWords != null){
                    for(int i = 0; i<listOfWords.size(); i++) {
                        totalstring += listOfWords.get(i);
                    }
                    mTextView.setText(totalstring);
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        Log.d(TAG,"SpeechRecognizer configured.");

        final Button button = (Button) findViewById(R.id.presentationButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Starting SpeechRecognizer");
                mSpeechRecognizer.startListening(mRecognizerIntent);
            }
        });
    }
}
