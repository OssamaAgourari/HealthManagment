package com.example.health.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.health.model.Appointment;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentRepository {

    private final FirebaseFirestore firestore;
    private static final String COLLECTION = "appointments";
    private static final String TAG = "AppointmentRepository";

    public AppointmentRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Create a new appointment
    public void createAppointment(Appointment appointment,
                                  MutableLiveData<Boolean> successLiveData,
                                  MutableLiveData<String> errorLiveData) {

        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("patientId", appointment.getPatientId());
        appointmentData.put("patientName", appointment.getPatientName());
        appointmentData.put("doctorId", appointment.getDoctorId());
        appointmentData.put("doctorName", appointment.getDoctorName());
        appointmentData.put("specialty", appointment.getSpecialty());
        appointmentData.put("date", appointment.getDate());
        appointmentData.put("time", appointment.getTime());
        appointmentData.put("status", "pending");
        appointmentData.put("reason", appointment.getReason());
        appointmentData.put("consultationFee", appointment.getConsultationFee());
        appointmentData.put("createdAt", FieldValue.serverTimestamp());
        appointmentData.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION)
                .add(appointmentData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Appointment created: " + documentReference.getId());
                    successLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error creating appointment: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors de la création du rendez-vous");
                });
    }

    // Get appointments for a specific patient
    public void getPatientAppointments(String patientId,
                                       MutableLiveData<List<Appointment>> appointmentsLiveData,
                                       MutableLiveData<String> errorLiveData) {
        firestore.collection(COLLECTION)
                .whereEqualTo("patientId", patientId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Appointment> appointments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Appointment appointment = document.toObject(Appointment.class);
                        appointment.setId(document.getId());
                        appointments.add(appointment);
                    }
                    Log.d(TAG, "✅ Loaded " + appointments.size() + " appointments for patient");
                    appointmentsLiveData.setValue(appointments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading appointments: " + e.getMessage());
                    errorLiveData.setValue("Erreur de chargement des rendez-vous");
                });
    }

    // Cancel an appointment
    public void cancelAppointment(String appointmentId,
                                  MutableLiveData<Boolean> successLiveData,
                                  MutableLiveData<String> errorLiveData) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "cancelled");
        updates.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION)
                .document(appointmentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Appointment cancelled");
                    successLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cancelling appointment: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors de l'annulation");
                });
    }
}
