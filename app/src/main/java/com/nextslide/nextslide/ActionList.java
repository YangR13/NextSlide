package com.nextslide.nextslide;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by yangren on 5/14/17.
 */

public class ActionList extends RecyclerView.Adapter<ActionList.ViewHolder>
{
    private ActionListManager mManager;
    private Context mContext;
    private ArrayList<KeyValue<Uri>> mEntries;
    private String TAG = "ActionList";

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout mLinearLayout;
        public ViewHolder(LinearLayout l) {
            super(l);
            mLinearLayout = l;
        }
        public TextWatcher mWatcher = null;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ActionList(ActionListManager manager, ArrayList<KeyValue<Uri>> entries) {
        mManager = manager;
        mContext = mManager.getContext();
        mEntries = entries;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ActionList.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_action_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final KeyValue<Uri> e = mEntries.get(position);
        final int position_ = position;
        LinearLayout parent = holder.mLinearLayout;
        final EditText editKey = (EditText)parent.getChildAt(0);
        final ImageButton editValue = (ImageButton)parent.getChildAt(1);
        editKey.clearFocus();
        editKey.setText(e.key, TextView.BufferType.EDITABLE);
        Uri imageUri = (Uri)e.value;
        if(imageUri != null) {
            Picasso.with(mContext)
                    .load(imageUri)
                    .fit()
                    .centerCrop()
                    .into(editValue);
        } else {
            Picasso.with(mContext)
                    .load(android.R.color.transparent)
                    .into(editValue);
        }
        if(holder.mWatcher != null) {
            editKey.removeTextChangedListener(holder.mWatcher);
        }
        holder.mWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(((CreateActivity)mManager).getCurrentFocus() == editKey) {
                    String key = s.toString();
                    Log.d(TAG,"onTextChanged: " + key + "at position: " + position_);
                    e.key = key;
                }
            }
        };

        editKey.addTextChangedListener(holder.mWatcher);

        editValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick");
                // Get image Uri and save to entry.
                mManager.getPic(position_);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    static interface ActionListManager {
        Context getContext();
        void getPic(int position);
    }
}