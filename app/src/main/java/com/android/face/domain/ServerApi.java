package com.android.face.domain;

import android.util.Log;

import io.reactivex.functions.Consumer;
import okhttp3.RequestBody;

public class ServerApi {
    private static String TAG = "ServerApi";

    private static ServerApi mInstance = null;
    private ServerApiManager mApiManager = null;

    private static final int SUCCESS_CODE = 1;
    public static final int CODE_INVALID_SECOND = 200;

    public interface ServerCallback {
        public void call(String msg, long code, Object result);
    }

    private ServerApi() {
        mApiManager = new ServerApiManager();
    }

    public static <T> T apiService(Class<T> clz) {
        return getInstance().mApiManager.getService(clz);
    }

    public static ServerApi getInstance() {
        if (mInstance == null) {
            mInstance = new ServerApi();
        }
        return mInstance;
    }

    public <T> void addApiService(Class<T> clz) {
        getInstance().mApiManager.addService(clz);
    }

    public void setBaseUrl(String s) {
        getInstance().mApiManager.setBaseUrl(s);
    }

    public void deleteDevice(final RequestBody body, final ServerCallback callback) {
        ServerApi.apiService(ServerApiService.class)
                .deleteDevice(body)
                .compose(RxSchedulers.<ServerResponse>io_main())
                .subscribe(new Consumer<ServerResponse>() {
                    @Override
                    public void accept(ServerResponse serverResponse) throws Exception {
                        Log.i("jingyi", "serverResponse=" + serverResponse);
                        if (serverResponse != null) {
                            if (callback != null) {
                                callback.call(serverResponse.msg, serverResponse.code, serverResponse.result);
                            }
                        } else {
                            if (callback != null) {
                                callback.call("fail", -1, null);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i("jingyi", "throwable=" + throwable.getMessage());
                        if (callback != null) {
                            callback.call("fail", -1, null);
                        }
                    }
                });
    }

    public void canRegister(final RequestBody body, final ServerCallback callback) {
        ServerApi.apiService(ServerApiService.class)
                .canRegister(body)
                .compose(RxSchedulers.<ServerResponse>io_main())
                .subscribe(new Consumer<ServerResponse>() {
                    @Override
                    public void accept(ServerResponse serverResponse) throws Exception {
                        Log.i("jingyi", "serverResponse=" + serverResponse);
                        if (serverResponse != null) {
                            if (callback != null) {
                                callback.call(serverResponse.msg, serverResponse.code, serverResponse.result);
                            }
                        } else {
                            if (callback != null) {
                                callback.call("fail", -1, null);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i("jingyi", "throwable=" + throwable.getMessage());
                        if (callback != null) {
                            callback.call("fail", -1, null);
                        }
                    }
                });
    }

    public void registerDevice(final Device device, final ServerCallback callback) {
        ServerApi.apiService(ServerApiService.class)
                .registerDevice(device)
                .compose(RxSchedulers.<ServerResponse>io_main())
                .subscribe(new Consumer<ServerResponse>() {
                    @Override
                    public void accept(ServerResponse serverResponse) throws Exception {
                        Log.i("jingyi", "serverResponse=" + serverResponse);
                        if (serverResponse != null) {
                            if (callback != null) {
                                callback.call(serverResponse.msg, serverResponse.code, serverResponse.result);
                            }
                        } else {
                            if (callback != null) {
                                callback.call("fail", -1, null);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i("jingyi", "throwable=" + throwable.getMessage());
                        if (callback != null) {
                            callback.call("fail", -1, null);
                        }
                    }
                });
    }

    public void updateDevice(final Device device, final ServerCallback callback) {
        ServerApi.apiService(ServerApiService.class)
                .updateDevice(device)
                .compose(RxSchedulers.<ServerResponse>io_main())
                .subscribe(new Consumer<ServerResponse>() {
                    @Override
                    public void accept(ServerResponse serverResponse) throws Exception {
                        Log.i("jingyi", "serverResponse=" + serverResponse);
                        if (serverResponse != null) {
                            if (callback != null) {
                                callback.call(serverResponse.msg, serverResponse.code, serverResponse.result);
                            }
                        } else {
                            if (callback != null) {
                                callback.call("fail", -1, null);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i("jingyi", "throwable=" + throwable.getMessage());
                        if (callback != null) {
                            callback.call("fail", -1, null);
                        }
                    }
                });
    }
}
