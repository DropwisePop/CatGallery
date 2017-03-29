package com.example.ben.catgallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

/**
 * This class is the foundation for activities that require media store access. It also allows
 * the user to set a toolbar and its intial state.
 */
public abstract class AbstractGalleryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //region Member Variables
    private static final int MEDIASTORE_LOADER_ID =  0;
    private static final int REQUEST_CODE_READ_EXTERNAL = 0;
    private Cursor mCursor;
    private Toolbar mToolbar;
    private boolean mToolbarShown;
    //endregion

    //region Lifecycle Methods
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

    //region Permission Methods
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
    protected void setToolbar(Toolbar toolbar, boolean showToolbar){
        mToolbar = toolbar;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setToolbarShown(showToolbar);
    }

    protected void setToolbarShown(boolean showToolbar){
        if (showToolbar){
            showToolbar();
        } else {
            hideToolbar();
        }
    }

    protected void showToolbar() {
        mToolbar.animate().translationY(0).setDuration(0);
        mToolbarShown = true;
    }

    protected void hideToolbar() {
        mToolbar.animate().translationY(mToolbar.getBottom()).setDuration(0);
        mToolbarShown = false;
    }

    public boolean toolbarShown() {
        return mToolbarShown;
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    //endregion

}
