package com.android.face.nfc;

import android.util.Log;

import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.bean.CardInfo;

public class Fk {
    final static String TAG = "Fk";
    // CardType
    final static int C_UNKNOW = 0;
    final static int C_ERROR = 1;
    final static int C_MANUFACTURER = 2;
    final static int C_MANAGEMENT = 3;
    final static int C_CONFIGURE = 4;
    final static int C_USERCARD = 5;
    final static int C_PROPERTY = 6;
    final static int C_MANAGER = 7;
    final static int C_ONLYCARD = 8;

    static int subcrc16Len;
    static char[] read_128;
    static char[] read_8;
    static String mCardID;

    static boolean verifycrc16(int len) {
        int d = (int) (read_128[len + 1] << 8 | read_128[len]);
        return d == crc16(read_128, len);
    }

    static int crc16(char[] data, int len) {
        char pwcrc = 0;
        for (int i = 0; i < len; i++) {
            char x = pwcrc;
            x = (char) ((x >>> 8) | (x << 8));
            x ^= data[i] & 0xff;
            x ^= (x & 0xff) >>> 4;
            x ^= (x << 8) << 4;
            x ^= ((x & 0xff) << 4) << 1;
            pwcrc = x;
        }
        return pwcrc & 0xffff;
    }

    static int crc8(char[] data, int len) {
        int i_crc = 0, i;
        for (i = 0; i < len; i++)
            i_crc ^= data[i] & 0xff;
        return i_crc;
    }

    static char[] strToBytes(String str) {
        if (str == null || str.length() < 2) {
            return null;
        }
        int size = str.length() / 2;
        char[] b = new char[size];
        for (int i = 0; i < size; i++) {
            int pos = i * 2;
            try {
                b[i] = (char) Integer.parseInt(str.substring(pos, pos + 2), 16);
            } catch (NumberFormatException e) {
                logi("strToBytes error:" + e);
                return null;
            }
        }
        return b;
    }

    static int verifyCard(String card_id, String data) {
        subcrc16Len = 0;
        read_8 = strToBytes(card_id);
        if (read_8 == null || read_8.length != 8) {
            logi("card_id str error!" + card_id);
            return C_ERROR;
        }
        mCardID = card_id;
        read_128 = strToBytes(data);
        if (read_128 == null) {
            return C_ONLYCARD;
        }
        if (read_128.length < 89) {
            logi("read_128.length error!" + read_128.length);
            return C_ERROR;
        }

        // to Decrypt data
        EncrypDecryp.Decryptionr(read_128, read_8, read_128);
        if (0 != crc8(read_128, 24)) {
            logi("crc8 error!");
            return C_ERROR;
        }
        /*
         * //if(gFKInfo.sysType.sysData.debug_flag==0)
         * if((read_128[0]==0x20)&&(read_128[1]==0x14)&&
         * (read_128[2]==0x03)&&(read_128[3]==0x08)&& (read_128[4]==0xFF)) {
         * return FkUtil.C_MANUFACTURER;//return(50);//50 }
         */

        if (0x81 == (read_128[16] & 0x81)) {
            if (read_128[28] == 0x12) {
                subcrc16Len = 30;
                if (!verifycrc16(87)) {
                    logi("verifycrc16 error!");
                    return C_ERROR;
                }
            }
            return C_MANAGEMENT;
        } else if (0x80 == read_128[16]) {
            subcrc16Len = 29;
            if (!verifycrc16(87)) {
                logi("verifycrc16 error!");
                return C_ERROR;
            }
            return C_CONFIGURE;
        } else {
            if (read_128[28] == 0x12) {
                subcrc16Len = 30;
                if (!verifycrc16(87)) {
                    logi("verifycrc16 error!");
                    return C_ERROR;
                }
            }
            int m_temp = (read_128[16] >> 2) & 0x03;
            if (2 > m_temp) {
                // Sound(5);
                return C_USERCARD;
            }
            if (m_temp == 0x02) {
                return C_PROPERTY;
            }
            if (m_temp == 0x03)// manager
            {
                return C_MANAGER;
            }
        }
        return C_UNKNOW;// 0
    }

    static boolean canOpenDoor(String card) {
        String[] strs = card.split("#");
        String strData = "";
        if(strs.length == 2){
            if(strs[1].length() > 26){
                strData = strs[1].substring(26,strs[1].length());
            }
        }
        int card_type = verifyCard(strs[0], strData);
        logi("cartype = " + card_type);
        switch (card_type) {
            case C_ONLYCARD: //身份证
            case C_USERCARD: //用户卡
            {
                return isValidCardID(mCardID);
            }
            default:
                return false;
        }
    }

    static boolean isValidCardID(String cardID) {
        CardInfo cardInfo = DataOperator.getInstance().getCard(cardID);
        //long currentTime = System.currentTimeMillis();
        if (cardInfo != null && cardInfo.filterType == CardInfo.TYPE_WHITE) {
            return true;
        }
        return false;
    }

    static void logi(String msg) {
        Log.i("Fk", msg);
    }
}