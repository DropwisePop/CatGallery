package com.dropwisepop.catgallery.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import android.view.View;
import android.widget.TextView;

import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.dropwisepop.catgallery.adapters.ThumbAdapter;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerView;

import java.io.File;
import java.util.ArrayList;

/**
 * ThumbActivity is the main screen of TheCatGallery.
 */
public class ThumbActivity extends AbstractGalleryActivity {

    //region Variables
    public static final String KEY_PREFERENCES_SORT_ORDER = "com.dropwisepop.catgallery.KEY_PREFERENCES_SORT_ORDER";
    public static final String KEY_PAGER_POSITION_RESULT = "com.dropwisepop.catgallery.PAGER_POSITION_RESULT";
    public static final String EXTRA_THUMB_POSITION = "com.dropwisepop.catgallery.EXTRA_THUMB_POSITION";

    private static final int REQUEST_CODE_PAGER_POSITION = 1;
    private static final int GOOD_THUMB_SIZE_IN_PIXELS = 480;

    private static int sCursorSize;

    private DragSelectRecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private ThumbAdapter mThumbAdapter;

    private TextView mSelectionCountText;

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

        mSelectionCountText = (TextView) findViewById(R.id.mytoolbar_selection_count);
        mSelectionCountText.setVisibility(View.GONE);

        mThumbAdapter.setSelectionListener(new DragSelectRecyclerViewAdapter.SelectionListener() {
            @Override
            public void onDragSelectionChanged(int count) {
                mSelectionCountText.setText(mThumbAdapter.getSelectedCount() + "/" + mThumbAdapter.getItemCount());
                if (count <= 1){    //invalidate only when mode changes
                    invalidateOptionsMenu();
                } else if (mThumbAdapter.getItemCount() == mThumbAdapter.getItemCount()){
                    invalidateOptionsMenu();
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setToolbarAsActionBar(toolbar, true);
        //toolbar must come after adapter so menu knows we are not in selection mode

        if (savedInstanceState == null){
            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            int sortOrder = preferences.getInt(KEY_PREFERENCES_SORT_ORDER, -1);
            if (sortOrder == -1){   //first time running application
                setSortOrder(SortOrder.RANDOM, false);
            } else {
                setSortOrder(SortOrder.values()[sortOrder], false);
            }
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

        int previousCursorSize = sCursorSize;
        sCursorSize = getCursorCount();

        if (sCursorSize != previousCursorSize){
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
            mSelectionCountText.setVisibility(View.INVISIBLE);
        } else if (mThumbAdapter.getSelectedCount() == mThumbAdapter.getItemCount()){
            inflater.inflate(R.menu.thumb_selection_mode_select_all, menu);
        } else {
            inflater.inflate(R.menu.thumb_selection_mode,  menu);
            mSelectionCountText.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_background:
                //blah
                break;
            case R.id.action_toggle_fit:
                mThumbAdapter.toggleFitMode();
                break;
            case R.id.action_order_ascending:
                setSortOrder(SortOrder.ASCENDING, true);
                break;
            case R.id.action_order_descending:
                setSortOrder(SortOrder.DESCENDING, true);
                break;
            case R.id.action_order_random:
                setSortOrder(SortOrder.RANDOM, true);
                break;
            case R.id.action_share:
                ArrayList<Uri> imageUris = new ArrayList<Uri>();
                for (int i = 0; i < mThumbAdapter.getSelectedCount(); i++){
                    imageUris.add(getUriFromMediaStore(mThumbAdapter.getSelectedIndices()[i]));
                }
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                shareIntent.setType("image/*");
                startActivity(Intent.createChooser(shareIntent, "Share images to.."));
                mThumbAdapter.clearSelected();  //TODO: this clears right away; shouldn't
                break;
            case R.id.action_delete:
                PopupMenu confirmDelete = new PopupMenu(this, findViewById(R.id.action_delete));
                confirmDelete.getMenuInflater().inflate(R.menu.popup_confirm_delete, confirmDelete.getMenu());
                confirmDelete.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Integer[] selectedIndices = mThumbAdapter.getSelectedIndices();
                        for(int i = 0; i < selectedIndices.length; i++){
                            File fileToDelete = new File(getUriFromMediaStore(selectedIndices[i]).getPath());
                            if (fileToDelete.exists()){
                                deleteImageFromMediaStore(fileToDelete);
                            }
                        }
                        return true;
                    }
                });
                confirmDelete.show();
                break;
            case R.id.action_select_all:
                mThumbAdapter.selectAll();
                break;
            case R.id.action_deselect_all:
                mThumbAdapter.clearSelected();
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
