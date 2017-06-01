package com.nextslide.nextslide;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import edu.cmu.pocketsphinx.Assets;

import static java.sql.Types.NULL;

/**
 * Created by yangren on 5/16/17.
 */

public class Presentation implements Parcelable {
    private String mName;
    private String mDescription;
    // HashMap used to store associations between Strings and Actions.
    private HashMap<String, Action> mMap;
    //private ViewGroup mParent;
    private int mParent;
    private Activity mActivity;
    static MediaPlayer mMediaPlayer;

    static String TAG = "Presentation";

    // Constructor.
    public Presentation(String name, String description)
    {
        mName = name;
        mDescription = description;
        mMap = new HashMap<String, Action>();
    }

    // Parcelable Constructor.
    public Presentation(Parcel in) {
        mName = in.readString();
        mDescription = in.readString();
        mMap = new HashMap<String, Action>();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            Action value = in.readParcelable(Action.class.getClassLoader());
            mMap.put(key,value);
        }
    }

    // JSON Constructor.
    public Presentation(JSONObject obj) {
        try {
            mName = obj.getString("name");
            mDescription = obj.getString("description");
            JSONArray mapArr = obj.getJSONArray("map_arr");
            mMap = new HashMap<String, Action>();
            for(int i=0; i < mapArr.length(); i++) {
                JSONObject mapObj = mapArr.getJSONObject(i);
                JSONObject actionObj = mapObj.getJSONObject("value");
                String type = actionObj.getString("type");
                Action action = null;
                if(type.compareTo("image") == 0) {
                    action = new ImageAction(actionObj);
                } else if (type.compareTo("sound") == 0) {
                    action = new SoundAction(actionObj);
                }
                mMap.put(mapObj.getString("key"), action);
            }
        }
        catch(JSONException ex){
            ex.printStackTrace();
        }
    }

    String getName() { return mName; }
    String getDescription() { return mDescription; }

    boolean containsKey(String key) { return mMap.containsKey(key); }
    Set<String> keySet() { return mMap.keySet(); }

    // Needs to be called as soon as the Presentation Activity is started!
    //public void setParentViewGroup(ViewGroup parent)
    public void setActivityAndLayoutId(Activity activity, int layout)
    {
        mActivity = activity;
        mParent = layout;
    }

    public void addAction(String key, Action value)
    {
        mMap.put(key, value);
    }

    public void performAction(String key)
    {
        Action a = mMap.get(key);
        if(a != null)
            a.performAction(mActivity, mParent);
    }

    /*
     * Implement Parcelable interface.
     */
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeString(mDescription);
        int size = 0;
        if(mMap != null) size = mMap.size();
        out.writeInt(size);
        if(mMap != null) {
            for(HashMap.Entry<String,Action> entry : mMap.entrySet()){
                out.writeString(entry.getKey());
                out.writeParcelable(entry.getValue(), flags);
            }
        }
    }

    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("name", mName);
            obj.put("description", mDescription);
            JSONArray mapArr = new JSONArray();
            if(mMap != null) {
                for(HashMap.Entry<String,Action> entry : mMap.entrySet()){
                    JSONObject mapObj = new JSONObject();
                    mapObj.put("key", entry.getKey());
                    mapObj.put("value", entry.getValue().toJSON());
                    mapArr.put(mapObj);
                }
            }
            obj.put("map_arr", mapArr);

            return obj;
        }
        catch(JSONException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static final Parcelable.Creator<Presentation> CREATOR
            = new Parcelable.Creator<Presentation>() {
        public Presentation createFromParcel(Parcel in) {
            return new Presentation(in);
        }

        public Presentation[] newArray(int size) {
            return new Presentation[size];
        }
    };

    /*
     * Nested static classes.
     */
    abstract static class Action implements Parcelable {
        private String mType;
        private int mTimesPerformed;

        // Constructor.
        public Action(String type) {
            mType = type;
            mTimesPerformed = 0;
        }
        // Parcelable Constructor.
        public Action(Parcel in) {
            mType = in.readString();
            mTimesPerformed = 0;
        }
        // JSON Constructor.
        public Action(JSONObject json) {
            try {
                mType = json.getString("type");
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
            mTimesPerformed = 0;
        }

        public String getTypeString()
        {
            return mType;
        }
        public void resetTimesPerformed()
        {
            mTimesPerformed = 0;
        }
        //public boolean performAction(ViewGroup parent)
        public boolean performAction(Activity activity, int parent)
        {
            mTimesPerformed++;
            return true;
        };

        /**
         * Implement Parcelable interface.
         */
        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(mType);
        }

        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", mType);
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
            return obj;
        };

    }

    static class ImageAction extends Action {
        private Uri mImage;
        public ImageAction(Uri image) {
            super("image");
            Log.d(TAG,"Reading image from ID");
            mImage = image;
        }
        public ImageAction(Parcel in) {
            super(in);
            Log.d(TAG,"Reading image from parcel");
            mImage = in.readParcelable(Uri.class.getClassLoader());
        }
        public ImageAction(JSONObject json) {
            super(json);
            Log.d(TAG,"Reading image from JSON");
            try {
                mImage = Uri.parse(json.getString("image"));
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
        }

        //public boolean performAction(ViewGroup parent) {
        public boolean performAction(Activity activity, int layout) {
            if(!super.performAction(activity, layout)) return false;
            // Remove all child views from the parent.
            ViewGroup layoutVG = (ViewGroup)activity.findViewById(layout);
            layoutVG.removeAllViews();

            // Create a new view with the image, and add it to the parent.
            ImageView v = (ImageView) LayoutInflater.from(layoutVG.getContext())
                    .inflate(R.layout.my_image_view, layoutVG, false);


            Picasso.with(activity)
                    .load(mImage)
                    //.fit()
                    .into(v);

            layoutVG.addView(v);
            return true;
        }

        /**
         * Implement Parcelable interface.
         */
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeParcelable(mImage, flags);
        }

        public static final Parcelable.Creator<ImageAction> CREATOR
                = new Parcelable.Creator<ImageAction>() {
            public ImageAction createFromParcel(Parcel in) {
                return new ImageAction(in);
            }

            public ImageAction[] newArray(int size) {
                return new ImageAction[size];
            }
        };

        public JSONObject toJSON() {
            JSONObject obj = super.toJSON();
            try {
                obj.put("image", mImage.toString());
                return obj;
            }
            catch(JSONException ex){
                ex.printStackTrace();
            }
            return null;
        }
    }
    static class SoundAction extends Action {
        private int mSound;
        public SoundAction(int image) {
            super("sound");
            Log.d(TAG,"Reading sound from ID");
            mSound = image;
        }
        public SoundAction(Parcel in) {
            super(in);
            Log.d(TAG,"Reading sound from parcel");
            mSound = in.readInt();
        }
        public SoundAction(JSONObject json) {
            super(json);
            Log.d(TAG,"Reading image from JSON");
            try {
                mSound = json.getInt("sound");
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
        }

        //public boolean performAction(ViewGroup parent) {
        public boolean performAction(final Activity activity, int layout) {
            if(!super.performAction(activity, layout)) return false;
            // Play sound here.
            Log.d(TAG,"Executing sound, mSound = " + String.valueOf(mSound));
            new AsyncTask<Integer, Void, Exception>() {
                @Override
                protected Exception doInBackground(Integer... params) {
                    if(mMediaPlayer != null) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                    Log.d(TAG, "Creating media player");
                    //if resource
                    int sound = params[0];
                    Log.d(TAG, "mSound = " + String.valueOf(sound));
                    mMediaPlayer = MediaPlayer.create(activity, sound);
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                        try {
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                            mMediaPlayer = null;
                            Log.d(TAG, "Media player released");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        }
                    });
                    Log.d(TAG, "Starting media player");
                    mMediaPlayer.start();

                    return null;
                }
            }.execute(mSound);
            return true;
        };

        /**
         * Implement Parcelable interface.
         */
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mSound);
        }

        public static final Parcelable.Creator<SoundAction> CREATOR
                = new Parcelable.Creator<SoundAction>() {
            public SoundAction createFromParcel(Parcel in) {
                return new SoundAction(in);
            }

            public SoundAction[] newArray(int size) {
                return new SoundAction[size];
            }
        };

        public JSONObject toJSON() {
            JSONObject obj = super.toJSON();
            try {
                obj.put("sound", mSound);
                return obj;
            }
            catch(JSONException ex){
                ex.printStackTrace();
            }
            return null;
        }
    }
