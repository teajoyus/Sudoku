package com.hat_cloud.sudoku;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;



public class SudokuMain extends Activity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_main);

        //为所有按钮设置一个监听器
        View continueButton = findViewById(R.id.button_continue_game);
        continueButton.setOnClickListener((OnClickListener) this);

        View startNewGameButton = findViewById(R.id.button_new_game);
        startNewGameButton.setOnClickListener((OnClickListener) this);

        View rankListButton = findViewById(R.id.button_stage_mode);
        rankListButton.setOnClickListener((OnClickListener) this);

        View aboutButton = findViewById(R.id.button_about);
        aboutButton.setOnClickListener( this);

        View exitButton = findViewById(R.id.button_exit);
        exitButton.setOnClickListener((OnClickListener) this);

    }

    public void onClick(View v){
        switch(v.getId()){

            case R.id.button_continue_game:
                startGame(Game.DIFFICULTY_CONTINUE);
                break;

            case R.id.button_new_game:
                openNewGameDialog();
                break;

            case R.id.button_about:
                Intent i = new Intent(this,About.class);
                startActivity(i);
                break;

            case R.id.button_exit:
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    //---Show the Difficulty of game to select----
    private static final String TAG = "Sudoku";
    private void openNewGameDialog(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.sudoku_difficulty_title)
                .setItems(R.array.difficulty,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                startGame(i);
                            }
                        })
                .show();
    }
    private void startGame(int i) {
        Log.d(TAG, "clicked on " + i);
        Intent intent = new Intent(this, Game.class);
        intent.putExtra(Game.KEY_DIFFICULTY, i);
        startActivity(intent);
    }



    @Override
    protected void onPause() {
        super.onPause();
        Music.stop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Music.play(this, R.raw.main);

    }
}
