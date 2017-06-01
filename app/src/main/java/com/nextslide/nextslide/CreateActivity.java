package com.nextslide.nextslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import java.util.AbstractMap;
import java.util.ArrayList;

public class CreateActivity extends Activity implements ActionList.ActionListManager {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<KeyValue<Uri>> mEntries;
    private String TAG = "Create Activity";
    private int mImagePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_create_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Display list of all entries.
        mEntries = new ArrayList<KeyValue<Uri>>();
        mAdapter = new ActionList(this, mEntries);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void createEntry(View view) {
        mEntries.add(new KeyValue<Uri>());
        mAdapter.notifyDataSetChanged();
    }

    public void savePresentation(View view) {
        // Create a new Presentation object.
        String name = ((EditText)findViewById(R.id.editText2)).getText().toString();
        String description = ((EditText)findViewById(R.id.editText4)).getText().toString();

        if(name.isEmpty()) return;  // Presentation name is required.

        Presentation presentation = new Presentation(name, description);

        for(KeyValue<Uri> e : mEntries)
        {
            String key = e.key;
            Uri image = e.value;
            if(!key.isEmpty() && image != null)
            {
                presentation.addAction(key, new Presentation.ImageAction(image));
            }
        }

        // Send the new presentation back to MainActivity.
        Intent returnIntent = new Intent();
        Bundle presentationBundle = new Bundle();
        presentationBundle.putParcelable("presentation_id", presentation);
        returnIntent.putExtras(presentationBundle);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 1111 && resultCode == RESULT_OK && resultData != null) {
            Uri selectedImage = resultData.getData();
            mEntries.get(mImagePosition).value = selectedImage;
            mAdapter.notifyDataSetChanged();
        }
    }

    // Implement ActionListManager interface.
    public Context getContext() {
        return getApplicationContext();
    }

    public void getPic(int position) {
        mImagePosition = position;
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.setType("image/*");
        startActivityForResult(intent, 1111);
    }

}
