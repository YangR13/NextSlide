package com.nextslide.nextslide;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
    private Presentation(Parcel in) {
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
        out.writeInt(mMap.size());
        for(HashMap.Entry<String,Action> entry : mMap.entrySet()){
            out.writeString(entry.getKey());
            out.writeParcelable(entry.getValue(), flags);
        }
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

    }

    static class ImageAction extends Action {
        private int mImage;
        public ImageAction(int image) {
            super("image");
            Log.d(TAG,"Reading image from ID");
            mImage = image;
        }
        public ImageAction(Parcel in) {
            super(in);
            Log.d(TAG,"Reading image from parcel");
            mImage = in.readInt();
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

            // Set the image. Replace this with Picasso function!s
            v.setImageResource(mImage);
/*
            Picasso.with(parent.getContext())
                    .load(mImage)
                    //.fit()
                    .into(v);
*/
            layoutVG.addView(v);
            return true;
        }

        /**
         * Implement Parcelable interface.
         */
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mImage);
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

        //public boolean performAction(ViewGroup parent) {
        public boolean performAction(final Activity activity, int layout) {
            if(!super.performAction(activity, layout)) return false;
            // Play sound here.
            Log.d(TAG,"Executing sound, mSound = " + String.valueOf(mSound));
            new AsyncTask<Integer, Void, Exception>() {
                @Override
                protected Exception doInBackground(Integer... params) {
                    if(mMediaPlayer == null) {
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
                    }
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
