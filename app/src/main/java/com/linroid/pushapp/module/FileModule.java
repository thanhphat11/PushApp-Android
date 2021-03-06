/*
 * Copyright (c) linroid 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linroid.pushapp.module;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.linroid.pushapp.module.identifier.AccountSavedFile;
import com.linroid.pushapp.module.identifier.PackageDownloadDir;
import com.linroid.pushapp.module.identifier.DataCacheDir;
import com.linroid.pushapp.module.identifier.HttpCacheDir;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

/**
 * Created by linroid on 7/24/15.
 */
@Module
public class FileModule {
    @HttpCacheDir
    @Provides
    @Singleton
    File provideHttpCacheDir(Application app) {
        return app.getCacheDir();
    }

    @DataCacheDir
    @Provides
    @Singleton
    File provideDataCacheDir(Application app) {
        return app.getCacheDir();
    }

    @PackageDownloadDir
    @Provides
    @Singleton
    File providePackageDownloadDir(Context context) {
        String state = Environment.getExternalStorageState();
        File rootDir;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            rootDir = new File(context.getExternalCacheDir(), "apk/");
        } else {
            rootDir = new File(context.getCacheDir(), "apk/");

        }
        if(!rootDir.exists()){
            rootDir.mkdirs();
        }
        return rootDir;
    }
    @AccountSavedFile
    @Provides
    @Singleton
    File provideAccountFile(Context context) {
        File file = new File(context.getFilesDir(), "account.json");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Timber.e("创建account.json失败");
            }
        }
        return file;
    }
}
