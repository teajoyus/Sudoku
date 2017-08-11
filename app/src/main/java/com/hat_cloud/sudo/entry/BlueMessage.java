package com.hat_cloud.sudo.entry;

import java.io.Serializable;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 消息发送接收之间的一个消息类型，用一个消息头来代表
 * 有什么字段直接调用add方法添加进去，然后再一个个拿出来，有点像是队列
 * 或者是通过MAP映射键值的关系
 **/

public class BlueMessage implements Serializable{

    public static final int HEADER_REQUEST_PK = 0x001;//请求发起挑战的消息
    public static final int HEADER_RESPON_PK = 0x002;//确认发起挑战的消息
    public static final int HEADER_CANEL_PK = 0x003;//取消发起挑战的消息
    public static final int HEADER_SEND_PUZZLE = 0x004;//发送棋局的消息
    public static final int HEADER_RECEIVE_PUZZLE = 0x005;//接收棋局的消息
    public static final int HEADER_PK_STOP= 0x006;//中途退出挑战的消息

    public static final int HEADER_PK_END= 0x10002;//游戏结束
    public static final int HEADER_PK_REQ_HELP= 0x10003;//游戏结束后请求帮助
    public static final int HEADER_PK_HELP= 0x10003;//游戏结束后帮助对方

    public static final int HEADER_COMPERTITION_NUMBER= 0x1001;//竞赛过程中 对方输入的数字的消息

    public static final int HEADER_COMMUNICATION_REFER= 0x3001;//交流类型时收到对方发过来的参考数字

    public static final int HEADER_HELP_REFER= 0x4001;//帮助时收到对方发过来的参考数字

    public static final int HEADER_CHAT_MESSAGE= 0x5001;//聊天消息



    private int type;//消息类型
    private List<String> list;
    private Map<String, Object> map;
    private static BlueMessage msg;
    public BlueMessage(int type){
        this.type = type;
    }
    /*
    private BlueMessage(){

    }
     * 单例
    public static BlueMessage getInstance(){
        if(msg==null){
            synchronized (BlueMessage.class){
                if(msg==null){
                    msg = new BlueMessage();
                }
            }
        }
        return msg;
    }
    */
    public int getType(){
        return type;
    }
    /**
     * 入队
     * @param s
     * @return
     */
    public BlueMessage enQueue(String s) {
        if (list == null) list = new ArrayList<>();
        list.add(s);
        return this;
    }
    /**
     * 首次添加数据入队
     * @param s
     * @return
     */
    public BlueMessage enQueueFirst(String s) {
        if (list == null) {
            list = new ArrayList<>();
        }else{
            list.clear();
        }
        list.add(s);
        return this;
    }

    /**
     * 添加到映射表
     * @param key
     * @param value
     */
    public void put(String key,Object value){
        if(map==null){
            map = new HashMap<>();
        }
        map.put(key,value);
    }
    /**
     * 首次添加数据到映射表,
     * @param key
     * @param value
     */
    public void putFirst(String key,Object value){
        if(map==null){
            map = new HashMap<>();
        }else{
            map.clear();
        }
        map.put(key,value);
    }

    /**
     * 根据键取出值
     * @param key
     * @return
     */
    public Object get(String key){
        if(map==null)return null;
        return map.get(key);
    }
    /**
     * 出队
     * @return
     */
    public String deQueue() {
        if (list.size() == 0) return null;
        String s = list.get(0);
        list.remove(0);
        return s;
    }

    /**
     * 清除
     * @return
     */
    public BlueMessage clear() {
        if (list != null) list.clear();
        if(map!=null)map.clear();
        return this;
    }

    @Override
    public String toString() {
        return "BlueMessage{" +
                "type=" + type +
                ", list=" + list +
                ", map=" + map +
                '}';
    }
}
