/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package com.nextslide.nextslide;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.R.attr.data;
import static android.widget.Toast.makeText;
import static java.lang.System.in;

public class RecognitionActivity extends Activity implements
        RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PHONE_SEARCH = "phones";
    private static final String MENU_SEARCH = "menu";
    private static final String TAG = "RecognitionActivity";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    private File mFile;
    private Presentation mPresentation;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_recognition);

        // Get presentation object.
        Intent intent = getIntent();
        Bundle b = this.getIntent().getExtras();
        if (b == null) {
            // TODO: Error! Go back to MainActivity!
        }
        mPresentation = b.getParcelable("presentation_id");
        if(mPresentation == null) {
            // TODO: Error! Go back to MainActivity!
        }

        int layout = R.id.presentation_layout;
        mPresentation.setActivityAndLayoutId(this, layout);

        setContentView(R.layout.activity_recognition);
        ((TextView) findViewById(R.id.speechText))
                .setText("Preparing the recognizer");

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        runRecognizerSetup();
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(RecognitionActivity.this);

                    Log.d(TAG,"creating keyword search file");
                    // Create keyword-activation search.

                    File assetsDir = assets.getExternalDir();
                    assetsDir.mkdirs();
                    mFile = new File(assetsDir, "keywords.gram");

                    //FileOutputStream outputStream;
                    FileWriter fileWriter = new FileWriter(mFile,false);
                    try {
                        //outputStream = openFileOutput(mFile.getName(), Context.MODE_PRIVATE);
                        String keyword = "";
                        int syllables = 0;
                        for (String key : mPresentation.keySet()) {
                            syllables = (int) Math.ceil((double)key.length()/5.0);
                            if(syllables == 1)
                                keyword = key + " /1.0/\n";
                            else
                                keyword = key + " /1e-" + String.valueOf(syllables-1) + "/\n";
                            fileWriter.write(keyword);
                        }
                        fileWriter.close();
                        //outputStream.close();

                        Log.d(TAG,"keyword search file created");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    File assetDir = assets.syncAssets();
                    Log.d(TAG,"Comparing two paths:");
                    Log.d(TAG,assetDir.getAbsolutePath());
                    Log.d(TAG,mFile.getAbsolutePath());
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.speechText))
                            .setText("Failed to init recognizer ");
                } else {
                    Log.d(TAG,"Made it through setup");
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }

        mFile.delete();
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        Log.d(TAG,"onPartialResult");
        if (hypothesis == null)
            return;
        Log.d(TAG,"hypothesis != null");
        String text = hypothesis.getHypstr();
        Log.d(TAG,"Got string: " + text);
        String mapKey = text.trim();
        if(mPresentation.containsKey(mapKey)) {
            mPresentation.performAction(mapKey);
        }
        else {
            Log.d(TAG,"HashMap does not contain key: /" + mapKey + "/");
        }

    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        Log.d(TAG,"onResult");
        ((TextView) findViewById(R.id.speechText)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Got string: " + text);
            String mapKey = text.trim();
            if(mPresentation.containsKey(mapKey)) {
                mPresentation.performAction(mapKey);
            }
            else {
                Log.d(TAG,"HashMap does not contain key: /" + mapKey + "/");
            }
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG,"onBeginningOfSpeech");
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        Log.d(TAG,"onEndOfSpeech");
        switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        Log.d(TAG,"Beginning to listen");
        recognizer.startListening(KWS_SEARCH);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */
        // Create grammar-based search for digit recognition
        File keywordsGrammar = new File(assetsDir, "keywords.gram");
        Log.d(TAG,"Reading 'keywords.gram'");
        FileInputStream inputStream = new FileInputStream(keywordsGrammar);
        //FileInputStream inputStream = openFileInput(keywordsGrammar.getName());
        String total = getStringFromInputStream(inputStream);
        Log.d(TAG, total);
        inputStream.close();
        Log.d(TAG,keywordsGrammar.getAbsolutePath());

        Log.d(TAG,"adding keyword search");
        recognizer.addKeywordSearch(KWS_SEARCH, keywordsGrammar);
        Log.d(TAG,"added keyword search");

        //recognizer.addKeyphraseSearch(KWS_SEARCH, "test /1.0/");
        //recognizer.addKeyphraseSearch(KWS_SEARCH, "mighty /1e-1/");

    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.speechText)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    public File getTempFile(Context context, String fileName) {
        File file;
        try {
            file = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            // Error while creating file
            file = null;
        }
        return file;
    }

    public static String getStringFromInputStream(InputStream stream) throws IOException
    {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }
}

