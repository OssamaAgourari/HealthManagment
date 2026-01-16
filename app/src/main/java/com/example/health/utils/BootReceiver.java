package com.example.health.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.health.model.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAppointmentNotifications(context);
        }
    }

    private void rescheduleAppointmentNotifications(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("appointments")
                .whereEqualTo("patientId", user.getUid())
                .whereIn("status", java.util.Arrays.asList("pending", "confirmed"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Appointment appointment = document.toObject(Appointment.class);
                        appointment.setId(document.getId());

                        if (NotificationScheduler.isAppointmentInFuture(
                                appointment.getDate(), appointment.getTime())) {
                            NotificationScheduler.scheduleAppointmentReminders(context, appointment);
                        }
                    }
                });
    }
}
