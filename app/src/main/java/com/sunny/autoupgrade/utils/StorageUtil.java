package com.sunny.autoupgrade.utils;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageUtil {

    private final static String apkFileName = "app.apk";

    private static String getFileDir() {

        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static boolean saveAPK(InputStream inputStream) throws IOException {

        String filePath = getFilePath();
        File file = new File(filePath);
        if (!file.exists()) {

            file.createNewFile();
        }else {

            boolean b = file.delete();
            System.out.printf(b ? "delete" : " delete failed");
        }

        if (Build.VERSION.SDK_INT >= 26) {

            Path path = Paths.get(filePath);
            Files.copy(inputStream, path);
        } else {

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[65535];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {

                fileOutputStream.write(buffer, 0, length);
            }

            fileOutputStream.flush();
            fileOutputStream.close();
        }

        return true;
    }

    public static String getFilePath() {

        return getFileDir() + File.separator + apkFileName;
    }
}
