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
    // test &&
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Contient toute la navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        //Gère la navigation
        NavController navController = navHostFragment.getNavController();
        //Connecte la bouttom nav an nav controller
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        //affiche / cache la bottom nav selon l'écran
        /*navController.addOnDestinationChangedListener((controller, destination, arguments) ->{
            int destId = destination.getId();
            if(destId == R.id.loginFragment || destId == R.id.registerFragment || destId == R.id.forgotPasswordFragment){
                binding.bottomNavigationView.setVisibility(View.GONE);
            } else {
                binding.bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });*/
    }
}