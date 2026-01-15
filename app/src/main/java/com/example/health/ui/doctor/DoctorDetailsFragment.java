package com.example.health.ui.doctor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.databinding.FragmentDoctorDetailsBinding;
import com.example.health.model.Doctor;
import com.example.health.viewModels.DoctorViewModel;

public class DoctorDetailsFragment extends Fragment {

    private FragmentDoctorDetailsBinding binding;
    private DoctorViewModel viewModel;

    public DoctorDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back button
        binding.backButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        // Get doctor ID from arguments
        if (getArguments() != null) {
            String doctorId = getArguments().getString("doctorId");
            if (doctorId != null) {
                viewModel.loadDoctorById(doctorId);
            }
        }

        // Observe doctor data
        viewModel.getSelectedDoctor().observe(getViewLifecycleOwner(), doctor -> {
            if (doctor != null) {
                displayDoctorInfo(doctor);
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#F44336"))
                    .setTextColor(Color.WHITE)
                    .show();
            }
        });

        // Book appointment button - Navigate to new form
        binding.bookAppointmentButton.setOnClickListener(v -> {
            Doctor doctor = viewModel.getSelectedDoctor().getValue();
            if (doctor != null) {
                // Prepare data to pass to booking form fragment
                Bundle bundle = new Bundle();
                bundle.putString("doctorId", doctor.getId());
                bundle.putString("doctorName", doctor.getFullName());
                bundle.putString("doctorSpecialty", doctor.getSpecialty());
                bundle.putDouble("consultationFee", doctor.getConsultationFee());

                // Navigate to new booking form fragment
                Navigation.findNavController(v).navigate(
                    R.id.action_doctorDetailsFragment_to_bookAppointmentFormFragment,
                    bundle
                );
            }
        });
    }

    private void displayDoctorInfo(Doctor doctor) {
        binding.doctorDetailName.setText(doctor.getFullName());
        binding.doctorDetailSpecialty.setText(doctor.getSpecialty());
        binding.doctorDetailRating.setText("⭐ " + String.format("%.1f", doctor.getRating()));
        binding.doctorDetailReviews.setText("(" + doctor.getTotalReviews() + " avis)");
        binding.doctorDetailExperience.setText(doctor.getExperience() + " ans");
        binding.doctorDetailFee.setText(String.format("%.0f€", doctor.getConsultationFee()));
        binding.doctorDetailAddress.setText(doctor.getAddress() + ", " + doctor.getCity());
        binding.doctorDetailPhone.setText(doctor.getPhone());
        binding.doctorDetailDescription.setText(doctor.getDescription());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}