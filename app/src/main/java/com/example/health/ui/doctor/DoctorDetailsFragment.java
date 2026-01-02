package com.example.health.ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.model.Doctor;
import com.example.health.viewModels.DoctorViewModel;

public class DoctorDetailsFragment extends Fragment {

    private DoctorViewModel viewModel;
    private TextView nameText, specialtyText, ratingText, reviewsText;
    private TextView experienceText, feeText, addressText, phoneText, descriptionText;
    private Button bookButton;
    private ImageButton backButton;

    public DoctorDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        backButton = view.findViewById(R.id.backButton);
        nameText = view.findViewById(R.id.doctorDetailName);
        specialtyText = view.findViewById(R.id.doctorDetailSpecialty);
        ratingText = view.findViewById(R.id.doctorDetailRating);
        reviewsText = view.findViewById(R.id.doctorDetailReviews);
        experienceText = view.findViewById(R.id.doctorDetailExperience);
        feeText = view.findViewById(R.id.doctorDetailFee);
        addressText = view.findViewById(R.id.doctorDetailAddress);
        phoneText = view.findViewById(R.id.doctorDetailPhone);
        descriptionText = view.findViewById(R.id.doctorDetailDescription);
        bookButton = view.findViewById(R.id.bookAppointmentButton);

        // Back button
        backButton.setOnClickListener(v -> {
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
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Book appointment button
        bookButton.setOnClickListener(v -> {
            Doctor doctor = viewModel.getSelectedDoctor().getValue();
            if (doctor != null) {
                // Prepare data to pass to booking fragment
                Bundle bundle = new Bundle();
                bundle.putString("doctorId", doctor.getId());
                bundle.putString("doctorName", doctor.getFullName());
                bundle.putString("doctorSpecialty", doctor.getSpecialty());
                bundle.putDouble("consultationFee", doctor.getConsultationFee());

                // Navigate to booking fragment
                Navigation.findNavController(v).navigate(
                    R.id.action_doctorDetailsFragment_to_bookApointementsFragment,
                    bundle
                );
            }
        });
    }

    private void displayDoctorInfo(Doctor doctor) {
        nameText.setText(doctor.getFullName());
        specialtyText.setText(doctor.getSpecialty());
        ratingText.setText("⭐ " + String.format("%.1f", doctor.getRating()));
        reviewsText.setText("(" + doctor.getTotalReviews() + " avis)");
        experienceText.setText(doctor.getExperience() + " ans");
        feeText.setText(String.format("%.0f€", doctor.getConsultationFee()));
        addressText.setText(doctor.getAddress() + ", " + doctor.getCity());
        phoneText.setText(doctor.getPhone());
        descriptionText.setText(doctor.getDescription());
    }
}