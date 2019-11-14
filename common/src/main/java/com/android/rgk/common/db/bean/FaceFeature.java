package com.android.rgk.common.db.bean;

import com.alibaba.fastjson.JSON;
import com.android.rgk.common.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FaceFeature {
    private static final String TAG = "FaceFeature";
    String created_at;
    String image_uuid;
    int width;
    int height;
    String image_url;
    List<Face> faces;

    public static FaceFeature toFaceFeature(String jsonString) {
        LogUtil.d(TAG, "toFaceFeature:" + jsonString);
        if (!jsonString.contains("faces")) {
            return null;
        }
        return JSON.parseObject(jsonString, FaceFeature.class);
    }

    public static FaceFeature toFaceFeature(InputStream inputStream) {
        LogUtil.d(TAG, "toFaceFeature: inputStream=" + inputStream);
        try {
            return JSON.parseObject(inputStream, FaceFeature.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public float[] getFaceFeature(int index) {
        if (faces == null || faces.size() == 0) {
            return null;
        }
        return faces.get(index).getFaceFeature();
    }

    @Override
    public String toString() {
        return "{" + created_at + ","
                + image_uuid + ","
                + width + ","
                + height + ","
                + image_url + ","
                + faces + "}";
    }

    static class Face {
        String created_at;
        String face_uuid;
        String rect;
        String landmarks21;
        List<Float> face_feature;
        int age;
        int gender;
        String image_url;
        int rgb_liveness;
        int nir_liveness;
        int depth_liveness;
        int quality;
        float confidence;
        float yaw;
        float pitch;
        float roll;

        float[] getFaceFeature() {
            float[] features = new float[face_feature.size()];
            for (int i = 0; i < features.length; i++) {
                features[i] = face_feature.get(i).floatValue();
            }
            return features;
        }

        @Override
        public String toString() {
            return "faces:{" + created_at + ","
                    + face_uuid + ","
                    + rect + ","
                    + landmarks21 + ","
                    + face_feature + ","
                    + age + ","
                    + gender + ","
                    + image_url + ","
                    + rgb_liveness + ","
                    + nir_liveness + ","
                    + depth_liveness + ","
                    + quality + ","
                    + confidence + ","
                    + yaw + ","
                    + pitch + ","
                    + roll + "}";
        }
    }
}
