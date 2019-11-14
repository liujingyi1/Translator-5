package com.android.rgk.common.db;

import com.android.rgk.common.db.bean.User;

import java.util.ArrayList;

public interface UserDao {
    ArrayList<User> getAllUsers();

    User getUserById(int id);

    User getUserByPersonId(int personId);

    User getUserByServerPersonId(String serverPersonId);

    User getUserByFaceId(int faceId);

    int insert(User user);

    int update(User user);

    int deleteUserByPersonId(int personId);

    int deleteUserByServerPersonId(String serverPersonId);

    int deleteUserByFaceId(int faceId);

    int deleteAllUsers();
}
