package com.zhou.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhou.mobilesafe.R;

/**
 * Created by Zhou on 2018/7/27.
 */

public class SettingClickView extends RelativeLayout {

    private TextView tv_title;
    private TextView tv_des;

    public SettingClickView(Context context) {
        this(context,null);
    }

    public SettingClickView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SettingClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View.inflate(context, R.layout.setting_click_view, this);
        tv_title = this.findViewById(R.id.tv_title);
        tv_des = this.findViewById(R.id.tv_des);
    }

    /**
     * 给标题设置文本
     * @param title
     */
    public void setTitle(String title){
        tv_title.setText(title);
    }

    /**
     * 给下面的描述内容设置文本
     * @param Des
     */
    public void setDes(String Des){
        tv_des.setText(Des);
    }

}
