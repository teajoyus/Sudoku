package com.hat_cloud.sudo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;


import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.hat_cloud.sudo.activity.BaseActivity;
import com.hat_cloud.sudo.activity.Game;
import com.hat_cloud.sudo.activity.GamePKCommunication;
import com.hat_cloud.sudo.activity.GamePKCompertition;
import com.hat_cloud.sudo.activity.Prefs;
import com.hat_cloud.sudo.entry.BlueMessage;
import com.hat_cloud.sudo.entry.Test;
import com.hat_cloud.sudo.iface.IGame;
import com.hat_cloud.sudoku.R;


public class PuzzleView extends View {
    private static final String TAG = "Sudoku";


    private float width;    // width of one tile
    private float height;   // height of one tile
    private int selX;       // X index of selection
    private int selY;       // Y index of selection
    private final Rect selRect = new Rect();

    private final IGame game;
    public PuzzleView(Context context,IGame game) {
        super(context);
        this.game = game;
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
	   

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
       if(w < h){
           width = w / 9f;
           height = width;
       }
       else{
           height = h / 9f;
           width = height;
       }
       getRect(selX, selY, selRect);
       Log.d(TAG, "onSizeChanged: width " + width + ", height "
               + height);
       super.onSizeChanged(w, h, oldw, oldh);
    }



