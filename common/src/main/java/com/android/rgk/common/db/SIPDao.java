package com.android.rgk.common.db;

import com.android.rgk.common.db.bean.SIPAccountInfo;

public interface SIPDao {
    int insertSIP(SIPAccountInfo info);

    int deleteSIPByType(String type);

    SIPAccountInfo getAccountInfoByType(String type);
}
