package com.hat_cloud.sudo.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.hat_cloud.sudo.entry.BlueMessage;
import com.hat_cloud.sudo.iface.IGame;
import com.hat_cloud.sudo.task.Task;
import com.hat_cloud.sudo.task.TaskService;
import com.hat_cloud.sudoku.R;

import java.lang.ref.WeakReference;

public class BaseActivity extends Activity {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAg = "BaseActivity";
    public Handler serverHandler =new MyHandler(this);
    static class MyHandler extends Handler{
        private final WeakReference<BaseActivity> mActivity;

        public MyHandler(BaseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mActivity.get()==null)return;
            switch (msg.what) {
                case Task.TASK_RECV_MSG:
                    mActivity.get().receive((BlueMessage) msg.obj);
                    break;
                case Task.TASK_CONNECT_FAILD:
                    mActivity.get().onConnectFaild();
                    break;
                case Task.TASK_CONNECT_SUCCESS:
                    mActivity.get().onConnectSuccess();
                    break;
                case Task.TASK_SEND_SUCCESS:
                    mActivity.get().onSendSuccess();
                    break;
                case Task.TASK_SEND_FAILD:
                    mActivity.get().onSendFaild();
                    break;
//                case  Task.TASK_REQUEST_CONNECT_SUCCESS:
//                    onRequestConnect((BluetoothDevice) msg.obj);
//                    break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openBlueTooch();

    }

    @Override
    protected void onResume() {
        super.onResume();
        TaskService.setHandler(serverHandler);
    }

    /**
     * 打开蓝牙
     */
    private void openBlueTooch() {
        if (getIntent().getStringExtra("no") != null) {
            return;
        }
        if(mBluetoothAdapter==null){
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, 100);
        } else {
            ensureDiscoverable();
            startServiceAsServer();
        }
    }

    /**
     * 开启蓝牙后返回来
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            ensureDiscoverable();
            startServiceAsServer();
        }
    }

    /**
     * 开启服务端
     */
    protected void startServiceAsServer() {
        // Android异步通信机制Handler，UI线程不能执行耗时操作，应该交给子线程去做
        // 子线程不允许去更新UI空间，必须要用到Handler机制（AsyncTask）
        if (!isServiceRunning()) {
            TaskService.start(this, serverHandler);
            // 向后台服务提交一个任务，作为服务器端去监听远程设备连接
            TaskService.newTask(new Task(serverHandler, Task.TASK_START_ACCEPT, null));
        }
    }

    /**
     * 使本地的蓝牙设备可被发现
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * 作为客户端去连接指定蓝牙设备
     *
     * @param mRemoteDevice
     */
    protected void startConnetAsClient(BluetoothDevice mRemoteDevice) {
        // 提交连接用户选择的设备对象，自己作为客户端
        TaskService.newTask(new Task(serverHandler, Task.TASK_START_CONN_THREAD, new Object[]{mRemoteDevice}));
    }

