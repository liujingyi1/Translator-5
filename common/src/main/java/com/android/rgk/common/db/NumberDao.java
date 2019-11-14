package com.android.rgk.common.db;

import java.util.ArrayList;

public interface NumberDao {
    ArrayList<String> getNumberByRoom(String room);

    int insertNumber(String room, String number, int order);

    int deleteByRoom(String room);

    String getRoomByNumber(String number);

    ArrayList<String> getAllRooms();

    int deleteByRoomAndNumber(String room, String number);
}
