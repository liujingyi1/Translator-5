package com.android.face.domain;

public class ServerResponse {
    public long code;
    public Object result;
    public String msg;

    @Override
    public String toString() {
        return "ServerResponse{" +
                "code=" + code +
                ", result='" + result + '\'' +
                ", message='" + msg + '\'' +
                '}';
    }
}
