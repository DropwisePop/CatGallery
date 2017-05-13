package com.dropwisepop.catgallery.adapters;

import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dropwisepop.catgallery.activities.FullscreenActivity;
import com.dropwisepop.catgallery.util.Util;
import com.dropwisepop.catgallery.views.TouchImageView;
import com.dropwisepop.catgallery.catgallery.R;

/**
 * Created by dropwisepop on 3/18/2017.
 */

public class FullscreenPagerAdapter extends PagerAdapter {

    //region Variables
    public static final String VIEW_TAG = "VIEW_TAG";

    private FullscreenActivity mFullscreenActivity;
    private OnTouchListener mOnTouchListener;
    //endregion


    //region Constructors
    public FullscreenPagerAdapter(FullscreenActivity fullscreenActivity) {
        mFullscreenActivity = fullscreenActivity;
    }
    //endregion


    //region Overridden Methods
    @Override
    public Object instantiateItem(ViewGroup container, final int index) {
        LayoutInflater inflater = LayoutInflater.from(mFullscreenActivity);
        final View view = inflater.inflate(R.layout.viewpager_page, container, false);

        final TouchImageView imageView = (TouchImageView) view.findViewById(R.id.viewpager_imageview);

                Glide.with(mFullscreenActivity)
                        .load(mFullscreenActivity.getUriWithFilePrefixFromDataList(index))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageView.setImageBitmap(resource);
                    }
                });

        imageView.setTag(VIEW_TAG + index);

        //region OnTouchListener setup
        if (mOnTouchListener != null) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnTouchListener.onImagedClicked();
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnTouchListener.onImageLongClicked(imageView);
                    return true;
                }
            });
            imageView.setSwipeListener(new TouchImageView.SwipeListener() {
                @Override
                public void onSwipeUp() {
                    mOnTouchListener.onSwipeUp();
                }

                @Override
                public void onSwipeDown() {
                    mOnTouchListener.onSwipeDown();
                }
            });
            imageView.setOnScaleListener(new TouchImageView.onScaleListener() {
                @Override
                public void onScaledToSuperMin() {
                     mOnTouchListener.onScaledToSuperMin(imageView, index);
                }

                @Override
                public void onScaleToMinOrMax() {
                    mOnTouchListener.onScaledToMinOrMax(imageView, index);
                }
            });
        }
        //endregion

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return mFullscreenActivity.getDataList().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    //endregion


    //region OnTouchListener and setter
    public interface OnTouchListener {
        void onImagedClicked();
        void onImageLongClicked(TouchImageView touchImageView);
        void onSwipeUp();
        void onSwipeDown();
        void onScaledToSuperMin(TouchImageView imageView, int index);
        void onScaledToMinOrMax(TouchImageView imageView, int index);
    }

    public void setOnTouchListener(OnTouchListener onTouchListener){
        mOnTouchListener = onTouchListener;
    }
    //endregion


}
