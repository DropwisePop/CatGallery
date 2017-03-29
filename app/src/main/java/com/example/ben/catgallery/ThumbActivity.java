package com.example.ben.catgallery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;

/**
 * ThumbActivity is the main screen of TheCatGallery.
 */
public class ThumbActivity extends AbstractGalleryActivity {

    //region Member Variables
    public static String KEY_PAGER_POSITION_RESULT = "com.example.ben.thegallery.PAGER_POSITION_RESULT";
    public static final String EXTRA_THUMB_POSITION = "com.example.ben.thegallery.EXTRA_THUMB_POSITION";
    private static final int REQUEST_CODE_PAGER_POSITION = 1;
    private static final int GOOD_THUMB_SIZE_IN_PIXELS = 480;
    RecyclerView mRecyclerView;
    //endregion

    //region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     //needs to second so super can find the toolbar
        setContentView(R.layout.activity_thumb);

        if (savedInstanceState == null){
            checkReadExternalStoragePermission();   //if permission is granted, launches loader
        } else {
            initializeLoader();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setToolbar(toolbar, true);

        mRecyclerView = (RecyclerView) findViewById(R.id.thumb_recyclerview);
        mRecyclerView.setAdapter(new ThumbAdapter(this));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getGoodSpanCount()));
    }
    //endregion

    //region Loader Callbacks
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }
    //endregion

    //region Methods for Activity Interaction
    public void startFullscreenActivity(int position) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        intent.putExtra(EXTRA_THUMB_POSITION, position);
        startActivityForResult(intent, REQUEST_CODE_PAGER_POSITION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //restartLoader();
        if (requestCode == REQUEST_CODE_PAGER_POSITION) {
            if (resultCode == Activity.RESULT_OK) {
                int position = data.getIntExtra(KEY_PAGER_POSITION_RESULT, 0);
                mRecyclerView.getLayoutManager().scrollToPosition(position);
            }
        }
    }
    //endregion

    //region Helper Methods
    private int getGoodSpanCount() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        return (screenWidth / GOOD_THUMB_SIZE_IN_PIXELS);
    }
    //endregion

}
