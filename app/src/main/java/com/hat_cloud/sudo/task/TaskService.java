package com.hat_cloud.sudo.task;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by ranjiaqing on 17/7/19.
 */

public class TaskService extends Service{
    private final String TAG = "TaskService";

    private TaskThread mThread;

    private BluetoothAdapter mBluetoothAdapter;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;

    private boolean isServerMode = true;

    private static Handler mActivityHandler;

    // 任务队列
    private static ArrayList<Task> mTaskList = new ArrayList<Task>();

    private ConnectedThread mCommThread;

    public static Handler getHandler(){
        return mActivityHandler;
    }
    public static void setHandler(Handler ActivityHandler){
        mActivityHandler = ActivityHandler;
    }
    /**
     * 用于被外部组件启动服务
     * @param c 上下文对象
     * @param handler Activity上的Handler对象，用于更新UI
     */
    public static void start(Context c, Handler handler) {

        mActivityHandler = handler;
        // 显示启动服务
        Intent intent = new Intent(c, TaskService.class);
        c.startService(intent);
    }

    /**
     * 关闭服务
     * @param c
     */
    public static void stop(Context c) {
        Intent intent = new Intent(c, TaskService.class);
        c.stopService(intent);
    }

    /**
     * 提交任务
     * @param target 目标任务
     */
    public static void newTask(Task target) {
        synchronized (mTaskList) {
            // 将任务添加到任务队列中
            mTaskList.add(target);
        }
    }

    @Override
    public void onCreate() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Your device is not support Bluetooth!");
            return;
        }
        // 启动服务线程
        mThread = new TaskThread();
        mThread.start();
        super.onCreate();
    }

    private Handler mServiceHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Task.TASK_GET_REMOTE_STATE:
                    android.os.Message activityMsg = mActivityHandler
                            .obtainMessage();
                    activityMsg.what = msg.what;
                    if (mAcceptThread != null && mAcceptThread.isAlive()) {
                        activityMsg.obj = "Waiting for connection...";
                    } else if (mCommThread != null && mCommThread.isAlive()) {
                        activityMsg.obj = mCommThread.getRemoteName() + "[Online]";
                    } else if (mConnectThread != null && mConnectThread.isAlive()) {
                        activityMsg.obj = "connecting:"
                                + mConnectThread.getDevice().getName();
                    } else {
                        activityMsg.obj = "Unknown state";
                        // 重新等待连接
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                        isServerMode = true;
                    }

                    mActivityHandler.sendMessage(activityMsg);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    /**
     * 任务处理线程
     */
    private class TaskThread extends Thread {
        private boolean isRun = true;
        private int mCount = 0;

        /**
         * 停止线程
         */
        public void cancel() {
            isRun = false;
        }

        @Override
        public void run() {
            Task task;
            while (isRun) {
                // 从任务列表里面获得第一个任务
                if (mTaskList.size() > 0) {
                    synchronized (mTaskList) {
                        task = mTaskList.get(0);
                        doTask(task);
                    }
                } else {
                    try {
                        Thread.sleep(200);
                        mCount++;
                    } catch (InterruptedException e) {
                    }
                    if (mCount >= 50) {
                        mCount = 0;
                        android.os.Message handlerMsg = mServiceHandler
                                .obtainMessage();
                        handlerMsg.what = Task.TASK_GET_REMOTE_STATE;
                        mServiceHandler.sendMessage(handlerMsg);
                    }
                }
            }
        }

    }

    /**
     * 任务处理
     * @param task
     */
    private void doTask(Task task) {
        switch (task.getTaskID()) {
            case Task.TASK_START_ACCEPT:
                // 作为服务器接受等待客户端的线程
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
                isServerMode = true;
                break;
            case Task.TASK_START_CONN_THREAD:
                if (task.mParams == null || task.mParams.length == 0) {
                    break;
                }
                if(task.getHandler()!=null)mActivityHandler = task.getHandler();
                BluetoothDevice remote = (BluetoothDevice) task.mParams[0];
                //
                mConnectThread = new ConnectThread(remote);
                mConnectThread.start();
                isServerMode = false;
                break;
            case Task.TASK_SEND_MSG:
                boolean sucess = false;
                if (mCommThread == null || !mCommThread.isAlive()
                        || task.mParams == null || task.mParams.length == 0) {
                    Log.e(TAG, "mCommThread or task.mParams null");
                } else {
                    sucess = mCommThread.write( task.mParams[0]);
                }
                if(task.getHandler()!=null)mActivityHandler = task.getHandler();
                //给UI发送是否成功的消息
                mActivityHandler.sendEmptyMessage(sucess?Task.TASK_SEND_SUCCESS:Task.TASK_SEND_FAILD);
                break;
        }
        synchronized (mTaskList) {
            mTaskList.remove(task);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThread.cancel();
    }

    // UUID号，表示不同的数据协议，不能随便改
    private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";



    /**
     * 等待客户端连接线程
     * @author tangpan09@gmail.com
     */
    private class AcceptThread extends Thread {
        private BluetoothServerSocket mServerSocket;
        private boolean isCancel = false;

        public AcceptThread() {
            Log.d(TAG, "AcceptThread");
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        "Bluetooth_Chat_Room", UUID.fromString(UUID_STR));
            } catch (IOException e) {
            }
            mServerSocket = tmp;
        }

        // 不停的等待监听客户端连接
        // 一旦监听到就会执行manageConnectedSocket()方法
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    if(mServerSocket==null){
                        try {
                            mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                                    "Bluetooth_Chat_Room", UUID.fromString(UUID_STR));
                        } catch (IOException e) {
                        }

                    }
                    // 阻塞等待
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    if (!isCancel) {
                        try {
                            mServerSocket.close();
                        } catch (IOException e1) {
                        }
                        // 异常结束时，再次监听
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                        isServerMode = true;
                    }
                    break;
                }
                if (socket != null) {
                    // 管理已经连接的客户端
                    manageConnectedSocket(socket);
                    Message handlerMsg = mActivityHandler.obtainMessage();
                    handlerMsg.what = Task.TASK_REQUEST_CONNECT_SUCCESS;
                    handlerMsg.obj = socket.getRemoteDevice();
                    mActivityHandler.sendMessage(handlerMsg);
                    try {
                        mServerSocket.close();
                        mActivityHandler.sendEmptyMessage(Task.TASK_CONNECT_FAILD);
                    } catch (IOException e) {
                    }
                    mAcceptThread = null;
                    break;
                }
            }
        }

        public void cancel() {
            try {
                Log.d(TAG, "AcceptThread canceled");
                isCancel = true;
                isServerMode = false;
                mServerSocket.close();
                mAcceptThread = null;
                if (mCommThread != null && mCommThread.isAlive()) {
                    mCommThread.cancel();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 作为客户端连接指定的蓝牙设备线程
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device) {

            Log.d(TAG, "ConnectThread");

            // 服务器监听线程不为空，将其结束掉，开启作为客户端
            if (mAcceptThread != null && mAcceptThread.isAlive()) {
                mAcceptThread.cancel();
            }

            // 连接消息监听线程不为空，将其结束掉，开启作为客户端
            if (mCommThread != null && mCommThread.isAlive()) {
                mCommThread.cancel();
            }

            BluetoothSocket tmp = null;
            mDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID
                        .fromString(UUID_STR));
            } catch (IOException e) {
                Log.d(TAG, "createRfcommSocketToServiceRecord error!");
            }

            mSocket = tmp;
        }

        public BluetoothDevice getDevice() {
            return mDevice;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mSocket.connect();
                mActivityHandler.sendEmptyMessage(Task.TASK_CONNECT_SUCCESS);
            } catch (IOException connectException) {
                Log.e(TAG, "Connect server failed");
                mActivityHandler.sendEmptyMessage(Task.TASK_CONNECT_FAILD);
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                }
                // Do work to manage the connection (in a separate thread)
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
                isServerMode = true;
                return;
            } // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mSocket);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
            }
            mConnectThread = null;
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        // 启动子线程来维持连接
        mCommThread = new ConnectedThread(socket);
        mCommThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;
