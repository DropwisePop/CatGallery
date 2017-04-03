package com.dropwisepop.catgallery.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.dropwisepop.catgallery.util.Util;

/**
 * This class is the foundation for activities that require media store access. It also allows
 * the user to set a toolbar and its initial state.
 */
public abstract class AbstractGalleryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //region Variables
    private static final int MEDIASTORE_LOADER_ID =  0;
    private static final int REQUEST_CODE_READ_EXTERNAL = 0;
    private static int SCREEN_WIDTH;
    private static int SCREEN_HEIGHT;

    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    private Cursor mCursor;
    private Toolbar mToolbar;
    private boolean mToolbarShown;
    //endregion


    //region Lifecycle


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SCREEN_WIDTH == 0){
            SCREEN_WIDTH = getScreenWidth();
            Log.d(Util.TAG, "SCREEN_WIDTH set to..." + SCREEN_WIDTH);
        }
        if (SCREEN_HEIGHT == 0){
            SCREEN_HEIGHT = getScreenHeight();
            Log.d(Util.TAG, "SCREEN_HEIGHT set to..." + SCREEN_HEIGHT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideStatusBar();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restartLoader();
    }
    //endregion


    //region Permissions
    protected void checkReadExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                initializeLoader();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_EXTERNAL);
            }
        } else {
            initializeLoader();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeLoader();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //endregion


    //region Loader Callbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

        return new CursorLoader(
                this,
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }
    //endregion


    //region Cursor Helper Methods
    public Uri getUriFromMediaStore(int position){
        int dataIndex = mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        mCursor.moveToPosition(position);

        String dataString = mCursor.getString(dataIndex);
        return Uri.parse("file://" + dataString);
    }

    public void initializeLoader(){
        getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
    }

    public void restartLoader() {
        getSupportLoaderManager().restartLoader(MEDIASTORE_LOADER_ID, null, this);
    }

    public Cursor getCursor(){
        return mCursor;
    }
    //endregion


    //region Toolbar and Status Bar
    public void setToolbarAsActionBar(Toolbar toolbar, boolean showToolbar){
        mToolbar = toolbar;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setToolbarShown(showToolbar, 0);
    }

    public void setToolbarShown(boolean showToolbar, int animationDuration){
        if (showToolbar){
            showToolbar(animationDuration);
        } else {
            hideToolbar(animationDuration);
        }
    }

    public void showToolbar(int animationDuration) {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            mToolbar.animate().translationY(0).setDuration(animationDuration);
        } else {
            mToolbar.animate().translationY(0).setDuration(animationDuration); //TODO: THIS bit right here is not working...toolbar does not appear
        }
        mToolbarShown = true;
    }

    public void hideToolbar(int animationDuration) {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            mToolbar.animate().translationY(SCREEN_WIDTH).setDuration(animationDuration);
        } else {
            mToolbar.animate().translationY(SCREEN_HEIGHT).setDuration(animationDuration);
        }
        mToolbarShown = false;
        hideStatusBar();
    }

    public boolean isToolbarShown() {
        return mToolbarShown;
    }

    public void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private int getScreenHeight(){
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.heightPixels;
    }

    private int getScreenWidth(){
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.widthPixels;
    }
    //endregion

}
