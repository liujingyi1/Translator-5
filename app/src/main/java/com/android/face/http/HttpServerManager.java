/*
 * Copyright © 2018 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.face.http;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.android.face.FaceApplication;
import com.android.face.HttpActivity;
import com.android.rgk.common.util.LogUtil;
import com.android.rgk.common.util.NetUtil;

/**
 * Created by Yan Zhenjie on 2018/6/9.
 */
public class HttpServerManager extends BroadcastReceiver {

    private static final String TAG = "HttpServerManager";

    private static final String ACTION = "com.yanzhenjie.andserver.receiver";

    private static final String CMD_KEY = "CMD_KEY";
    private static final String MESSAGE_KEY = "MESSAGE_KEY";

    private static final int CMD_VALUE_START = 1;
    private static final int CMD_VALUE_ERROR = 2;
    private static final int CMD_VALUE_STOP = 4;

    private static HttpServerManager mInstance;

    private CoreService mCoreService;
    Context context;

    private HttpServerManager(Context context) {
        this.context  = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(ACTION);
        context.registerReceiver(this, filter);
    }

    /**
     *
     */
    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new HttpServerManager(context);
        }
    }

    /**
     * Notify serverStart.
     *
     * @param context context.
     */
    public static void onServerStart(Context context, String hostAddress) {
        sendBroadcast(context, CMD_VALUE_START, hostAddress);
    }

    /**
     * Notify serverStop.
     *
     * @param context context.
     */
    public static void onServerError(Context context, String error) {
        sendBroadcast(context, CMD_VALUE_ERROR, error);
    }

    /**
     * Notify serverStop.
     *
     * @param context context.
     */
    public static void onServerStop(Context context) {
        sendBroadcast(context, CMD_VALUE_STOP);
    }

    private static void sendBroadcast(Context context, int cmd) {
        sendBroadcast(context, cmd, null);
    }

    private static void sendBroadcast(Context context, int cmd, String message) {
        Intent broadcast = new Intent(ACTION);
        broadcast.putExtra(CMD_KEY, cmd);
        broadcast.putExtra(MESSAGE_KEY, message);
        context.sendBroadcast(broadcast);
    }

    public void startServer() {
    }

    public void stopServer() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.i(TAG, "onReceive=" + intent.getAction());
        if (ACTION.equals(action)) {
            int cmd = intent.getIntExtra(CMD_KEY, 0);
            switch (cmd) {
                case CMD_VALUE_START: {
                    String ip = intent.getStringExtra(MESSAGE_KEY);
                    LogUtil.i(TAG, "onReceive CMD_VALUE_START ip=" + ip);
                    break;
                }
                case CMD_VALUE_ERROR: {
                    String error = intent.getStringExtra(MESSAGE_KEY);
                    LogUtil.i(TAG, "onReceive MESSAGE_KEY error=" + error);
                    break;
                }
                case CMD_VALUE_STOP: {
                    LogUtil.i(TAG, "onReceive CMD_VALUE_STOP");
                    break;
                }
            }
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean hasNetwork = NetUtil.hasConnect(context);

            LogUtil.i(TAG, "hasNetwork=" + hasNetwork);
            if (hasNetwork) {
                if (mCoreService == null) {
                    Intent serviceIntent = new Intent(context, CoreService.class);
                    context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
                } else {
                    mCoreService.startServer();
                }
            }
        }
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.CoreServiceBinder)service).getService();
            mCoreService.startServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService = null;
        }
    };
}