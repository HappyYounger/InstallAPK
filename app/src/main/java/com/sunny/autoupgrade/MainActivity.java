package com.sunny.autoupgrade;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.sunny.autoupgrade.network.APIService;
import com.sunny.autoupgrade.network.entities.AppConfigurationEntity;
import com.sunny.autoupgrade.utils.Configuration;
import com.sunny.autoupgrade.utils.StorageUtil;
import com.sunny.autoupgrade.utils.VersionUtil;

import java.io.File;
import java.io.IOException;
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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    @BindView(R.id.textView_version)
    TextView versionView;

    private int versionCode;
    private int remoteVersionCode;

    private static final String[] PERMISSIONS =
            {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //请求用户开放权限
        requestPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //用户点击取消按钮后
        if (resultCode == 0) {

        }
    }

    private void requestPermission() {

        if (!EasyPermissions.hasPermissions(this, PERMISSIONS)) {

            EasyPermissions.requestPermissions(this, "请求读写磁盘的权限", 100, PERMISSIONS);
        } else {

            afterHasPermission();
        }
    }

    private void afterHasPermission() {

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
            versionCode = BuildConfig.VERSION_CODE;

            //版本有更新
            if (remoteVersionCode > versionCode) {

                //下载APK
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

                        try {
                            //保存到制定目录中
                            if (StorageUtil.saveAPK(responseBody.byteStream())) {

                                //保存成功后，开始安装新版本APK
                                VersionUtil.installAPK(MainActivity.this,
                                        new File(StorageUtil.getFilePath()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
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

                        //获取到配置信息之后
                        afterGetAppConfig(appConfigurationEntity);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        //用户同意开放权限之后
        afterHasPermission();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

}
