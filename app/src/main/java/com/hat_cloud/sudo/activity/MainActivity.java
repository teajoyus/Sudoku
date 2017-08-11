package com.hat_cloud.sudo.activity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.hat_cloud.sudo.task.Task;
import com.hat_cloud.sudo.task.TaskService;
import com.hat_cloud.sudoku.R;

import java.util.ArrayList;


public class MainActivity extends Activity {
    private static final int REQUES_SELECT_BT_CODE = 0x1001;
    // 启用请求码
    private static final int REQUES_BT_ENABLE_CODE = 0x1002;
    private static final String TAG = MainActivity.class.getName();
    private ListView mListView;
    private EditText mET;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothDevice mRemoteDevice;

    private ArrayAdapter<String> mAdapter;

    // 聊天内容保存对象
    private ArrayList<String> mChatContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);// 使用主界面布局

        mChatContent = new ArrayList<String>();
        // 使用ListView的Adapter
        mAdapter = new ArrayAdapter<String>(this,
                // 数据项显示样式布局
                android.R.layout.simple_list_item_1,
                // 显示的数据源
                mChatContent);


        mListView.setAdapter(mAdapter);

        // 打开蓝牙设备
        openBtDevice();
    }

    /**
     * 用户点击发送按钮
     * @param v
     */
    public void onSendClick(View v){
        String msg = mET.getText().toString().trim();
        if(msg.length() <= 0){
            Toast.makeText(this, "The message can not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // 将用户输入的消息，添加到ListView中去显示
        // 获得自己的名字＋消息
        mChatContent.add(mBluetoothAdapter.getName() + ":" + msg);
        // 更新ListView显示
        mAdapter.notifyDataSetChanged();
        // 将发送消息任务提交给后台服务
        TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{msg}));
        // 清空输入框
        mET.setText("");
    }

    private boolean openBtDevice(){
        // 获得蓝牙设备适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 蓝牙设备不被支持
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Your device is not support Bluetooth!");
            Toast.makeText(this, "The device does not have Bluetooth", Toast.LENGTH_LONG).show();
            return false;
        }

        // 启用蓝牙设备
        if (!mBluetoothAdapter.isEnabled()) {
            // 隐式Intent
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUES_BT_ENABLE_CODE);
        }else{
            // 如果蓝牙设备已经启用，直接启动后台服务
            startServiceAsServer();
        }
        return true;
    }

    private void startServiceAsServer() {
        // Android异步通信机制Handler，UI线程不能执行耗时操作，应该交给子线程去做
        // 子线程不允许去更新UI空间，必须要用到Handler机制（AsyncTask）
        TaskService.start(this, mHandler);
        // 向后台服务提交一个任务，作为服务器端去监听远程设备连接
        TaskService.newTask(new Task(mHandler, Task.TASK_START_ACCEPT, null));
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Task.TASK_SEND_MSG:
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case Task.TASK_RECV_MSG:
                    //
                    mChatContent.add(msg.obj.toString());
                    mAdapter.notifyDataSetChanged();
                    break;
                case Task.TASK_GET_REMOTE_STATE:
                    setTitle(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

    // 当startActivityForResult启动画面结束的时候，该方法被回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //
        if(requestCode == REQUES_BT_ENABLE_CODE && resultCode == RESULT_OK){
            // 作为蓝牙服务端启动后台服务
            startServiceAsServer();
        }else if(requestCode == REQUES_SELECT_BT_CODE && resultCode == RESULT_OK){
            mRemoteDevice = data.getParcelableExtra("DEVICE");
            if(mRemoteDevice == null)
                return;
            // 提交连接用户选择的设备对象，自己作为客户端
            TaskService.newTask(new Task(mHandler, Task.TASK_START_CONN_THREAD, new Object[]{mRemoteDevice}));
        }
    }




    @Override
    protected void onDestroy() {
        //
        TaskService.stop(this);
        super.onDestroy();
    }
}