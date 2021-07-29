/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ckc.photopicker;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 线程调度
 * ckc 200105
 * <p>
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
public class AppExecutors {

    private static final AppExecutors ourInstance = new AppExecutors();

    public static AppExecutors getInstance() {
        return ourInstance;
    }

    private AppExecutors() {//Executor mDiskExecutor, Executor mNetworkExecutor, Executor mMainThreadExecutor
    }

    private final int THREAD_COUNT = 3;

    private Executor mDiskExecutor;

    private Executor mNetworkExecutor;

    private Executor mMainThreadExecutor;

    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 获得磁盘线程执行器
     */
    public Executor getDiskExecutor() {
        if (mDiskExecutor == null) {
            mDiskExecutor = Executors.newSingleThreadExecutor();
        }
        return mDiskExecutor;
    }

    /**
     * 获得网络线程执行器
     */
    public Executor getNetworkExecutor() {
        if (mNetworkExecutor == null) {
            mNetworkExecutor = Executors.newCachedThreadPool();
        }
        return mNetworkExecutor;
    }

    /**
     * 获得主线程执行器
     */
    public Executor getMainThreadExecutor() {
        if (mMainThreadExecutor == null) {
            mMainThreadExecutor = new MainThreadExecutor();
        }
        return mMainThreadExecutor;
    }

    /**
     * 主线程执行器类
     */
    public static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
