package com.dropwisepop.catgallery.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dropwisepop.catgallery.activities.ThumbActivity;
import com.dropwisepop.catgallery.catgallery.R;
import com.dropwisepop.catgallery.dragselectrecyclerview.DragSelectRecyclerViewAdapter;

public class ThumbAdapter extends DragSelectRecyclerViewAdapter<ThumbAdapter.ThumbViewHolder> {


    //region Variables
    private static final String KEY_FIT_MODE = "KEY_FIT_MODE";

    private enum FitMode {CENTER_CROP, FIT};
    private FitMode mFitMode;

    private ThumbActivity mThumbActivity;
    //endregion


    //region Constructor
    public ThumbAdapter(ThumbActivity thumbActivity) {
        mThumbActivity = thumbActivity;

        SharedPreferences preferences = mThumbActivity.getPreferences(Context.MODE_PRIVATE);
        int fitMode = preferences.getInt(KEY_FIT_MODE, 0);
        mFitMode = FitMode.values()[fitMode];
    }
    //endregion


    //region Required Methods
    @Override
    public ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_thumbnail, parent, false);
        return new ThumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ThumbViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!

        final ImageView imageView = holder.getImageView();

        if (mFitMode == FitMode.CENTER_CROP){
            Glide.with(mThumbActivity)
                    .load(mThumbActivity.getUriWithFilePrefixFromDataList(position))
                    .centerCrop()
                    .into(imageView);
            imageView.setScaleX(1);
            imageView.setScaleY(1);

            int pad = valueInDP(2);
            imageView.setPadding(pad, pad, pad, pad);
            imageView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            Glide.with(mThumbActivity)
                    .load(mThumbActivity.getUriWithFilePrefixFromDataList(position))
                    .fitCenter()
                    .into(imageView);

            int pad = valueInDP(5);
            imageView.setPadding(pad, pad, pad, pad);
            imageView.setBackgroundColor(Color.BLACK);
        }

        if (isIndexSelected(position)) {
            imageView.setColorFilter(Color.argb(150, 0, 0, 0));
            imageView.setScaleX(0.90f);
            imageView.setScaleY(0.90f);

        } else {
            imageView.clearColorFilter();
            imageView.setScaleX(1);
            imageView.setScaleY(1);
        }
    }

    @Override
    public int getItemCount() {
        return mThumbActivity.getDataList().size();
    }
    //endregion


    //region toggleFitMode()
    public void toggleFitMode() {
        if (mFitMode == FitMode.CENTER_CROP){
            mFitMode = FitMode.FIT;
        } else {
            mFitMode = FitMode.CENTER_CROP;
        }

        SharedPreferences preferences =  mThumbActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_FIT_MODE, mFitMode.ordinal());
        editor.apply();

        notifyDataSetChanged();
    }

    public void setFitMode(FitMode fitMode) {
        mFitMode = fitMode;
    }

    //endregion


    //region ThumbViewHolder Class
    class ThumbViewHolder extends RecyclerView.ViewHolder{

        private final ImageView mImageView;

        ThumbViewHolder(View v) {
            super(v);

            mImageView = (ImageView) v.findViewById(R.id.recyclerview_imageview);

            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mThumbActivity.onThumbClicked(getAdapterPosition());
                }
            });
            mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mThumbActivity.onThumbLongClicked(getAdapterPosition());
                    return true;
                }
            });
        }

        ImageView getImageView() {
            return mImageView;
        }
    }
    //endregion


    //region Helper Methods
    private int valueInDP(int val){
        final float scale = mThumbActivity.getResources().getDisplayMetrics().density;
        return (int) (val * scale + 0.5f);
    }

    //endregion

}



