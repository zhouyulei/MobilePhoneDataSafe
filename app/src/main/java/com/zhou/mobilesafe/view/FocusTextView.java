package com.zhou.mobilesafe.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Zhou on 2018/7/19.
 */

public class FocusTextView extends android.support.v7.widget.AppCompatTextView {
    public FocusTextView(Context context) {
        super(context);
    }

    public FocusTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
