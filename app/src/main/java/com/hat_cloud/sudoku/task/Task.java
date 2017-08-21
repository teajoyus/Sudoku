package com.hat_cloud.sudoku.task;
import android.os.Handler;

/**
 * 定义一个消息任务
 * 与BlueMessage类区别在于BlueMessage是存放数据的，而Task类是一个消息任务，用来封装蓝牙任务给Service去处理
 * BlueMessage类与Task类不存在关联关系
 */

public class Task {
    /**
     * 请求等待蓝牙连接（作为服务器）
     */
    public static final int TASK_START_ACCEPT = 1;
    /**
     * 请求连接远程蓝牙设备（作为客户端）
     */
    public static final int TASK_START_CONN_THREAD = 2;
    /**
     * 发送消息
     */
    public static final int TASK_SEND_MSG = 3;
    /**
     * 获得蓝牙运行状态
     */
    public static final int TASK_GET_REMOTE_STATE = 4;
    /**
     * 接受到蓝牙聊天消息
     */
    public static final int TASK_RECV_MSG = 5;
    /**
     * 连接失败
     */
    public static final int TASK_CONNECT_FAILD = 6;
    /**
     * 连接成功
     */
    public static final int TASK_CONNECT_SUCCESS = 7;
    /**
     * 发送成功
     */
    public static final int TASK_SEND_SUCCESS = 8;
    /**
     * 发送失败
     */
    public static final int TASK_SEND_FAILD = 9;
    /**
     * 请求连接
     */
    public static final int TASK_REQUEST_CONNECT = 10;
    /**
     * 请求连接成功
     */
    public static final int TASK_REQUEST_CONNECT_SUCCESS = 11;
    /**
     * 请求连接不成功
     */
    public static final int TASK_REQUEST_CONNECT_FAILD = 12;


    // 任务ID
    private int mTaskID;
    // 任务参数列表
    public Object[] mParams;

    private Handler mH;

    public Task(Handler handler, int taskID, Object[] params){
        this.mH = handler;
        this.mTaskID = taskID;
        this.mParams = params;
    }

    public Handler getHandler(){
        return this.mH;
    }

    public int getTaskID(){
        return mTaskID;
    }
}