package com.example.health.ui.appointment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.health.databinding.FragmentBookApointementsBinding;
import com.example.health.viewModels.AppointmentViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class BookApointementsFragment extends Fragment {

    private FragmentBookApointementsBinding binding;
    private AppointmentViewModel viewModel;
    private Button selectedTimeButton = null;

    public BookApointementsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookApointementsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);

        // Get doctor data from arguments and pass to ViewModel
        if (getArguments() != null) {
            viewModel.initDoctorData(
                    getArguments().getString("doctorId"),
                    getArguments().getString("doctorName"),
                    getArguments().getString("doctorSpecialty"),
                    getArguments().getDouble("consultationFee", 0)
            );
        }

        setupUI();
        setupClickListeners();
        observeViewModel();
    }

    private void setupUI() {
        // Setup calendar (minimum date is today)
        binding.calendarView.setMinDate(System.currentTimeMillis());

        // Setup time slots
        setupTimeSlots();
    }

    private void setupTimeSlots() {
        binding.timeSlotsGrid.removeAllViews();

        for (String timeSlot : viewModel.getTimeSlots()) {
            Button timeButton = new Button(requireContext());

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            timeButton.setLayoutParams(params);

            timeButton.setText(timeSlot);
            timeButton.setTextSize(14);
            timeButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
            timeButton.setTextColor(Color.parseColor("#333333"));

            timeButton.setOnClickListener(v -> selectTimeSlot(timeButton, timeSlot));

            binding.timeSlotsGrid.addView(timeButton);
        }
    }

    private void selectTimeSlot(Button button, String time) {
        // Deselect previous button
        if (selectedTimeButton != null) {
            selectedTimeButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
            selectedTimeButton.setTextColor(Color.parseColor("#333333"));
        }

        // Select new button
        selectedTimeButton = button;
        button.setBackgroundColor(Color.parseColor("#4CAF50"));
        button.setTextColor(Color.WHITE);

        // Notify ViewModel
        viewModel.selectTime(time);
    }

    private void setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Calendar date change
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                viewModel.selectDate(year, month, dayOfMonth)
        );

        // Confirm booking button
        binding.confirmBookingButton.setOnClickListener(v -> {
            String reason = binding.reasonEditText.getText().toString().trim();
            viewModel.setReason(reason);
            viewModel.confirmBooking();
        });
    }

    private void observeViewModel() {
        // Observe doctor name
        viewModel.getDoctorName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                binding.doctorNameText.setText(name);
            }
        });

        // Observe doctor specialty
        viewModel.getDoctorSpecialty().observe(getViewLifecycleOwner(), specialty -> {
            if (specialty != null) {
                binding.doctorSpecialtyText.setText(specialty);
            }
        });

        // Observe consultation fee
        viewModel.getConsultationFee().observe(getViewLifecycleOwner(), fee -> {
            if (fee != null) {
                binding.consultationFeeText.setText(String.format(Locale.getDefault(), "%.0f€", fee));
            }
        });

        // Observe selected time display
        viewModel.getSelectedTimeDisplay().observe(getViewLifecycleOwner(), timeDisplay -> {
            if (timeDisplay != null && !timeDisplay.isEmpty()) {
                binding.selectedTimeText.setText(timeDisplay);
                binding.selectedTimeText.setVisibility(View.VISIBLE);
            }
        });

        // Observe booking success
        viewModel.getBookingSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Snackbar.make(binding.getRoot(), "Rendez-vous confirme avec succes!", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#4CAF50"))
                    .setTextColor(Color.WHITE)
                    .show();
                binding.getRoot().postDelayed(() ->
                    Navigation.findNavController(requireView()).navigateUp(), 1500);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#F44336"))
                    .setTextColor(Color.WHITE)
                    .show();
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.confirmBookingButton.setEnabled(!isLoading);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}