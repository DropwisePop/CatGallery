package com.example.ben.catgallery;

import android.database.Cursor;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by dropwisepop on 3/18/2017.
 */

public class FullscreenPagerAdapter extends PagerAdapter{

    //region Member Variables
    private FullscreenActivity mFullscreenActivity;
    //endregion

    //region Constructors
    public FullscreenPagerAdapter(FullscreenActivity fullscreenActivity) {
        mFullscreenActivity = fullscreenActivity;
    }
    //endregion

    //region Interfaces
    public interface PageClicked{
        void pageClicked();
    }
    //endregion

    //region Overridden Methods
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = LayoutInflater.from(mFullscreenActivity);
        View view = inflater.inflate(R.layout.page_fullscreen, container, false);

        final TouchImageView imageView = (TouchImageView) view.findViewById(R.id.fullscreen_imageview);
        imageView.setImageURI(mFullscreenActivity.getUriFromMediaStore(position));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFullscreenActivity.pageClicked();
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
    //endregion
}
