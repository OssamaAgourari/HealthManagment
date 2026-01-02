package com.example.health;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.health.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        navController = navHostFragment.getNavController();

        // Handle bottom nav item selection manually for better control
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Get current destination
            if (navController.getCurrentDestination() != null) {
                int currentDestId = navController.getCurrentDestination().getId();

                // Don't navigate if already on the same destination
                if (currentDestId == itemId) {
                    return true;
                }
            }

            // Navigate using global actions
            try {
                if (itemId == R.id.homeFragment) {
                    navController.navigate(R.id.action_global_homeFragment);
                    return true;
                } else if (itemId == R.id.doctorsListFragment) {
                    navController.navigate(R.id.action_global_doctorsListFragment);
                    return true;
                } else if (itemId == R.id.myAppointmentsFragment) {
                    navController.navigate(R.id.action_global_myAppointmentsFragment);
                    return true;
                } else if (itemId == R.id.patientFormFragment) {
                    navController.navigate(R.id.action_global_patientFormFragment);
                    return true;
                } else if (itemId == R.id.profileFragment) {
                    navController.navigate(R.id.action_global_profileFragment);
                    return true;
                }
            } catch (Exception e) {
                // If navigation fails, just return false
                e.printStackTrace();
                return false;
            }

            return false;
        });

        // Show/hide bottom nav based on current screen AND update selected item
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();

            // Hide bottom nav on auth screens
            if (destId == R.id.loginFragment || destId == R.id.registerFragment || destId == R.id.forgotPasswordFragment) {
                binding.bottomNavigationView.setVisibility(View.GONE);
            } else {
                binding.bottomNavigationView.setVisibility(View.VISIBLE);

                // Update bottom nav selection based on current destination
                if (destId == R.id.homeFragment) {
                    binding.bottomNavigationView.setSelectedItemId(R.id.homeFragment);
                } else if (destId == R.id.doctorsListFragment) {
                    binding.bottomNavigationView.setSelectedItemId(R.id.doctorsListFragment);
                } else if (destId == R.id.myAppointmentsFragment) {
                    binding.bottomNavigationView.setSelectedItemId(R.id.myAppointmentsFragment);
                } else if (destId == R.id.patientFormFragment) {
                    binding.bottomNavigationView.setSelectedItemId(R.id.patientFormFragment);
                } else if (destId == R.id.profileFragment) {
                    binding.bottomNavigationView.setSelectedItemId(R.id.profileFragment);
                }
            }
        });
    }
}