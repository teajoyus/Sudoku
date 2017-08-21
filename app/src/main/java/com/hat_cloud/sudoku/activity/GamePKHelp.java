package com.hat_cloud.sudoku.activity;


import android.app.ActionBar;
import android.os.Bundle;

import com.hat_cloud.sudoku.entry.BlueMessage;
import com.hat_cloud.sudoku.R;

/**
 * 对战结束后，帮助对方挑战的页面，由于是帮助的，所以改写GameCommon类的某些规则即可
 */
public class GamePKHelp extends GameCommon {
    private static final String TAG = "GamePKHelp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        BlueMessage msg = new BlueMessage(BlueMessage.HEADER_PK_HELP);
        send(msg);
    }

    /**
     * 修改标题
     */
    @Override
    protected void initActionBar() {
        super.initActionBar();
        ActionBar bar = getActionBar();
        if(bar!=null){
            if(name!=null){
                bar.setTitle("\t\t\t\t"+getResources().getString(R.string.help_with_name).replace("XXX",name));
            }
        }
    }

    @Override
    public void Congratulations() {
//        super.Congratulations();
    }

    @Override
    public boolean setTileIfValid(int x, int y, int value) {
//        boolean b  = super.setTileIfValid(x, y, value);
             //发送输入数字的消息给对方
             BlueMessage msg = new BlueMessage(BlueMessage.HEADER_HELP_REFER);
             msg.put("value",value);
             msg.put("x",x);
             msg.put("y",y);
             send(msg);
         return true;
    }



}
