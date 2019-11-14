package com.android.rgk.common.db;

import com.android.rgk.common.db.bean.AccessLog;

import java.util.List;

public interface AccessLogDao {
    List<AccessLog> getAllAccessLogs();
    List<AccessLog> getAccessLogs(int type);
    long insertAccessLog(AccessLog accessLog);
    int deleteAllAccessLog();
}
