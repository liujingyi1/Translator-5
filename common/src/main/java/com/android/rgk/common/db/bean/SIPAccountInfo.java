package com.android.rgk.common.db.bean;

public class SIPAccountInfo {
    private static final String TAG = "SIPAccountInfo";

    public static final String TYPE_MAIN = "1";
    public static final String TYPE_DEPUTY = "2";

    public String domain;
    public String user;
    public String password;
    public String outbund;
    public String type = TYPE_MAIN;

    @Override
    public String toString() {
        return domain + "," + user + "," + password + "," + outbund;
    }
}
