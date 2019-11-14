package com.android.face.settings;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.face.linphone.manager.LinphoneManager;
import com.android.face.linphone.utils.LinphoneUtils;
import com.android.rgk.common.lock.LockManager;
import com.ragentek.face.R;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInformation extends Activity {
    private static String TAG = "UserInformation";
    private TextView textView, textView2, sip, sip_status, connect, connect_status, connect_ip, bluetooth, bt_address;
    private EditText editText;
    private String roomNumber = "0101";
    private boolean register = false;
    private String addressStr = "00:00:00:00:00:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_information);

        initView();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"action = "+intent.getAction());
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                Log.d(TAG,"activeNetwork = "+activeNetwork);
                if (activeNetwork != null) {
                    if (activeNetwork.isConnected()) {
                        connect_status.setText("网络已连接！");
                        connect_ip.setVisibility(View.VISIBLE);
                        connect_ip.setText(getIp_address());
                        Toast.makeText(context,"网络已连接！", Toast.LENGTH_SHORT).show();
                    } else {
                        connect_status.setText("没有网络连接！");
                        connect_ip.setVisibility(View.GONE);
                        Toast.makeText(context,"没有网络连接！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    connect_status.setText("无网络连接！");
                    connect_ip.setVisibility(View.GONE);
                    Toast.makeText(context,"无网络连接！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void initView(){
        textView = (TextView) findViewById(R.id.ui_textView);
        sip = (TextView) findViewById(R.id.sip);
        sip_status = (TextView) findViewById(R.id.sip_status);
        connect = (TextView) findViewById(R.id.connect);
        connect_status = (TextView) findViewById(R.id.connect_status);
        connect_ip = (TextView) findViewById(R.id.connect_ip);
        bluetooth = (TextView) findViewById(R.id.bluetooth);
        bt_address = (TextView) findViewById(R.id.bt_address);

        register = LinphoneUtils.isRegistered();

        if (register) {
            sip_status.setText("在线！");
        } else {
            sip_status.setText("离线！");
        }
        connect_ip.setVisibility(View.GONE);
        addressStr = getBlutooth_address();
        Log.d("xmw","addr = "+addressStr);
        bt_address.setText(addressStr);
    }

    public void bluetoothAddress() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {
            addressStr = mBluetoothAdapter.getAddress();
        }
        bt_address.setText(addressStr);
    }

    private String getBlutooth_address() {
        String str = null;
        try {
            str = LockManager.getInstance().readNvStr(4);
        } catch (Exception e) {
        }
        if(str!=null && str.length()==12){
            String address = str.substring(0, 2);
            for(int i=2;i<12;i+=2){
                address+=":"+str.substring(i, i+2);
            }
            return address;
        }
        return "0";
    }

    private String getIp_address(){
        String net = null;
        try {
            net = LockManager.getInstance().getEthernet();
        } catch (Exception e){
        }
        Log.d(TAG,"net = "+net);
        int isDHCP = -1;
        String ipAddr = "0.0.0.0";
        String netMask = "0.0.0.0";
        String gateway = "0.0.0.0";
        String dns1 = "0.0.0.0";
        String dns2 = "0.0.0.0";
        try {
            JSONObject jsonObject = new JSONObject(net);
            isDHCP = jsonObject.getInt("isDHCP");
            ipAddr = jsonObject.getString("ip");
            netMask = jsonObject.getString("mask");
            gateway = jsonObject.getString("gateway");
            dns1 = jsonObject.getString("DNS");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ipAddr;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
            case KeyEvent.KEYCODE_F2:
                onBackPressed();
                break;
            case KeyEvent.KEYCODE_F3:
                break;
            case KeyEvent.KEYCODE_BACK:
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
