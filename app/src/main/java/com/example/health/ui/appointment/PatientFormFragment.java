package com.example.health.ui.appointment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.databinding.FragmentPatientFormBinding;
import com.example.health.model.Doctor;
import com.example.health.viewModels.PatientFormViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PatientFormFragment extends Fragment {

    private FragmentPatientFormBinding binding;
    private PatientFormViewModel viewModel;
    private NavController navController;

    private List<Doctor> doctorsList = new ArrayList<>();
    private final List<Button> timeSlotButtons = new ArrayList<>();
    private Button selectedTimeButton = null;

    private final String[] timeSlots = {
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
            "17:00", "17:30", "18:00"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPatientFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PatientFormViewModel.class);
        navController = Navigation.findNavController(view);

        setupCalendar();
        setupTimeSlots();
        setupGenderSelection();
        setupClickListeners();
        observeViewModel();

        // Load doctors
        viewModel.loadDoctors();
    }

    private void setupCalendar() {
        binding.calendarView.setMinDate(System.currentTimeMillis());

        binding.calendarView.setOnDateChangeListener((calendarView, year, month, dayOfMonth) -> {
            // Reset selected time button
            if (selectedTimeButton != null) {
                selectedTimeButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
                selectedTimeButton.setTextColor(Color.parseColor("#333333"));
                selectedTimeButton = null;
            }
            viewModel.selectDate(year, month, dayOfMonth);
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
            timeButton.setTag(time);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            timeButton.setLayoutParams(params);

            timeButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
            timeButton.setTextColor(Color.parseColor("#333333"));
            timeButton.setEnabled(true);

            timeButton.setOnClickListener(v -> {
                if (!timeButton.isEnabled()) {
                    Snackbar.make(binding.getRoot(), "Ce creneau est deja reserve", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (selectedTimeButton != null) {
                    selectedTimeButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    selectedTimeButton.setTextColor(Color.parseColor("#333333"));
                }

                timeButton.setBackgroundColor(Color.parseColor("#4CAF50"));
                timeButton.setTextColor(Color.WHITE);
                selectedTimeButton = timeButton;

                viewModel.selectTime(time);
                updateSelectedDateTimeText();
            });

            timeSlotButtons.add(timeButton);
            binding.timeSlotsGrid.addView(timeButton);
        }
    }

    private void setupGenderSelection() {
        binding.radioMale.setOnClickListener(v -> {
            binding.radioMale.setChecked(true);
            binding.radioFemale.setChecked(false);
        });

        binding.radioFemale.setOnClickListener(v -> {
            binding.radioFemale.setChecked(true);
            binding.radioMale.setChecked(false);
        });
    }

    private void updateTimeSlotsAvailability(Set<String> bookedSlots) {
        for (Button button : timeSlotButtons) {
            String time = (String) button.getTag();
            if (bookedSlots != null && bookedSlots.contains(time)) {
                button.setEnabled(false);
                button.setBackgroundColor(Color.parseColor("#FFCDD2"));
                button.setTextColor(Color.parseColor("#B71C1C"));
                button.setAlpha(0.6f);
            } else {
                button.setEnabled(true);
                button.setAlpha(1.0f);

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
        binding.fermer.setOnClickListener(v -> navController.navigateUp());

        binding.envoyer.setOnClickListener(v -> submitForm());
    }

    private void submitForm() {
        String name = "";
        if (binding.editName.getText() != null) {
            name = binding.editName.getText().toString().trim();
        }

        String phone = "";
        if (binding.editPhone.getText() != null) {
            phone = binding.editPhone.getText().toString().trim();
        }

        String ageText = "";
        if (binding.editAge.getText() != null) {
            ageText = binding.editAge.getText().toString().trim();
        }

        String description = "";
        if (binding.editDescription.getText() != null) {
            description = binding.editDescription.getText().toString().trim();
        }

        if (name.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Veuillez entrer le nom du patient", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#FF9800"))
                    .setTextColor(Color.WHITE)
                    .show();
            return;
        }

        if (ageText.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Veuillez entrer l'age du patient", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#FF9800"))
                    .setTextColor(Color.WHITE)
                    .show();
            return;
        }

        int age = Integer.parseInt(ageText);

        String gender = binding.radioMale.isChecked() ? "Homme" :
                binding.radioFemale.isChecked() ? "Femme" : "Non renseigne";

        boolean diabete = binding.diabete.isChecked();
        boolean hypertension = binding.hypertension.isChecked();

        viewModel.bookAppointmentForSomeoneElse(name, phone, description, gender, age, diabete, hypertension);
    }

    private void observeViewModel() {
        // Observe doctors list
        viewModel.getDoctors().observe(getViewLifecycleOwner(), doctors -> {
            if (doctors != null) {
                doctorsList = doctors;
                setupDoctorDropdown(doctors);
            }
        });

        // Observe selected doctor
        viewModel.getSelectedDoctor().observe(getViewLifecycleOwner(), doctor -> {
            if (doctor != null) {
                binding.selectedDoctorInfo.setVisibility(View.VISIBLE);
                binding.selectedDoctorName.setText(doctor.getFullName());
                binding.selectedDoctorSpecialty.setText(doctor.getSpecialty());
                binding.selectedDoctorFee.setText(String.format(Locale.getDefault(), "Tarif: %.0f DH", doctor.getConsultationFee()));
            } else {
                binding.selectedDoctorInfo.setVisibility(View.GONE);
            }
        });

        // Observe booked time slots
        viewModel.getBookedTimeSlots().observe(getViewLifecycleOwner(), bookedSlots -> {
            if (bookedSlots != null) {
                updateTimeSlotsAvailability(bookedSlots);
            }
        });

        // Observe booking success
        viewModel.getBookingSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.envoyer.setEnabled(true);
                Snackbar.make(binding.getRoot(), "Rendez-vous reserve avec succes!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#4CAF50"))
                        .setTextColor(Color.WHITE)
                        .show();
                navController.navigate(R.id.action_global_myAppointmentsFragment);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.envoyer.setEnabled(true);
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
                binding.envoyer.setEnabled(!isLoading);
            }
        });
    }

    private void setupDoctorDropdown(List<Doctor> doctors) {
        List<String> doctorNames = new ArrayList<>();
        for (Doctor doctor : doctors) {
            doctorNames.add(doctor.getFullName() + " - " + doctor.getSpecialty());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                doctorNames
        );
        binding.doctorDropdown.setAdapter(adapter);

        binding.doctorDropdown.setOnItemClickListener((parent, view, position, id) -> {
            if (position < doctors.size()) {
                viewModel.selectDoctor(doctors.get(position));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

