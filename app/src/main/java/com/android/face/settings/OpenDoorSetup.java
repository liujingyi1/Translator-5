package com.android.face.settings;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.face.mcu.McuManager;
import com.android.rgk.common.lock.LockManager;
import com.ragentek.face.R;

import org.json.JSONException;
import org.json.JSONObject;

public class OpenDoorSetup extends Activity {
    public static final String TAG = "Internet";

    private TextView dhcp;
    private  static int i = 0,j = 0;
    private EditText ip_et, mask_et, gw_et, dns_et;
    private String net = "";
    private Button submit;
    private static int count = 0;
    private static int dot = 0;
    private  int isDHCP = -1;
    private String ipAddr = "0.0.0.0";
    private String netMask = "0.0.0.0";
    private String gateway = "0.0.0.0";
    private String dns1 = "0.0.0.0";
    private String dns2 = "0.0.0.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //继承AppCompatActivity时无效
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_open_door_setup);

        ip_et = (EditText) findViewById(R.id.ip_edit);
        mask_et = (EditText) findViewById(R.id.mask_edit);
        gw_et = (EditText) findViewById(R.id.gateway_edit);
        dns_et = (EditText) findViewById(R.id.dns_edit);
        dhcp = (TextView) findViewById(R.id.dhcp);

        submit = (Button) findViewById(R.id.submit_btn);

        String net_dhcp = LockManager.getInstance().getEthernet();
        Log.d(TAG,"net_dhcp = "+net_dhcp);
        try {
            JSONObject jsonObject = new JSONObject(net_dhcp);
            isDHCP = jsonObject.getInt("isDHCP");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isDHCP == 1) {
            dhcp.setText(R.string.dynamic_model);
        } else {
            dhcp.setText(R.string.static_manual);
        }
        submit.setEnabled(false);
        submit.setVisibility(View.INVISIBLE);

        ip_et.setFocusable(false);
        mask_et.setFocusable(false);
        gw_et.setFocusable(false);
        dns_et.setFocusable(false);

        getEthernet();

        ip_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ipAddr = charSequence.toString();
                if (ipAddr.length() > 15){
                    Toast.makeText(getApplicationContext(),"输入有误！",Toast.LENGTH_SHORT).show();
                    ipAddr = ipAddr.substring(0,15);
                    ip_et.setText(ipAddr);
                    ip_et.setSelection(ipAddr.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mask_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                netMask = charSequence.toString();
                if (netMask.length() > 15){
                    Toast.makeText(getApplicationContext(),"输入有误！",Toast.LENGTH_SHORT).show();
                    netMask = netMask.substring(0,15);
                    mask_et.setText(netMask);
                    mask_et.setSelection(netMask.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        gw_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                gateway = charSequence.toString();
                if (gateway.length() > 15){
                    Toast.makeText(getApplicationContext(),"输入有误！",Toast.LENGTH_SHORT).show();
                    gateway = gateway.substring(0,15);
                    gw_et.setText(gateway);
                    gw_et.setSelection(gateway.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        dns_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dns1 = charSequence.toString();
                if (dns1.length() > 15){
                    Toast.makeText(getApplicationContext(),"输入有误！",Toast.LENGTH_SHORT).show();
                    dns1 = dns1.substring(0,15);
                    dns_et.setText(dns1);
                    dns_et.setSelection(dns1.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onPause() {
        count = 0;
        dot = 0;
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.v(TAG,"onKeyDown event.getRepeatCount() "+event.getRepeatCount());
        //Log.d(TAG,"onKeyDown keycode = "+keyCode);
        switch(keyCode){
            case KeyEvent.KEYCODE_0:
                break;
            case KeyEvent.KEYCODE_1:
                break;
            case KeyEvent.KEYCODE_2:
                break;
            case KeyEvent.KEYCODE_3:
                break;
            case KeyEvent.KEYCODE_4:
                break;
            case KeyEvent.KEYCODE_5:
                break;
            case KeyEvent.KEYCODE_6:
                break;
            case KeyEvent.KEYCODE_7:
                break;
            case KeyEvent.KEYCODE_8:
                break;
            case KeyEvent.KEYCODE_9:
                break;
            case KeyEvent.KEYCODE_F2:
                dot++;
                Log.d(TAG, "count = "+count+", dot = " + dot);
                if (count == 1 && dot >=1 && dot <= 3) {
                    ipAddr = ipAddr + ".";
                    Log.d(TAG, "ipAddr = " + ipAddr);
                    ip_et.setText(ipAddr);
                    ip_et.setSelection(ipAddr.length());
                } else if (count == 2 && dot >=4 && dot <= 6) {
                    netMask = netMask + ".";
                    Log.d(TAG, "netMask = " + netMask);
                    mask_et.setText(netMask);
                    mask_et.setSelection(netMask.length());
                } else if (count == 3 && dot >=7 && dot <= 9) {
                    gateway = gateway + ".";
                    Log.d(TAG, "gateway = " + gateway);
                    gw_et.setText(gateway);
                    gw_et.setSelection(gateway.length());
                } else if (count == 4 && dot >=10 && dot <= 12) {
                    dns1 = dns1 + ".";
                    Log.d(TAG, "dns1 = " + dns1);
                    dns_et.setText(dns1);
                    dns_et.setSelection(dns1.length());
                } else if (count == 0 || count == 5){
                    onBackPressed();
                } else {
                    Toast.makeText(getApplicationContext(), "输入有误！", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                break;
            case KeyEvent.KEYCODE_F3:
                count++;
                Log.d(TAG,"count = "+count);
                if (count == 1) {
                    isDHCP = 0;  //static
                    dhcp.setText(R.string.static_manual);
                    submit.setVisibility(View.VISIBLE);
                    submit.setEnabled(true);
                    ip_et.setText("");
                    ip_et.setFocusable(true);
                    ip_et.requestFocus();
                } else if (count == 2) {
                    if(ipAddr.equals("")){
                        String net_ip = LockManager.getInstance().getEthernet();
                        Log.d(TAG,"net_ip = "+net_ip);
                        try {
                            JSONObject jsonObject = new JSONObject(net_ip);
                            ipAddr = jsonObject.getString("ip");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if ("".equals(ipAddr)){
                            ip_et.setText("0.0.0.0");
                        } else {
                            ip_et.setText(ipAddr);
                        }
                    }
                    mask_et.setText("");
                    mask_et.setFocusable(true);
                    mask_et.requestFocus();
                } else if (count == 3) {
                    if(netMask.equals("")){
                        String net_mask = LockManager.getInstance().getEthernet();
                        Log.d(TAG,"net_mask = "+net_mask);
                        try {
                            JSONObject jsonObject = new JSONObject(net_mask);
                            netMask = jsonObject.getString("mask");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if ("".equals(netMask)){
                            mask_et.setText("0.0.0.0");
                        } else {
                            mask_et.setText(netMask);
                        }
                    }
                    gw_et.setText("");
                    gw_et.setFocusable(true);
                    gw_et.requestFocus();
                } else if (count == 4) {
                    if(gateway.equals("")){
                        String net_gw = LockManager.getInstance().getEthernet();
                        Log.d(TAG,"net_gw = "+net_gw);
                        try {
                            JSONObject jsonObject = new JSONObject(net_gw);
                            gateway = jsonObject.getString("gateway");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if ("".equals(gateway)){
                            gw_et.setText("0.0.0.0");
                        } else {
                            gw_et.setText(gateway);
                        }
                    }
                    dns_et.setText("");
                    dns_et.setFocusable(true);
                    dns_et.requestFocus();
                } else if (count == 5) {
                    if(dns1.equals("")){
                        String net_dns = LockManager.getInstance().getEthernet();
                        Log.d(TAG,"net_dns = "+net_dns);
                        try {
                            JSONObject jsonObject = new JSONObject(net_dns);
                            dns1 = jsonObject.getString("dns");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if ("".equals(dns1)){
                            dns_et.setText("0.0.0.0");
                        } else {
                            dns_et.setText(dns1);
                        }
                    }
                    Log.d(TAG, "isDHCP = "+isDHCP+", ip = "+ipAddr+", mask = "+netMask+", gateway = "+gateway+", dns = "+dns1);
                    boolean success = McuManager.setEthernet(isDHCP, ipAddr, netMask, gateway, dns1);
                    Log.d(TAG, "success = " + success);
                    if (success) {
                        Toast.makeText(getApplicationContext(), "设置成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "设置失败，请重新设置！", Toast.LENGTH_SHORT).show();
                    }
                    clearFocus();
                    getEthernet();
                    submit.setVisibility(View.INVISIBLE);
                } else if (count == 6){
                    dhcp.setText(R.string.dynamic_model);
                    submit.setVisibility(View.INVISIBLE);
                    count = 0;
                    dot = 0;
                    isDHCP = 1;  //dynamic
                    boolean success = McuManager.setEthernet(isDHCP, "", "", "", "");
                    Log.d(TAG, "success dynamic = " + success);
                    getEthernet();
                }
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

    private void getEthernet(){
        net = LockManager.getInstance().getEthernet();
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
        Log.d(TAG,"net = "+net+", isDHCP = "+isDHCP);
        if ("".equals(ipAddr) || "".equals(netMask) || "".equals(gateway) || "".equals(dns1)) {
            ip_et.setText("0.0.0.0");
            mask_et.setText("0.0.0.0");
            gw_et.setText("0.0.0.0");
            dns_et.setText("0.0.0.0");
        } else {
            ip_et.setText(ipAddr);
            mask_et.setText(netMask);
            gw_et.setText(gateway);
            dns_et.setText(dns1);
        }
    }

    private void clearFocus(){
        ip_et.setFocusable(false);
        ip_et.clearFocus();
        mask_et.setFocusable(false);
        mask_et.clearFocus();
        gw_et.setFocusable(false);
        gw_et.clearFocus();
        dns_et.setFocusable(false);
        dns_et.clearFocus();
    }
}
