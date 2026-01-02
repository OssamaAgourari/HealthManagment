package com.example.health.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.health.model.Doctor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {

    private final FirebaseFirestore firestore;
    private static final String COLLECTION = "doctors";
    private static final String TAG = "DoctorRepository";

    public DoctorRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Get all doctors
    public void getAllDoctors(MutableLiveData<List<Doctor>> doctorsLiveData,
                              MutableLiveData<String> errorLiveData) {
        firestore.collection(COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Doctor> doctors = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Doctor doctor = document.toObject(Doctor.class);
                        doctor.setId(document.getId());
                        doctors.add(doctor);
                    }
                    Log.d(TAG, "✅ Loaded " + doctors.size() + " doctors");
                    doctorsLiveData.setValue(doctors);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading doctors: " + e.getMessage());
                    errorLiveData.setValue("Erreur de chargement des médecins");
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
