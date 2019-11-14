package com.android.face.http.bean;

public class SysVersion {
    public String swVersion;
    public String readHeadVersion;
    public String cloudCallVersion;

    public String getCloudCallVersion() {
        return cloudCallVersion;
    }

    public void setCloudCallVersion(String cloudCallVersion) {
        this.cloudCallVersion = cloudCallVersion;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getReadHeadVersion() {
        return readHeadVersion;
    }

    public void setReadHeadVersion(String readHeadVersion) {
        this.readHeadVersion = readHeadVersion;
    }
}