    @Override
    protected void onDraw(Canvas canvas) {

      float lineSize;
      float topPadding = Math.abs(getWidth() - getHeight())/2;
      float leftPadding = Math.abs(getWidth() - getHeight())/2;
      if(getWidth() > getHeight()){
          topPadding = 0;
          lineSize = getHeight();
      }
      else{
          leftPadding = 0;
          lineSize = getWidth();
      }




      // Draw the background...

      Paint background = new Paint();
      background.setColor(getResources().getColor(
            R.color.puzzle_background));
      //canvas.drawRect(0, 0, getWidth(), getHeight(), background);
      canvas.drawRect(leftPadding, topPadding, lineSize + leftPadding, lineSize + topPadding, background);

      // Draw the board...

      // Define colors for the grid lines
      Paint dark = new Paint();
      dark.setColor(getResources().getColor(R.color.puzzle_dark));
      Paint hilite = new Paint();
      hilite.setColor(getResources().getColor(R.color.puzzle_hilite));
      Paint light = new Paint();
      light.setColor(getResources().getColor(R.color.puzzle_light));




      // Draw the numbers...

      // Define color and style for numbers
      Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
      foreground.setColor(getResources().getColor(
            R.color.puzzle_foreground));
      foreground.setStyle(Style.FILL);
      foreground.setTextSize(height * 0.75f);
      foreground.setTextScaleX(width / height);
      foreground.setTextAlign(Paint.Align.CENTER);

      Paint foregroundInit = new Paint(Paint.ANTI_ALIAS_FLAG);
      foregroundInit.setColor(getResources().getColor(
            R.color.puzzle_foreground_init));
      foregroundInit.setStyle(Style.FILL);
      foregroundInit.setTextSize(height * 0.75f);
      foregroundInit.setTextScaleX(width / height);
      foregroundInit.setTextAlign(Paint.Align.CENTER);

      Paint backgroundGray = new Paint();
      Rect r = new Rect();

      // Draw the number in the center of the tile
      FontMetrics fm = foreground.getFontMetrics();
      // Centering in X: use alignment (and X at midpoint)
      float x = width / 2;
      // Centering in Y: measure ascent/descent first
      float y = height / 2 - (fm.ascent + fm.descent) / 2;
      for (int i = 0; i < 9; i++) {
         for (int j = 0; j < 9; j++) {
             /*if(!this.game.getTileString(i, j).isEmpty()){
                    //Rect r = new Rect();
                    getRect(i, j, r);
                    canvas.drawRect(r, numberBackgroud);
            }*/
             if (((i > 2 && i < 6) || (j > 2 && j < 6))
                        && !((i > 2 && i < 6) && (j > 2 && j < 6))){
                   getRect(i, j, r);
                   backgroundGray.setColor(getResources().getColor(R.color.puzzle_graybackgroud));
                   canvas.drawRect(r, backgroundGray);
             }

             if(game.isInitNumber(i, j)){
                 canvas.drawText(this.game.getTileString(i, j), i
                          * width + leftPadding + x, j * height + y + topPadding, foregroundInit);
             }
             else{
                 /** 如果是竞赛类型，并且是对方已经输入的数字的话，那么就是显示固定的颜色*/
                    if(game.getType()==IGame.GAME_PK_COMPERTITION&&game.isTrue(IGame.GAME_PK_COMPERTITION,i,j)){
                         Log.i(TAG, "onDraw: isPKNumber:"+i+":"+j);
                         Paint pk = new Paint(Paint.ANTI_ALIAS_FLAG);
                         getRect(i, j, r);
                        pk.setColor(getResources().getColor(R.color.puzzle_hint_3));
                         canvas.drawRect(r, pk);
                     /*****************/
                    //如果是交流类型的话，则额外要绘制每个方格里面的参考数字，如果方格还没有确定数字的话
                 }else if(game.getType()==IGame.GAME_PK_TCOMMUNICATION&&game.isTrue(IGame.GAME_PK_COMPERTITION,i,j)) {
                        Paint prefence = new Paint(Paint.ANTI_ALIAS_FLAG);
                        prefence.setColor(getResources().getColor(
                                R.color.puzzle_foreground));
                        prefence.setStyle(Style.FILL);
                        prefence.setTextSize(foreground.getTextSize()/3);
                        prefence.setTextScaleX(width / height);
                        prefence.setTextAlign(Paint.Align.CENTER);
                        int paddingX = (int) (x/1.9);
                        int paddingY = (int) (y/2.5);
                        int[][] arr = (int[][]) game.getData(i,j);
                        for(int ii = 0;ii < 3; ii++){
                            for(int jj = 0;jj < 3; jj++){
                                if(arr[jj][ii]==0)continue;
                                canvas.drawText(jj*3+(ii+1)+"", i
                                        * width + leftPadding+ paddingX*(ii+1) , j * height +  topPadding+ paddingY*(jj+1), prefence);
                            }

                        }

                    }else {
                     canvas.drawText(this.game.getTileString(i, j), i
                             * width + leftPadding + x, j * height + y + topPadding, foreground);
                 }
             }

         }
      }



      // Draw the minor grid lines
      for (int i = 0; i < 9; i++) {
          if (i % 3 == 0)
              continue;
         canvas.drawLine(leftPadding, i * height + topPadding, lineSize + leftPadding,
                 i * height + topPadding, light);
         canvas.drawLine(leftPadding, i * height + 1 + topPadding, lineSize + leftPadding,
                 i * height + 1 + topPadding, hilite);
         canvas.drawLine(i * width + leftPadding, topPadding, i * width + leftPadding,
                 lineSize + topPadding, light);
         canvas.drawLine(i * width + 1 + leftPadding, topPadding,
                 i * width + 1 + leftPadding, lineSize + topPadding, hilite);
      }
      // Draw the major grid lines
      for (int i = 0; i <= 9; i++) {
         if (i % 3 != 0)
            continue;
         canvas.drawLine(leftPadding, i * height + topPadding, lineSize + leftPadding,
                 i * height + topPadding, dark);
         canvas.drawLine(leftPadding, i * height + 1 + topPadding, lineSize + leftPadding,
                 i * height + 1 + topPadding, hilite);
         canvas.drawLine(i * width + leftPadding, topPadding, i * width + leftPadding,
                 lineSize + topPadding, dark);
         canvas.drawLine(i * width + 1 + leftPadding, topPadding, i * width + 1 + leftPadding,
                 lineSize + topPadding, hilite);
      }





      if(Prefs.getHints(getContext())&& (Test.TEST||game.allowTip()) ){
          // Draw the hints...
          // Pick a hint color based on #moves left
          Paint hint = new Paint();
          int c[] = { getResources().getColor(R.color.puzzle_hint_0),
                getResources().getColor(R.color.puzzle_hint_1),
                getResources().getColor(R.color.puzzle_hint_2), };

          for (int i = 0; i < 9; i++) {
             for (int j = 0; j < 9; j++) {
                int movesLeft = 9 - game.getUsedTiles(i, j).length;
                if(game.getTileString(i, j).isEmpty()){
                    if (movesLeft < c.length) {
                       getRect(i, j, r);
                       hint.setColor(c[movesLeft]);
                       canvas.drawRect(r, hint);
                    }
                }
             }
          }
      }




      // Draw the selection...

      Log.d(TAG, "selRect=" + selRect);
      Paint selected = new Paint();
      selected.setColor(getResources().getColor(
            R.color.puzzle_selected));
      canvas.drawRect(selRect, selected);



    }
    private long down_time,up_time;
    private static final int DELAYED_TIME = 200;
    @SuppressWarnings("all")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "onTouchEvent event: "+event);
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            down_time = event.getEventTime();
            return true;
        }

        up_time = event.getEventTime();
        if(down_time==0){
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE&&(up_time-down_time)<1000L) {
          return super.onTouchEvent(event);
      }
      if(event.getAction() == MotionEvent.ACTION_MOVE&&down_time==0){
          return super.onTouchEvent(event);
      }
      float topPadding = Math.abs(getWidth() - getHeight())/2;
      float leftPadding = Math.abs(getWidth() - getHeight())/2;
      if(getWidth() > getHeight())
          topPadding = 0;
      else
          leftPadding = 0;

      float x = event.getX() - leftPadding;
      float y = event.getY() - topPadding;

      if(x < 0 || x > 9 * width || y < 0 || y > 9 * height)
          return super.onTouchEvent(event);

      select((int) (x / width),
            (int) (y / height));
        //如果长按2秒的话认为是长按事件
        if(up_time - down_time > 1000L){
            //TODO
            if(!game.hasNumber(selX,selY)){
                game.confirmTile(selX,selY);

            }
        }else {
            if (!game.isInitNumber(selX, selY))
                game.showKeypadOrError(selX, selY);
        }
      Log.d(TAG, "onTouchEvent: x " + selX + ", y " + selY);
        down_time = 0L;
      return true;
    }


    @SuppressWarnings("all")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event="
            + event);
      switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_UP:
         select(selX, selY - 1);
         break;
      case KeyEvent.KEYCODE_DPAD_DOWN:
         select(selX, selY + 1);
         break;
      case KeyEvent.KEYCODE_DPAD_LEFT:
         select(selX - 1, selY);
         break;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
         select(selX + 1, selY);
         break;


      case KeyEvent.KEYCODE_0:
      case KeyEvent.KEYCODE_SPACE: setSelectedTile(0); break;
      case KeyEvent.KEYCODE_1:     setSelectedTile(1); break;
      case KeyEvent.KEYCODE_2:     setSelectedTile(2); break;
      case KeyEvent.KEYCODE_3:     setSelectedTile(3); break;
      case KeyEvent.KEYCODE_4:     setSelectedTile(4); break;
      case KeyEvent.KEYCODE_5:     setSelectedTile(5); break;
      case KeyEvent.KEYCODE_6:     setSelectedTile(6); break;
      case KeyEvent.KEYCODE_7:     setSelectedTile(7); break;
      case KeyEvent.KEYCODE_8:     setSelectedTile(8); break;
      case KeyEvent.KEYCODE_9:     setSelectedTile(9); break;
      case KeyEvent.KEYCODE_ENTER:
      case KeyEvent.KEYCODE_DPAD_CENTER:
         game.showKeypadOrError(selX, selY);
         break;


      default:
         return super.onKeyDown(keyCode, event);
      }
      return true;
    }
	   
    public void clearTile(){
        game.clearTile(selX,selY);
    }
    public void clearAllTile(){
        game.clearAllTile();
    }

    public void setSelectedTile(int tile) {
        Log.i(TAG,"selX:"+selX);
        Log.i(TAG,"selY:"+selY);
        Log.i(TAG,"tile:"+tile);
      if (game.setTileIfValid(selX, selY, tile)) {
         invalidate();// may change hints
      } else {
         // Number is not valid for this tile

         Log.d(TAG, "setSelectedTile: invalid: " + tile);

         startAnimation(AnimationUtils.loadAnimation((Context) game,
               R.anim.shake));
          Toast.makeText((Context) game,getResources().getString(R.string.invalid_toast).replace("XXX",tile+""),Toast.LENGTH_SHORT).show();
          return;
      }
      if(game.isWon()){
          game.Congratulations();
      }
      Log.d("debug", "puz:" + Game.toPuzzleString(game.getPuzzle()));
      Log.d("debug", "initpuz:" + Game.toPuzzleString(game.getInitPuzzle()));
    }



    private void select(int x, int y) {
      invalidate(selRect);
      selX = Math.min(Math.max(x, 0), 8);
      selY = Math.min(Math.max(y, 0), 8);
      getRect(selX, selY, selRect);
      invalidate(selRect);
    }



    private void getRect(int x, int y, Rect rect) {
      float topPadding = Math.abs(getWidth() - getHeight())/2;
      float leftPadding = Math.abs(getWidth() - getHeight())/2;
      if(getWidth() > getHeight())
          topPadding = 0;
      else
          leftPadding = 0;

      rect.set((int) (x * width + leftPadding), (int) (y * height + topPadding), (int) (x
            * width + leftPadding + width), (int) (y * height + height + topPadding));
    }
	   
	   
}
