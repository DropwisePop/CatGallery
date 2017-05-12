package com.dropwisepop.catgallery.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.dropwisepop.catgallery.adapters.ThumbAdapter;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import static com.dropwisepop.catgallery.activities.FullscreenActivity.EXTRA_PAGER_POSITION_RESULT;


/**
 * ThumbActivity is the main screen of TheCatGallery.
 */
public class ThumbActivity extends AbstractGalleryActivity {

    //region Variables
    private static final String KEY_GRID_LAYOUT_MANAGER_SAVED_STATE = "KEY_GRID_LAYOUT_MANAGER_SAVED_STATE";
    private static final String KEY_THUMB_BASE_COLOR = "KEY_THUMB_BASE_COLOR";

    static final String EXTRA_THUMB_POSITION = "com.dropwisepop.catgallery.EXTRA_THUMB_POSITION";

    private static final int REQUEST_CODE_WRITE_EXTERNAL = 0;
    private static final int REQUEST_CODE_PAGER_POSITION = 1;
    private static final int REQUEST_CODE_SHARE_INTENT = 2;

    private static final int GOOD_THUMB_SIZE_IN_PIXELS = 480;

    private DragSelectRecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private ThumbAdapter mThumbAdapter;
    private TextView mSelectionCountTextView;

    private int mBaseColor;
    private int mCurrentDataListSize;
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
                mSelectionCountTextView.setText(mThumbAdapter.getSelectedCount() +
                        "/" + mThumbAdapter.getItemCount());

                //the menu must be reset in the following scenarios...
                if (count == 0 || count == 1 || count == 5 || count == 6
                        || count == mThumbAdapter.getItemCount()
                        || count == mThumbAdapter.getItemCount() - 1){
                    invalidateOptionsMenu();
                }
            }
        });
        mGridLayoutManager = new GridLayoutManager(this, getGoodSpanCount());
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mSelectionCountTextView = (TextView) findViewById(R.id.main_toolbar_textview);

        if (savedInstanceState == null){
            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            int baseColor = preferences.getInt(KEY_THUMB_BASE_COLOR, -1);

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
    protected void onStop() {
        super.onStop();
        mCurrentDataListSize = getDataList().size();
    }

    @Override
    public void onBackPressed() {
        if (mThumbAdapter.getSelectedCount() > 0){
            mThumbAdapter.clearSelected();
        } else {
            super.onBackPressed();
        }
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

        int difference = mCurrentDataListSize - prevDataListSize;
        if (difference >=5 && prevDataListSize!= 0 && getSortOrder() == SortOrder.SHUFFLED){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_reshuffle, null);
            builder.setView(dialogView);
            final TextView reshuffleText = (TextView) dialogView.findViewById(R.id.reshuffle_text);
            reshuffleText.setText(difference + " images have been added. Reshuffle?");
            final AlertDialog shuffleDialog = builder.create();

            final Button reshuffleButton = (Button) dialogView.findViewById(R.id.dialog_reshuffle_button);
            reshuffleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collections.shuffle(getDataList());
                    mThumbAdapter.notifyDataSetChanged();
                    shuffleDialog.dismiss();
                }
            });

            shuffleDialog.show();
        }

        mThumbAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }
    //endregion


    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(mThumbAdapter == null || mThumbAdapter.getSelectedCount() == 0) {
            inflater.inflate(R.menu.thumb_default, menu);
            mSelectionCountTextView.setText(mThumbAdapter.getItemCount() + " IMAGES");
            switch (getSortOrder()){
                case ASCENDING:
                    menu.findItem(R.id.action_order_ascending).setIcon(R.drawable.ic_play_arrow);
                    break;
                case DESCENDING:
                    menu.findItem(R.id.action_order_descending).setIcon(R.drawable.ic_play_arrow);
                    break;
                case SHUFFLED:
                    menu.findItem(R.id.action_order_shuffled).setIcon(R.drawable.ic_play_arrow);
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
                Collections.shuffle(getDataList());
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
                ArrayList<Uri> imageUris = new ArrayList<>();
                for (int i = 0; i < mThumbAdapter.getSelectedCount(); i++){
                    imageUris.add(getUriWithFilePrefixFromDataList(mThumbAdapter.getSelectedIndices()[i]));
                }
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                shareIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(shareIntent, "share..."),
                        REQUEST_CODE_SHARE_INTENT);
                break;
            case R.id.action_delete:
                PopupMenu confirmDelete = new PopupMenu(this, findViewById(R.id.action_delete));
                confirmDelete.getMenuInflater()
                        .inflate(R.menu.popup_confirm_delete, confirmDelete.getMenu());
                confirmDelete.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Integer[] selectedIndices = mThumbAdapter.getSelectedIndices();
                        final ArrayList<String> dataStringsToDelete = new ArrayList<String>();
                        for (int i = 0; i < selectedIndices.length; i++){
                            String dataString = getDataList().get(selectedIndices[i]);
                            dataStringsToDelete.add(dataString);
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(ThumbActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        final View dialogView = inflater.inflate(R.layout.dialog_deletion_in_progress, null);
                        builder.setView(dialogView);
                        builder.setCancelable(false);

                        final ProgressBar progressBar = (ProgressBar) dialogView.findViewById(R.id.deletion_progress_bar);
                        progressBar.setMax(dataStringsToDelete.size());
                        final TextView percentageTextView = (TextView) dialogView.findViewById(R.id.deletion_percentage_text);
                        percentageTextView.setText(0 + "%");
                        final TextView countTextView = (TextView) dialogView.findViewById(R.id.deletion_count_text);
                        countTextView.setText("0/" + dataStringsToDelete.size());

                        final AlertDialog dialog = builder.create();
                        dialog.show();

                        new AsyncTask<Void, Integer, Void>(){
                            @Override
                            protected Void doInBackground(Void... params) {
                                for(int i = 0; i < dataStringsToDelete.size(); i++){
                                    deleteImage(dataStringsToDelete.get(i));
                                    publishProgress(i);
                                }
                                return null;
                            }

                            @Override
                            protected void onProgressUpdate(Integer... values) {
                                progressBar.setProgress(values[0]);
                                float percentage = (values[0] * 100.0f) /dataStringsToDelete.size();
                                percentageTextView.setText((int) Math.floor(percentage) + "%");
                                countTextView.setText(values[0] + "/" + dataStringsToDelete.size());
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                dialog.dismiss();
                                mThumbAdapter.notifyDataSetChanged();
                                mThumbAdapter.clearSelected();
                            }
                        }.execute();

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


    //region Thumb Touch Events
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
        editor.putInt(KEY_THUMB_BASE_COLOR, mBaseColor);
        editor.apply();
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
