package com.example.health.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.health.model.Doctor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {

    private final FirebaseFirestore firestore;
    private static final String COLLECTION = "doctors";
    private static final String TAG = "DoctorRepository";

    public interface DoctorsCallback {
        void onSuccess(List<Doctor> doctors);
        void onError(String error);
    }

    public DoctorRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Get all doctors with callback (fast - uses cache first)
    public void getAllDoctors(DoctorsCallback callback) {
        // Try cache first for instant load
        firestore.collection(COLLECTION)
                .get(Source.CACHE)
                .addOnSuccessListener(cacheSnapshot -> {
                    if (!cacheSnapshot.isEmpty()) {
                        List<Doctor> doctors = parseDoctors(cacheSnapshot);
                        Log.d(TAG, "⚡ Loaded " + doctors.size() + " doctors from cache");
                        callback.onSuccess(doctors);
                    }
                    // Then fetch fresh data from server
                    fetchFromServer(callback);
                })
                .addOnFailureListener(e -> {
                    // No cache, fetch from server
                    fetchFromServer(callback);
                });
    }

    private void fetchFromServer(DoctorsCallback callback) {
        firestore.collection(COLLECTION)
                .get(Source.SERVER)
                .addOnSuccessListener(serverSnapshot -> {
                    List<Doctor> doctors = parseDoctors(serverSnapshot);
                    Log.d(TAG, "✅ Loaded " + doctors.size() + " doctors from server");
                    callback.onSuccess(doctors);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading doctors: " + e.getMessage());
                    callback.onError("Erreur de chargement des médecins");
                });
    }

    private List<Doctor> parseDoctors(com.google.firebase.firestore.QuerySnapshot snapshot) {
        List<Doctor> doctors = new ArrayList<>();
        for (QueryDocumentSnapshot document : snapshot) {
            Doctor doctor = document.toObject(Doctor.class);
            doctor.setId(document.getId());
            doctors.add(doctor);
        }
        return doctors;
    }

    // Legacy method for compatibility
    public void getAllDoctors(MutableLiveData<List<Doctor>> doctorsLiveData,
                              MutableLiveData<String> errorLiveData) {
        getAllDoctors(new DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                doctorsLiveData.setValue(doctors);
            }
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    // Get doctor by ID
    public void getDoctorById(String doctorId,
                              MutableLiveData<Doctor> doctorLiveData,
                              MutableLiveData<String> errorLiveData) {
        firestore.collection(COLLECTION)
                .document(doctorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Doctor doctor = documentSnapshot.toObject(Doctor.class);
                        if (doctor != null) {
                            doctor.setId(documentSnapshot.getId());
                        }
                        Log.d(TAG, "✅ Doctor loaded: " + doctor.getFullName());
                        doctorLiveData.setValue(doctor);
                    } else {
                        errorLiveData.setValue("Médecin introuvable");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading doctor: " + e.getMessage());
                    errorLiveData.setValue("Erreur de chargement du médecin");
                });
    }

    // Search doctors by specialty
    public void searchBySpecialty(String specialty,
                                  MutableLiveData<List<Doctor>> doctorsLiveData,
                                  MutableLiveData<String> errorLiveData) {
        firestore.collection(COLLECTION)
                .whereEqualTo("specialty", specialty)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Doctor> doctors = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Doctor doctor = document.toObject(Doctor.class);
                        doctor.setId(document.getId());
                        doctors.add(doctor);
                    }
                    Log.d(TAG, "✅ Found " + doctors.size() + " doctors in " + specialty);
                    doctorsLiveData.setValue(doctors);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error searching doctors: " + e.getMessage());
                    errorLiveData.setValue("Erreur de recherche");
                });
    }
}
