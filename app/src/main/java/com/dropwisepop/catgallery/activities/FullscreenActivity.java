package com.dropwisepop.catgallery.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.dropwisepop.catgallery.adapters.FullscreenPagerAdapter;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.util.Util;
import com.dropwisepop.catgallery.views.TouchImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.dropwisepop.catgallery.adapters.FullscreenPagerAdapter.VIEW_TAG;

public class FullscreenActivity extends AbstractGalleryActivity {

    //region Variables

    private static final String KEY_FULLSCREEN_BASE_COLOR = "KEY_FULLSCREEN_BASE_COLOR";
    private static final String KEY_STEP = "KEY_STEP";

    static final String EXTRA_PAGER_POSITION_RESULT = "com.dropwisepop.catgallery.EXTRA_PAGER_POSITION_RESULT";

    private ViewPager mViewPager;
    private FullscreenPagerAdapter mFullscreenPagerAdapter;
    private Toolbar mToolbar;
    private View mToolbarExtension;
    private TextView mCountTextView;

    private int mBaseColor;

    private int mStep;
    private int mCurrentPosition;
    private String mLastDataString;
    //these items are set when the viewpager page comes into focus

    private int mCurrentDataListSize;
    //endregion


    //region Lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbarExtension = findViewById(R.id.toolbar_extension);
        mCountTextView = (TextView) findViewById(R.id.main_toolbar_textview);

        mViewPager = (ViewPager) findViewById(R.id.fullscreen_viewpager);
        mFullscreenPagerAdapter = new FullscreenPagerAdapter(this);
        mFullscreenPagerAdapter.setOnTouchListener(new FullscreenPagerAdapter.OnTouchListener() {
            @Override
            public void onImagedClicked() {
                int nextItem = mViewPager.getCurrentItem() + mStep;
                if (nextItem < 0 || nextItem > mFullscreenPagerAdapter.getCount() -1){
                    TouchImageView imageView = (TouchImageView) mViewPager.
                            findViewWithTag(VIEW_TAG + (mViewPager.getCurrentItem()));
                    imageView.setAlpha(0.50f);
                    imageView.animate().alpha(1).setDuration(250).setInterpolator(new DecelerateInterpolator());
                } else {
                    mViewPager.setCurrentItem(nextItem, false);
                }
            }

            @Override
            public void onImageLongClicked(TouchImageView imageView) {
                if (imageView.isZoomed()){
                    imageView.animateZoomToMinScale();
                } else {
                    imageView.animateZoomToMaxScale();
                }
            }

            @Override
            public void onSwipeUp() {
                hideToolbar(150);
            }

            @Override
            public void onSwipeDown() {
                showToolbar(150);
            }

            @Override
            public void onScaledToSuperMin(TouchImageView touchImageView, int index) {
                float superMinScale = touchImageView.getSuperMinScale();
                TouchImageView nextImageView = (TouchImageView) mViewPager.
                        findViewWithTag(VIEW_TAG + (index + 1));
                TouchImageView prevImageView = (TouchImageView) mViewPager.
                        findViewWithTag(VIEW_TAG + (index - 1));

                if (nextImageView != null){
                    nextImageView.setZoom(superMinScale);
                }
                if (prevImageView != null){
                    prevImageView.setZoom(superMinScale);
                }
            }

            @Override
            public void onScaledToMinOrMax(TouchImageView imageView, int index) {
                float minScale = imageView.getMinScale();
                TouchImageView nextImageView = (TouchImageView) mViewPager.
                        findViewWithTag(VIEW_TAG  + (index + 1));
                TouchImageView prevImageView = (TouchImageView) mViewPager.
                        findViewWithTag(VIEW_TAG  + (index - 1));

                if (nextImageView != null){
                    nextImageView.setZoom(minScale);
                }
                if (prevImageView != null){
                    prevImageView.setZoom(minScale);
                }
            }
        });

