package com.example.health.viewModels.auth;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterViewModel extends ViewModel {
    private final String TAG = "RegisterViewModel";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private MutableLiveData<String> email = new MutableLiveData<>("");
    private MutableLiveData<String> password = new MutableLiveData<>("");
    public MutableLiveData<Patient> userLiveDate = new MutableLiveData<>(new Patient());
    private MutableLiveData<String> erreurMessage = new MutableLiveData<>("");
    private MutableLiveData<Boolean> isRegistered = new MutableLiveData<>(false);

    // Constructeur
    public RegisterViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Getters et Setters

    public String getTAG() {
        return TAG;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public void setAuth(FirebaseAuth auth) {
        this.auth = auth;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public void setEmail(MutableLiveData<String> email) {
        this.email = email;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public void setPassword(MutableLiveData<String> password) {
        this.password = password;
    }

    public MutableLiveData<Patient> getUserLiveDate() {
        return userLiveDate;
    }

    public void setUserLiveDate(MutableLiveData<Patient> userLiveDate) {
        this.userLiveDate = userLiveDate;
    }

    public MutableLiveData<String> getErreurMessage() {
        return erreurMessage;
    }

    public void setErreurMessage(MutableLiveData<String> erreurMessage) {
        this.erreurMessage = erreurMessage;
    }

    public MutableLiveData<Boolean> getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(MutableLiveData<Boolean> isRegistered) {
        this.isRegistered = isRegistered;
    }

    public void register() {
        String emailString = email.getValue();
        String passwordString = password.getValue();
        Patient patient = userLiveDate.getValue();

        // Validation
        if (emailString == null || emailString.isEmpty()) {
            erreurMessage.setValue("L'email est requis");
            return;
        }
        if (passwordString == null || passwordString.isEmpty()) {
            erreurMessage.setValue("Le mot de passe est requis");
            return;
        }
        if (passwordString.length() < 6) {
            erreurMessage.setValue("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        // Créer un compte Firebase Auth
        auth.createUserWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            Log.d(TAG, "✅ Firebase Auth account created. UID: " + uid);

                            // Créer le document utilisateur dans Firestore
                            createUserInFirestore(uid, patient, emailString);
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Erreur inconnue";
                        Log.e(TAG, "❌ Firebase Auth registration failed: " + error);

                        // Messages d'erreur en français
                        if (error.contains("already in use")) {
                            erreurMessage.setValue("Cet email est déjà utilisé");
                        } else if (error.contains("invalid-email")) {
                            erreurMessage.setValue("Email invalide");
                        } else if (error.contains("weak-password")) {
                            erreurMessage.setValue("Mot de passe trop faible");
                        } else {
                            erreurMessage.setValue("Erreur d'inscription: " + error);
                        }
                        isRegistered.setValue(false);
                    }
                });
    }

    private void createUserInFirestore(String uid, Patient patient, String emailString) {
        // Préparer les données utilisateur
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", emailString);
        userData.put("firstName", patient.getFirstName() != null ? patient.getFirstName() : "");
        userData.put("lastName", patient.getLastName() != null ? patient.getLastName() : "");
        userData.put("phone", patient.getPhone() != null ? patient.getPhone() : "");
        userData.put("birthDate", patient.getBirthDate() != null ? patient.getBirthDate() : "");
        userData.put("gender", patient.getGender() != null ? patient.getGender() : "");
        userData.put("createdAt", System.currentTimeMillis());

        Log.d(TAG, "Creating user in Firestore...");

        // Sauvegarder dans Firestore
        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User created in Firestore successfully!");
                    isRegistered.setValue(true);
                    erreurMessage.setValue("");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error creating user in Firestore: " + e.getMessage());
                    erreurMessage.setValue("Erreur lors de la création du profil: " + e.getMessage());
                    isRegistered.setValue(false);

                    // Supprimer le compte Firebase Auth si la création Firestore échoue
                    if (auth.getCurrentUser() != null) {
                        auth.getCurrentUser().delete();
                    }
                });
    }
}