//    // TODO.
//    static class SoundAction extends Action {
//        private int mSound;
//        public SoundAction(int sound) {
//            super("sound");
//            Log.d(TAG, "resource ID = " + String.valueOf(sound));
//            mSound = sound;
//            Log.d(TAG, "mSound = " + String.valueOf(mSound));
//        }
//        public SoundAction(Parcel in) {
//            super(in);
//            Log.d(TAG,"Reading sound from parcel");
//            mSound = in.readInt();
//        }
//
//        public boolean performAction(final Activity activity, int layout) {
//            if(!super.performAction(activity, layout)) return false;
//            Log.d(TAG, "mSound = " + String.valueOf(mSound));
//            // Don't do anything with layout!
//            // Play sound here.
////            Log.d(TAG,"Executing sound, mSound = " + String.valueOf(mSound));
////            new AsyncTask<Integer, Void, Exception>() {
////                @Override
////                protected Exception doInBackground(Integer... params) {
////                    if(mMediaPlayer == null) {
////                        Log.d(TAG,"Creating media player");
////                        //if resource
////                        int sound = params[0];
////                        Log.d(TAG,"mSound = " + String.valueOf(sound));
////                        mMediaPlayer = MediaPlayer.create(activity, sound);
////                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
////                            public void onCompletion(MediaPlayer mp) {
////                                try {
////                                    mMediaPlayer.stop();
////                                    mMediaPlayer.release();
////                                    mMediaPlayer=null;
////                                    Log.d(TAG, "Media player released");
////                                }
////                                catch (Exception e)
////                                {
////                                    e.printStackTrace();
////                                }
////                            }
////                        });
////                        Log.d(TAG,"Stating media player");
////                        mMediaPlayer.start();
//
////                        //if uri
////                        mMediaPlayer = new MediaPlayer();
////                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
////                        mMediaPlayer.setDataSource(mActivity, myUri);
////                        try{
////                            mMediaPlayer.prepare();
////                        }
////                        catch(Exception e){
////                            return e;
////                        }
////                        mMediaPlayer.start();
////
////
////                    }
////                    return null;
////                }
////            }.execute(mSound);
//            return true;
//        }
//
//        /**
//         * Implement Parcelable interface.
//         */
//        public void writeToParcel(Parcel out, int flags) {
//            super.writeToParcel(out, flags);
//        }
//
//        public static final Parcelable.Creator<SoundAction> CREATOR
//                = new Parcelable.Creator<SoundAction>() {
//            public SoundAction createFromParcel(Parcel in) {
//                return new SoundAction(in);
//            }
//            public SoundAction[] newArray(int size) {
//                return new SoundAction[size];
//            }
//        };
//    }
}
