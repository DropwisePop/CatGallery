package com.dropwisepop.catgallery.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dropwisepop.catgallery.adapters.FullscreenPagerAdapter;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.views.TouchImageView;

/**
 * Created by dropwisepop on 3/18/2017.
 */

public class FullscreenActivity extends AbstractGalleryActivity {

    //region Variables
    public static final String EXTRA_PAGER_POSITION = "com.dropwisepop.catgallery.EXTRA_ADAPTER_POSITION";

    private static boolean sDestroyed = true;
    private static int sStep = 1;

    private int mStartPosition;
    private int mPreviousPosition;
    private ViewPager mViewPager;
    private FullscreenPagerAdapter mFullscreenPagerAdapter;

    private View mToolbarExtension;
    //endregion


    //region Lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mToolbarExtension = findViewById(R.id.toolbar_extension);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setToolbarAsActionBar(toolbar, false);

        mViewPager = (ViewPager) findViewById(R.id.fullscreen_viewpager);
        mFullscreenPagerAdapter = new FullscreenPagerAdapter(this);
        mFullscreenPagerAdapter.setOnTouchListener(new FullscreenPagerAdapter.OnTouchListener() {
            @Override
            public void onImagedClicked() {
                int nextItem = mViewPager.getCurrentItem() + sStep;
                if (nextItem < 0 || nextItem > mFullscreenPagerAdapter.getCount()){
                    //TODO: end of list animation
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
            mStartPosition = callingIntent.getIntExtra(ThumbActivity.EXTRA_THUMB_POSITION, 0);
            mPreviousPosition = mStartPosition;
            mViewPager.setCurrentItem(mStartPosition, false);

        } else {
            mStartPosition = savedInstanceState.getInt(EXTRA_PAGER_POSITION, 0);
            mPreviousPosition = mStartPosition;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(EXTRA_PAGER_POSITION, mViewPager.getCurrentItem());
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


    //region Loader Callbacks
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        mFullscreenPagerAdapter.notifyDataSetChanged();
        if (sDestroyed) {
            mViewPager.setCurrentItem(mStartPosition, false);
            sDestroyed = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        super.onLoaderReset(loader);
        this.finish();
    }
    //endregion


    //region Toolbar and StatusBar
    @Override
    public void showToolbar(int animationDuration) {
        mToolbarExtension.animate().translationY(getToolbar().getTop()).setDuration(animationDuration);
        getToolbar().animate().translationY(getStatusBarHeight()).setDuration(animationDuration);
        showStatusBar();
    }

    @Override
    public void hideToolbar(int animationDuration) {
        mToolbarExtension.animate().translationY(-250).setDuration(animationDuration);
        getToolbar().animate().translationY(-250).setDuration(animationDuration);
        hideStatusBar();
    }

    private int getStatusBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    //endregion


    //region Activity Interaction
    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(ThumbActivity.KEY_PAGER_POSITION_RESULT, mViewPager.getCurrentItem());
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed();
    }
    //endregion
}
