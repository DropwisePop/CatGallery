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
    private static final String KEY_PREFERENCES_FIT_MODE = "com.dropwisepop.catgallery.KEY_PREFERENCES_FIT_MODE";

    private enum FitMode {CENTER_CROP, FIT};
    private static FitMode sFitMode;

    private ThumbActivity mThumbActivity;
    //endregion


    //region Constructor
    public ThumbAdapter(ThumbActivity thumbActivity) {
        mThumbActivity = thumbActivity;
    }
    //endregion


    //region Required Methods
    @Override
    public ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thumbnail_thumb, parent, false);
        return new ThumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ThumbViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!

        final ImageView imageView = holder.getImageView();

        SharedPreferences preferences = mThumbActivity.getPreferences(Context.MODE_PRIVATE);
        int fitMode = preferences.getInt(KEY_PREFERENCES_FIT_MODE, 0);
        sFitMode = FitMode.values()[fitMode];

        if (sFitMode == FitMode.CENTER_CROP){
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
            imageView.setColorFilter(Color.argb(200, 0, 0, 0));
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
        if (sFitMode == FitMode.CENTER_CROP){
            sFitMode = FitMode.FIT;
        } else {
            sFitMode = FitMode.CENTER_CROP;
        }

        SharedPreferences preferences =  mThumbActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_PREFERENCES_FIT_MODE, sFitMode.ordinal());
        editor.apply();

        notifyDataSetChanged();
    }
    //endregion


    //region ThumbViewHolder Class
    class ThumbViewHolder extends RecyclerView.ViewHolder{

        private final ImageView mImageView;

        ThumbViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.thumb_imageview);

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


    //region Other
    private int valueInDP(int val){
        final float scale = mThumbActivity.getResources().getDisplayMetrics().density;
        return (int) (val * scale + 0.5f);
    }

    //endregion

}



