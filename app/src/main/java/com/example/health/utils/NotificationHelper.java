package com.example.health.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.health.MainActivity;
import com.example.health.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "appointment_reminders";
    public static final String CHANNEL_NAME = "Rappels de rendez-vous";
    public static final String CHANNEL_DESCRIPTION = "Notifications pour les rappels de rendez-vous";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showAppointmentReminder(Context context, String appointmentId,
                                                String doctorName, String date, String time) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("navigate_to", "appointments");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                appointmentId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Rappel de rendez-vous";
        String message = String.format("Vous avez rendez-vous avec %s le %s a %s", doctorName, date, time);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_medical_information_24)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(appointmentId.hashCode(), builder.build());
        } catch (SecurityException e) {
            // Notification permission not granted
            e.printStackTrace();
        }
    }

    public static void showUpcomingAppointmentReminder(Context context, String appointmentId,
                                                        String doctorName, String date, String time,
                                                        int minutesBefore) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("navigate_to", "appointments");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (appointmentId + "_" + minutesBefore).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Rappel de rendez-vous";
        String timeText;
        if (minutesBefore >= 60) {
            int hours = minutesBefore / 60;
            timeText = hours + " heure" + (hours > 1 ? "s" : "");
        } else {
            timeText = minutesBefore + " minutes";
        }
        String message = String.format("Votre rendez-vous avec %s est dans %s (%s a %s)",
                doctorName, timeText, date, time);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_medical_information_24)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify((appointmentId + "_" + minutesBefore).hashCode(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void cancelNotification(Context context, String appointmentId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(appointmentId.hashCode());
        notificationManager.cancel((appointmentId + "_60").hashCode());
        notificationManager.cancel((appointmentId + "_1440").hashCode());
    }
}
