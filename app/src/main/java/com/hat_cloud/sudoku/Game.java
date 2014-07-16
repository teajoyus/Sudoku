package com.hat_cloud.sudoku;


import java.util.ArrayList;
import java.util.Random;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Game extends Activity {
	   private static final String TAG = "Sudoku";

	   public static final String KEY_DIFFICULTY =
	      "org.example.sudoku.difficulty";
	   public static final int DIFFICULTY_EASY = 0;
	   public static final int DIFFICULTY_MEDIUM = 1;
	   public static final int DIFFICULTY_HARD = 2;
	   
	   private static final String PREF_PUZZLE = "puzzle" ;
	   private static final String PREF_INIT_PUZZLE = "initPuzzle";
	   protected static final int DIFFICULTY_CONTINUE = -1;
	   
	   protected int puzzle[];
	   protected int initPuzzle[];
	   private final String seedPuzzle =
			      "978312645"
			    + "312645978"
			    + "645978312" 
			    + "789123456"
			    + "123456789"
			    + "456789123" 
			    + "897231564"
			    + "231564897"
			    + "564897231";
/*		   private final String easyPuzzle =
			      "978312645"
			    + "312645978"
			    + "645978312" 
			    + "789123456"
			    + "123456789"
			    + "456789123" 
			    + "897231564"
			    + "231564897"
			    + "564897230";
   private final String easyPuzzle =
	      "360000000004230800000004200" +
	      "070460003820000014500013020" +
	      "001900000007048300000000045";
	   private final String mediumPuzzle =
	      "650000070000506000014000005" +
	      "007009000002314700000700800" +
	      "500000630000201000030000097";
	   private final String hardPuzzle =
	      "009000000080605020501078000" +
	      "000000700706040102004000000" +
	      "000720903090301080000000600";*/

	   private PuzzleView puzzleView;
	      
	   


	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      Log.d(TAG, "onCreate");
	      
	      int diff = getIntent().getIntExtra(KEY_DIFFICULTY,
	              DIFFICULTY_EASY);
	      puzzle = getPuzzle(diff);
	      /*if(diff == DIFFICULTY_CONTINUE){
	    	  initPuzzle = fromPuzzleString(getPreferences(MODE_PRIVATE).getString(PREF_INIT_PUZZLE, seedPuzzle));
	      }else{
	    	  initPuzzle = puzzle;
	      }*/
	      
	      calculateUsedTiles();
	      puzzleView = new PuzzleView(this);
	      setContentView(R.layout.activity_game);
	      
	      ActionBar actionBar=getActionBar();
          actionBar.show();
          actionBar.setDisplayHomeAsUpEnabled(true);
	      
	      LinearLayout mLayout;
	      mLayout = (LinearLayout) findViewById(R.id.gamelayout);
	      mLayout.addView(puzzleView);
	      
	      puzzleView.requestFocus();
	      
	      getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
	   }
	   
	   @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu, menu);
			return true;
		}
	   @Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch(item.getItemId()){
				case R.id.settings:
					startActivity(new Intent(this,Prefs.class));
					return true;
					
				case android.R.id.home:
					finish();
		            return true;
			
			}
			return false;
		}
	   
	   @Override
		protected void onResume() {
		
			super.onResume();
			Music.play(this, R.raw.game);
			
		}
	   
	   @Override
	   protected void onPause() {    
		   super.onPause();    
		   Log.d(TAG, "onPause" );    
		   Music.stop(this);    
		   // Save the current puzzle    
		   getPreferences(MODE_PRIVATE).edit().putString(PREF_PUZZLE, toPuzzleString(puzzle)).commit();
		   getPreferences(MODE_PRIVATE).edit().putString(PREF_INIT_PUZZLE, toPuzzleString(initPuzzle)).commit();
	   }
	   
	   // ...

	   /** Use seed to create a random Sudoku Array */
	   private String creatSudokuArray(String seed){
		   int[] seedPuzzle = fromPuzzleString(seed);
		   ArrayList <Integer>randomList = new ArrayList<Integer>();
		   Random random=new Random();  
		   for (int i = 0; i < 9; i++) {  
			   int randomNum=random.nextInt(9)+1;  
			   while (true) {  
				   if (!randomList.contains(randomNum)) {  
					   randomList.add(randomNum);  
					   break;  
				   }
				   randomNum=random.nextInt(9)+1; 
			   }
		   }
		   
		   for (int i = 0; i < 9; i++) {
			   for (int j = 0; j < 9; j++) {  
				   for (int k = 0; k < 9; k++) {   
					   if(seedPuzzle[i * 9 + j]==randomList.get(k)){
						   seedPuzzle[i * 9 + j]=randomList.get((k+1)%9);  
						   break;  
					   }
				   }
			   }
		   }
		   
		   return toPuzzleString(seedPuzzle);
		   
	   }
	   
	   /** Given a difficulty level, come up with a new puzzle */
	   private int[] getPuzzle(int diff) {
	      String puzString = creatSudokuArray(seedPuzzle);
	      Log.d(TAG, "on getPuzzle(), puzString:" + puzString);
	      int[] puzInt = fromPuzzleString(puzString);
	      int removeNum = 0;
	      
	      //Continue last game
	      switch (diff) {
	      case DIFFICULTY_CONTINUE:
	    		  puzString = getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE, seedPuzzle);
	    		  initPuzzle = fromPuzzleString(getPreferences(MODE_PRIVATE).getString(PREF_INIT_PUZZLE, seedPuzzle));
	    		  return fromPuzzleString(puzString);
	      case DIFFICULTY_HARD:
	    	  removeNum = 45;
	          break;
	      case DIFFICULTY_MEDIUM:
	    	  removeNum = 36;
	    	  break;
	      case DIFFICULTY_EASY:
	      default:
	    	  removeNum = 18;
	    	  break;
	      }
	      
	      Log.d(TAG, "on getPuzzle(), removeNum:" + Integer.toString(removeNum));
	      
	      Random random = new Random();
	      int removeNums[] = new int[removeNum];
	      removeNums[0] = random.nextInt(81);
	      for (int i = 1; i < removeNum; i++) {
	    	  removeNums[i] = random.nextInt(81);
	    	  
	    	  for (int j = 0; j < i; j++) {
	    		  while (removeNums[i] == removeNums[j]) {//������������������������������
						i--;
					}
	    	  }
	      }
	      
	      Log.d(TAG, "on getPuzzle(), removeNums:" + toPuzzleString(removeNums));
	      
	      for(int i = 0; i < removeNums.length; i++){
	    	  puzInt[removeNums[i]] = 0;
	      }
	      initPuzzle = puzInt;
	      puzString = toPuzzleString(puzInt);
	      Log.d(TAG, "on getPuzzle(), puzString:" + puzString);    
	      return fromPuzzleString(puzString);
	   }

	   /** Convert an array into a puzzle string */
	   static protected String toPuzzleString(int[] puz) {
	      StringBuilder buf = new StringBuilder();
	      for (int element : puz) {
	         buf.append(element);
	      }
	      return buf.toString();
	   }
	  
	   /** Convert a puzzle string into an array */
	   static protected int[] fromPuzzleString(String string) {
	      int[] puz = new int[string.length()];
	      for (int i = 0; i < puz.length; i++) {
	         puz[i] = string.charAt(i) - '0';
	      }
	      return puz;
	   }

	   /** Return the tile at the given coordinates */
	   private int getTile(int x, int y) {
	      return puzzle[y * 9 + x];
	   }
	   /** Change the tile at the given coordinates */

	   private void setTile(int x, int y, int value) {
	      puzzle[y * 9 + x] = value;
	   }

	   /** Return a string for the tile at the given coordinates */
	   protected String getTileString(int x, int y) {
	      int v = getTile(x, y);
	      if (v == 0)
	         return "";
	      else
	         return String.valueOf(v);
	   }

	   /** Change the tile only if it's a valid move */
	   protected boolean setTileIfValid(int x, int y, int value) {
	      int tiles[] = getUsedTiles(x, y);
	      if (value != 0) {
	         for (int tile : tiles) {
	            if (tile == value)
	               return false;
	         }
	      }
	      setTile(x, y, value);
	      calculateUsedTiles();
	      return true;
	   }

	   protected boolean isInitNumber(int x, int y) {
		      if(initPuzzle[y * 9 + x] != 0)
		    	  return true;
		      else
		    	  return false;
		   }
	   
	   /** Judge that whether the completion of this game */
	   protected boolean isWon(){
		   for(int i : puzzle){
			   if( i == 0)
				   return false;
		   }
		   return true;
	   }
	   
	   /** Congratulations to the game player */
	   protected void Congratulations(){
		   Builder builder = new AlertDialog.Builder(this);
		   builder.setIcon(R.drawable.prize);
		   builder.setTitle(getResources().getString(R.string.Congratulations_text));
		   builder.setCancelable(false);
		   builder.setPositiveButton(getResources().getString(R.string.Congratulations_back_to_mainmenu), 
				   new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {

						finish();
					}
				});
		   /*builder.setNegativeButton(getResources().getString(R.string.Congratulations_one_more_time), 
				   new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Bundle savedInstanceState = new Bundle();
						savedInstanceState.putBoolean("isReset", true);
						savedInstanceState.putInt("difficulty", diff);

						onCreate(savedInstanceState);
						
					}
				});*/
		   
		   builder.show();
		   
	   }
	   
	   /** Open the keypad if there are any valid moves */
	   protected void showKeypadOrError(int x, int y) {
	      int tiles[] = getUsedTiles(x, y);
	      if (tiles.length == 9) {
	         Toast toast = Toast.makeText(this,
	               R.string.no_moves_label, Toast.LENGTH_SHORT);
	         toast.setGravity(Gravity.CENTER, 0, 0);
	         toast.show();
	      } else {
	         Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
	         Dialog v = new Keypad(this, tiles, puzzleView);
	         v.show();
	      }
	   }

	   /** Cache of used tiles */
	   private final int used[][][] = new int[9][9][];

	   /** Return cached used tiles visible from the given coords */
	   protected int[] getUsedTiles(int x, int y) {
	      return used[x][y];
	   }

	   /** Compute the two dimensional array of used tiles */
	   private void calculateUsedTiles() {
	      for (int x = 0; x < 9; x++) {
	         for (int y = 0; y < 9; y++) {
	            used[x][y] = calculateUsedTiles(x, y);
	            // Log.d(TAG, "used[" + x + "][" + y + "] = "
	            // + toPuzzleString(used[x][y]));
	         }
	      }
	   }

	   
	   /** Compute the used tiles visible from this position */
	   private int[] calculateUsedTiles(int x, int y) {
	      int c[] = new int[9];
	      // horizontal
	      for (int i = 0; i < 9; i++) { 
	         if (i == x)
	            continue;
	         int t = getTile(i, y);
	         if (t != 0)
	            c[t - 1] = t;
	      }
	      // vertical
	      for (int i = 0; i < 9; i++) { 
	         if (i == y)
	            continue;
	         int t = getTile(x, i);
	         if (t != 0)
	            c[t - 1] = t;
	      }
	      // same cell block
	      int startx = (x / 3) * 3; 
	      int starty = (y / 3) * 3;
	      for (int i = startx; i < startx + 3; i++) {
	         for (int j = starty; j < starty + 3; j++) {
	            if (i == x && j == y)
	               continue;
	            int t = getTile(i, j);
	            if (t != 0)
	               c[t - 1] = t;
	         }
	      }
	      // compress
	      int nused = 0; 
	      for (int t : c) {
	         if (t != 0)
	            nused++;
	      }
	      int c1[] = new int[nused];
	      nused = 0;
	      for (int t : c) {
	         if (t != 0)
	            c1[nused++] = t;
	      }
	      return c1;
	   }
	}
