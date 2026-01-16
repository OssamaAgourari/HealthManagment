package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    private final MutableLiveData<String> fullName = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> phone = new MutableLiveData<>();
    private final MutableLiveData<String> memberSince = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();

    public ProfileViewModel() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void loadProfileData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("Utilisateur non connecte");
            return;
        }

        isLoading.setValue(true);

        // Load email from Firebase Auth
        String userEmail = user.getEmail();
        email.setValue(userEmail != null ? userEmail : "Email non disponible");

        // Load member since
        long creationTimestamp = user.getMetadata() != null ?
                user.getMetadata().getCreationTimestamp() : System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        memberSince.setValue(sdf.format(new Date(creationTimestamp)));

        // Load full name and phone from Firestore
        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String userPhone = documentSnapshot.getString("phone");

                        // Build full name
                        StringBuilder nameBuilder = new StringBuilder();
                        if (firstName != null && !firstName.trim().isEmpty()) {
                            nameBuilder.append(firstName.trim());
                        }
                        if (lastName != null && !lastName.trim().isEmpty()) {
                            if (nameBuilder.length() > 0) {
                                nameBuilder.append(" ");
                            }
                            nameBuilder.append(lastName.trim());
                        }

                        String name = nameBuilder.toString();
                        if (name.isEmpty()) {
                            // Fallback to email prefix
                            if (userEmail != null) {
                                name = userEmail.split("@")[0];
                            } else {
                                name = "Utilisateur";
                            }
                        }
                        fullName.setValue(name);

                        // Phone
                        if (userPhone != null && !userPhone.trim().isEmpty()) {
                            phone.setValue(userPhone);
                        } else {
                            phone.setValue("Non renseigne");
                        }
                    } else {
                        // Fallback to Firebase Auth
                        String displayName = user.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            fullName.setValue(displayName);
                        } else if (userEmail != null) {
                            fullName.setValue(userEmail.split("@")[0]);
                        } else {
                            fullName.setValue("Utilisateur");
                        }
                        phone.setValue("Non renseigne");
                    }
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors du chargement du profil");
                    isLoading.setValue(false);

                    // Fallback
                    String displayName = user.getDisplayName();
                    if (displayName != null && !displayName.isEmpty()) {
                        fullName.setValue(displayName);
                    } else if (userEmail != null) {
                        fullName.setValue(userEmail.split("@")[0]);
                    } else {
                        fullName.setValue("Utilisateur");
                    }
                    phone.setValue("Non renseigne");
                });
    }

    public void logout() {
        auth.signOut();
        logoutSuccess.setValue(true);
    }

    // Getters
    public MutableLiveData<String> getFullName() {
        return fullName;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public MutableLiveData<String> getPhone() {
        return phone;
    }

    public MutableLiveData<String> getMemberSince() {
        return memberSince;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<Boolean> getLogoutSuccess() {
        return logoutSuccess;
    }
}

