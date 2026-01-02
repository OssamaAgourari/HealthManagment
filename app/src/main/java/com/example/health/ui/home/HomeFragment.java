package com.example.health.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private TextView welcomeUserText;
    private CardView findDoctorsCard;
    private CardView myAppointmentsCard;
    private CardView profileCard;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        welcomeUserText = view.findViewById(R.id.welcomeUserText);
        findDoctorsCard = view.findViewById(R.id.findDoctorsCard);
        myAppointmentsCard = view.findViewById(R.id.myAppointmentsCard);
        profileCard = view.findViewById(R.id.profileCard);

        // Display user name
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                welcomeUserText.setText(displayName);
            } else {
                String email = currentUser.getEmail();
                if (email != null) {
                    welcomeUserText.setText(email.split("@")[0]);
                } else {
                    welcomeUserText.setText("Utilisateur");
                }
            }
        }

        // Setup card click listeners
        findDoctorsCard.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.doctorsListFragment);
        });

        myAppointmentsCard.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_myAppointmentsFragment);
        });

        profileCard.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_profileFragment);
        });
    }
}