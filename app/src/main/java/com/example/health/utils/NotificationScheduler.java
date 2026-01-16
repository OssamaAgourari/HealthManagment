package com.example.health.utils;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.health.model.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private static final String TAG_PREFIX = "appointment_notification_";

    public static void scheduleAppointmentReminders(Context context, Appointment appointment) {
        if (appointment == null || appointment.getId() == null) {
            return;
        }

        long appointmentTimeMillis = getAppointmentTimeInMillis(appointment.getDate(), appointment.getTime());
        if (appointmentTimeMillis <= 0) {
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();

        // Schedule 24-hour reminder (1440 minutes)
        scheduleReminder(context, appointment, appointmentTimeMillis, currentTimeMillis, 1440);

        // Schedule 1-hour reminder (60 minutes)
        scheduleReminder(context, appointment, appointmentTimeMillis, currentTimeMillis, 60);
    }

    private static void scheduleReminder(Context context, Appointment appointment,
                                          long appointmentTimeMillis, long currentTimeMillis,
                                          int minutesBefore) {
        long reminderTimeMillis = appointmentTimeMillis - (minutesBefore * 60 * 1000L);

        // Only schedule if the reminder time is in the future
        if (reminderTimeMillis <= currentTimeMillis) {
            return;
        }

        long delayMillis = reminderTimeMillis - currentTimeMillis;

        Data inputData = new Data.Builder()
                .putString(AppointmentNotificationWorker.KEY_APPOINTMENT_ID, appointment.getId())
                .putString(AppointmentNotificationWorker.KEY_DOCTOR_NAME, appointment.getDoctorName())
                .putString(AppointmentNotificationWorker.KEY_DATE, appointment.getDate())
                .putString(AppointmentNotificationWorker.KEY_TIME, appointment.getTime())
                .putInt(AppointmentNotificationWorker.KEY_MINUTES_BEFORE, minutesBefore)
                .build();

        String tag = TAG_PREFIX + appointment.getId() + "_" + minutesBefore;

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AppointmentNotificationWorker.class)
                .setInputData(inputData)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }

    public static void cancelAppointmentReminders(Context context, String appointmentId) {
        if (appointmentId == null) {
            return;
        }

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(TAG_PREFIX + appointmentId + "_1440");
        workManager.cancelAllWorkByTag(TAG_PREFIX + appointmentId + "_60");

        // Also cancel any shown notifications
        NotificationHelper.cancelNotification(context, appointmentId);
    }

    public static long getAppointmentTimeInMillis(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) {
            return -1;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = dateFormat.parse(dateStr);

            if (date == null) {
                return -1;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            String[] timeParts = timeStr.split(":");
            if (timeParts.length >= 2) {
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }

            return calendar.getTimeInMillis();
        } catch (ParseException | NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean isAppointmentInFuture(String dateStr, String timeStr) {
        long appointmentTime = getAppointmentTimeInMillis(dateStr, timeStr);
        return appointmentTime > System.currentTimeMillis();
    }
}
