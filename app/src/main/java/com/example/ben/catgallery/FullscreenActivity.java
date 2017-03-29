package com.example.ben.catgallery;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

/**
 * Created by dropwisepop on 3/18/2017.
 */

public class FullscreenActivity extends AbstractGalleryActivity
    implements FullscreenPagerAdapter.PageClicked{

    //region Variables
    public static String EXTRA_PAGER_POSITION = "com.example.ben.thegallery.EXTRA_ADAPTER_POSITION";
    private static enum Direction { FORWARDS, BACKWARDS};
    private static Direction sDirection = Direction.FORWARDS;
    private static boolean sDestroyed = true;
    private static int sStep = 1;
    private int mStartPosition;
    private int mPreviousPosition;
    private ViewPager mViewPager;
    private FullscreenPagerAdapter mFullscreenPagerAdapter;
    //endregion

    //region Lifecycle Methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        if (savedInstanceState == null){
            Intent callingIntent = getIntent();
            mStartPosition = callingIntent.getIntExtra(ThumbActivity.EXTRA_THUMB_POSITION, 0);
            mPreviousPosition = mStartPosition;
            initializeLoader();
        } else {
            mStartPosition = savedInstanceState.getInt(EXTRA_PAGER_POSITION, 0);
            mPreviousPosition = mStartPosition;
            hideToolbar();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setToolbar(toolbar, false);

        mViewPager = (ViewPager) findViewById(R.id.fullscreen_viewpager);
        mFullscreenPagerAdapter = new FullscreenPagerAdapter(this);
        mViewPager.setAdapter(mFullscreenPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
            hideToolbar();
            sDestroyed = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        super.onLoaderReset(loader);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(ThumbActivity.KEY_PAGER_POSITION_RESULT, mViewPager.getCurrentItem());
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(EXTRA_PAGER_POSITION, mViewPager.getCurrentItem());
    }
    //endregion

    //region Interfaces and Related
    @Override
    public void pageClicked() {
        stepViewPager();
    }
    //endregion

    //region Helper Methods
    public void stepViewPager() {
        int nextPage = mViewPager.getCurrentItem() + sStep;
        if (-1 < nextPage && nextPage < mFullscreenPagerAdapter.getCount()) {
            mViewPager.setCurrentItem(nextPage, false);
        }
    }
    //endregion
}
