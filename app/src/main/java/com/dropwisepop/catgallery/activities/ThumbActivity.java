package com.dropwisepop.catgallery.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;

import com.dropwisepop.catgallery.util.Util;
import com.dropwisepop.catgallery.adapters.ThumbAdapter;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerView;

/**
 * ThumbActivity is the main screen of TheCatGallery.
 */
public class ThumbActivity extends AbstractGalleryActivity
        implements ThumbAdapter.ClickListener{

    //region Variables
    public static final String KEY_PAGER_POSITION_RESULT = "com.dropwisepop.catgallery.PAGER_POSITION_RESULT";
    public static final String EXTRA_THUMB_POSITION = "com.dropwisepop.catgallery.EXTRA_THUMB_POSITION";

    private static final int REQUEST_CODE_PAGER_POSITION = 1;
    private static final int GOOD_THUMB_SIZE_IN_PIXELS = 480;

    private DragSelectRecyclerView mRecyclerView;
    private ThumbAdapter mAdapter;
    //endregion


    //region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumb);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setToolbarAsActionBar(toolbar, true);

        mRecyclerView = (DragSelectRecyclerView) findViewById(R.id.thumb_recyclerview);
        mAdapter = new ThumbAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getGoodSpanCount()));

        if (savedInstanceState == null){
            checkReadExternalStoragePermission();   //if permission is granted, launches loader
        } else {
            initializeLoader();
            mAdapter.restoreInstanceState(savedInstanceState);
            //TODO: if the cursor changes at all, the selection shifts
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveInstanceState(outState);
    }

    //endregion


    //region Menu
    
    //endregion


    //region Loader Callbacks
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }
    //endregion


    //region Click Callbacks
    @Override
    public void onClick(int index) {
        if(mAdapter.getSelectedCount() == 0){
            startFullscreenActivity(index);
        } else {
            mAdapter.toggleSelected(index);
            hideStatusBar();
        }
    }

    @Override
    public void onLongClick(int index) {
        Log.d(Util.TAG, "onLongClick at " + index);
        mRecyclerView.setDragSelectActive(true, index);
        hideStatusBar();
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


    //region Other Methods


    @Override
    public void onBackPressed() {
        if (mAdapter.getSelectedCount() > 0){
            mAdapter.clearSelected();
        } else {
            super.onBackPressed();
        }
    }

    private int getGoodSpanCount() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        return (screenWidth / GOOD_THUMB_SIZE_IN_PIXELS);
    }
    //endregion

}
