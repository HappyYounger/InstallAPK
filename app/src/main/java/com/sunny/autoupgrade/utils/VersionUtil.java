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

        intent.setDataAndType(data, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void install(Context context, String packageName, String apkPath) throws IOException {

        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        OutputStream out = session.openWrite(packageName, 0, -1);
        File file = new File(apkPath);
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[65536];
        int length;
        while ((length = fis.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        session.fsync(out);
        fis.close();
        out.close();

        session.commit(createIntentSender(context, sessionId));
    }

    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent("ACTION_INSTALL_COMPLETE"),
                0);
        return pendingIntent.getIntentSender();
    }

    public static void uninstall(Context context){

        String appPackage = context.getPackageName();

        Intent intent = new Intent(context, context.getClass());
        PendingIntent sender = PendingIntent.getActivity(context, 0, intent, FLAG_ONE_SHOT);
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        packageInstaller.uninstall(appPackage, sender.getIntentSender());
    }
}
