package com.hat_cloud.sudo.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.hat_cloud.sudo.activity.BaseActivity;
import com.hat_cloud.sudo.entry.BlueMessage;
import com.hat_cloud.sudoku.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends BaseActivity implements View.OnClickListener{
    private ListView mList;
    private EditText inputEdit;
    private LinearLayout mRootLayout, mChatLayout;
    private ChatListViewAdapter mAdapter;
    public static  List<HashMap<String, Object>> chatContent = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra("no", "no");//表示让BaseActivity不要去打开蓝牙服务端
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mRootLayout = (LinearLayout) findViewById(R.id.root);
        mChatLayout = (LinearLayout) findViewById(R.id.topPanel);
        inputEdit = (EditText) findViewById(R.id.inputEdit);
        Button sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this);
        mList = (ListView) findViewById(R.id.listView1);
        mAdapter = new ChatListViewAdapter(this,chatContent);
        mList.setAdapter(mAdapter);
    }

    /**
     * 收到对方发来的聊天消息
     * @param msg
     */
    private void onReceiveMessage(BlueMessage msg){
        String content = (String) msg.get("chat");
        showTargetMessage(content);
    }

    /**
     * 添加对方的消息到数据列表中
     * @param msg
     */
    public static void addTargetMessage(String msg){
        HashMap<String, Object> data = new HashMap<>();
        System.out.println("Read data.");
        data.put(ChatListViewAdapter.KEY_ROLE,
                ChatListViewAdapter.ROLE_TARGET);
        data.put(ChatListViewAdapter.KEY_TEXT, msg);
        SimpleDateFormat df1 = new SimpleDateFormat("E MM月dd日 yy HH:mm ");
        data.put(ChatListViewAdapter.KEY_DATE, df1.format(System.currentTimeMillis()).toString());
        data.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
        chatContent.add(data);
    }
    /**
     * 显示对方信息
     */
    private void showTargetMessage( String msg){
        addTargetMessage(msg);
        mAdapter.notifyDataSetChanged();
    }
    /**
     * 显示自己信息
     */
    private void showOwnMessage(String msg){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(ChatListViewAdapter.KEY_ROLE, ChatListViewAdapter.ROLE_OWN);
        map.put(ChatListViewAdapter.KEY_TEXT, msg);
        SimpleDateFormat df2 = new SimpleDateFormat("E MM月dd日 yy HH:mm ");
        map.put(ChatListViewAdapter.KEY_DATE, df2.format(System.currentTimeMillis()).toString());
        map.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
        chatContent.add(map);
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void onClick(View v) {
            String msg = inputEdit.getText().toString().trim();
            if(msg.length() == 0){
                showToast(R.string.chat_empty);
                return;
            }
            //发送蓝牙消息
            BlueMessage blueMessage = new BlueMessage(BlueMessage.HEADER_CHAT_MESSAGE);
            blueMessage.put("chat",msg);
            send(blueMessage);

            showOwnMessage(msg);

            inputEdit.setText("");

    }
    @Override
    protected void receive(BlueMessage msg) {
        super.receive(msg);
        switch (msg.getType()){
            case BlueMessage.HEADER_CHAT_MESSAGE:
                onReceiveMessage(msg);
                break;
        }
    }
}
