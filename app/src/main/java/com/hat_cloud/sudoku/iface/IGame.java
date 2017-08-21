package com.hat_cloud.sudoku.iface;

import com.hat_cloud.sudoku.entry.Test;

/**
 * 不同游戏类型的相关公共方法，被提取了出来
 * 从而实现在PuzzleView里引用IGame对象，根据不同的游戏类型，IGame对象可以做出不同的反应
 */

public interface IGame {

    String KEY_DIFFICULTY = "org.example.sudoku.difficulty";
    String BLUE_NAME = "org.example.sudoku.blue.name";
    String BLUE_TYPE_PK = "org.example.sudoku.blue.type.pk";
    String BLUE_TIP_PK = "org.example.sudoku.blue.type.tip";

    int DIFFICULTY_EASY = 0;
    int DIFFICULTY_MEDIUM = 1;
    int DIFFICULTY_HARD = 2;


    int GAME_LOCAL = -1;
    int GAME_PK_TIME = 0;
    int GAME_PK_COMPERTITION = 1;
    int GAME_PK_TCOMMUNICATION = 2;

    int GAME_PK_HELP = 3;

    String PREF_PUZZLE = "puzzle";
    String PREF_INIT_PUZZLE = "initPuzzle";
    String PREF_TIME = "time";
    int DIFFICULTY_BY_BLUE = -1;

    //三个难度的数字数量
    int EASY = 45;
    int MIDDLE = 36;
    int HARD = Test.TEST ? 2 : 18;

    /**
     * 返回对战的类型
     * @return
     */
    int getType();

    /**
     * 根据type来拿到一个指定坐标的数据
     * @param trueType
     * @param i
     * @param j
     * @return
     */
    Object getData( int trueType,int i, int j);

    /**
     * 根据type来判断一个指定坐标的某个逻辑真假
     * @param trueType
     * @param i
     * @param j
     * @return
     */
    boolean isTrue(int trueType, int i, int j);

    /**
     * 是否是初始化棋局时的数字
     * @param i
     * @param j
     * @return
     */
    boolean isInitNumber(int i, int j);

    /**
     * 在该位置上用户有没有已经输入了数字
     * @param i
     * @param j
     * @return
     */
    boolean hasNumber(int i, int j);

    /**
     * 返回该格子的数字字符串
     * @param i
     * @param j
     * @return
     */
    String getTileString(int i, int j);

    /**
     * 返回已经用过的数字
     * @param i
     * @param j
     * @return
     */
    int[] getUsedTiles(int i, int j);

    /**
     * 显示键盘或者提示错误
     * @param selX
     * @param selY
     */
    void showKeypadOrError(int selX, int selY);

    /**
     * 确认该格子上的数字，长按的时候会调用
     * @param selX
     * @param selY
     */
    void confirmTile(int selX, int selY);

    /**
     * 判断输入有没有效，有的话就设置格子数字
     * @param selX
     * @param selY
     * @param tile
     * @return
     */
    boolean setTileIfValid(int selX, int selY, int tile);

    /**
     * 判断是不是已经挑战成功了
     * @return
     */

    boolean isWon();

    /**
     * 挑战成功后的祝贺词
     */
    void Congratulations();

    int[] getPuzzle();

    int[] getInitPuzzle();

    /**
     * 是否允许提示
     * @return
     */
    boolean allowTip();

    /**
     * 清空某个格子的数字
     * @param x
     * @param y
     */
    void clearTile(int x,int y);

    /**
     * 清空所有格子输入的数字
     */
    void clearAllTile();

}
