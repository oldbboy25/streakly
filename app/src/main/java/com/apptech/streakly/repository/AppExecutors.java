package com.apptech.streakly.repository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static final ExecutorService DISK_IO = Executors.newSingleThreadExecutor();

    public static ExecutorService diskIO() {
        return DISK_IO;
    }
}
