/*
 * Copyright 2018 Yan Zhenjie.
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
package com.android.face.http.controller;

import android.util.Base64;
import android.util.Log;

import com.android.face.http.component.LoginInterceptor;
import com.android.face.http.model.UserInfo;
import com.android.face.ota.utils.MD5;
import com.yanzhenjie.andserver.annotation.Addition;
import com.yanzhenjie.andserver.annotation.CookieValue;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PathVariable;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.cookie.Cookie;
import com.yanzhenjie.andserver.http.session.Session;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by YanZhenjie on 2018/6/9.
 */
@RestController
@RequestMapping(path = "/user")
class UserController {
    private static final String TAG = "UserController";

    private static final String ACCOUNT = "123";
    private static final String PASSWORD = "123";
    private static final String COOKIENAME = "account";

    @GetMapping(path = "/get/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String info(@PathVariable(name = "userId") String userId) {
        return userId;
    }

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String login(HttpRequest request, HttpResponse response, @RequestParam(name = "account") String account,
                 @RequestParam(name = "password") String password,
                 @RequestParam(name = "remember", required = false, defaultValue = "false") String remember) {

//        try {
//            account = new String(Base64.decode(account, Base64.DEFAULT), "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

//        String md5psw = md5Decode32("321");

        Log.i("jingyi", "account = "+account+" password="+password);
        if (account.equals(ACCOUNT) && password.equals(PASSWORD)) {
            Session session = request.getValidSession();
            session.setAttribute(LoginInterceptor.LOGIN_ATTRIBUTE, true);

            Log.i("jingyi", "remember="+remember);

            if (remember.equals("true")) {
                Cookie cookie = new Cookie(COOKIENAME, account + "=" + password);
                response.addCookie(cookie);
            }
            response.sendRedirect("/index.html");
            Log.i("jingyi", "1111");
            return "Login successful.";
        } else {
            response.setStatus(401);
            Log.i("jingyi", "2222");

            return "Login failed.";
        }
    }

    @Addition(stringType = "login", booleanType = true)
    @GetMapping(path = "/userInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    UserInfo userInfo(@CookieValue("account") String account) {
        Log.i(TAG, "Account: " + account);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId("123");
        userInfo.setUserName("AndServer");
        return userInfo;
    }

    @PostMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String logout(HttpRequest request, HttpResponse response) {
        Session session = request.getValidSession();
        session.setAttribute(LoginInterceptor.LOGIN_ATTRIBUTE, false);
        return "success";
    }

    /**
     * 32位MD5加密
     * @param content -- 待加密内容
     * @return
     */
    public String md5Decode32(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }
        //对生成的16字节数组进行补零操作
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}