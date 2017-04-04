package com.dropwisepop.catgallery.adapters;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dropwisepop.catgallery.views.TouchImageView;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.activities.FullscreenActivity;

/**
 * Created by dropwisepop on 3/18/2017.
 */

public class FullscreenPagerAdapter extends PagerAdapter {

    //region Member Variables
    private static final String VIEW_TAG = "VIEW_TAG";
    private FullscreenActivity mFullscreenActivity;
    private ViewPager mViewPager;
    //endregion

    //region Constructors
    public FullscreenPagerAdapter(FullscreenActivity fullscreenActivity, ViewPager viewPager) {
        mFullscreenActivity = fullscreenActivity;
        mViewPager = viewPager;
    }
    //endregion

    //region Interfaces
    public interface PageClicked {
        public void pageClicked();
    }
    //endregion

    //region Overridden Methods
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = LayoutInflater.from(mFullscreenActivity);
        final View view = inflater.inflate(R.layout.page_fullscreen, container, false);

        final TouchImageView imageView = (TouchImageView) view.findViewById(R.id.fullscreen_imageview);

        Glide.with(mFullscreenActivity)
                .load(mFullscreenActivity.getUriFromMediaStore(position))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageView.setImageBitmap(resource);
                    }
                });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFullscreenActivity.pageClicked();
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (imageView.isZoomed()){
                    imageView.animateZoomToMinScale();
                } else {
                    imageView.animateZoomToMaxScale();
                }
                return true;
            }
        });
        imageView.setSwipeListener(new TouchImageView.SwipeListener() {
            @Override
            public void onSwipeUp() {
                mFullscreenActivity.showToolbar(100);
            }

            @Override
            public void onSwipeDown() {
                mFullscreenActivity.hideToolbar(100);
                mFullscreenActivity.hideStatusBar();
            }
        });

        imageView.setTag(VIEW_TAG + position);
        imageView.setOnScaleListener(new TouchImageView.onScaleListener() {
            @Override
            public void onScaledToSuperMin() {
                float superMinScale = imageView.getSuperMinScale();
                TouchImageView nextImageView = (TouchImageView) mViewPager.findViewWithTag(VIEW_TAG + (position + 1));
                TouchImageView prevImageView = (TouchImageView) mViewPager.findViewWithTag(VIEW_TAG + (position - 1));
                if (nextImageView != null){
                    nextImageView.setZoom(superMinScale);
                }
                if (prevImageView != null){
                    prevImageView.setZoom(superMinScale);
                }
            }

            @Override
            public void onScaleToMinOrMax() {
                float minScale = imageView.getMinScale();
                TouchImageView nextImageView = (TouchImageView) mViewPager.findViewWithTag(VIEW_TAG + (position + 1));
                TouchImageView prevImageView = (TouchImageView) mViewPager.findViewWithTag(VIEW_TAG + (position - 1));
                if (nextImageView != null){
                    nextImageView.setZoom(minScale);
                }
                if (prevImageView != null){
                    prevImageView.setZoom(minScale);
                }
            }

            //this is strictly to handle cases where the view is already instantiated;
            //otherwise, zoom is adjusted when the image view is instantiated

        });


        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        Cursor cursor = mFullscreenActivity.getCursor();
        return ((cursor == null) ? 0 : cursor.getCount());
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    /*
    public boolean touchImageViewAtSuperMinScale() {
        return (mTouchImageView.getCurrentZoom() == mTouchImageView.getSuperMinScale());
    }

    public void zoomTouchImageViewToSuperMinScale(){
        mTouchImageView.setZoom(mTouchImageView.getSuperMinScale());
    }
    */

    //endregionk
}
