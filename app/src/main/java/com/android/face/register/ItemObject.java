package com.android.face.register;

import java.io.File;

public class ItemObject {
    public File file;
    public boolean checked;

    public ItemObject(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }
}