//        private BufferedWriter mBw;
        private ObjectOutputStream oos;
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread");
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mInStream = tmpIn;
            mOutStream = tmpOut;
            // 获得远程设备的输出缓存字符流
//            mBw = new BufferedWriter(new PrintWriter(mOutStream));
            try {
                oos = new ObjectOutputStream(mOutStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public OutputStream getOutputStream() {
            return mOutStream;
        }

        public boolean write(Object msg) {
            if (msg == null)
                return false;
            try {
//                mBw.write(msg + "\n");
//                mBw.flush();
                oos.writeObject(msg);
                oos.flush();
                System.out.println("Write:" + msg);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        public String getRemoteName() {
            return mSocket.getRemoteDevice().getName();
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
            }
            mCommThread = null;
        }

        public void run() {
            // 将上线提示信息写入到远程设备中
//            write(mBluetoothAdapter.getName() + "Already Online");
            android.os.Message handlerMsg;
            // 获得远程设备的缓存字符输入流
//            BufferedReader br = new BufferedReader(new InputStreamReader(
//                    mInStream));
            ObjectInputStream ois = null;
            try {
                 ois = new ObjectInputStream(mInStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    // 读取远程设备的一行信息
                    Object obj = ois.readObject();
                    System.out.println("Received:" + obj);
                    if (obj == null)
                        continue;

                    if (mActivityHandler == null) {
                        return;
                    }

                    // 获得远程设备的名字＋内容
//                    buffer = mSocket.getRemoteDevice().getName() + ":" + buffer;
                    // ͨ通过Activity更新到UI上
                    handlerMsg = mActivityHandler.obtainMessage();
                    handlerMsg.what = Task.TASK_RECV_MSG;
                    handlerMsg.obj = obj;
                    mActivityHandler.sendMessage(handlerMsg);
                } catch (Exception e) {
                    try {
                        mSocket.close();
                    } catch (IOException e1) {
                    }
                    mCommThread = null;
                    if (isServerMode) {
                        // 检查远程设备状态
                        handlerMsg = mServiceHandler.obtainMessage();
                        handlerMsg.what = Task.TASK_GET_REMOTE_STATE;
                        mServiceHandler.sendMessage(handlerMsg);
                        // 重新启动服务端连接线程
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}