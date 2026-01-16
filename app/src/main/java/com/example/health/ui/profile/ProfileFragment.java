package com.example.health.ui.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.databinding.FragmentProfileBinding;
import com.example.health.viewModels.ProfileViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Load profile data
        viewModel.loadProfileData();

        // Setup observers
        observeViewModel();

        // Logout button
        binding.logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void observeViewModel() {
        // Observe full name
        viewModel.getFullName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                binding.profileName.setText(name);
                binding.profileFullName.setText(name);
            }
        });

        // Observe email
        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null) {
                binding.profileEmail.setText(email);
            }
        });

        // Observe phone
        viewModel.getPhone().observe(getViewLifecycleOwner(), phone -> {
            if (phone != null) {
                binding.profilePhone.setText(phone);
            }
        });

        // Observe member since
        viewModel.getMemberSince().observe(getViewLifecycleOwner(), memberSince -> {
            if (memberSince != null) {
                binding.profileMemberSince.setText(memberSince);
            }
        });

        // Observe error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Observe logout success
        viewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Deconnexion")
            .setMessage("Etes-vous sur de vouloir vous deconnecter?")
            .setPositiveButton("Oui", (dialog, which) -> viewModel.logout())
            .setNegativeButton("Non", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}