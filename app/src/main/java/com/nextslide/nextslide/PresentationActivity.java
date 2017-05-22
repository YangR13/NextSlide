package com.nextslide.nextslide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

/**
 * Created by yangren on 5/14/17.
 */

public class PresentationActivity extends AppCompatActivity {
    private Presentation mPresentation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

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

        ViewGroup parent = (ViewGroup) findViewById(R.id.presentation_view);
        mPresentation.setParentViewGroup(parent);

        // Test!
        mPresentation.performAction("test");
    }
}
