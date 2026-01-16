package com.example.health.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AppointmentNotificationWorker extends Worker {

    public static final String KEY_APPOINTMENT_ID = "appointment_id";
    public static final String KEY_DOCTOR_NAME = "doctor_name";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_MINUTES_BEFORE = "minutes_before";

    public AppointmentNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();

        String appointmentId = inputData.getString(KEY_APPOINTMENT_ID);
        String doctorName = inputData.getString(KEY_DOCTOR_NAME);
        String date = inputData.getString(KEY_DATE);
        String time = inputData.getString(KEY_TIME);
        int minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, 60);

        if (appointmentId == null || doctorName == null || date == null || time == null) {
            return Result.failure();
        }

        NotificationHelper.showUpcomingAppointmentReminder(
                getApplicationContext(),
                appointmentId,
                doctorName,
                date,
                time,
                minutesBefore
        );

        return Result.success();
    }
}
