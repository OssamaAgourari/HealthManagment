package com.example.health.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView profileName;
    private TextView profileEmail;
    private TextView profileFullName;
    private TextView profilePhone;
    private TextView profileMemberSince;
    private Button logoutButton;

    private FirebaseAuth auth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize views
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileFullName = view.findViewById(R.id.profileFullName);
        profilePhone = view.findViewById(R.id.profilePhone);
        profileMemberSince = view.findViewById(R.id.profileMemberSince);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Load user data
        loadUserData();

        // Logout button
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // Display Name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                profileName.setText(displayName);
                profileFullName.setText(displayName);
            } else {
                String email = currentUser.getEmail();
                if (email != null) {
                    String name = email.split("@")[0];
                    profileName.setText(name);
                    profileFullName.setText(name);
                } else {
                    profileName.setText("Utilisateur");
                    profileFullName.setText("Non renseigné");
                }
            }

            // Email
            String email = currentUser.getEmail();
            if (email != null) {
                profileEmail.setText(email);
            } else {
                profileEmail.setText("Email non disponible");
            }

            // Phone
            String phone = currentUser.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                profilePhone.setText(phone);
            } else {
                profilePhone.setText("Non renseigné");
            }

            // Member Since (account creation time)
            long creationTimestamp = currentUser.getMetadata().getCreationTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.FRENCH);
            String memberSince = sdf.format(new Date(creationTimestamp));
            profileMemberSince.setText(memberSince);
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter?")
            .setPositiveButton("Oui", (dialog, which) -> {
                // Logout
                auth.signOut();

                // Navigate to login screen
                Navigation.findNavController(requireView())
                    .navigate(R.id.loginFragment);
            })
            .setNegativeButton("Non", null)
            .show();
    }
}