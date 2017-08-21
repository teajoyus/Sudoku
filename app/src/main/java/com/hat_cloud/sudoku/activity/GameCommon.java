package com.hat_cloud.sudoku.activity;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hat_cloud.sudoku.entry.BlueMessage;
import com.hat_cloud.sudoku.entry.Music;
import com.hat_cloud.sudoku.entry.Rank;
import com.hat_cloud.sudoku.iface.IGame;
import com.hat_cloud.sudoku.view.CalcTimeTextView;
import com.hat_cloud.sudoku.view.Keypad;
import com.hat_cloud.sudoku.view.PuzzleView;
import com.hat_cloud.sudoku.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * 几个游戏类型的基类。因为几个类型是有很多共同的地方的，所以抽出来形成一个基类
 * 不同类型的游戏只要继承这个类，就可以实现自己的游戏规则。
 * 该类已经继承自BaseActivity，所以该类就拥有了蓝牙通信的功能，只需要把接口通信方法与游戏关联起来即可
 * 该类实现了IGame接口，IGame是数独游戏的相关类，在PuzzleView会持有对IGame的引用，从而根据不同的子类传进去的类型根据多态实现不同的反应
 */

public class GameCommon extends BaseActivity implements IGame {
    public static final String TAG_GameCommon = "Sudoku";
    protected int puzzle[];
    protected int initPuzzle[];
    protected int helpPuzzle[];
    protected PuzzleView puzzleView;
    protected boolean tip;
    protected int diff;
    protected int type;
    protected int time;
    protected String name;
    protected CalcTimeTextView time_tv;
    protected TextView unRead;
    protected ImageView chat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra("no", "no");//表示让BaseActivity不要去打开蓝牙服务端
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initIntentData(); //接收参数
        initPuzzle();//初始化棋盘数据
        getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_BY_BLUE);
        initView();//初始化布局

        for (int i=0;i<puzzle.length;i++){
            System.out.print(puzzle[i]+",");
        }
        System.out.println("");
        for (int i=0;i<initPuzzle.length;i++){
            System.out.print(initPuzzle[i]+",");
        }
        System.out.println("");
        test();
    }
    private void test(){
        BlueMessage blueMessage = new BlueMessage(BlueMessage.HEADER_CHAT_MESSAGE);
        blueMessage.put("chat", SystemClock.uptimeMillis()+"");
       // onReceiveChat(blueMessage);

    }
    /**
     * 初始化棋盘数据
     */
    protected void initPuzzle(){
        puzzle = getPuzzle(diff);
        //如果是自己生成棋局,那么要把棋局发送给对方
        if(diff!=DIFFICULTY_BY_BLUE) {
            sendPuzzle();
        }
        calculateUsedTiles();
        if(getType()!=IGame.GAME_LOCAL) {
            Prefs.setHints(this, tip);
        }
    }
    /**
     * 接收参数
     */
    protected void initIntentData(){
        diff = getIntent().getIntExtra(KEY_DIFFICULTY,
                DIFFICULTY_EASY);
        type = getIntent().getIntExtra(BLUE_TYPE_PK,
                -1);
        name = getIntent().getStringExtra(BLUE_NAME);
        tip = getIntent().getBooleanExtra(IGame.BLUE_TIP_PK,false);
        time = getIntent().getIntExtra(PREF_TIME,0);
    }

    /**
     * 初始化布局
     */
    protected void initView(){
        TextView type_tv = (TextView) findViewById(R.id.type_tv);
         time_tv = (CalcTimeTextView) findViewById(R.id.time_tv);
        time_tv.setVisibility(View.VISIBLE);
        if(type!=-1) {
        String arr[] = new String[]{getResources().getString(R.string.pk_type_time),
                getResources().getString(R.string.pk_type_comp),getResources().getString(R.string.pk_type_comm)};
        type_tv.setText(arr[type]);
            type_tv.setVisibility(View.VISIBLE);
            if(time!=0){
                time_tv.setTime(time);
            }
        }

        initActionBar();//初始化标题
        initPuzzleView();//初始化puzzle布局
        if(type!=-1) {
            chat = (ImageView) findViewById(R.id.chat);
            unRead = (TextView) findViewById(R.id.unread);
            chat.setVisibility(View.VISIBLE);
            gotoChatListener();
        }
    }

    /**
     * 点击进入聊天界面
     */
    protected void gotoChatListener(){
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameCommon.this, ChatActivity.class);
                startActivity(intent);
                unRead.setText("0");
                unRead.setVisibility(View.INVISIBLE);
            }
        });

    }
    /**
     * 初始化标题
     */
    protected void initActionBar(){
        ActionBar actionBar;
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if(name!=null){
                actionBar.setTitle("\t\t\t\t"+getResources().getString(R.string.pk_with_name).replace("XXX",name));
            }
        }
    }
    /**
     * 初始化puzzle布局
     */
    protected void initPuzzleView(){
        puzzleView = new PuzzleView(this, this);
        LinearLayout mLayout;
        mLayout = (LinearLayout) findViewById(R.id.gamelayout);
        mLayout.addView(puzzleView);
        puzzleView.requestFocus();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * 菜单项，可以进入设置页面
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, Prefs.class));
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
    public void onPause() {
        super.onPause();
        Log.d(TAG_GameCommon, "onPause");
        Music.stop();
    }

    /**
     * 如果是客户端的话，就把数独的棋局发送给对方，让两个人的棋局是一样的
     */
   private void sendPuzzle(){
        BlueMessage msg = new BlueMessage(BlueMessage.HEADER_SEND_PUZZLE);
        msg.put(PREF_PUZZLE,toPuzzleString(puzzle));
        msg.put(PREF_INIT_PUZZLE,toPuzzleString(initPuzzle));
        send(msg);
    }
    /**
     * Use seed to create a random Sudoku Array
     */
    private String creatSudokuArray(String seed) {
        int[] seedPuzzle = fromPuzzleString(seed);
        ArrayList<Integer> randomList = new ArrayList<Integer>();
        Random random = new Random();
        for (int i = 0; i < 9; i++) {
            int randomNum = random.nextInt(9) + 1;
            while (true) {
                if (!randomList.contains(randomNum)) {
                    randomList.add(randomNum);
                    break;
                }
                randomNum = random.nextInt(9) + 1;
            }
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = 0; k < 9; k++) {
                    if (seedPuzzle[i * 9 + j] == randomList.get(k)) {
                        seedPuzzle[i * 9 + j] = randomList.get((k + 1) % 9);
                        break;
                    }
                }
            }
        }

        return toPuzzleString(seedPuzzle);

    }
    /**
     * Given a difficulty level, come up with a new puzzle
     */
    private int[] getPuzzle(int diff) {
        String seedPuzzle = "978312645"
                + "312645978"
                + "645978312"
                + "789123456"
                + "123456789"
                + "456789123"
                + "897231564"
                + "231564897"
                + "564897231";
        String puzString = creatSudokuArray(seedPuzzle);
        Log.d(TAG_GameCommon, "on getPuzzle(), puzString:" + puzString);
        int[] puzInt = fromPuzzleString(puzString);
        int removeNum;

        //Continue last game
        switch (diff) {
            case DIFFICULTY_BY_BLUE:
                puzString = getIntent().getStringExtra(PREF_PUZZLE);
                initPuzzle = fromPuzzleString(getIntent().getStringExtra(PREF_INIT_PUZZLE));
                return fromPuzzleString(puzString);
            case DIFFICULTY_HARD:
                removeNum = IGame.EASY;
                break;
            case DIFFICULTY_MEDIUM:
                removeNum = IGame.MIDDLE;
                break;
            case DIFFICULTY_EASY:
            default:
                removeNum = IGame.HARD;
                break;
        }

        Log.d(TAG_GameCommon, "on getPuzzle(), removeNum:" + Integer.toString(removeNum));

        Random random = new Random();
        int removeNums[] = new int[removeNum];
        removeNums[0] = random.nextInt(81);
        for (int i = 1; i < removeNum; i++) {
            removeNums[i] = random.nextInt(81);

            for (int j = 0; j < i; j++) {
                while (removeNums[i] == removeNums[j]) {
                    i--;
                }
            }
        }

        Log.d(TAG_GameCommon, "on getPuzzle(), removeNums:" + toPuzzleString(removeNums));

        for (int removeNum1 : removeNums) {
            puzInt[removeNum1] = 0;
        }
        initPuzzle = puzInt;
        puzString = toPuzzleString(puzInt);
        Log.d(TAG_GameCommon, "on getPuzzle(), puzString:" + puzString);
        return fromPuzzleString(puzString);
    }
    /**
     * 判断该空格是不是初始给出的数字
     * @param x
     * @param y
     * @return
     */
    @Override
    public boolean isInitNumber(int x, int y) {
        return initPuzzle[y * 9 + x] != 0;
    }

    @Override
    public boolean hasNumber(int x, int y) {
      return puzzle[y * 9 + x] != 0;
    }

    /**
     * 把该格子的数字转换为字符串
     * @param x
     * @param y
     * @return
     */
    @Override
    public String getTileString(int x, int y) {
        int v = getTile(x, y);
        if (v == 0)
            return "";
        else
            return String.valueOf(v);
    }
    /**
     * 获得该格子的数字
     */
    protected int getTile(int x, int y) {
        return puzzle[y * 9 + x];
    }

    /**
     * 改变该各自的数字
     */

    protected void setTile(int x, int y, int value) {
        puzzle[y * 9 + x] = value;
        if(helpPuzzle!=null) {
            helpPuzzle[y * 9 + x] = 0;
        }

    }

    /**
     * Return cached used tiles visible from the given coords
     * @param x
     * @param y
     * @return
     */
    @Override
    public int[] getUsedTiles(int x, int y ) {
        return used[x][y];
    }

    /**
     * 清除某个位置的数字
     * @param x
     * @param y
     */
    @Override
    public void clearTile(int x, int y) {
        puzzle[y * 9 + x] = 0;
        puzzleView.invalidate();
    }
    /**
     * 清除所有输入位置的数字
     */
    @Override
    public void clearAllTile() {
        for (int i = 0;i<initPuzzle.length;i++){
            puzzle[i] = initPuzzle[i];
        }
        puzzleView.invalidate();
    }

    /**
     * 当从数字键盘上按下后，调用到此方法
     * @param x
     * @param y
     */
    @Override
    public void showKeypadOrError(int x, int y) {
        //isWon代表游戏已经结束的了
        if(isWon()){
            showToast(R.string.pk_end_tip);
            return ;
        }
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
        }
    }

    /**
     * 长按的时候确定数字
     * @param x
     * @param y
     */
    @Override
    public void confirmTile(int x, int y) {
       // initPuzzle[y * 9 + x] = puzzle[y * 9 + x];
       // puzzleView.invalidate();
    }

    /**
     * 如果数字是有效的话则填入并且发送给对方，无效的话返回false
     * @param x
     * @param y
     * @param value
     * @return
     */
    @Override
    public boolean setTileIfValid(int x, int y, int value) {
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
    /**
     * Cache of used tiles
     */
    protected final int used[][][] = new int[9][9][];
    /**
     * Compute the two dimensional array of used tiles
     */
    protected void calculateUsedTiles() {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                used[x][y] = calculateUsedTiles(x, y);
                // Log.d(TAG, "used[" + x + "][" + y + "] = "
                // + toPuzzleString(used[x][y]));
            }
        }
    }
    /**
     * Compute the used tiles visible from this position
     */
    protected int[] calculateUsedTiles(int x, int y) {
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

    /**
     * 判断是不是已经赢了
     * @return
     */
    @Override
    public boolean isWon() {
        for (int i : puzzle) {
            if (i == 0)
                return false;
        }
        return true;
    }

    private boolean isWon = true;
    /**
     * 比赛胜利了
     */
    @Override
    public void Congratulations() {
        //如果不是自己赢了的话，那么只提示挑战成功
        //要自己赢了的话才会去通知对方
        if(isWon){
            time_tv.stop();//停止即时
            if(type!=IGame.GAME_LOCAL) {
                BlueMessage msg = new BlueMessage(BlueMessage.HEADER_PK_END);
                msg.put("time",time_tv.getText().toString());
                msg.put("time_num",time_tv.getTime()+"");
                send(msg);
            }
            Rank new_rank = new Rank();
            new_rank.setName(mBluetoothAdapter==null||mBluetoothAdapter.getName()==null?"emulator":mBluetoothAdapter.getName());
            new_rank.setTime(time_tv.getText().toString());
            new_rank.setTime_num(time_tv.getTime());
            new_rank.setType(getType());
            update(new_rank);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.prize);
        //输赢的提示不一样
        if(isWon) {
            builder.setTitle(getResources().getString(R.string.pk_congratulations_text));
        }else{
            builder.setTitle(getResources().getString(R.string.pk_congratulations_finish_text));
        }
        String content = getResources().getString(R.string.pk_congratulations_content_text).replace("XXX",time_tv.getText().toString());
        builder.setMessage(content);
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.confirm),
                null);
        builder.show();
    }

    /**
     * 更新排行榜
     * @param rank
     */
    private void update(Rank rank){
        //如果成绩第一则记录排行
        SharedPreferences sharedPreferences = getSharedPreferences("rank",MODE_ENABLE_WRITE_AHEAD_LOGGING );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String s  = sharedPreferences.getString(getType()+"",null);
        //如果有记录的话，则比较一下比赛的时间
        Rank old_rank = null;
        if(s!=null&&Rank.isValid(s)) {
            old_rank = new Rank(s);
        }
        //如果现在挑战的时间比记录里面的短，那么就替换
        if(old_rank==null||rank.getTime_num()<old_rank.getTime_num()){
            editor.putString(getType()+"",rank.toString());
            Log.i(TAG_GameCommon, "Congratulations: "+rank);
            editor.commit();
//            SharedPreferences preferences =  getSharedPreferences(IGame.BLUE_NAME,MODE_ENABLE_WRITE_AHEAD_LOGGING );
//            Map<String ,String> map = (Map<String, String>) preferences.getAll();
//            Log.i(TAG_GameCommon, "map: "+map);
        }
    }
    /**
     * 得到用户输入的puzzle
     * @return
     */
    @Override
    public int[] getPuzzle() {
        return puzzle;
    }

    /**
     * 得到初始生成的puzzle
     * @return
     */
    @Override
    public int[] getInitPuzzle() {
        return initPuzzle;
    }

    /**
     * 是否允许提示
     * @return
     */
    @Override
    public boolean allowTip() {
        return tip;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public boolean isTrue(int trueType,int x,int y) {
        if(IGame.GAME_PK_HELP==trueType){
            return helpPuzzle!=null&&helpPuzzle[y * 9 + x]!=0;
        }
        return false;
    }

    /**
     * Convert an array into a puzzle string
     */
    static public String toPuzzleString(int[] puz) {
        StringBuilder buf = new StringBuilder();
        for (int element : puz) {
            buf.append(element);
        }
        return buf.toString();
    }

    /**
     * Convert a puzzle string into an array
     */
    static public int[] fromPuzzleString(String string) {
        int[] puz = new int[string.length()];
        float l = 0f;
        for (int i = 0; i < puz.length; i++) {
            puz[i] = string.charAt(i) - '0';
        }
        return puz;
    }

    @Override
    public Object getData(int type,int x, int y) {
        //如果是帮助的话就返回帮助的参考数字
        if(type ==IGame.GAME_PK_HELP&&helpPuzzle!=null){
            return String.valueOf(helpPuzzle[y*9+x]);
        }
        return null;
    }

    /**
     * 收到对方赢了的消息
     */
    private  void onPKEnd(BlueMessage msg){
        //false表示输了
        isWon = false;
        Rank rank = new Rank();
        rank.setName(name);
        rank.setTime_num(Integer.parseInt((String) msg.get("time_num")));
        rank.setType(getType());
        rank.setTime((String) msg.get("time"));
        update(rank);
        //弹出提示框提示对方已经胜利了
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.pk_end_text));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.confirm),
                null);
        //弹出提示框询问要不要获取对方的帮助
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle(getResources().getString(R.string.pk_end_req_help_text));
        builder2.setCancelable(false);
        builder2.setPositiveButton(getResources().getString(R.string.confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BlueMessage msg = new BlueMessage(BlueMessage.HEADER_PK_REQ_HELP);
//                        int p[] = new int[]{0,0,5,0,0,8,2,1,0,6,2,0,7,0,0,4,4,0,7,0,9,3,4,5,6,3,8,0,5,3,2,0,0,1,0,7,0,8,0,1,0,0,4,5,0,0,0,0,4,5,0,2,0,0,5,0,0,0,0,2,0,0,1,0,6,2,0,0,0,5,3,0,9,7,1,0,0,0,0,0,0};
//                        int init_p[] = new int[]{0,0,5,0,0,8,0,1,0,6,2,0,7,0,0,0,4,0,7,0,9,3,4,5,6,0,8,0,5,3,2,0,0,1,0,7,0,8,0,1,0,0,4,5,0,0,0,0,4,5,0,2,0,0,5,0,0,0,0,2,0,0,1,0,6,2,0,0,0,5,3,0,9,7,1,0,0,0,0,0,0};
//                        msg.put(IGame.PREF_PUZZLE,toPuzzleString(p));
//                        msg.put(IGame.PREF_INIT_PUZZLE,toPuzzleString(init_p));
                        msg.put(IGame.PREF_PUZZLE,toPuzzleString(puzzle));
                        msg.put(IGame.PREF_INIT_PUZZLE,toPuzzleString(initPuzzle));
                        CalcTimeTextView tv = (CalcTimeTextView) findViewById(R.id.time_tv);
                        msg.put(IGame.PREF_TIME,tv.getTime());
                        send(msg);
                        dialog.dismiss();
                    }
                });
        builder2.setNegativeButton(getResources().getString(R.string.cancel),null);
        builder2.show();
        builder.show();
    }
    /**
     * 收到对方请求帮助的消息
     */
    private  void onPKEndRequestHelp(final BlueMessage msg){
        //弹出提示框询问要不要获取对方的帮助
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle(getResources().getString(R.string.pk_end_help_text));
        builder2.setCancelable(false);
        builder2.setPositiveButton(getResources().getString(R.string.confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(GameCommon.this,GamePKHelp.class);
                        intent.putExtra(IGame.KEY_DIFFICULTY, IGame.DIFFICULTY_BY_BLUE);
                        intent.putExtra(IGame.BLUE_NAME, name);
                        intent.putExtra(IGame.BLUE_TYPE_PK, getResources().getString(R.string.pk_type_comp));
                        intent.putExtra(IGame.BLUE_TIP_PK, tip);
                        intent.putExtra(IGame.PREF_PUZZLE, (String) msg.get(IGame.PREF_PUZZLE));
                        intent.putExtra(IGame.PREF_INIT_PUZZLE, (String) msg.get(IGame.PREF_INIT_PUZZLE));
                        intent.putExtra(IGame.PREF_TIME, (Integer) msg.get(IGame.PREF_TIME));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        builder2.setNegativeButton(getResources().getString(R.string.cancel),null);
        builder2.show();

    }

    /**
     * 接收到对方同意帮助的消息
     */
    private void onConnectHelp(){
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle(getResources().getString(R.string.pk_end_connect_help_text));
        builder2.setNegativeButton(getResources().getString(R.string.confirm),null);
        builder2.show();
    }
    /**
     * 收到对方在帮助的时候发过来的参考数字
     * @param msg
     */
    protected void onHelpRefer(BlueMessage msg){
        int value = (Integer) msg.get("value");
        int x = (Integer) msg.get("x");
        int y = (Integer) msg.get("y");
        if(helpPuzzle==null){
            helpPuzzle = new int[puzzle.length];
        }
        helpPuzzle[y * 9 + x] = value;
        puzzleView.invalidate();//刷新页面
    }

    protected  void onReceiveChat(BlueMessage msg){
        //将消息添加到列表中
        ChatActivity.addTargetMessage((String) msg.get("chat"));
        int num = Integer.parseInt(unRead.getText().toString());
        unRead.setText(String.valueOf(num+1));
        unRead.setVisibility(View.VISIBLE);
        unRead.setScaleX(1.3f);
        unRead.setScaleY(1.3f);
        //未读消息的动画
        unRead.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500).setListener(new unReadAnimatorListener());

    }
    @Override
    protected void receive(BlueMessage msg) {
        super.receive(msg);
        switch (msg.getType()){
            //收到比赛结束的通知H
            case BlueMessage.HEADER_PK_END:
                onPKEnd(msg);
                break;
            //收到请求帮助的通知
            case BlueMessage.HEADER_PK_REQ_HELP:
                onPKEndRequestHelp( msg);
                break;
            case BlueMessage.HEADER_HELP_REFER:
                onHelpRefer(msg);
                break;
            case BlueMessage.HEADER_CHAT_MESSAGE:
                onReceiveChat(msg);
                break;
            case BlueMessage.HEADER_PK_HELP:
                onConnectHelp();
                break;


        }
    }
    class unReadAnimatorListener implements Animator.AnimatorListener{

        @Override
        public void onAnimationEnd(Animator animation) {
            if(unRead.getScaleX()-1.0f<0.1f){
                unRead.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500);
            }else{
                unRead.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500);
            }
        }
        @Override
        public void onAnimationCancel(Animator animation) {}
        @Override
        public void onAnimationRepeat(Animator animation) { }
        @Override
        public void onAnimationStart(Animator animation) { }
    }

    @Override
    protected void onPKStop() {
        super.onPKStop();
        showToast(R.string.pk_stop_msg);
    }

    @Override
    public void onBackPressed() {
        if(isWon()){
            super.onBackPressed();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.pk_stop));
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BlueMessage msg = new BlueMessage(BlueMessage.HEADER_PK_STOP);
                send(msg);
                GameCommon.super.onBackPressed();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel),null);
        builder.show();
    }
}
