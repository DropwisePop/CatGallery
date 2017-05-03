package com.dropwisepop.catgallery.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dropwisepop.catgallery.activities.AbstractGalleryActivity;
import com.dropwisepop.catgallery.activities.ThumbActivity;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerViewAdapter;

import static com.dropwisepop.catgallery.activities.ThumbActivity.KEY_PREFERENCES_SORT_ORDER;

/**
 * Created by dropwisepop on 4/2/2017.
 */

public class ThumbAdapter extends DragSelectRecyclerViewAdapter<ThumbAdapter.ThumbViewHolder> {


    //region Variables
    private static final String KEY_PREFERENCES_FIT_MODE = "com.dropwisepop.catgallery.KEY_PREFERENCES_FIT_MODE";

    private enum FitMode {CENTER_CROP, FIT};
    private static FitMode sFitMode;

    private ThumbActivity mThumbActivity;   //TODO: make this a generic context
    private OnTouchListener mOnTouchListener;
    //endregion


    //region Constructor
    public ThumbAdapter(ThumbActivity thumbActivity) {
        mThumbActivity = thumbActivity;
    }
    //endregion


    //region Overridden Methods
    @Override
    public ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thumbnail_thumb, parent, false);
        return new ThumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ThumbViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!

        ImageView imageView = holder.getImageView();

        SharedPreferences preferences = mThumbActivity.getPreferences(Context.MODE_PRIVATE);
        int fitMode = preferences.getInt(KEY_PREFERENCES_FIT_MODE, 0);
        sFitMode = FitMode.values()[fitMode];

        float scaleFactorForMargins = 1;
        if (sFitMode == FitMode.CENTER_CROP){
            Glide.with(mThumbActivity)
                    .load(mThumbActivity.getUriFromMediaStore(position))
                    .centerCrop()
                    .into(imageView);
            imageView.setScaleX(1);
            imageView.setScaleY(1);
        } else {
            Glide.with(mThumbActivity)
                    .load(mThumbActivity.getUriFromMediaStore(position))
                    .fitCenter()
                    .into(imageView);
            scaleFactorForMargins = 0.95f;
        }

        if (isIndexSelected(position)) {
            imageView.setColorFilter(Color.argb(200, 0, 0, 0));
            imageView.setScaleX(0.90f * scaleFactorForMargins);
            imageView.setScaleY(0.90f * scaleFactorForMargins);
        } else {
            imageView.clearColorFilter();
            imageView.setScaleX(1 * scaleFactorForMargins);
            imageView.setScaleY(1 * scaleFactorForMargins);
        }
    }

    @Override
    public int getItemCount() {
        Cursor cursor = mThumbActivity.getCursor();
        return (cursor == null ? 0 : cursor.getCount());
    }


    //endregion


    //region OnTouchListener and setter
    public interface OnTouchListener {
        void onThumbClicked(int index);
        void onThumbLongClicked(int index);
    }

    public void setOnTouchListener(OnTouchListener onTouchListener){
        mOnTouchListener = onTouchListener;
    }
    //endregion


    //region Misc Methods
    public void toggleFitMode() {
        if (sFitMode == FitMode.CENTER_CROP){
            sFitMode = FitMode.FIT;
        } else {
            sFitMode = FitMode.CENTER_CROP;
        }

        SharedPreferences preferences =  mThumbActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_PREFERENCES_FIT_MODE, sFitMode.ordinal());
        editor.commit();

        notifyDataSetChanged();
    }
    //endregion


    //region ThumbViewHolder Class
    class ThumbViewHolder extends RecyclerView.ViewHolder{

        private final ImageView mImageView;

        ThumbViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.thumb_imageview);

            //region OnTouchListener setup
            if (mOnTouchListener != null){
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnTouchListener.onThumbClicked(getAdapterPosition());
                    }
                });
                mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mOnTouchListener.onThumbLongClicked(getAdapterPosition());
                        return true;
                    }
                });
            }
            //endregion

        }

        ImageView getImageView() {
            return mImageView;
        }

    }
    //endregion


}



