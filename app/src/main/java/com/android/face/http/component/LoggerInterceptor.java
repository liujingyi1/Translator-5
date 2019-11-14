package com.android.face.http.component;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.face.http.util.JsonUtils;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.framework.HandlerInterceptor;
import com.yanzhenjie.andserver.framework.handler.RequestHandler;
import com.yanzhenjie.andserver.http.HttpMethod;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.util.MultiValueMap;

@Interceptor
public class LoggerInterceptor implements HandlerInterceptor {
    private static final String TAG = "LoggerInterceptor";
    
    @Override
    public boolean onIntercept(@NonNull HttpRequest request, @NonNull HttpResponse response,
                               @NonNull RequestHandler handler) {
        String path = request.getPath();
        HttpMethod method = request.getMethod();
        MultiValueMap<String, String> valueMap = request.getParameter();
        Log.i(TAG, "Path: " + path);
        Log.i(TAG, "Method: " + method.value());
        Log.i(TAG, "Param: " + JsonUtils.toJsonString(valueMap));
        return false;
    }
}
