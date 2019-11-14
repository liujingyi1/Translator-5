package com.android.rgk.common.db;

import android.content.Context;

import com.android.rgk.common.db.bean.AccessLog;
import com.android.rgk.common.db.bean.CardInfo;
import com.android.rgk.common.db.bean.DoorMagnet;
import com.android.rgk.common.db.bean.Image;
import com.android.rgk.common.db.bean.SIPAccountInfo;
import com.android.rgk.common.db.bean.User;
import com.android.rgk.common.db.impl.AccessLogDaoImpl;
import com.android.rgk.common.db.impl.CardDaoImpl;
import com.android.rgk.common.db.impl.DoorMagnetDaoImpl;
import com.android.rgk.common.db.impl.ImageDaoImpl;
import com.android.rgk.common.db.impl.NumberDaoImpl;
import com.android.rgk.common.db.impl.PasswordDaoImpl;
import com.android.rgk.common.db.impl.SIPDaoImpl;
import com.android.rgk.common.db.impl.UserDaoImpl;
import com.android.rgk.common.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class DataOperator implements UserDao, NumberDao, CardDao, AccessLogDao, DoorMagnetDao,
        PasswordDao, ImageDao, SIPDao {
    private static DataOperator mInstance;

    private Context mContext;
    private DatabaseHelper mDatabaseHelper;
    private UserDao mUserDao;
    private NumberDao mNumberDao;
    private SIPDao mSIPDao;
    private CardDao mCardDao;
    private AccessLogDao mAccessLogDao;
    private DoorMagnetDao mDoorMagnetDao;
    private PasswordDao mPasswordDao;
    private ImageDao mImageDao;

    private DataOperator(Context context) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(context);
        mDatabaseHelper.getReadableDatabase();
        mUserDao = new UserDaoImpl(mDatabaseHelper);
        mNumberDao = new NumberDaoImpl(mDatabaseHelper);
        mSIPDao = new SIPDaoImpl(mDatabaseHelper);
        mCardDao = new CardDaoImpl(mDatabaseHelper);
        mAccessLogDao = new AccessLogDaoImpl(mDatabaseHelper);
        mDoorMagnetDao = new DoorMagnetDaoImpl(mDatabaseHelper);
        mPasswordDao = new PasswordDaoImpl(mDatabaseHelper);
        mImageDao = new ImageDaoImpl(mDatabaseHelper);
    }

    public static void init(Context context) {
        LogUtil.i("DataOperator", "init()");
        if (mInstance == null) {
            LogUtil.i("DataOperator", "init() create DataOperator");
            mInstance = new DataOperator(context);
        }
    }

    public static DataOperator getInstance() {
        return mInstance;
    }

    public static void destroy() {
        LogUtil.i("DataOperator", "destroy()");
        if (mInstance != null) {
            mInstance.release();
            mInstance = null;
            LogUtil.i("DataOperator", "destroy(): mInstance = null");
        }
    }

    private void release() {
        mDataChangeListeners.clear();
        mDatabaseHelper = null;
        mUserDao = null;
        mNumberDao = null;
        mSIPDao = null;
    }

    @Override
    public ArrayList<User> getAllUsers() {
        return mUserDao.getAllUsers();
    }

    @Override
    public User getUserById(final int id) {
        return mUserDao.getUserById(id);
    }

    @Override
    public User getUserByPersonId(final int personId) {
        return mUserDao.getUserByPersonId(personId);
    }

    @Override
    public User getUserByServerPersonId(String serverPersonId) {
        return mUserDao.getUserByServerPersonId(serverPersonId);
    }

    @Override
    public User getUserByFaceId(final int faceId) {
        return mUserDao.getUserByFaceId(faceId);
    }

    @Override
    public int insert(final User user) {
        int rowId = mUserDao.insert(user);
        if (rowId > 0) {
            notifyChange(DataColumns.UserColumns.TABLE_USER);
        }
        return rowId;
    }

    @Override
    public int update(final User user) {
        int rowId = mUserDao.update(user);
        if (rowId > 0) {
            notifyChange(DataColumns.UserColumns.TABLE_USER);
        }
        return rowId;
    }

    @Override
    public int deleteUserByPersonId(final int personId) {
        int count = mUserDao.deleteUserByPersonId(personId);
        if (count > 0) {
            notifyChange(DataColumns.UserColumns.TABLE_USER);
        }
        return count;
    }

    @Override
    public int deleteUserByServerPersonId(String serverPersonId) {
        int count = mUserDao.deleteUserByServerPersonId(serverPersonId);
        if (count > 0) {
            notifyChange(DataColumns.UserColumns.TABLE_USER);
        }
        return count;
    }

    @Override
    public int deleteUserByFaceId(final int faceId) {
        int count = mUserDao.deleteUserByFaceId(faceId);
        if (count > 0) {
            notifyChange(DataColumns.UserColumns.TABLE_USER);
        }
        return count;
    }

    @Override
    public int deleteAllUsers() {
        int count = mUserDao.deleteAllUsers();
        if (count > 0) {
            notifyChange(DataColumns.UserColumns.TABLE_USER);
        }
        return count;
    }

    @Override
    public ArrayList<String> getNumberByRoom(String room) {
        return mNumberDao.getNumberByRoom(room);
    }

    @Override
    public int insertNumber(String room, String number, int order) {
        return mNumberDao.insertNumber(room, number, order);
    }

    @Override
    public int deleteByRoom(String room) {
        return mNumberDao.deleteByRoom(room);
    }

    @Override
    public String getRoomByNumber(String number) {
        return mNumberDao.getRoomByNumber(number);
    }

    @Override
    public ArrayList<String> getAllRooms() {
        return mNumberDao.getAllRooms();
    }

    @Override
    public int deleteByRoomAndNumber(String room, String number) {
        return mNumberDao.deleteByRoomAndNumber(room, number);
    }

    private ArrayList<DataChangeListener> mDataChangeListeners = new ArrayList<>();

    public void addListener(DataChangeListener listener) {
        mDataChangeListeners.add(listener);
    }

    public void removeListener(DataChangeListener listener) {
        mDataChangeListeners.remove(listener);
    }

    private void notifyChange(String table) {
        for (DataChangeListener listener : mDataChangeListeners) {
            listener.onChanged(table);
        }
    }

    @Override
    public CardInfo getCard(String cardNo) {
        return mCardDao.getCard(cardNo);
    }

    @Override
    public List<CardInfo> getAllCards() {
        return mCardDao.getAllCards();
    }

    @Override
    public long insertCard(String cardNo, int cardType, long timeStamp, long endTime, int filterType) {
        return mCardDao.insertCard(cardNo, cardType, timeStamp, endTime, filterType);
    }

    @Override
    public int updateCard(String cardNo, int filterType) {
        return mCardDao.updateCard(cardNo, filterType);
    }

    @Override
    public int deleteCard(String cardNo) {
        return mCardDao.deleteCard(cardNo);
    }

    @Override
    public List<AccessLog> getAllAccessLogs() {
        return mAccessLogDao.getAllAccessLogs();
    }

    @Override
    public List<AccessLog> getAccessLogs(int type) {
        return mAccessLogDao.getAccessLogs(type);
    }

    @Override
    public long insertAccessLog(AccessLog accessLog) {
        return mAccessLogDao.insertAccessLog(accessLog);
    }

    @Override
    public int deleteAllAccessLog() {
        return mAccessLogDao.deleteAllAccessLog();
    }

    @Override
    public List<DoorMagnet> getAllDoorMagnets() {
        return mDoorMagnetDao.getAllDoorMagnets();
    }

    @Override
    public long insertDoorMagnet(long time, int status) {
        return mDoorMagnetDao.insertDoorMagnet(time, status);
    }

    @Override
    public int deleteAllDoorMagnets() {
        return mDoorMagnetDao.deleteAllDoorMagnets();
    }

    @Override
    public List<Image> getImages(int start, int length) {
        return mImageDao.getImages(start, length);
    }

    @Override
    public long insertImage(String fileName, long date) {
        return mImageDao.insertImage(fileName, date);
    }

    @Override
    public int deleteImageByIds(List<Long> ids) {
        return mImageDao.deleteImageByIds(ids);
    }

    @Override
    public int insertSIP(SIPAccountInfo info) {
        return mSIPDao.insertSIP(info);
    }

    public int deleteSIP() {
        return deleteSIPByType(SIPAccountInfo.TYPE_MAIN);
    }

    @Override
    public int deleteSIPByType(String type) {
        return mSIPDao.deleteSIPByType(type);
    }

    public SIPAccountInfo getAccountInfo() {
        return getAccountInfoByType(SIPAccountInfo.TYPE_MAIN);
    }

    @Override
    public SIPAccountInfo getAccountInfoByType(String type) {
        return mSIPDao.getAccountInfoByType(type);
    }

    public interface DataChangeListener {
        void onChanged(String table);
    }

    @Override
    public int insertPassword(String password, long startTime, long expireDate) {
        return mPasswordDao.insertPassword(password, startTime, expireDate);
    }

    @Override
    public boolean checkPassword(String password, long currentTime) {
        return mPasswordDao.checkPassword(password, currentTime);
    }

    @Override
    public int deleteOverduePassword(long currentTime) {
        return mPasswordDao.deleteOverduePassword(currentTime);
    }
}
