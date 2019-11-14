package com.android.face.domain;

import com.android.face.mcu.McuManager;
import com.android.rgk.common.util.LogUtil;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerApiManager {
    private static final String TAG = "ServerApiManager";
    private static final String BASE_URL = "https://mockapi.eolinker.com/7LhbHJscf2bcfc83a4b7988a241cbe0ec2b2fddaee62964/";

    private HashMap<Class, Retrofit> mRetrofitServiceHashMap;

    private Retrofit mApiRetrofit;
    private ConcurrentHashMap<Class, Object> cachedApis;

    public ServerApiManager() {
        setBaseUrl(null);
    }

    public void setBaseUrl(String s) {
        String url = s != null ? s : McuManager.getHttpUrl(BASE_URL);
        LogUtil.i(TAG, "setBaseUrl:" + url);
        mApiRetrofit = null;
        mRetrofitServiceHashMap = new HashMap<>();
        cachedApis = new ConcurrentHashMap<>();
        // init okhttp 3 logger
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        // init cache
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(logInterceptor)
                .build();

        mApiRetrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(StringConverterFactory.create())
                .build();

        mRetrofitServiceHashMap.put(ServerApiService.class, mApiRetrofit);
    }

    public <T> void addService(Class<T> clz) {
        mRetrofitServiceHashMap.put(clz, mApiRetrofit);
    }

    public <T> T getService(Class<T> clz) {
        Object obj = cachedApis.get(clz);
        if (obj != null) {
            return (T) obj;
        } else {
            Retrofit retrofit = mRetrofitServiceHashMap.get(clz);
            if (retrofit != null) {
                T service = retrofit.create(clz);
                cachedApis.put(clz, service);
                return service;
            } else {
                return null;
            }
        }
    }
}