    /**
     * 客户端：带上难度和是否提示,发起挑战
     */
    protected void requestPK(String name, int difficulty, int type,boolean tip) {
        BlueMessage msg = new BlueMessage(BlueMessage.HEADER_REQUEST_PK);
        msg.put("name", name);
        msg.put("difficulty", difficulty);
        msg.put("type", type);
        msg.put("tip", tip);
        send(msg);
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    final public void send(BlueMessage msg) {
        // 将发送消息任务提交给后台服务
        TaskService.newTask(new Task(null, Task.TASK_SEND_MSG, new Object[]{msg}));
    }

    /**
     * 接收消息的回调
     *
     * @param msg
     */
    protected void receive(BlueMessage msg) {
        Log.i(TAg, "receive: " + msg);
        //是接收到对方发起挑战的消息
        switch (msg.getType()) {
            case BlueMessage.HEADER_REQUEST_PK:
                onRequestConnect(msg);
                break;
            case BlueMessage.HEADER_RESPON_PK:
                onClientByServerConfirm();
                break;
            case BlueMessage.HEADER_CANEL_PK:
                onClientByServerCanelConfirm();
                break;
            case BlueMessage.HEADER_SEND_PUZZLE:
                onServerReceivePuzzle(msg);
                break;
            case BlueMessage.HEADER_PK_STOP:
                onPKStop();
                break;
        }

    }

    /**
     * 连接成功后的回调
     */
    protected void onConnectSuccess() {
        Log.i(TAg, "onConnectSuccess");
    }

    /**
     * 连接失败后的回调
     */
    protected void onConnectFaild() {
        Log.i(TAg, "onConnectFaild");
        startServiceAsServer();
    }

    /**
     * 发送成功后的回调
     */
    protected void onSendSuccess() {
        Log.i(TAg, "onSendSuccess");
    }

    /**
     * 发送失败后的回调
     */
    protected void onSendFaild() {
        Log.i(TAg, "onSendFaild");
    }

    /**
     * 服务端：确认挑战后的回调
     */
    protected void onServerConfirm() {
        BlueMessage msg = new BlueMessage(BlueMessage.HEADER_RESPON_PK);
        send(msg);
    }

    /**
     * 客户端：服务端确认了挑战
     */
    protected void onClientByServerConfirm() {
        Toast.makeText(this, getResources().getString(R.string.start_pk), Toast.LENGTH_SHORT).show();

    }

    /**
     * 服务端：取消挑战后的回调
     */
    protected void onServerCanelConfirm() {
        BlueMessage msg = new BlueMessage(BlueMessage.HEADER_CANEL_PK);
        send(msg);
    }

    /**
     * 客户端：服务端拒绝了挑战
     */
    private void onClientByServerCanelConfirm() {
        Toast.makeText(this, getResources().getString(R.string.no_pk), Toast.LENGTH_SHORT).show();
    }

    /**
     * 服务端：收到了客户端发来的棋局，那么就可以进入开始游戏了
     */
    private void onServerReceivePuzzle(BlueMessage msg) {
        Intent intent = null;
        if(type==0){
            intent =new Intent(this, GamePKTime.class);
        }else if(type==1){
            intent =new Intent(this, GamePKCompertition.class);
        }else{
            intent =new Intent(this, GamePKCommunication.class);
        }
        Prefs.setHints(this,tip);
        intent.putExtra(IGame.KEY_DIFFICULTY, IGame.DIFFICULTY_BY_BLUE);
        intent.putExtra(IGame.BLUE_NAME, name);
        intent.putExtra(IGame.BLUE_TYPE_PK, type);
        intent.putExtra(IGame.BLUE_TIP_PK, tip);
        intent.putExtra(IGame.PREF_PUZZLE, (String) msg.get(IGame.PREF_PUZZLE));
        intent.putExtra(IGame.PREF_INIT_PUZZLE, (String) msg.get(IGame.PREF_INIT_PUZZLE));
        startActivity(intent);
    }

    protected void onPKStop() {

    }

    /**
     * 服务端：客户端请求连接的回调
     */
    private int diff;
    private int type;
    private boolean tip;
    private String name;

    protected void onRequestConnect(BlueMessage msg) {
        if(this.isFinishing()){
            return;
        }
        Log.i(TAg, "onRequestConnect");
        name = (String) msg.get("name");
        diff = (Integer) msg.get("difficulty");
        type = (Integer) msg.get("type");
        tip = (Boolean) msg.get("tip");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.request_connect));
        String content = getResources().getString(R.string.confirm_connect).replace("XXX", name);
        String diff_arr[] = new String[]{getResources().getString(R.string.sudoku_difficulty_easy_label),
                getResources().getString(R.string.sudoku_difficulty_medium_label),
                getResources().getString(R.string.sudoku_difficulty_hard_label)};
        String type_arr[] = new String[]{getResources().getString(R.string.pk_type_time),
                getResources().getString(R.string.pk_type_comp), getResources().getString(R.string.pk_type_comm)};
        String tip_arr[] = new String[]{getResources().getString(R.string.tip),
                getResources().getString(R.string.no_tip)};
//        String difficulty = getResources().getString(R.string.sudoku_difficulty_title)+":"+diff_arr[diff];
//        String typePK = type_arr[type];
        //每行显示姓名、难度、提示
        builder.setTitle(content + "\n" + type_arr[type] +"("+tip_arr[tip?0:1]+ "):" + diff_arr[diff]);
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                onServerConfirm();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onServerCanelConfirm();
            }
        });
        builder.show();
    }

    /**
     * 判断服务是否已经启动
     *
     * @return
     */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        Log.i(TAg, "this.getPackageName()" + this.getPackageName());
        try {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ((this.getPackageName() + ".TaskService").equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    protected void showToast(String content){
        Toast.makeText(BaseActivity.this,content,Toast.LENGTH_SHORT).show();
    }
    protected void showToast(int id){
        showToast(getResources().getString(id));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serverHandler.removeCallbacksAndMessages(null);
    }
}
