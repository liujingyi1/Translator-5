package com.android.face.domain;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerApiService {

    @Headers("Content-Type:application/json; charset=utf-8")
    @POST("v1/device/delete")
    Observable<ServerResponse> deleteDevice(
            @Body RequestBody body
    );

    @Headers("Content-Type:application/json; charset=utf-8")
    @POST("v1/device/canRegister")
    Observable<ServerResponse> canRegister(
            @Body RequestBody requestBody
    );

    @Headers("Content-Type:application/json; charset=utf-8")
    @POST("v1/device/register")
    Observable<ServerResponse> registerDevice(
            @Body Device deviceNo
    );

    @Headers("Content-Type:application/json; charset=utf-8")
    @POST("v1/device/update")
    Observable<ServerResponse> updateDevice(
            @Body Device deviceNo
    );
}
