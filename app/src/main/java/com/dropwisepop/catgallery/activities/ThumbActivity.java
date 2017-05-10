package com.dropwisepop.catgallery.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.dropwisepop.catgallery.adapters.ThumbAdapter;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerView;
import com.dropwisepop.catgallery.util.Util;

import java.util.ArrayList;

import static com.dropwisepop.catgallery.activities.FullscreenActivity.EXTRA_PAGER_POSITION_RESULT;


/**
 * ThumbActivity is the main screen of TheCatGallery.
 */
public class ThumbActivity extends AbstractGalleryActivity {

    //region Variables
    private static final String KEY_PREFERENCES_THUMB_BASE_COLOR = "com.dropwisepop.catgallery.KEY_PREFERENCES_THUMB_BASE_COLOR";
    private static final String KEY_GRID_LAYOUT_MANAGER_SAVED_STATE = "com.dropwisepop.catgallery.KEY_GRID_LAYOUT_MANAGER_SAVED_STATE";
    private static final String KEY_THUMB_BASE_COLOR = "com.dropwisepop.catgallery.KEY_THUMB_BASE_COLOR";

    static final String EXTRA_THUMB_POSITION = "com.dropwisepop.catgallery.EXTRA_THUMB_POSITION";

    private static final int REQUEST_CODE_WRITE_EXTERNAL = 0;
    private static final int REQUEST_CODE_PAGER_POSITION = 1;
    private static final int REQUEST_CODE_SHARE_INTENT = 2;

    private static final int GOOD_THUMB_SIZE_IN_PIXELS = 480;

    private DragSelectRecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private ThumbAdapter mThumbAdapter;
    private TextView mCountTextView;

    private int mBaseColor;

    private int mCurrentDataListSize;

