package com.example.health.utils;

import android.util.Log;

import com.example.health.model.Doctor;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SampleDataHelper {

    private static final String TAG = "SampleDataHelper";

    public static void addSampleDoctors() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Sample Doctor 1
        Map<String, Object> doctor1 = new HashMap<>();
        doctor1.put("firstName", "Marie");
        doctor1.put("lastName", "Dubois");
        doctor1.put("specialty", "Cardiologue");
        doctor1.put("photoUrl", "");
        doctor1.put("address", "15 Avenue des Champs-Élysées");
        doctor1.put("city", "Paris");
        doctor1.put("phone", "+33 1 42 25 63 00");
        doctor1.put("email", "marie.dubois@health.com");
        doctor1.put("experience", 15);
        doctor1.put("consultationFee", 80.0);
        doctor1.put("rating", 4.8);
        doctor1.put("totalReviews", 142);
        doctor1.put("description", "Spécialisée en cardiologie interventionnelle avec plus de 15 ans d'expérience.");
        doctor1.put("available", true);
        doctor1.put("createdAt", System.currentTimeMillis());

        // Sample Doctor 2
        Map<String, Object> doctor2 = new HashMap<>();
        doctor2.put("firstName", "Jean");
        doctor2.put("lastName", "Martin");
        doctor2.put("specialty", "Dermatologue");
        doctor2.put("photoUrl", "");
        doctor2.put("address", "28 Rue de Rivoli");
        doctor2.put("city", "Paris");
        doctor2.put("phone", "+33 1 42 60 34 56");
        doctor2.put("email", "jean.martin@health.com");
        doctor2.put("experience", 10);
        doctor2.put("consultationFee", 60.0);
        doctor2.put("rating", 4.5);
        doctor2.put("totalReviews", 98);
        doctor2.put("description", "Expert en dermatologie esthétique et traitement de l'acné.");
        doctor2.put("available", true);
        doctor2.put("createdAt", System.currentTimeMillis());

        // Sample Doctor 3
        Map<String, Object> doctor3 = new HashMap<>();
        doctor3.put("firstName", "Sophie");
        doctor3.put("lastName", "Leroy");
        doctor3.put("specialty", "Pédiatre");
        doctor3.put("photoUrl", "");
        doctor3.put("address", "45 Boulevard Saint-Germain");
        doctor3.put("city", "Paris");
        doctor3.put("phone", "+33 1 43 26 92 84");
        doctor3.put("email", "sophie.leroy@health.com");
        doctor3.put("experience", 12);
        doctor3.put("consultationFee", 55.0);
        doctor3.put("rating", 4.9);
        doctor3.put("totalReviews", 215);
        doctor3.put("description", "Pédiatre passionnée, spécialisée dans le suivi de la croissance et le développement de l'enfant.");
        doctor3.put("available", true);
        doctor3.put("createdAt", System.currentTimeMillis());

        // Sample Doctor 4
        Map<String, Object> doctor4 = new HashMap<>();
        doctor4.put("firstName", "Pierre");
        doctor4.put("lastName", "Bernard");
        doctor4.put("specialty", "Généraliste");
        doctor4.put("photoUrl", "");
        doctor4.put("address", "12 Rue de la Paix");
        doctor4.put("city", "Lyon");
        doctor4.put("phone", "+33 4 72 77 40 00");
        doctor4.put("email", "pierre.bernard@health.com");
        doctor4.put("experience", 20);
        doctor4.put("consultationFee", 25.0);
        doctor4.put("rating", 4.6);
        doctor4.put("totalReviews", 180);
        doctor4.put("description", "Médecin généraliste expérimenté, consultations pour toute la famille.");
        doctor4.put("available", true);
        doctor4.put("createdAt", System.currentTimeMillis());

        // Sample Doctor 5
        Map<String, Object> doctor5 = new HashMap<>();
        doctor5.put("firstName", "Claire");
        doctor5.put("lastName", "Petit");
        doctor5.put("specialty", "Ophtalmologue");
        doctor5.put("photoUrl", "");
        doctor5.put("address", "8 Avenue Victor Hugo");
        doctor5.put("city", "Marseille");
        doctor5.put("phone", "+33 4 91 33 20 00");
        doctor5.put("email", "claire.petit@health.com");
        doctor5.put("experience", 8);
        doctor5.put("consultationFee", 70.0);
        doctor5.put("rating", 4.7);
        doctor5.put("totalReviews", 95);
        doctor5.put("description", "Spécialiste en chirurgie réfractive et traitement des pathologies de la rétine.");
        doctor5.put("available", true);
        doctor5.put("createdAt", System.currentTimeMillis());

        // Add all doctors to Firestore
        db.collection("doctors").add(doctor1)
                .addOnSuccessListener(ref -> Log.d(TAG, "✅ Doctor 1 added: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Error adding doctor 1: " + e.getMessage()));

        db.collection("doctors").add(doctor2)
                .addOnSuccessListener(ref -> Log.d(TAG, "✅ Doctor 2 added: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Error adding doctor 2: " + e.getMessage()));

        db.collection("doctors").add(doctor3)
                .addOnSuccessListener(ref -> Log.d(TAG, "✅ Doctor 3 added: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Error adding doctor 3: " + e.getMessage()));

        db.collection("doctors").add(doctor4)
                .addOnSuccessListener(ref -> Log.d(TAG, "✅ Doctor 4 added: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Error adding doctor 4: " + e.getMessage()));

        db.collection("doctors").add(doctor5)
                .addOnSuccessListener(ref -> Log.d(TAG, "✅ Doctor 5 added: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Error adding doctor 5: " + e.getMessage()));

        Log.d(TAG, "📤 Adding 5 sample doctors to Firestore...");
    }
}
