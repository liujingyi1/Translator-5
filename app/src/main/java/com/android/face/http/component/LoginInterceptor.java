package com.android.face.http.component;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.face.http.util.JsonUtils;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.framework.HandlerInterceptor;
import com.yanzhenjie.andserver.framework.handler.MethodHandler;
import com.yanzhenjie.andserver.framework.handler.RequestHandler;
import com.yanzhenjie.andserver.http.HttpMethod;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.session.Session;
import com.yanzhenjie.andserver.mapping.Addition;
import com.yanzhenjie.andserver.util.MultiValueMap;

import org.apache.commons.lang3.ArrayUtils;

@Interceptor
public class LoginInterceptor implements HandlerInterceptor {

    public static final String LOGIN_ATTRIBUTE = "USER.LOGIN.SIGN";
    private static final String TAG = "LoginInterceptor";

    private static final long SESSION_TIME_OUT_CREATE = 1000 * 60 * 60 * 2;
    private static final long SESSION_TIME_OUT_ACCESS = 1000 * 60;

    @Override
    public boolean onIntercept(@NonNull HttpRequest request, @NonNull HttpResponse response,
                               @NonNull RequestHandler handler) {
        String path = request.getPath();
        HttpMethod method = request.getMethod();
        MultiValueMap<String, String> valueMap = request.getParameter();
        Log.i(TAG, "LoginInterceptor Path: " + path);
        Log.i(TAG, "LoginInterceptor Method: " + method.value());
        Log.i(TAG, "LoginInterceptor Param: " + JsonUtils.toJsonString(valueMap));
        Log.i(TAG, "LoginInterceptor MethodHandler: " + (handler instanceof MethodHandler));

        if (path.equals("/")) {
            if (!getLoginState(request)) {
                response.sendRedirect("/login.html");
            } else {
                response.sendRedirect("/index.html");
            }
            return true;
        } else if (path.equals("/login.html")) { 
            Log.i(TAG, "LoginInterceptor login.html:" );
            if (getLoginState(request)) {
                response.sendRedirect("/index.html");
                return true;
            }
        } else if (handler instanceof MethodHandler) {
            MethodHandler methodHandler = (MethodHandler)handler;
            Addition addition = methodHandler.getAddition();
            if (!isLogin(request, addition)) {
                response.sendRedirect("/login.html");
                return true;
                //throw new BasicException(401, "You are not logged in yet.");
            }
        } else if (path.equals("/index.html")) {
            if (!getLoginState(request)) {
                response.sendRedirect("/login.html");
                return true;
            }
        }
        return false;
    }

    private boolean isNeedLogin(Addition addition) {
        if (addition == null) return false;

        String[] stringType = addition.getStringType();
        if (ArrayUtils.isEmpty(stringType)) return false;

        boolean[] booleanType = addition.getBooleanType();
        if (ArrayUtils.isEmpty(booleanType)) return false;
        return stringType[0].equalsIgnoreCase("login") && booleanType[0];
    }

    private boolean isLogin(HttpRequest request, Addition addition) {
        if (isNeedLogin(addition)) {
            return getLoginState(request);
        }
        return true;
    }

    private boolean getLoginState(HttpRequest request) {
        Session session = request.getSession();

        if (session != null) {
            Log.i(TAG, "getLoginState session.isValid()="+session.isValid());

            long current = System.currentTimeMillis();
            long lastAccessedTime = session.getLastAccessedTime();
            long createTime = session.getCreatedTime();
            Log.i(TAG, "getLoginState lastAccessedTime="+(current - lastAccessedTime)+" createTime="+(current-createTime));
            if ((current - lastAccessedTime) > SESSION_TIME_OUT_ACCESS ||
                    (current-createTime) > SESSION_TIME_OUT_CREATE) {
                return false;
            }
            Object o = session.getAttribute(LOGIN_ATTRIBUTE);
            Log.i(TAG, "getLoginState o="+((boolean)o));
            return o != null && (o instanceof Boolean) && ((boolean)o);
        }
        return false;
    }
}
