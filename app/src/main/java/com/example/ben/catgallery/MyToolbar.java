package com.example.ben.catgallery;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;

/**
 * An extended support mytoolbar. Primarily handles hiding and showing. TODO: Javadoc done?
 */
public class MyToolbar extends Toolbar {

    private boolean shown = false;

    //region Constructors //TODO: Do I need all of these?
    public MyToolbar(Context context) {
        super(context);
        Log.d(Util.TAG, "MyToolbar Created Anew, Praise Thy Lord for He is God.");
    }

    public MyToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(Util.TAG, "MyToolbar Created Anew, Praise Thy Lord for He is God2.");
    }

    public MyToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(Util.TAG, "MyToolbar Created Anew, Praise Thy Lord for He is God3.");
    }
    //endregion

    //region Other Methods
    public void show() {
        animate().translationY(0);
        shown = true;
    }

    public void hide() {
        animate().translationY(getBottom());
        shown = false;
    }

    public boolean getToolbarShown() {
        return shown;
    }

    public void setToolbarShown(boolean showToolbar) {
        if (showToolbar) {
            Log.d(Util.TAG, "SHOW");
            show();
        } else {
            Log.d(Util.TAG, "HIDE");
            hide();
        }
    }

    public void toggleToolbar() {
        if (shown) {
            hide();
        } else {
            show();
        }
    }
    //endregion

}
