package com.android.face;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import com.android.face.http.HttpServerManager;
import com.ragentek.face.R;

public class HttpActivity extends AppCompatActivity implements View.OnClickListener {

    private HttpServerManager mServerManager;

    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnBrowser;
    private TextView mTvMessage;

    private String mRootUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);

        mBtnStart = (Button)findViewById(R.id.btn_start);
        mBtnStop = (Button)findViewById(R.id.btn_stop);
        mBtnBrowser = (Button)findViewById(R.id.btn_browse);
        mTvMessage = (TextView) findViewById(R.id.tv_message);

        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mBtnBrowser.setOnClickListener(this);

        // AndServer run in the service.
//        mServerManager = new HttpServerManager(this);
//        mServerManager.register();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_start: {
                mServerManager.startServer();
                break;
            }
            case R.id.btn_stop: {
                mServerManager.stopServer();
                break;
            }
            case R.id.btn_browse: {
                if (!TextUtils.isEmpty(mRootUrl)) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(Uri.parse(mRootUrl));
                    startActivity(intent);
                }
                break;
            }
        }
    }

    /**
     * Start notify.
     */
    public void onServerStart(String ip) {
        mBtnStart.setVisibility(View.GONE);
        mBtnStop.setVisibility(View.VISIBLE);
        mBtnBrowser.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(ip)) {
//            List<String> addressList = new LinkedList<>();
//            mRootUrl = "http://" + ip + ":8080/";
//            addressList.add(mRootUrl);
//            addressList.add("http://" + ip + ":8080/login.html");
            List<String> addressList = new LinkedList<>();
            mRootUrl = "http://" + ip + ":8080/";
            addressList.add("请在浏览器输入:"+mRootUrl);
            mTvMessage.setText(TextUtils.join("\n", addressList));
        } else {
            mRootUrl = null;
            mTvMessage.setText("server ip error");
        }
    }

    /**
     * Error notify.
     */
    public void onServerError(String message) {
        mRootUrl = null;
        mBtnStart.setVisibility(View.VISIBLE);
        mBtnStop.setVisibility(View.GONE);
        mBtnBrowser.setVisibility(View.GONE);
        mTvMessage.setText(message);
    }

    /**
     * Stop notify.
     */
    public void onServerStop() {
        mRootUrl = null;
        mBtnStart.setVisibility(View.VISIBLE);
        mBtnStop.setVisibility(View.GONE);
        mBtnBrowser.setVisibility(View.GONE);
        mTvMessage.setText("server stop success");
    }
}
