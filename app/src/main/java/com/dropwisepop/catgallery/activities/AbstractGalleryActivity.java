package com.dropwisepop.catgallery.activities;

import android.Manifest;
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
import android.view.View;

/**
 * This class is the foundation for activities that require media store access. It also allows
 * the user to set a toolbar and its initial state.
 */
public abstract class AbstractGalleryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //region Variables
    public enum SortOrder {ASCENDING, DESCENDING, RANDOM};
    private static SortOrder sSortOrder = SortOrder.RANDOM;

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

        String sortOrder = "ERROR? I HOPE!";
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
        sCursor = null;
    }
    //endregion


    //region Cursor
    public Uri getUriFromMediaStore(int position) {
        int dataIndex = sCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        sCursor.moveToPosition(position);

        String dataString = sCursor.getString(dataIndex);
        return Uri.parse("file://" + dataString);
    }

    public Uri getUriFromMediaStoreNoFileSlashSlash(int position) {
        int dataIndex = sCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        sCursor.moveToPosition(position);

        String dataString = sCursor.getString(dataIndex);
        return Uri.parse(dataString);
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
    public void setSortOrder(SortOrder sortOrder) {
        sSortOrder = sortOrder;
        restartLoader();
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
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);   // | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    public void showStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    //endregion

}
