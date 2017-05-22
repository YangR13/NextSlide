package com.nextslide.nextslide;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PresentationList.PresentationListManager {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Presentation> mPresentations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Display list of all presentations.
        mPresentations = new ArrayList<Presentation>();
        mAdapter = new PresentationList(this, mPresentations);
        mRecyclerView.setAdapter(mAdapter);

        // TODO: For each presentation, give user option to edit or present.
        // TODO: Create presentation builder activity. Activity allows users to add files from gallery to resource folder (maybe, might be better solutions). Activity also displays existing associations.

        // TEST: Build a test presentation list!
        Presentation p1 = new Presentation("Test Presentation 1", "Description 1");
        p1.addAction("test", new Presentation.ImageAction(R.drawable.karpkarp));
        Presentation p2 = new Presentation("Test Presentation 2", "Description 2");
        p2.addAction("test", new Presentation.ImageAction(R.drawable.karpkarp));

        mPresentations.add(p1);
        mPresentations.add(p2);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecognitionActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Impement PresentationListManager.
    public void startPresentation(Presentation presentation) {
        // Create an PresentationActivity Intent.
        Intent intent = new Intent(this, PresentationActivity.class);
        Bundle presentationBundle = new Bundle();
        presentationBundle.putParcelable("presentation_id", presentation);
        intent.putExtras(presentationBundle);
        this.startActivity(intent);
    }
}
