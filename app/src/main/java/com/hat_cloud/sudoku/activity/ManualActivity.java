package com.hat_cloud.sudoku.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hat_cloud.sudoku.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ManualActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        new Thread(){
            @Override
            public void run() {
                final String str = getText();
                final Spanned text =Html.fromHtml(str);
                //先显示文本
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.content);
                        tv.setText(text);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());//设置超链接可以打开网页
                    }
                });
                //有图片的话显示图片
                final Spanned text2=Html.fromHtml(str,imgGetter,null);
                    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.content);
                        tv.setText(text2);

                    }
                });

            }
        }.start();
    }
    private String getText() {
        InputStream in = null;
        StringBuilder sb =new StringBuilder();
        try {
            in = getAssets().open("manual.txt");
            byte[] buff = new byte[1024];
            int len = 0;
            while((len=in.read(buff))>0){
                sb.append(new String(buff,0,len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
    //有需要的话可以加载图片
    Html.ImageGetter imgGetter = new Html.ImageGetter() {
        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            URL url;
            try {
                url = new URL(source);
                drawable = Drawable.createFromStream(url.openStream(), "");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            return drawable;
        }
    };
}
