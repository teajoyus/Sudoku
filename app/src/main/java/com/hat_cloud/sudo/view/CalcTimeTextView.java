package com.hat_cloud.sudo.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by linmh on 2017/8/9.
 */

public class CalcTimeTextView extends TextView {
    int hh,mm,ss;
    int time;
    public static final int DELAYED  = 1000;
    public CalcTimeTextView(Context context) {
        super(context);
    }

    public CalcTimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setText("00:00:00");
        handler.sendEmptyMessage(0);
    }

    public CalcTimeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            calc();
            handler.sendEmptyMessageDelayed(0,DELAYED);

        }
    };
    public int  getTime(){
        return time;
    }
    public void setTime(int time){
        ss = time%60;
        mm = time/60;
        hh = time/3600;
    }
    /**
     * 每秒钟执行一次
     */
    private void calc(){
        if(ss==59){
             if(mm==59){
                 hh++;
                 ss=0;
                 mm=0;
             }else{
                 ss = 0;
                 mm++;
             }
        }else{
            ss++;
        }
        time++;//累计秒数
        String h = hh<10?"0"+hh:hh+"";
        String m = mm<10?"0"+mm:mm+"";
        String s = ss<10?"0"+ss:ss+"";
        setText(h+":"+m+":"+s);
    }
}
