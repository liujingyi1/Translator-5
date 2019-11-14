package com.android.rgk.common.db;

public class DataColumns {
    public interface UserColumns {
        String TABLE_USER = "users";
        String USER_ID = "_id";
        String PERSON_ID = "person_id";
        String SERVER_PERSON_ID = "server_person_id";
        String USER_NAME = "name";
        String USER_AGE = "age";
        String USER_GENDER = "gender";
        String USER_SCORE = "score";
        String USER_HEAD = "head";
        String PERSON_TYPE = "person_type";
        String USER_EXPIRE_DATE = "expire_date";
        String USER_FACE_DBID = "face_dbid";
        String USER_UID = "uid";
    }

    public interface NumberColumns {
        String TABLE_NUMBER = "number";
        String NUMBER_ID = "_id";
        String ROOM = "room";
        String NUMBER = "number";
        String ORDER = "dial_order";
    }

    public interface CardColumns {
        String TABLE_CARD = "card";
        String CARD_ID = "_id";
        String CARD_NO = "card_no";
        String CARD_TYPE = "card_type";
        String TIME_STAMP = "time_stamp";
        String END_TIME = "end_time";
        String FILTER_TYPE = "filter_type";
    }

    public interface SIPAccountColumns {
        String TABLE_SIP = "sip_account";
        String SIP_ID = "_id";
        String DOMAIN = "domain";
        String USER = "user";
        String SIP_PASSWORD = "password";
        String OUTBUND = "outbund";
        String SIP_TYPE = "filter_type";
    }

    public interface AccessLogColumns {
        String TABLE_ACCESS_LOG = "access_log";
        String ACCESS_LOG_ID = "_id";
        String OPEN_TYPE = "open_type";
        String OPEN_TIME = "open_time";
        String LOCK_STATUS = "lock_status";
        String OPEN_RESULT = "open_result";
        String PARAMETERS = "parameters";
    }

    public interface DoorMagnetColumns {
        String TABLE_DOOR_MAGNET = "door_magnet";
        String DOOR_MAGNET_ID = "_id";
        String LOG_TIME = "log_time";
        String SENSOR_STATUS = "sensor_status";
    }

    public interface PasswordColumns {
        String TABLE_PASSWORD = "password";
        String PASSWORD_ID = "_id";
        String PASSWORD = "password_number";
        String STARTTIME = "start_time";
        String EXPIREDATE = "expire_date";
    }

    public interface ImageColumns {
        String TABLE_IMAGE = "image";
        String IMAGE_ID = "_id";
        String IMAGE_FILE = "file";
        String IMAGE_DATE = "date";
    }
}
