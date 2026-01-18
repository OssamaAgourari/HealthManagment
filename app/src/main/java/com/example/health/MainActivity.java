package com.example.health;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.health.databinding.ActivityMainBinding;
import com.example.health.model.Appointment;
import com.example.health.utils.NotificationHelper;
import com.example.health.utils.NotificationScheduler;
import com.example.health.utils.ThemeHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final Set<Integer> AUTH_SCREENS = Set.of(
            R.id.loginFragment,
            R.id.registerFragment,
            R.id.forgotPasswordFragment
    );

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    rescheduleExistingAppointments();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate to avoid flickering
        ThemeHelper.applyTheme(this);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupNotifications();
    }

    private void setupNotifications() {
        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                rescheduleExistingAppointments();
            }
        } else {
            rescheduleExistingAppointments();
        }
    }

    private void rescheduleExistingAppointments() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("appointments")
                .whereEqualTo("patientId", user.getUid())
                .whereIn("status", Arrays.asList("pending", "confirmed"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Appointment appointment = document.toObject(Appointment.class);
                        appointment.setId(document.getId());

                        if (NotificationScheduler.isAppointmentInFuture(
                                appointment.getDate(), appointment.getTime())) {
                            NotificationScheduler.scheduleAppointmentReminders(this, appointment);
                        }
                    }
                });
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isAuthScreen = AUTH_SCREENS.contains(destination.getId());
            binding.bottomNavigationView.setVisibility(isAuthScreen ? View.GONE : View.VISIBLE);
        });
    }
}