package com.hat_cloud.sudo.activity;


import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.hat_cloud.sudo.entry.BlueMessage;
import com.hat_cloud.sudo.iface.IGame;
import com.hat_cloud.sudoku.R;

public class GamePKHelp extends GameCommon {
    private static final String TAG = "GamePKHelp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
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
                bar.setTitle("\t\t\t\t"+getResources().getString(R.string.pk_with_name).replace("XXX",name));
            }
        }
    }
    @Override
    public boolean setTileIfValid(int x, int y, int value) {
        boolean b  = super.setTileIfValid(x, y, value);
         if(b){
             //发送输入数字的消息给对方
             BlueMessage msg = new BlueMessage(BlueMessage.HEADER_HELP_REFER);
             msg.put("value",value);
             msg.put("x",x);
             msg.put("y",y);
             send(msg);
         }
         return b;
    }



}
