package com.dropwisepop.catgallery.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.dropwisepop.catgallery.adapters.ThumbAdapter;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerView;

/**
 * ThumbActivity is the main screen of TheCatGallery.
 */
public class ThumbActivity extends AbstractGalleryActivity {

    //region Variables
    public static final String KEY_PAGER_POSITION_RESULT = "com.dropwisepop.catgallery.PAGER_POSITION_RESULT";
    public static final String EXTRA_THUMB_POSITION = "com.dropwisepop.catgallery.EXTRA_THUMB_POSITION";

    private static final int REQUEST_CODE_PAGER_POSITION = 1;
    private static final int GOOD_THUMB_SIZE_IN_PIXELS = 480;

    private static int sCursorSize;
    private static int sPreviousCursorSize;

    private DragSelectRecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private ThumbAdapter mThumbAdapter;

    Parcelable mGridLayoutManagerSavedState;
    //endregion


    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumb);

        mRecyclerView = (DragSelectRecyclerView) findViewById(R.id.thumb_recyclerview);
        mThumbAdapter = new ThumbAdapter(this);
        mThumbAdapter.setOnTouchListener(new ThumbAdapter.OnTouchListener() {
            @Override
            public void onThumbClicked(int index) {
                if(mThumbAdapter.getSelectedCount() == 0){
                    startFullscreenActivity(index);
                } else {
                    mThumbAdapter.toggleSelected(index);
                }
            }

            @Override
            public void onThumbLongClicked(int index) {
                mRecyclerView.setDragSelectActive(true, index);
            }
        });
        mRecyclerView.setAdapter(mThumbAdapter);
        mGridLayoutManager = new GridLayoutManager(this, getGoodSpanCount());
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mThumbAdapter.setSelectionListener(new DragSelectRecyclerViewAdapter.SelectionListener() {
            @Override
            public void onDragSelectionChanged(int count) {
                invalidateOptionsMenu();    //TODO: change only when we go from 0 to another num
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setToolbarAsActionBar(toolbar, true);
        //toolbar must come after adapter so menu knows if we are in selection mode

        if (savedInstanceState == null){
            checkReadExternalStoragePermission();   //if permission is granted, launches loader
        } else {
            mThumbAdapter.restoreInstanceState(savedInstanceState);
            mGridLayoutManagerSavedState = savedInstanceState.getParcelable(EXTRA_THUMB_POSITION);

            if(getSortOrder() != SortOrder.RANDOM) {
                restartLoader();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        sCursorSize = getCursorCount();
        mThumbAdapter.saveInstanceState(outState);
        outState.putParcelable(EXTRA_THUMB_POSITION, mGridLayoutManager.onSaveInstanceState());
    }
    //endregion


    //region Loader Callbacks
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);

        sPreviousCursorSize = sCursorSize;
        sCursorSize = getCursorCount();

        if (sCursorSize != sPreviousCursorSize){
            mThumbAdapter.clearSelected();
        }

        mThumbAdapter.notifyDataSetChanged();
        if (mGridLayoutManagerSavedState != null) {
            mGridLayoutManager.onRestoreInstanceState(mGridLayoutManagerSavedState);
            mGridLayoutManagerSavedState = null;
        }
    }
    //endregion


    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(mThumbAdapter.getSelectedCount() == 0) {
            inflater.inflate(R.menu.thumb_default, menu);
        } else {
            inflater.inflate(R.menu.thumb_selection_mode,  menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_toggle_fit:
                mThumbAdapter.toggleFitMode();
                break;
            case R.id.action_order_ascending:
                setSortOrder(SortOrder.ASCENDING);
                break;
            case R.id.action_order_descending:
                setSortOrder(SortOrder.DESCENDING);
                break;
            case R.id.action_order_random:
                setSortOrder(SortOrder.RANDOM);
                break;
            case R.id.action_delete:
                PopupMenu confirmDelete = new PopupMenu(this, findViewById(R.id.action_delete));
                confirmDelete.getMenuInflater().inflate(R.menu.popup_confirm_delete, confirmDelete.getMenu());
                confirmDelete.show();
                break;
        }
        return true;
    }
    //endregion


    //region Activity Interaction
    public void startFullscreenActivity(int position) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        intent.putExtra(EXTRA_THUMB_POSITION, position);
        startActivityForResult(intent, REQUEST_CODE_PAGER_POSITION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
        if (mThumbAdapter.getSelectedCount() > 0){
            mThumbAdapter.clearSelected();
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
