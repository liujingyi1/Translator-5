package com.android.rgk.common.util;

import com.android.rgk.common.Constant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
    public static void saveLog(String fileName, String content) {
        if (fileName == null) {
            throw new IllegalArgumentException("The fileName is null...");
        }
        String path = Constant.ImagePath + fileName;
        FileWriter writer = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
