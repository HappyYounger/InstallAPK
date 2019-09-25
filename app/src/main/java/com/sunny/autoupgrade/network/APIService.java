package com.sunny.autoupgrade.network;

import com.sunny.autoupgrade.network.entities.AppConfigurationEntity;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface APIService {

    @GET("appConfig")
    Observable<AppConfigurationEntity> getAppConfig();


    @GET("/static/{path}")
    Observable<ResponseBody> downloadAPK(@Path("path") String path);

//    @GET("/{path}")
//    Observable<ResponseBody> downloadAPK(@Path("path") String path);
}
