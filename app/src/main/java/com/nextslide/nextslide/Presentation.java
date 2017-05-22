package com.nextslide.nextslide;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by yangren on 5/16/17.
 */

public class Presentation implements Parcelable {
    private String mName;
    private String mDescription;
    // HashMap used to store associations between Strings and Actions.
    private HashMap<String, Action> mMap;
    private ViewGroup mParent;

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

    // Needs to be called as soon as the Presentation Activity is started!
    public void setParentViewGroup(ViewGroup parent)
    {
        mParent = parent;
    }

    public void addAction(String key, Action value)
    {
        mMap.put(key, value);
    }

    public void performAction(String key)
    {
        mMap.get(key).performAction(mParent);
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
        public boolean performAction(ViewGroup parent)
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
            mImage = image;
        }
        public ImageAction(Parcel in) {
            super(in);
            mImage = in.readInt();
        }

        public boolean performAction(ViewGroup parent) {
            if(!super.performAction(parent)) return false;
            // Remove all child views from the parent.
            parent.removeAllViews();

            // Create a new view with the image, and add it to the parent.
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_image_view, parent, false);

            // Set the image. Replace this with Picasso function!s
            v.setImageResource(mImage);

            parent.addView(v);
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

    // TODO.
    static class SoundAction extends Action {
        // private ??? mSound;
        public SoundAction() {
            super("sound");
        }
        public SoundAction(Parcel in) {
            super(in);
        }
        public boolean performAction(ViewGroup parent) {
            if(!super.performAction(parent)) return false;
            // Don't do anything with parent!
            // Play sound here.

            return true;
        }

        /**
         * Implement Parcelable interface.
         */
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
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
}
