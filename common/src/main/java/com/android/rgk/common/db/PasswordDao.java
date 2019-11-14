package com.android.rgk.common.db;

public interface PasswordDao {
    int insertPassword(String password, long startTime, long expireDate);

    boolean checkPassword(String password, long currentTime);

    int deleteOverduePassword(long currentTime);
}