        mViewPager.setAdapter(mFullscreenPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
                @Override
                public void onPageSelected(int newPosition) {
                    if (mCurrentPosition <= newPosition) {
                        mStep = 1;
                    } else {
                        mStep = -1;
                    }
                    mCurrentPosition = newPosition;
                    mLastDataString = getDataList().get(mCurrentPosition);
                    mCountTextView.setText(newPosition +  1 + "/"
                            + mFullscreenPagerAdapter.getCount());
                }
        });

        mCurrentDataListSize = getDataList().size();

        mCountTextView.setText(mViewPager.getCurrentItem() + 1 + "/"
                + mFullscreenPagerAdapter.getCount());

        if (savedInstanceState == null) {
            Intent callingIntent = getIntent();

            mViewPager.setCurrentItem(callingIntent.getIntExtra(ThumbActivity.EXTRA_THUMB_POSITION, 0), false);

            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            setBaseColor(preferences.getInt(KEY_FULLSCREEN_BASE_COLOR, Color.WHITE));

            mStep = 1;
        } else {
            setBaseColor(savedInstanceState.getInt(KEY_FULLSCREEN_BASE_COLOR));
            mStep = savedInstanceState.getInt(KEY_STEP, 1);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(KEY_FULLSCREEN_BASE_COLOR, mBaseColor);
        savedInstanceState.putInt(KEY_STEP, mStep);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideToolbar(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCurrentDataListSize = getDataList().size();
    }
    //endregion


    //region Loader
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);

        mFullscreenPagerAdapter.notifyDataSetChanged();

        int prevDataListSize = mCurrentDataListSize;
        mCurrentDataListSize = getDataList().size();
        if(prevDataListSize != mCurrentDataListSize){
            mViewPager.setCurrentItem(getDataList().indexOf(mLastDataString));
        }

        mCountTextView.setText(mViewPager.getCurrentItem() + 1 + "/"
                + mFullscreenPagerAdapter.getCount());
    }
    //endregion


    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fullscreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case (R.id.action_fullscreen_share):
                Uri uriToSend = getUriWithFilePrefixFromDataList(mViewPager.getCurrentItem());
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriToSend);
                shareIntent.setType("image/*");
                startActivity(Intent.createChooser(shareIntent, "Share images to.."));
                break;
            case (R.id.action_fullscreen_delete):
                PopupMenu confirmDelete = new PopupMenu(this, findViewById(R.id.action_fullscreen_delete));
                confirmDelete.getMenuInflater().inflate(R.menu.popup_confirm_delete, confirmDelete.getMenu());
                confirmDelete.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        String dataStringToDelete = getDataList().get(mViewPager.getCurrentItem());

                        if (mFullscreenPagerAdapter.getCount() == 1){
                            mCountTextView.setText("0/0");
                            deleteImage(dataStringToDelete);
                            finish();
                        } else {
                            if (mStep == 1  && mViewPager.getCurrentItem() < mFullscreenPagerAdapter.getCount()){
                                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, false);
                            } else if (mStep == -1 && mViewPager.getCurrentItem() != 0){
                                mViewPager.setCurrentItem(mViewPager.getCurrentItem() -1, false);
                            }

                            mCountTextView.setText(mViewPager.getCurrentItem() +  1 + "/"
                                    + (mFullscreenPagerAdapter.getCount() - 1));
                            deleteImage(dataStringToDelete);
                            mFullscreenPagerAdapter.notifyDataSetChanged();
                        }

                        return true;
                    }
                });
                confirmDelete.show();
                break;
            case R.id.action_fullscreen_info:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View content = inflater.inflate(R.layout.dialog_information, null);
                builder.setView(content);

                TextView pathTextView = (TextView) content.findViewById(R.id.dialog_text_path);
                TextView resoTextView = (TextView) content.findViewById(R.id.dialog_text_reso);
                TextView sizeTextView = (TextView) content.findViewById(R.id.dialog_text_size);
                TextView dateTextView = (TextView) content.findViewById(R.id.dialog_text_date);

                final AlertDialog dialog = builder.create();

                Button doneButton = (Button) content.findViewById(R.id.dialog_button_done);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                final String dataString = getDataList().get(mViewPager.getCurrentItem());
                String[] projection = {
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.WIDTH,
                        MediaStore.Images.Media.HEIGHT,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.DATE_MODIFIED};
                String selection = MediaStore.Images.Media.DATA + " = ?";
                String[] selectionArgs = new String[] { dataString };

                Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null);

                if (cursor.moveToFirst()) {
                    pathTextView.setText(dataString);
                    int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
                    resoTextView.setText(width + " x " + height);

                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                    sizeTextView.setText(humanReadableByteCount(size, true));

                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    dateTextView.setText(sdf.format(new Date(date * 1000)));
                }
                cursor.close();

                dialog.show();
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

            case R.id.action_fullscreen_edit:
                Uri uriToEdit = getUriWithFilePrefixFromDataList(mViewPager.getCurrentItem());
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                editIntent.setDataAndType(uriToEdit, "image/*");
                editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(editIntent, "edit with..."));
                break;
        }
        return true;
    }
    //endregion


    //region Activity Interaction
    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_PAGER_POSITION_RESULT, mViewPager.getCurrentItem());
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed();
    }
    //endregion


    //region Toolbar and StatusBar
    public void showToolbar(int animationDuration) {
        mToolbarExtension.animate().translationY(mToolbar.getTop()).setDuration(animationDuration);
        mToolbar.animate().translationY(mToolbar.getTop()).setDuration(animationDuration);
        showStatusBar();
    }

    public void hideToolbar(int animationDuration) {
        mToolbarExtension.animate().translationY(-250).setDuration(animationDuration);
        mToolbar.animate().translationY(-250).setDuration(animationDuration);
        hideStatusBar();
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


    //region setBaseColor
    public void setBaseColor(int baseColor) {
        mBaseColor = baseColor;
        findViewById(android.R.id.content).setBackgroundColor(baseColor);

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_FULLSCREEN_BASE_COLOR, mBaseColor);
        editor.apply();
    }
    //endregion


    //region Helper Methods
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    //endregion
}
