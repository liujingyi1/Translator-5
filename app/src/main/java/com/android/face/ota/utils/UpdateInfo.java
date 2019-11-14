package com.android.face.ota.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateInfo implements Parcelable {

    private String size;
    private String url;
    private String version;
    private String md5;
    private String fileType;
    private String fileId;
    private String crc;


    public UpdateInfo(String size, String url, String version, String md5, String fileType, String fileId, String crc) {
        this.size = size;
        this.url = url;
        this.version = version;
        this.md5 = md5;
        this.fileType = fileType;
        this.fileId = fileId;
        this.crc = crc;
    }

    protected UpdateInfo(Parcel in) {
        size = in.readString();
        url = in.readString();
        version = in.readString();
        md5 = in.readString();
        fileType = in.readString();
        fileId = in.readString();
        crc = in.readString();
    }

    public static final Creator <UpdateInfo> CREATOR = new Creator <UpdateInfo>() {
        @Override
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }

        @Override
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };

    public void setSize(String size) {
        this.size = size;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }


    public String getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public String getMd5() {
        return md5;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileId() {
        return fileId;
    }

    public String getCrc() {
        return crc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(size);
        dest.writeString(url);
        dest.writeString(version);
        dest.writeString(md5);
        dest.writeString(fileType);
        dest.writeString(fileId);
        dest.writeString(crc);
    }
}
