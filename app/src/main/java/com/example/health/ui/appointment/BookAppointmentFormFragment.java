package com.example.health.ui.appointment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.databinding.FragmentBookAppointmentFormBinding;
import com.example.health.model.Appointment;
import com.example.health.model.Patient;
import com.example.health.utils.NotificationScheduler;
import com.example.health.viewModels.BookAppointmentViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BookAppointmentFormFragment extends Fragment {

    private FragmentBookAppointmentFormBinding binding;
    private BookAppointmentViewModel viewModel;

    // Doctor data from arguments
    private String doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private double consultationFee;

    // Selected time button reference
    private Button selectedTimeButton = null;

    // List of time slot buttons for updating availability
    private List<Button> timeSlotButtons = new ArrayList<>();

    // Available time slots
    private final String[] timeSlots = {
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
            "17:00", "17:30", "18:00"
    };

    public BookAppointmentFormFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookAppointmentFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(BookAppointmentViewModel.class);

        // Get doctor data from arguments
        if (getArguments() != null) {
            doctorId = getArguments().getString("doctorId");
            doctorName = getArguments().getString("doctorName");
            doctorSpecialty = getArguments().getString("doctorSpecialty");
            consultationFee = getArguments().getDouble("consultationFee", 0);

            // Set doctor ID in ViewModel
            viewModel.setDoctorId(doctorId);
        }

        // Setup UI
        setupDoctorInfo();
        setupCalendar();
        setupTimeSlots();
        setupClickListeners();
        observeViewModel();

        // Load patient info
        viewModel.loadCurrentPatientInfo();
    }

    private void setupDoctorInfo() {
        binding.doctorNameText.setText(doctorName);
        binding.doctorSpecialtyText.setText(doctorSpecialty);
        binding.consultationFeeText.setText(String.format(Locale.getDefault(), "Tarif: %.0f", consultationFee));
    }

    private void setupCalendar() {
        // Set minimum date to today
        binding.calendarView.setMinDate(System.currentTimeMillis());

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(selectedCalendar.getTime());

            // Reset selected time button
            if (selectedTimeButton != null) {
                selectedTimeButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
                selectedTimeButton.setTextColor(Color.parseColor("#333333"));
                selectedTimeButton = null;
            }

            viewModel.setSelectedDate(formattedDate);
            updateSelectedDateTimeText();
        });
    }

    private void setupTimeSlots() {
        binding.timeSlotsGrid.removeAllViews();
        timeSlotButtons.clear();

        for (String time : timeSlots) {
            Button timeButton = new Button(requireContext());
            timeButton.setText(time);
            timeButton.setTextSize(14);
            timeButton.setAllCaps(false);
            timeButton.setTag(time); // Store time as tag for identification

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            timeButton.setLayoutParams(params);

            // Default style - available
            timeButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
            timeButton.setTextColor(Color.parseColor("#333333"));
            timeButton.setEnabled(true);

            timeButton.setOnClickListener(v -> {
                if (!timeButton.isEnabled()) {
                    Snackbar.make(binding.getRoot(), "Ce creneau est deja reserve", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // Deselect previous button
                if (selectedTimeButton != null) {
                    selectedTimeButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    selectedTimeButton.setTextColor(Color.parseColor("#333333"));
                }

                // Select new button
                timeButton.setBackgroundColor(Color.parseColor("#4CAF50"));
                timeButton.setTextColor(Color.WHITE);
                selectedTimeButton = timeButton;

                viewModel.setSelectedTime(time);
                updateSelectedDateTimeText();
            });

            timeSlotButtons.add(timeButton);
            binding.timeSlotsGrid.addView(timeButton);
        }
    }

    private void updateTimeSlotsAvailability(Set<String> bookedSlots) {
        for (Button button : timeSlotButtons) {
            String time = (String) button.getTag();
            if (bookedSlots != null && bookedSlots.contains(time)) {
                // Slot is booked - disable and style as unavailable
                button.setEnabled(false);
                button.setBackgroundColor(Color.parseColor("#FFCDD2")); // Light red
                button.setTextColor(Color.parseColor("#B71C1C")); // Dark red
                button.setAlpha(0.6f);
            } else {
                // Slot is available
                button.setEnabled(true);
                button.setAlpha(1.0f);

                // Check if this is the currently selected button
                if (button == selectedTimeButton) {
                    button.setBackgroundColor(Color.parseColor("#4CAF50"));
                    button.setTextColor(Color.WHITE);
                } else {
                    button.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    button.setTextColor(Color.parseColor("#333333"));
                }
            }
        }
    }

    private void updateSelectedDateTimeText() {
        String date = viewModel.getSelectedDate().getValue();
        String time = viewModel.getSelectedTime().getValue();

        StringBuilder text = new StringBuilder();
        if (date != null && !date.isEmpty()) {
            text.append("Date: ").append(date);
        }
        if (time != null && !time.isEmpty()) {
            if (text.length() > 0) text.append(" | ");
            text.append("Heure: ").append(time);
        }

        if (text.length() == 0) {
            text.append("Selectionnez une date et une heure");
        }

        binding.selectedDateTimeText.setText(text.toString());
    }

    private void setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Confirm booking button
        binding.confirmBookingButton.setOnClickListener(v -> {
            String reason = binding.reasonEditText.getText().toString().trim();
            viewModel.bookAppointment(doctorId, doctorName, doctorSpecialty, consultationFee, reason);
        });
    }

    private void observeViewModel() {
        // Observe patient data
        viewModel.getCurrentPatient().observe(getViewLifecycleOwner(), patient -> {
            if (patient != null) {
                displayPatientInfo(patient);
            }
        });

        // Observe booked time slots
        viewModel.getBookedTimeSlots().observe(getViewLifecycleOwner(), bookedSlots -> {
            if (bookedSlots != null) {
                updateTimeSlotsAvailability(bookedSlots);
            }
        });

        // Observe created appointment to schedule notifications
        viewModel.getCreatedAppointment().observe(getViewLifecycleOwner(), appointment -> {
            if (appointment != null) {
                // Schedule notification reminders for this appointment
                NotificationScheduler.scheduleAppointmentReminders(requireContext(), appointment);
            }
        });

        // Observe booking success
        viewModel.getBookingSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.confirmBookingButton.setEnabled(true);
                Snackbar.make(binding.getRoot(), "Rendez-vous reserve avec succes!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#4CAF50"))
                        .setTextColor(Color.WHITE)
                        .show();
                // Navigate to appointments list
                Navigation.findNavController(requireView()).navigate(R.id.action_global_myAppointmentsFragment);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.confirmBookingButton.setEnabled(true);
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#F44336"))
                        .setTextColor(Color.WHITE)
                        .show();
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.confirmBookingButton.setEnabled(!isLoading);
            }
        });
    }

    private void displayPatientInfo(Patient patient) {
        // Nom complet
        String firstName = patient.getFirstName();
        String lastName = patient.getLastName();
        StringBuilder fullNameBuilder = new StringBuilder();

        if (firstName != null && !firstName.trim().isEmpty()) {
            fullNameBuilder.append(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullNameBuilder.length() > 0) {
                fullNameBuilder.append(" ");
            }
            fullNameBuilder.append(lastName.trim());
        }

        String fullName = fullNameBuilder.toString();
        binding.patientNameText.setText(fullName.isEmpty() ? "Non renseigne" : fullName);

        // Email
        String email = patient.getEmail();
        binding.patientEmailText.setText(email != null && !email.trim().isEmpty() ? email : "Non renseigne");

        // Telephone
        String phone = patient.getPhone();
        binding.patientPhoneText.setText(phone != null && !phone.trim().isEmpty() ? phone : "Non renseigne");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


