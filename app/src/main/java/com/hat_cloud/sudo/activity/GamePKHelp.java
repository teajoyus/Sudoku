package com.hat_cloud.sudo.activity;


import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.hat_cloud.sudo.entry.BlueMessage;
import com.hat_cloud.sudoku.R;

public class GamePKHelp extends GameCommon {
    private static final String TAG = "GamePKHelp";
    public int pk_show[];//显示对方

    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
    }
    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    // ...


    @Override
    protected void setTile(int x, int y, int value) {
        super.setTile(x, y, value);
        //输入数字了后就清空对方已经的输入
        if(pk_show!=null){
            pk_show[y * 9 + x] = 0;
        }
    }

    private void setPKTile(int x, int y, int num) {
        if(pk_show==null){
            pk_show = new int[puzzle.length];
        }
        pk_show[y * 9 + x] = num;
    }

    public boolean isPKNumber(int x, int y) {
        return pk_show==null?false:pk_show[y * 9 + x] != 0;
    }

    @Override
    public boolean setTileIfValid(int x, int y, int value) {
        boolean b  = super.setTileIfValid(x, y, value);
         if(b){
             //发送输入数字的消息给对方
             BlueMessage msg = new BlueMessage(BlueMessage.HEADER_COMPERTITION_NUMBER);
             msg.put("num",value);
             msg.put("x",x);
             msg.put("y",y);
             send(msg);
         }
         return b;
    }


    @Override
    public void onBackPressed() {
        Builder builder = new Builder(this);
        builder.setMessage(getResources().getString(R.string.pk_stop));
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BlueMessage msg = new BlueMessage(BlueMessage.HEADER_PK_STOP);
                send(msg);
                GamePKHelp.super.onBackPressed();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel),null);
    }

    @Override
    protected void receive(BlueMessage msg) {
        super.receive(msg);
        switch (msg.getType()){
            //收到对方输入的数字
            case  BlueMessage.HEADER_COMPERTITION_NUMBER:
                int x = (int) msg.get("x");
                int y = (int) msg.get("y");
                int num = (int) msg.get("num");
                setPKTile(x, y,num);
                puzzleView.invalidate();
                break;

        }
    }


}
