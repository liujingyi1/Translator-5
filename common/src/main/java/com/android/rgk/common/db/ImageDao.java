package com.android.rgk.common.db;

import com.android.rgk.common.db.bean.Image;

import java.util.List;

public interface ImageDao {
    List<Image> getImages(int start, int length);

    long insertImage(String fileName, long date);

    int deleteImageByIds(List<Long> ids);
}
