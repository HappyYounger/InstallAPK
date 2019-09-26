package com.sunny.autoupgrade.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.sunny.autoupgrade.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class VersionUtil {

    public static String getVersionName(Context context) throws PackageManager.NameNotFoundException {

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionName;
    }

    public static int getVersionCode(Context context) throws PackageManager.NameNotFoundException {

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionCode;

    }

    public static String getAppName(Context context) throws PackageManager.NameNotFoundException {

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        int labelRes = applicationInfo.labelRes;
        return context.getResources().getString(labelRes);
    }

    public static int getVersionCode() {

        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName() {

        return BuildConfig.VERSION_NAME;
    }

    public static void installAPK(Context context, File apkFile) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data;
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data = FileProvider.getUriForFile(context, "com.thinker.member.bull.android_bull_member.fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        } else {

            data = Uri.fromFile(apkFile);

        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(data, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
