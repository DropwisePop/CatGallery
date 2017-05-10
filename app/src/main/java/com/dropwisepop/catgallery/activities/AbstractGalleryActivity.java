package com.dropwisepop.catgallery.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is the foundation for activities that require media store access. It provides
 * methods to retrieve and delete images from the mediastore and stores retrieved data in a static
 * class. It ensures that the data is updated when the user navigates away from and back to the
 * activity.
 */
public abstract class AbstractGalleryActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{

    //region Variables
    static final String KEY_SORT_ORDER = "com.dropwisepop.catgallery.KEY_SORT_ORDER";
    static final String KEY_PREFERENCES_SORT_ORDER = "com.dropwisepop.catgallery.KEY_PREFERENCES_SORT_ORDER";
    private static final int MEDIASTORE_LOADER_ID = 0;

    enum SortOrder { ASCENDING, DESCENDING, SHUFFLED;};
    private SortOrder sSortOrder;

    private static ArrayList<String> sDataList = new ArrayList<>();
    //endregion


    //region Lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){
            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            sSortOrder = SortOrder.values()[preferences.getInt(KEY_PREFERENCES_SORT_ORDER, 0)];
        } else {
            sSortOrder = SortOrder.values()[savedInstanceState.getInt(KEY_SORT_ORDER, 0)];
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SORT_ORDER, sSortOrder.ordinal());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restartLoader();
    }
    //endregion


    //region Loader
    public void initializeLoader() {
        getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
    }

    public void restartLoader() {
        getSupportLoaderManager().restartLoader(MEDIASTORE_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
        };

        String selectionClause = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

        String sortOrder = null;
        switch (sSortOrder){
            case ASCENDING:
                sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " ASC";
                break;
            case DESCENDING:
                sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
                break;
            case SHUFFLED:
                sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
                //sDataList is then shuffled when cursor finished loading
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
        if (sSortOrder != SortOrder.SHUFFLED){
            sDataList.clear();
            while (cursor.moveToNext()){
                int dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                sDataList.add(cursor.getString(dataIndex));
            }
        } else {
            if (getDataList().isEmpty()){   //if empty, populate then shuffle
                while (cursor.moveToNext()){
                    int dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    sDataList.add(cursor.getString(dataIndex));
                }
                Collections.shuffle(sDataList);
            } else {                        //else, add new data, remove old data
                ArrayList<String> updatedDataList = new ArrayList<String>();
                while (cursor.moveToNext()){
                    int dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    String dataString = cursor.getString(dataIndex);
                    updatedDataList.add(dataString);
                }

                for (String dataString: updatedDataList) {  //add new data
                    if (!sDataList.contains(dataString)) { sDataList.add(dataString); }
                }
                ArrayList<String> valuesToRemove = new ArrayList<>();
                for (String dataString: sDataList){         //remove deleted data
                    if (!updatedDataList.contains(dataString)) { valuesToRemove.add(dataString); }
                }
                sDataList.removeAll(valuesToRemove);
            }
        }
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //empty
    }

    //endregion


    //region sDataList Helpers
    public ArrayList<String> getDataList() {
        return sDataList;
    }

    public Uri getUriWithFilePrefixFromDataList(int position) {
        String dataString = sDataList.get(position);
        return Uri.parse("file://" + dataString);
    }

    public void deleteImage(String dataStringToDelete){
        String[] projection = { MediaStore.Images.Media._ID };
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[] { dataStringToDelete };

        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null);

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri uriToDelete = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(uriToDelete, null, null);

            getDataList().remove(dataStringToDelete);
        }
        cursor.close();
    }

    public void deleteImages(ArrayList<String> dataStringsToDelete){
        for (String dataString : dataStringsToDelete){
            String[] projection = { MediaStore.Images.Media._ID };
            String selection = MediaStore.Images.Media.DATA + " = ?";
            String[] selectionArgs = new String[] { dataString };

            Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null);

            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri uriToDelete = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                contentResolver.delete(uriToDelete, null, null);

                getDataList().remove(dataString);
            }
            cursor.close();
        }
    }
    //endregion


    //region SortOrder Getters and Setters
    public void setSortOrder(SortOrder sortOrder) { //TODO: move to ImageData
        sSortOrder = sortOrder;

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_PREFERENCES_SORT_ORDER, sortOrder.ordinal());
        editor.apply();

        restartLoader();
        invalidateOptionsMenu();
    }

    public void shuffleDataList(){
        Collections.shuffle(sDataList);
    }

    public SortOrder getSortOrder() {
        return sSortOrder;
    }
    //endregion


    //region Other
    int map(int x, int in_min, int in_max, int out_min, int out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
    //endregion
}
