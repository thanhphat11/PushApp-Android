package com.linroid.pushapp.module;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.linroid.pushapp.BuildConfig;
import com.linroid.pushapp.Constants;
import com.linroid.pushapp.R;
import com.linroid.pushapp.module.identifier.DataCacheDir;
import com.linroid.pushapp.module.identifier.HttpCacheDir;
import com.linroid.pushapp.util.StringPreference;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.DiskLruCache;
import com.squareup.okhttp.internal.io.FileSystem;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hugo.weaving.DebugLog;
import io.realm.RealmObject;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import timber.log.Timber;

/**
 * Created by linroid on 7/24/15.
 */
@Module
public class NetworkModule {
    @Provides
    @Singleton
    OkHttpClient provideOkHttp(Cache cache) {
        OkHttpClient okHttp = new OkHttpClient();
        okHttp.setCache(cache);
        okHttp.setConnectTimeout(30, TimeUnit.SECONDS);
        okHttp.setReadTimeout(30, TimeUnit.SECONDS);
        okHttp.setWriteTimeout(30, TimeUnit.SECONDS);
        okHttp.networkInterceptors().add(new StethoInterceptor());
        return okHttp;
    }

    @Provides
    @Singleton
    Cache provideHttpCache(@HttpCacheDir File httpCacheDir) {
        //100M;
        int cacheSize = 1024 * 1024 * 100;
        try {
            return new Cache(httpCacheDir, cacheSize);
        } catch (Exception e) {
            Timber.e("install http cache false");
        }

        return null;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
    }

    @Provides
    @Singleton
    DiskLruCache provideDataCache(@DataCacheDir File cacheDir) {
        //10M
        return DiskLruCache.create(FileSystem.SYSTEM,
                cacheDir,
                BuildConfig.VERSION_CODE,
                1,
                1024 * 1024 * 10);
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter(Gson gson, OkHttpClient okHttpClient,
                                   @Named(Constants.SP_TOKEN) final StringPreference token,
                                   ErrorHandler errorHandler) {
        return new RestAdapter.Builder()
                .setErrorHandler(errorHandler)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setEndpoint(BuildConfig.ENDPOINT)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (!TextUtils.isEmpty(token.getValue())) {
                            request.addHeader(Constants.AUTH_HEADER, token.getValue());
                        }
                    }
                })
                .build();
    }

    @Provides
    @Singleton
    ErrorHandler provideErrorHandler(final Context ctx) {
        return new ErrorHandler() {
            @Override
            @DebugLog
            public Throwable handleError(RetrofitError retrofitError) {
                Timber.e(retrofitError, "请求出现错误:%s", retrofitError.getUrl());
                RetrofitError.Kind kind = retrofitError.getKind();
                String message;
                if (RetrofitError.Kind.NETWORK.equals(kind)) {
                    message = ctx.getString(R.string.network_error);
                } else if (RetrofitError.Kind.HTTP.equals(kind)) {
                    message = ctx.getString(R.string.http_error);
                } else if (RetrofitError.Kind.CONVERSION.equals(kind)) {
                    message = ctx.getString(R.string.conversion_error);
                } else {
                    message = ctx.getString(R.string.unexpected_error);
                }
                return new Exception(message);
            }
        };
    }

}
