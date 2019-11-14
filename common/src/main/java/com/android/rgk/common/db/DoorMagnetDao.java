package com.android.rgk.common.db;

import com.android.rgk.common.db.bean.DoorMagnet;

import java.util.List;

public interface DoorMagnetDao {
    List<DoorMagnet> getAllDoorMagnets();
    long insertDoorMagnet(long time, int status);
    int deleteAllDoorMagnets();
}
