package com.dropwisepop.catgallery.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.dropwisepop.catgallery.adapters.FullscreenPagerAdapter;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.views.TouchImageView;

public class FullscreenActivity extends AbstractGalleryActivity {

    //region Variables
    static final String EXTRA_PAGER_POSITION_RESULT = "com.dropwisepop.catgallery.EXTRA_PAGAER_POSITION_RESULT";

    private static final String KEY_PAGER_POSITION = "com.dropwisepop.catgallery.KEY_PAGER_POSITION";
    private static final String KEY_PREFERENCES_FULLSCREEN_BASE_COLOR = "com.dropwisepop.catgallery.KEY_PREFERENCES_FULLSCREEN_BASE_COLOR";
    private static final String KEY_FULLSCREEN_BASE_COLOR = "com.dropwisepop.catgallery.KEY_FULLSCREEN_BASE_COLOR";


    private static boolean sDestroyed = true;
    private static int sStep = 1;

    private int mStartPosition;
    private int mPreviousPosition;

    private ViewPager mViewPager;
    private FullscreenPagerAdapter mFullscreenPagerAdapter;
    private Toolbar mToolbar;
    private View mToolbarExtension;

    private int mBaseColor;
    //endregion


    //region Lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_fullscreen);
        super.onCreate(savedInstanceState);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbarExtension = findViewById(R.id.toolbar_extension);

        mViewPager = (ViewPager) findViewById(R.id.fullscreen_viewpager);
        mFullscreenPagerAdapter = new FullscreenPagerAdapter(this);
        mFullscreenPagerAdapter.setOnTouchListener(new FullscreenPagerAdapter.OnTouchListener() {
            @Override
            public void onImagedClicked() {
                int nextItem = mViewPager.getCurrentItem() + sStep;
                if (nextItem < 0 || nextItem > mFullscreenPagerAdapter.getCount() -1){
                    TouchImageView imageView = (TouchImageView) mViewPager.
                            findViewWithTag(FullscreenPagerAdapter.VIEW_TAG + (mViewPager.getCurrentItem()));
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
                        findViewWithTag(FullscreenPagerAdapter.VIEW_TAG + (index + 1));
                TouchImageView prevImageView = (TouchImageView) mViewPager.
                        findViewWithTag(FullscreenPagerAdapter.VIEW_TAG + (index - 1));

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
                        findViewWithTag(FullscreenPagerAdapter.VIEW_TAG  + (index + 1));
                TouchImageView prevImageView = (TouchImageView) mViewPager.
                        findViewWithTag(FullscreenPagerAdapter.VIEW_TAG  + (index - 1));

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
                public void onPageSelected(int position) {
                    if (mPreviousPosition < position) {
                        sStep = 1;
                    } else if (position < mPreviousPosition) {
                        sStep = -1;
                    }
                    mPreviousPosition = position;
                }
        });

        if (savedInstanceState == null) {
            Intent callingIntent = getIntent();

            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            setBaseColor(preferences.getInt(KEY_PREFERENCES_FULLSCREEN_BASE_COLOR, Color.WHITE));

            mStartPosition = callingIntent.getIntExtra(ThumbActivity.EXTRA_THUMB_POSITION, 0);
            mPreviousPosition = mStartPosition;
            mViewPager.setCurrentItem(mStartPosition, false);
        } else {
            setBaseColor(savedInstanceState.getInt(KEY_FULLSCREEN_BASE_COLOR));
            mStartPosition = savedInstanceState.getInt(KEY_PAGER_POSITION, 0);
            mPreviousPosition = mStartPosition;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(KEY_FULLSCREEN_BASE_COLOR, mBaseColor);
        savedInstanceState.putInt(KEY_PAGER_POSITION, mViewPager.getCurrentItem());
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideToolbar(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sDestroyed = true;
    }
    //endregion


    //region Loader
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        mFullscreenPagerAdapter.notifyDataSetChanged();
        if (sDestroyed) {
            mViewPager.setCurrentItem(mStartPosition, false);
            sDestroyed = false;
        }
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
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                Uri uriToSend = getUriWithFilePrefixFromDataList(mViewPager.getCurrentItem());
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

                        //TODO: this is not 100% efficient; clean it up!
                        int nextItem;
                        if (sStep == 1){
                            nextItem = mViewPager.getCurrentItem();
                        } else {
                            nextItem = mViewPager.getCurrentItem() + sStep;
                        }

                        if (getDataList().size() == 1) {
                            finish();
                        } else if (nextItem < 0) {
                            nextItem = 0;
                        } else if (nextItem > getDataList().size()){
                            nextItem = mViewPager.getCurrentItem() - 1;
                        }
                        mViewPager.setCurrentItem(nextItem, false);

                        deleteImage(dataStringToDelete);
                        mFullscreenPagerAdapter.notifyDataSetChanged();

                        return true;
                    }
                });
                confirmDelete.show();
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
        editor.putInt(KEY_PREFERENCES_FULLSCREEN_BASE_COLOR, mBaseColor);
        editor.apply();
    }
    //endregion
}
