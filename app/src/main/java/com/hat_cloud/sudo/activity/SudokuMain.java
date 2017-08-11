package com.hat_cloud.sudo.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.hat_cloud.sudo.entry.Music;
import com.hat_cloud.sudo.iface.IGame;
import com.hat_cloud.sudoku.R;


public class SudokuMain extends BaseActivity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_main);

        //---Set listener for all buttons---
        View continueButton = findViewById(R.id.button_continue_game);
        continueButton.setOnClickListener(this);
        continueButton.setVisibility(View.GONE);
        View startNewGameButton = findViewById(R.id.button_new_game);
        startNewGameButton.setOnClickListener(this);

        View rankListButton = findViewById(R.id.button_stage_mode);
        rankListButton.setOnClickListener(this);

        View aboutButton = findViewById(R.id.button_about);
        aboutButton.setOnClickListener(this);

        View exitButton = findViewById(R.id.button_exit);
        exitButton.setOnClickListener(this);

    }

    public void onClick(View v){
        switch(v.getId()){

            case R.id.button_continue_game:
                startGame(Game.DIFFICULTY_CONTINUE);
                break;

            case R.id.button_new_game:
                openNewGameDialog();
                break;
            case R.id.button_stage_mode:
                Intent intent = new Intent(this,BlueActivity.class);
                startActivity(intent);
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
        item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    //---Show the difficulty of game to select----
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
        Intent intent = new Intent(this, GamePKCommunication.class);
        intent.putExtra(IGame.KEY_DIFFICULTY, i);
        intent.putExtra(IGame.BLUE_NAME, "");
        intent.putExtra(IGame.BLUE_TYPE_PK, 2);
        intent.putExtra(IGame.BLUE_TIP_PK, true);
        startActivity(intent);
    }



    @Override
    protected void onPause() {
        super.onPause();
        Music.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
       // Music.play(this, R.raw.main);

    }

}