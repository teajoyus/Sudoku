package com.hat_cloud.sudoku.activity;


import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import com.hat_cloud.sudoku.entry.BlueMessage;
import com.hat_cloud.sudoku.R;

/**
 * 交流类型的对战，继承自GameCommon，只要复写GameCommon的一些方法就可以实现自己的游戏规则
 */
public class GamePKCompertition extends GameCommon {
    private static final String TAG = "GamePKCompertition";
    public int pk_show[];//显示对方

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

    /**
     * 判断是不是对方输入的数字，用来作为对方已经输入的提示
     * @param x
     * @param y
     * @return
     */

    public boolean isPKNumber(int x, int y) {
        return pk_show==null?false:pk_show[y * 9 + x] != 0;
    }

    @Override
    public boolean isTrue(int trueType,int x, int y) {
        if(!super.isTrue(trueType,x,y)) {
            if(trueType==GAME_PK_COMPERTITION) {
                return isPKNumber(x, y) && getTileString(x, y).isEmpty();
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public boolean setTileIfValid(int x, int y, int value) {
        boolean b  = super.setTileIfValid(x, y, value);
        //发送输入数字的消息给对方
        if(b){
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
                GamePKCompertition.super.onBackPressed();
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
