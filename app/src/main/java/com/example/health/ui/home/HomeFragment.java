package com.example.health.ui.home;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.databinding.FragmentHomeNewBinding;
import com.example.health.viewModels.HomeViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeNewBinding binding;
    private HomeViewModel viewModel;
    private NavController navController;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeNewBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding.setVm(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        // Load home data
        viewModel.loadHomeData();

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        // Language selector - French
        binding.langFrench.setOnClickListener(v -> {
            viewModel.setLanguage("fr");
            updateLanguageUI("fr");
            setLocale("fr");
        });

        // Language selector - English
        binding.langEnglish.setOnClickListener(v -> {
            viewModel.setLanguage("en");
            updateLanguageUI("en");
            setLocale("en");
        });

        // Book appointment button (no appointment)
        binding.bookAppointmentButton.setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_bookingChoiceFragment)
        );

        // Quick book card
        binding.quickBookCard.setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_bookingChoiceFragment)
        );

        // View details button
        binding.viewDetailsButton.setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_myAppointmentsFragment)
        );
    }

    private void observeViewModel() {
        // Observe appointment status
        viewModel.getHasUpcomingAppointment().observe(getViewLifecycleOwner(), hasAppointment -> {
            if (hasAppointment != null && hasAppointment) {
                binding.appointmentExistsLayout.setVisibility(View.VISIBLE);
                binding.noAppointmentLayout.setVisibility(View.GONE);
            } else {
                binding.appointmentExistsLayout.setVisibility(View.GONE);
                binding.noAppointmentLayout.setVisibility(View.VISIBLE);
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Observe current language
        viewModel.getCurrentLanguage().observe(getViewLifecycleOwner(), this::updateLanguageUI);
    }

    private void updateLanguageUI(String languageCode) {
        if ("fr".equals(languageCode)) {
            binding.langFrench.setAlpha(1.0f);
            binding.langEnglish.setAlpha(0.7f);
        } else {
            binding.langFrench.setAlpha(0.7f);
            binding.langEnglish.setAlpha(1.0f);
        }
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = requireContext().getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Recreate activity to apply language change
        requireActivity().recreate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}