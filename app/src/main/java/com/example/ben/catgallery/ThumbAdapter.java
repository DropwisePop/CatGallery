package com.example.ben.catgallery;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by dropwisepop on 3/16/2017.
 */

public class ThumbAdapter extends RecyclerView.Adapter<ThumbAdapter.ViewHolder> {

    //region ViewHolder Class
    //--------------------------------------------------------------------------------
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mImageView;

        ViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.thumb_imageview);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mThumbActivity.startFullscreenActivity(getAdapterPosition());
                }
            });

        }

        ImageView getImageView() {
            return mImageView;
        }
    }
    //--------------------------------------------------------------------------------
    //endregion

    //region Member Variables
    ThumbActivity mThumbActivity;
    //endregion

    //region Constructors and Lifecycle Methods
    public ThumbAdapter(ThumbActivity thumbActivity) {
        mThumbActivity = thumbActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thumbnail_thumb, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(mThumbActivity)
                .load(mThumbActivity.getUriFromMediaStore(position))
                .centerCrop()
                .into(holder.getImageView());
    }

    @Override
    public int getItemCount() {
        Cursor cursor = mThumbActivity.getCursor();
        return (cursor == null ? 0 : cursor.getCount());
    }
    //endregion
}
