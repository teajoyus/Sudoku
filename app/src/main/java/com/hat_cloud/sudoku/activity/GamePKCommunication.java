package com.hat_cloud.sudoku.activity;


import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.hat_cloud.sudoku.entry.BlueMessage;
import com.hat_cloud.sudoku.view.Keypad;
import com.hat_cloud.sudoku.R;


/**
 * 交流类型的对战，继承自GameCommon，只要复写GameCommon的一些方法就可以实现自己的游戏规则
 */
public class GamePKCommunication extends GameCommon {
    private static final String TAG = "GamePKCommunication";
    //用于代表每个格子里面的参考数字
    private int referPuzzle[][][]= new int[9][9][9] ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        test();
    }
    private void test(){
        for (int i = 0;i<9;i++){
            for (int j = 0;j<9;j++){
                if(initPuzzle[i*9+j]==0){
                    for (int k = 0;k<9;k++) {
                        referPuzzle[i][j][k] = k+1;
                    }
                    referPuzzle[i][j][j] = 0;
                    break;
                }
            }
        }
        puzzleView.invalidate();
    }

    /**
     * 在puzzleView绘制时调用，返回该方格是否有需要绘制的参考数字
     * @param trueType
     * @param x
     * @param y
     * @return
     */
    @Override
    public boolean isTrue(int trueType, int x, int y) {
        if(super.isTrue(trueType,x,y)){
            return true;
        }
        if(trueType==GAME_PK_TCOMMUNICATION) {
            if (!getTileString(x, y).isEmpty()) {
                return false;
            }
            for (int i = 0; i < 9; i++) {
                //!=0代表有参考数字
                if (referPuzzle[y][x][i] != 0) {
                    return true;
                }
            }
        }
         return false;
    }

    /**
     * 得到某个位置上的参考数字
     * @param x
     * @param y
     * @return
     */
    public int[][] getReferPuzzle(int x, int y){
        int t[][] = new int[3][3];
        for (int i = 0,j=-1;i<9;i++){
            if(i%3==0)j++;
            t[j][i%3] = referPuzzle[y][x][i];
        }

        return t;
    }

    /**
     * 设置该格子的参考数字
     * @param x
     * @param y
     */
    private void setReferPuzzle(int x, int y,int value){
        referPuzzle[y][x][value -1 ] = value;
    }
    @Override
    public Object getData(int type,int x, int y) {
        return getReferPuzzle(x, y);
    }

    /**
     * 如果想要在长按确定数字之后，还能再长按修改确定数字的话，那么可以把这个方法直接返回false
     * @return
     */
    @Override
    public boolean hasNumber(int x, int y) {
        return super.hasNumber(x, y);
    }

    /**
     * 基类在清除数据时，这个位置的参考数字也要清除掉
     * @param x
     * @param y
     */
    @Override
    public void clearTile(int x, int y) {
        //清空该格子的参考数字
        for (int i = 0;i<9;i++){
            referPuzzle[y][x][i] = 0;
        }
        super.clearTile(x, y);
    }
    /**
     * 基类在清除数据时，参考数字也要清除掉
     */
    @Override
    public void clearAllTile() {
        //清空全部的参考数字
        for (int x = 0;x<9;x++) {
            for (int y = 0; y < 9; y++) {
                for (int i = 0; i < 9; i++) {
                    referPuzzle[y][x][i] = 0;
                }
            }
        }
        super.clearAllTile();
    }

    /**
     * 复写自己对于检验输入数字是否有效的规则
     * @param x
     * @param y
     * @param value
     * @return
     */
    @Override
    public boolean setTileIfValid(int x, int y, int value) {
        int tiles[] = getUsedTiles(x, y);
        //如果数字是无效的就直接返回了
        if (value != 0) {
            for (int tile : tiles) {
                if (tile == value){
                    if(longClick)longClick = false;
                    return false;
                }
            }
        }
        //如果是长按的话那么就直接设置数字
        if(longClick){
            setTile(x,y,value);
            longClick = false;
        }else {
            //如果已经有确定数字了，就不能再填入参考数字了
            if(hasNumber(x,y)){
                showToast(R.string.invalid_has_number_toast);
                return false;
            }
            setReferPuzzle(x, y, value);//更新格子的参考数字
            sendReferPuzzle(x,y,value);//发送参考数字给对方
        }
        calculateUsedTiles();
        return true;
    }

    /**
     * 发送参考数字给对方
     */
    private void sendReferPuzzle(int x,int y,int value){
        BlueMessage msg = new BlueMessage(BlueMessage.HEADER_COMMUNICATION_REFER);
        msg.put("x",x);
        msg.put("y",y);
        msg.put("value",value);
        send(msg);

    }

    /**
     * 收到对方发过来的参考数字
     * @param msg
     */
    private void onReceiveReferPuzzle(BlueMessage msg){
        int x = (Integer) msg.get("x");
        int y = (Integer) msg.get("y");
        int value = (Integer) msg.get("value");
        //如果该格子还没有确定数字的话，那么就会显示该参考数字
        if(getTileString(x,y).isEmpty()){
                referPuzzle[y][x][value-1] = value;
                puzzleView.invalidate();
        }
    }
    private  boolean longClick = false;

    /**
     * 长按时调用此方法
     * @param x
     * @param y
     */
    @Override
    public void confirmTile(int x, int y) {
        int tiles[] = getUsedTiles(x, y);
        if (tiles.length == 9) {
            Toast toast = Toast.makeText(this,
                    R.string.no_moves_label, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Log.d(TAG_GameCommon, "showKeypad: used=" + toPuzzleString(tiles));
            Dialog v = new Keypad(this, tiles, puzzleView);
            v.show();
            longClick = true;
        }

    }

    @Override
    protected void receive(BlueMessage msg) {
        super.receive(msg);
        switch (msg.getType()){
            case BlueMessage.HEADER_COMMUNICATION_REFER:
                onReceiveReferPuzzle(msg);
                break;
        }
    }
}
