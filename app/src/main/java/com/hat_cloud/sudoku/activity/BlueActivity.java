package com.hat_cloud.sudoku.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hat_cloud.sudoku.iface.IGame;
import com.hat_cloud.sudoku.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 蓝牙搜素、配对、发起对战的类
 * 继承了BaseActivity所以也就有了蓝牙通信功能，可以直接调用父类的方法来完成蓝牙通信
 */
public class BlueActivity extends BaseActivity implements View.OnClickListener{
    private static final int REQUEST_ENABLE = 100;
    private static final String TAG = "BlueActivity";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ListView lv;
    private List<BluetoothDevice> blueList = new ArrayList<>();
    private MyDevicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue);
        lv = (ListView) findViewById(R.id.lv);
        openBlueTooch();
        show();
        regist();
        if(mBluetoothAdapter!=null){
            mBluetoothAdapter.startDiscovery();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * 菜单项，可以进入修改用户姓名
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        motifyName();
        return true;
    }
    private void motifyName(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        et.setPadding(15,0,15,0);
        et.setText(mBluetoothAdapter.getName());
        builder.setTitle(getResources().getString(R.string.blue_motify_name));
        builder.setView(et);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = et.getText().toString();
                if(!name.isEmpty()){
                    mBluetoothAdapter.setName(name);
                }
            }
        });
        builder.show();
    }
    private void openBlueTooch() {
        if (mBluetoothAdapter!=null&&!mBluetoothAdapter.isEnabled()) {
            //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE);
        }
    }

    private void show() {
        if(mBluetoothAdapter==null)return;;
        //获取本机蓝牙名称
        String name = mBluetoothAdapter.getName();
        //获取本机蓝牙地址
        String address = mBluetoothAdapter.getAddress();
        Log.d(TAG, "bluetooth name =" + name + " address =" + address);
        //获取已配对蓝牙设备
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Log.d(TAG, "bonded device size =" + devices.size());
        if(blueList.size()>0)blueList.clear();
        for (BluetoothDevice bonddevice : devices) {
            Log.d(TAG, "bonded device name =" + bonddevice.getName() + " address" + bonddevice.getAddress());
            blueList.add(bonddevice);
        }
        adapter = new MyDevicesAdapter();
        lv.setAdapter(adapter);
    }

    /**
     * 注册接收广播
     */
    private void regist() {
        IntentFilter filter = new IntentFilter();
        //发现设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBluetoothReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        //蓝牙设备状态改变
        filter2.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondDevices, filter2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE) {

        }
    }


    class MyDevicesAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return blueList.size();
        }

        @Override
        public Object getItem(int i) {
            return blueList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holer = null;
            if (view == null) {
                view = LayoutInflater.from(BlueActivity.this).inflate(R.layout.blue_item, null, false);
                holer = new ViewHolder();
                holer.name = (TextView) view.findViewById(R.id.name);
                holer.address = (TextView) view.findViewById(R.id.address);
                holer.btn = (Button) view.findViewById(R.id.btn);
                holer.btn.setOnClickListener(BlueActivity.this);
                view.setTag(holer);
            } else {
                holer = (ViewHolder) view.getTag();
            }
            holer.name.setText(blueList.get(i).getName());
            holer.address.setText(blueList.get(i).getAddress());
            if(blueList.get(i).getBondState()== BluetoothDevice.BOND_NONE){
                holer.btn.setText(getResources().getString(R.string.blue_item_pd));
            }else {
                holer.btn.setText(getResources().getString(R.string.blue_item_connect));
            }
            holer.btn.setTag(blueList.get(i));
            return view;
        }
    }

    public static class ViewHolder {
        TextView name;
        TextView address;
        Button btn;
    }

    /**
     * 如果还没配对的话那么点击就是配对
     * 如果已经配对的话那么点击就是连接
     * @param view
     */
    @Override
    public void onClick(View view) {
        BluetoothDevice device = (BluetoothDevice) view.getTag();
        //如果还没配对就必须先配对
        if(device.getBondState()== BluetoothDevice.BOND_NONE){
            showToast(R.string.blue_item_pd_try,device.getName());
            //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
            Method createBondMethod = null;
            try {
                createBondMethod =BluetoothDevice.class.getMethod("createBond");
                Log.d(TAG, "开始配对");
                Boolean  returnValue = (Boolean) createBondMethod.invoke(device);
                Log.d(TAG, "returnValue:"+returnValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        //如果是已经配对的话就可以进行连接
        }else if(device.getBondState() == BluetoothDevice.BOND_BONDED){
            connect(device);
        }

    }

    /**
     * 接收蓝牙信息
     */
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mBluetoothReceiver action =" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {//每扫描到一个设备，系统都会发送此广播。
                //获取蓝牙设备
                BluetoothDevice scanDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (scanDevice == null || scanDevice.getName() == null) return;
                Log.d(TAG, "name=" + scanDevice.getName() + "address=" + scanDevice.getAddress());
                 if(!blueList.contains(scanDevice))blueList.add(scanDevice);//如果没有存在该记录才添加，防止重复
                adapter.notifyDataSetChanged();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showToast(getResources().getString(R.string.blue_connect_finish));
            }
        }
    };
    /**
     * 接收请求配对的蓝牙信息
     */
    private BroadcastReceiver bondDevices = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = null;
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(TAG, "完成配对");
                        showToast(R.string.blue_item_bond_success);
                        show();//刷新界面
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d(TAG, "取消配对");
                        showToast(R.string.blue_item_bond_faild);
                    default:
                        break;
                }
            }

        }
    };

    /**
     * 连接已经配对的蓝牙
     * @param device
     */
    private BluetoothDevice device;//要pk的玩家
    private void connect(BluetoothDevice device){
        this.device = device;
        startConnetAsClient(device);
    }
    /**
     * 销毁时要注销掉广播
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothReceiver);
        unregisterReceiver(bondDevices);
        if(mBluetoothAdapter!=null&&mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
    }


    /**
     * 客户端：连接成功后的回调
     */
    protected void onConnectSuccess(){
        super.onConnectSuccess();
        showToast("onConnectSuccess");
        showDifficultyAdilog();//连接成功后发起挑战
    }
    /**
     * 连接失败后的回调
     */
    protected void onConnectFaild(){
        showToast("onConnectFaild");
    }
    /**
     * 发送成功后的回调
     */
    protected void onSendSuccess(){
        super.onSendSuccess();
        showToast("onSendSuccess");
    }
    /**
     * 发送失败后的回调
     */
    protected void onSendFaild(){
        super.onSendFaild();
        showToast("onSendFaild");
    }

    @Override
    protected void onClientByServerConfirm() {
        super.onClientByServerConfirm();

    }
}
