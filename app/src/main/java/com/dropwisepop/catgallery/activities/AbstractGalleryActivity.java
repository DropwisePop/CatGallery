package com.dropwisepop.catgallery.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import com.dropwisepop.catgallery.util.Util;

import java.io.File;

import static com.dropwisepop.catgallery.activities.ThumbActivity.KEY_PREFERENCES_SORT_ORDER;

/**
 * This class is the foundation for activities that require media store access. It also allows
 * the user to set a toolbar and its initial state.
 */
public abstract class AbstractGalleryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //region Variables
    public enum SortOrder { ASCENDING, DESCENDING, RANDOM;};
    private static SortOrder sSortOrder;

    private static final int REQUEST_CODE_WRITE_EXTERNAL = 0;
    private static final int MEDIASTORE_LOADER_ID = 0;

    private static Cursor sCursor;

    private Toolbar mToolbar;
    //endregion


    //region Lifecycle
    @Override
    protected void onRestart() {
        super.onRestart();
        if (sSortOrder != SortOrder.RANDOM){
            restartLoader();
        }
    }
    //endregion


    //region Permissions
    protected void checkReadExternalStoragePermission() {
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


    //region Loader Callbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };

        String selectionClause = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

        String sortOrder = "ERROR!";
        switch (sSortOrder){
            case ASCENDING:
                sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " ASC";
                break;
            case DESCENDING:
                sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
                break;
            case RANDOM:
                sortOrder = "RANDOM()";
                break;
        }

        return new CursorLoader(
                this,
                MediaStore.Files.getContentUri("external"),
                projection,
                selectionClause,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        sCursor = cursor;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (sCursor.getCount() == 0){   //no idea if this is valid, but it works for now 2017-02-05
            sCursor = null;
        }
    }
    //endregion


    //region Cursor
    public Uri getUriFromMediaStore(int position) {
        int dataIndex = sCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        sCursor.moveToPosition(position);

        String dataString = sCursor.getString(dataIndex);
        return Uri.parse("file://" + dataString);
    }

    public void initializeLoader() {
        getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
    }

    public void restartLoader() {
        getSupportLoaderManager().restartLoader(MEDIASTORE_LOADER_ID, null, this);
    }

    public Cursor getCursor() {
        return sCursor;
    }

    public int getCursorCount() {
        return (sCursor == null ? 0 : sCursor.getCount());
    }
    //endregion


    //region SortOrder Getters and Setters
    public void setSortOrder(SortOrder sortOrder, boolean restartLoader) {
        sSortOrder = sortOrder;

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_PREFERENCES_SORT_ORDER, sortOrder.ordinal());
        editor.apply();

        if (restartLoader){
            restartLoader();
        }
    }

    public static SortOrder getSortOrder() {
        return sSortOrder;
    }
    //endregion


    //region Toolbar and Status Bar
    public void setToolbarAsActionBar(Toolbar toolbar, boolean showToolbar) {
        mToolbar = toolbar;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setToolbarShown(showToolbar, 0);
    }

    public void setToolbarShown(boolean showToolbar, int animationDuration) {
        if (showToolbar) {
            showToolbar(animationDuration);
        } else {
            hideToolbar(animationDuration);
        }
    }

    public void showToolbar(int animationDuration) {
        mToolbar.animate().translationY(0).setDuration(animationDuration);
        showStatusBar();
    }

    public void hideToolbar(int animationDuration) {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setDuration(animationDuration);
        hideStatusBar();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void showStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    //endregion


    //region Other
    public void deleteImageFromMediaStore(File file){
        // Set up the projection (we only need the ID)
        String[] projection = { MediaStore.Images.Media._ID };

        // Match on the file path
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[] { file.getAbsolutePath() };

        // Query for the ID of the media matching the file path
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            // We found the ID. Deleting the item via the content provider will also remove the file
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
        } else {
            // File not found in media store DB
        }
        c.close();
    }
    //endregion
}
