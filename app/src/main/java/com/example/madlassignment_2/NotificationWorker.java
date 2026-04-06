package com.example.madlassignment_2;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Roll No 48 % 3 = 0 -> "Review your saved notes today"
        AppNotificationManager.sendNotification(getApplicationContext(), "Reminder", "Review your saved notes today");
        return Result.success();
    }
}
