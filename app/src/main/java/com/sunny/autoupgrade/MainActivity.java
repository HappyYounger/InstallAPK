package com.sunny.autoupgrade;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.CustomInstallListener;
import com.allenliu.versionchecklib.v2.callback.ForceUpdateListener;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.sunny.autoupgrade.network.APIService;
import com.sunny.autoupgrade.network.entities.AppConfigurationEntity;
import com.sunny.autoupgrade.utils.Configuration;
import com.sunny.autoupgrade.utils.StorageUtil;
import com.sunny.autoupgrade.utils.VersionUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    AutoInstaller installer;
    private final static String TAG = "AutoUpgrade";

    @BindView(R.id.textView_version)
    TextView versionView;

    private int versionCode;

    private int remoteVersionCode;

    private static final String[] PERMISSIONS =
            {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.REQUEST_DELETE_PACKAGES};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//                installer = AutoInstaller.getDefault(this);


        versionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                upgrade();


//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        String fileName = StorageUtil.getFilePath();
//                        installer.install(fileName);
//                    }
//                }).start();

//                updateApp();
//                VersionUtil.uninstall(MainActivity.this);

                uninstall();
//                finish();
            }

        });


        requestPermission();
//
//
//        installer.setOnStateChangedListener(new AutoInstaller.OnStateChangedListener() {
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//
//            @Override
//            public void onNeed2OpenService() {
//
//            }
//        });

//        updateApp("http://10.0.2.2:8080/api/v1/appConfig");
    }

    private void updateApp() {

        //需要在设备系统中的安全设置中开放：未知来源（unknown sources）
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(StorageUtil.getFilePath()));
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void uninstall() {

        VersionUtil.uninstall(this);
    }

    private void update1() {

        try {
            VersionUtil.install(this, getPackageName(), StorageUtil.getFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateApp(String requestUrl) {

        AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl(requestUrl)
                .request(new RequestVersionListener() {

                    @Nullable
                    @Override
                    public UIData onRequestVersionSuccess(DownloadBuilder downloadBuilder, String result) {

                        downloadBuilder.setDownloadUrl("http://10.0.2.2:8080/static/app.apk")
                                .setDirectDownload(true)
                                .setShowNotification(false)
                                .setShowDownloadingDialog(false)
                                .setShowDownloadFailDialog(false)
                                .setForceUpdateListener(new ForceUpdateListener() {
                                    @Override
                                    public void onShouldForceUpdate() {

                                        finish();
                                    }
                                }).setCustomDownloadInstallListener(new CustomInstallListener() {
                            @Override
                            public void install(Context context, Uri apk) {

                                System.out.println();
                            }
                        });


//                        return UIData.create().setDownloadUrl("http://10.0.2.2:8080/static/app.apk");

                        return null;
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {

                    }
                })
                .executeMission(this);
    }

    private void requestPermission() {

        if (!EasyPermissions.hasPermissions(this, PERMISSIONS)) {

            EasyPermissions.requestPermissions(this, "请求读写磁盘的权限", 100, PERMISSIONS);
        } else {

//            afterHasPermission();
        }
    }

    private void afterHasPermission() {
        versionCode = VersionUtil.getVersionCode();
        logcat(String.valueOf(versionCode));
        versionView.setText(String.valueOf(versionCode));

        requestAppConfig();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    private void afterGetAppConfig(AppConfigurationEntity appConfigurationEntity) {

        if (appConfigurationEntity.getCode() == 0) {

            remoteVersionCode = appConfigurationEntity.getResult().getVerCode();
            if (remoteVersionCode > versionCode) {

                downloadAPI(appConfigurationEntity.getResult().getUrl());
            }

        } else {

            Toast.makeText(this, appConfigurationEntity.getMsg(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadAPI(String path) {

        Retrofit retrofit = new Retrofit.Builder().baseUrl(Configuration.SERVER)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        APIService apiService = retrofit.create(APIService.class);

        Observable<ResponseBody> observable = apiService.downloadAPK(path);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                        if (saveAPK(responseBody.byteStream())) {


//                            String packageName = getPackageName();
//                            String fileName = StorageUtil.getFilePath();
//                            installer.install(fileName);
//
//                            try {
//                                VersionUtil.install(MainActivity.this, packageName, fileName);
//                            } catch (IOException e) {
//
//                                e.printStackTrace();
//                            }


//                            upgrade();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    private boolean saveAPK(InputStream inputStream) {

        try {

            return StorageUtil.saveAPK(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void inAppUpdate() {

        // Creates instance of the manager.
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
            }
        });
    }

    private void upgrade() {

        try {

//            java.lang.Process process = Runtime.getRuntime().exec("su");
            java.lang.Process process = Runtime.getRuntime().exec("su");

            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行pm install命令
//            String command = "pm install -r " + apkPath + "\n";

            String apkPath = StorageUtil.getFilePath();

            apkPath = Configuration.testFile;

            String command = "pm install -r " + apkPath + "\n";

            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
//            Runtime.getRuntime().exec("pm install -r " + apkPath.toString() + "\n");
            Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void logcat(String message) {

        Log.e(TAG, message);
    }


    private void requestAppConfig() {

        Retrofit retrofit =
                new Retrofit.Builder().baseUrl(Configuration.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();
        APIService apiService = retrofit.create(APIService.class);

        Observable<AppConfigurationEntity> observable =
                apiService.getAppConfig();


        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AppConfigurationEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AppConfigurationEntity appConfigurationEntity) {

                        afterGetAppConfig(appConfigurationEntity);
                    }

                    @Override
                    public void onError(Throwable e) {

                        logcat(e.getLocalizedMessage());
//                        Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        //       afterHasPermission();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

}
