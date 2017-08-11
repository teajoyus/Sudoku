package com.hat_cloud.sudo.iface;

import com.hat_cloud.sudo.entry.Test;

/**
 */

public interface IGame {

    String KEY_DIFFICULTY = "org.example.sudoku.difficulty";
    String BLUE_NAME = "org.example.sudoku.blue.name";
    String BLUE_TYPE_PK = "org.example.sudoku.blue.type.pk";
    String BLUE_TIP_PK = "org.example.sudoku.blue.type.tip";

    int DIFFICULTY_EASY = 0;
    int DIFFICULTY_MEDIUM = 1;
    int DIFFICULTY_HARD = 2;


    int GAME_PK_TIME = 0;
    int GAME_PK_COMPERTITION = 1;
    int GAME_PK_TCOMMUNICATION = 2;

    String PREF_PUZZLE = "puzzle";
    String PREF_INIT_PUZZLE = "initPuzzle";
    String PREF_TIME = "time";
    int DIFFICULTY_BY_BLUE = -1;

    //三个难度的数字数量
    int EASY = 45;
    int MIDDLE = 36;
    int HARD = Test.TEST ? 2 : 18;

    int getType();
    Object getData( int i, int j);
    boolean isTrue(int trueType, int i, int j);

    boolean isInitNumber(int i, int j);

    boolean hasNumber(int i, int j);

    String getTileString(int i, int j);

    int[] getUsedTiles(int i, int j);

    void showKeypadOrError(int selX, int selY);

    void confirmTile(int selX, int selY);

    boolean setTileIfValid(int selX, int selY, int tile);

    boolean isWon();

    void Congratulations();

    int[] getPuzzle();

    int[] getInitPuzzle();

    boolean allowTip();

    void clearTile(int x,int y);
    void clearAllTile();

}
