package com.dropwisepop.catgallery.adapters;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
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
    private FullscreenActivity mFullscreenActivity;
    //endregion

    //region Constructors
    public FullscreenPagerAdapter(FullscreenActivity fullscreenActivity) {
        mFullscreenActivity = fullscreenActivity;
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
        View view = inflater.inflate(R.layout.page_fullscreen, container, false);

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
                    imageView.zoomToMinScale();
                } else {
                    imageView.zoomToMaxScale();
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
