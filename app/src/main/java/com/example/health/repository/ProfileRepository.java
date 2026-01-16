package com.example.health.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.health.model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public ProfileRepository() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void getProfile(MutableLiveData<Patient> patientLiveData,
                           MutableLiveData<String> errorMessage,
                           MutableLiveData<Boolean> isLoading) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("Utilisateur non connecte");
            return;
        }

        isLoading.setValue(true);

        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Patient patient = new Patient();
                        patient.setUid(user.getUid());
                        patient.setEmail(user.getEmail());
                        patient.setFirstName(documentSnapshot.getString("firstName"));
                        patient.setLastName(documentSnapshot.getString("lastName"));
                        patient.setPhone(documentSnapshot.getString("phone"));
                        patient.setBirthDate(documentSnapshot.getString("birthDate"));
                        patient.setGender(documentSnapshot.getString("gender"));

                        Long createdAt = documentSnapshot.getLong("createdAt");
                        if (createdAt != null) {
                            patient.setCreatedAt(createdAt);
                        }

                        patientLiveData.setValue(patient);
                    } else {
                        errorMessage.setValue("Profil non trouve");
                    }
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors du chargement du profil: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public void updateProfile(Patient patient,
                              MutableLiveData<Boolean> updateSuccess,
                              MutableLiveData<String> errorMessage,
                              MutableLiveData<Boolean> isLoading) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("Utilisateur non connecte");
            return;
        }

        isLoading.setValue(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", patient.getFirstName());
        updates.put("lastName", patient.getLastName());
        updates.put("phone", patient.getPhone());
        updates.put("birthDate", patient.getBirthDate());
        updates.put("gender", patient.getGender());

        firestore.collection("users")
                .document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    updateSuccess.setValue(true);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors de la mise a jour: " + e.getMessage());
                    updateSuccess.setValue(false);
                    isLoading.setValue(false);
                });
    }
}
