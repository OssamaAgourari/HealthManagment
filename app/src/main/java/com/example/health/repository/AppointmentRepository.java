package com.example.health.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.health.model.Appointment;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppointmentRepository {

    private final FirebaseFirestore firestore;
    private static final String COLLECTION = "appointments";
    private static final String TAG = "AppointmentRepository";

    public AppointmentRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Check if a time slot is available for a specific doctor on a specific date
    public void checkSlotAvailability(String doctorId, String date, String time,
                                      MutableLiveData<Boolean> isAvailable,
                                      MutableLiveData<String> errorLiveData) {
        firestore.collection(COLLECTION)
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", date)
                .whereEqualTo("time", time)
                .whereIn("status", List.of("pending", "confirmed"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // If no appointments found, slot is available
                    isAvailable.setValue(queryDocumentSnapshots.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking slot availability: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors de la verification du creneau");
                    isAvailable.setValue(false);
                });
    }

    // Get all booked time slots for a specific doctor on a specific date
    public void getBookedTimeSlots(String doctorId, String date,
                                   MutableLiveData<Set<String>> bookedSlots,
                                   MutableLiveData<String> errorLiveData) {
        firestore.collection(COLLECTION)
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> slots = new HashSet<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String status = document.getString("status");
                        // Only consider pending or confirmed appointments
                        if ("pending".equals(status) || "confirmed".equals(status)) {
                            String time = document.getString("time");
                            if (time != null) {
                                slots.add(time);
                            }
                        }
                    }
                    Log.d(TAG, "Found " + slots.size() + " booked slots for doctor " + doctorId + " on " + date);
                    bookedSlots.setValue(slots);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting booked slots: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors du chargement des creneaux");
                    bookedSlots.setValue(new HashSet<>());
                });
    }

    // Create a new appointment with slot verification
    public void createAppointmentWithCheck(Appointment appointment,
                                           MutableLiveData<Boolean> successLiveData,
                                           MutableLiveData<String> errorLiveData) {
        createAppointmentWithCheck(appointment, successLiveData, errorLiveData, null);
    }

    // Create a new appointment with slot verification and return the created appointment
    public void createAppointmentWithCheck(Appointment appointment,
                                           MutableLiveData<Boolean> successLiveData,
                                           MutableLiveData<String> errorLiveData,
                                           MutableLiveData<Appointment> createdAppointmentLiveData) {

        // First check if the slot is available
        firestore.collection(COLLECTION)
                .whereEqualTo("doctorId", appointment.getDoctorId())
                .whereEqualTo("date", appointment.getDate())
                .whereEqualTo("time", appointment.getTime())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if any existing appointment has pending or confirmed status
                    boolean slotTaken = false;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if ("pending".equals(status) || "confirmed".equals(status)) {
                            slotTaken = true;
                            break;
                        }
                    }

                    if (slotTaken) {
                        errorLiveData.setValue("Ce creneau est deja reserve. Veuillez en choisir un autre.");
                        successLiveData.setValue(false);
                    } else {
                        // Slot is available, create the appointment
                        createAppointment(appointment, successLiveData, errorLiveData, createdAppointmentLiveData);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking slot: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors de la verification du creneau");
                });
    }

    // Create a new appointment
    public void createAppointment(Appointment appointment,
                                  MutableLiveData<Boolean> successLiveData,
                                  MutableLiveData<String> errorLiveData) {
        createAppointment(appointment, successLiveData, errorLiveData, null);
    }

    // Create a new appointment with callback to get the created appointment
    public void createAppointment(Appointment appointment,
                                  MutableLiveData<Boolean> successLiveData,
                                  MutableLiveData<String> errorLiveData,
                                  MutableLiveData<Appointment> createdAppointmentLiveData) {

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
                    Log.d(TAG, "Appointment created: " + documentReference.getId());
                    appointment.setId(documentReference.getId());
                    if (createdAppointmentLiveData != null) {
                        createdAppointmentLiveData.setValue(appointment);
                    }
                    successLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating appointment: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors de la creation du rendez-vous");
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
                    Log.d(TAG, "Loaded " + appointments.size() + " appointments for patient");
                    appointmentsLiveData.setValue(appointments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading appointments: " + e.getMessage());
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
                    Log.d(TAG, "Appointment cancelled");
                    successLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cancelling appointment: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors de l'annulation");
                });
    }
}