    private Parcelable mGridLayoutManagerSavedState;    //TODO: what does this do?
    //endregion


    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumb);

        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mRecyclerView = (DragSelectRecyclerView) findViewById(R.id.thumb_recyclerview);
        mThumbAdapter = new ThumbAdapter(this);
        mRecyclerView.setAdapter(mThumbAdapter);
        mThumbAdapter.setSelectionListener(new DragSelectRecyclerViewAdapter.SelectionListener() {
            @Override
            public void onDragSelectionChanged(int count) {
                mCountTextView.setText(mThumbAdapter.getSelectedCount() + "/" + mThumbAdapter.getItemCount());
                //the menu must be reset in the following scenarios...
                if (count == 0 || count == 1
                        || count == 5 || count == 6
                        || count == mThumbAdapter.getItemCount()
                        || count == mThumbAdapter.getItemCount() - 1){
                    invalidateOptionsMenu();
                }
            }
        });
        mGridLayoutManager = new GridLayoutManager(this, getGoodSpanCount());
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mCountTextView = (TextView) findViewById(R.id.main_toolbar_text);

        mCurrentDataListSize = 0;

        if (savedInstanceState == null){
            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            int baseColor = preferences.getInt(KEY_PREFERENCES_THUMB_BASE_COLOR, -1);

            if (baseColor == -1){   //first time starting application
                setBaseColor(Color.WHITE);
                checkWriteExternalStoragePermission();
            } else {
                setBaseColor(baseColor);
                initializeLoader();
            }
        } else {
            mThumbAdapter.restoreInstanceState(savedInstanceState);
            setBaseColor(savedInstanceState.getInt(KEY_THUMB_BASE_COLOR));
            mGridLayoutManagerSavedState = savedInstanceState.getParcelable(KEY_GRID_LAYOUT_MANAGER_SAVED_STATE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mThumbAdapter.saveInstanceState(outState);
        outState.putInt(KEY_THUMB_BASE_COLOR, mBaseColor);
        outState.putParcelable(KEY_GRID_LAYOUT_MANAGER_SAVED_STATE, mGridLayoutManager.onSaveInstanceState());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCurrentDataListSize = getDataList().size();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    //endregion


    //region Permissions
    protected void checkWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                initializeLoader();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_EXTERNAL);
            }
        } else {
            initializeLoader();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_WRITE_EXTERNAL:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeLoader();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //endregion


    //region Loader
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);


        int prevDataListSize = mCurrentDataListSize;
        mCurrentDataListSize = getDataList().size();

        if (mCurrentDataListSize != prevDataListSize){
            mThumbAdapter.clearSelected();
        }

        mThumbAdapter.notifyDataSetChanged();

        /*  TODO: find out what this does!
        if (mGridLayoutManagerSavedState != null) {
            mGridLayoutManager.onRestoreInstanceState(mGridLayoutManagerSavedState);
            mGridLayoutManagerSavedState = null;
        }
        */
    }
    //endregion


    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(mThumbAdapter == null || mThumbAdapter.getSelectedCount() == 0) {
            inflater.inflate(R.menu.thumb_default, menu);
            mCountTextView.setText(mThumbAdapter.getItemCount() + " IMAGES");

            switch (getSortOrder()){
                case ASCENDING:
                    //TODO: instead, set icon
                    //menu.findItem(R.id.action_order_ascending).setTitle("ascending <-");
                    break;
                case DESCENDING:
                    //menu.findItem(R.id.action_order_descending).setTitle("descending <-");
                    break;
                case SHUFFLED:
                    //menu.findItem(R.id.action_order_shuffled).setTitle("shuffled <-");
                    break;
            }

        } else {
            inflater.inflate(R.menu.thumb_selection_mode,  menu);

            if (mThumbAdapter.getSelectedCount() > 5){
                menu.findItem(R.id.action_share).setEnabled(false);
            }
            if ((mThumbAdapter.getSelectedCount() == mThumbAdapter.getItemCount())){
                menu.findItem(R.id.action_select_all).setEnabled(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
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
            case R.id.action_order_shuffled:
                setSortOrder(SortOrder.SHUFFLED);
                shuffleDataList();
                break;
            case R.id.action_background_white:
                setBaseColor(Color.WHITE);
                break;
            case R.id.action_background_grey:
                setBaseColor(Color.GRAY);
                break;
            case R.id.action_background_black:
                setBaseColor(Color.BLACK);
                break;
            case R.id.action_share:
                ArrayList<Uri> imageUris = new ArrayList<Uri>();
                for (int i = 0; i < mThumbAdapter.getSelectedCount(); i++){
                    imageUris.add(getUriWithFilePrefixFromDataList(mThumbAdapter.getSelectedIndices()[i]));
                }
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                shareIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(shareIntent, "share..."),REQUEST_CODE_SHARE_INTENT);
                break;
            case R.id.action_delete:
                PopupMenu confirmDelete = new PopupMenu(this, findViewById(R.id.action_delete));
                confirmDelete.getMenuInflater().inflate(R.menu.popup_confirm_delete, confirmDelete.getMenu());
                confirmDelete.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Integer[] selectedIndices = mThumbAdapter.getSelectedIndices();
                        ArrayList<String> dataStringsToDelete = new ArrayList<String>();
                        for (int i = 0; i < selectedIndices.length; i++){
                            String dataString = getDataList().get(selectedIndices[i]);
                            dataStringsToDelete.add(dataString);
                        }
                        deleteImages(dataStringsToDelete);
                        mThumbAdapter.notifyDataSetChanged();
                        mThumbAdapter.clearSelected();
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


    //region ImageView Touch
    public void onThumbClicked(int index) {
        if(mThumbAdapter.getSelectedCount() == 0){
            startFullscreenActivity(index);
        } else {
            mThumbAdapter.toggleSelected(index);
        }
    }

    public void onThumbLongClicked(int index) {
        mRecyclerView.setDragSelectActive(true, index);
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
        if (requestCode == REQUEST_CODE_SHARE_INTENT){
            mThumbAdapter.clearSelected();
        }
        if (requestCode == REQUEST_CODE_PAGER_POSITION) {
            if (resultCode == Activity.RESULT_OK) {
                int position = data.getIntExtra(EXTRA_PAGER_POSITION_RESULT, 0);
                mRecyclerView.getLayoutManager().scrollToPosition(position);
            }
        }
    }
    //endregion


    //region setBaseColor
    public void setBaseColor(int baseColor) {
        mBaseColor = baseColor;
        findViewById(android.R.id.content).setBackgroundColor(baseColor);

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_PREFERENCES_THUMB_BASE_COLOR, mBaseColor);
        editor.apply();
    }
    //endregion


    //region Other
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
