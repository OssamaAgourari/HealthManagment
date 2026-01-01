package com.example.health.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import com.example.health.model.PatientForm;

public class PatientRepository {

    private final FirebaseFirestore firestore;
    private static final String COLLECTION = "patient_forms";

    public PatientRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void submitForm(PatientForm form, MutableLiveData<Boolean> result) {
        firestore.collection(COLLECTION)
                .add(form)
                .addOnSuccessListener(doc -> result.setValue(true))
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e("PatientRepository", "Error submitting form : ", e);
                });
    }
}

