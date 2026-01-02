package com.example.health.ui.appointment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.model.Appointment;
import com.example.health.viewModels.AppointmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookApointementsFragment extends Fragment {

    private AppointmentViewModel viewModel;

    // Doctor data from arguments
    private String doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private double consultationFee;

    // UI Components
    private ImageButton backButton;
    private TextView doctorNameText, doctorSpecialtyText, consultationFeeText;
    private TextView selectedTimeText;
    private CalendarView calendarView;
    private GridLayout timeSlotsGrid;
    private EditText reasonEditText;
    private Button confirmBookingButton;

    // Selected values
    private String selectedDate = "";
    private String selectedTime = "";
    private Button selectedTimeButton = null;

    // Time slots
    private final String[] timeSlots = {
        "09:00", "10:00", "11:00",
        "14:00", "15:00", "16:00",
        "17:00", "18:00", "19:00"
    };

    public BookApointementsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book_apointements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);

        // Get doctor data from arguments
        if (getArguments() != null) {
            doctorId = getArguments().getString("doctorId");
            doctorName = getArguments().getString("doctorName");
            doctorSpecialty = getArguments().getString("doctorSpecialty");
            consultationFee = getArguments().getDouble("consultationFee", 0);
        }

        // Initialize views
        backButton = view.findViewById(R.id.backButton);
        doctorNameText = view.findViewById(R.id.doctorNameText);
        doctorSpecialtyText = view.findViewById(R.id.doctorSpecialtyText);
        consultationFeeText = view.findViewById(R.id.consultationFeeText);
        selectedTimeText = view.findViewById(R.id.selectedTimeText);
        calendarView = view.findViewById(R.id.calendarView);
        timeSlotsGrid = view.findViewById(R.id.timeSlotsGrid);
        reasonEditText = view.findViewById(R.id.reasonEditText);
        confirmBookingButton = view.findViewById(R.id.confirmBookingButton);

        // Back button
        backButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Display doctor information
        doctorNameText.setText(doctorName);
        doctorSpecialtyText.setText(doctorSpecialty);
        consultationFeeText.setText(String.format("%.0f€", consultationFee));

        // Setup calendar (minimum date is today)
        calendarView.setMinDate(System.currentTimeMillis());

        // Set default selected date to today
        Calendar today = Calendar.getInstance();
        selectedDate = formatDate(today.getTimeInMillis());

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = formatDate(calendar.getTimeInMillis());
        });

        // Setup time slots
        setupTimeSlots();

        // Observe booking success
        viewModel.getBookingSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(),
                    "Rendez-vous confirmé avec succès!",
                    Toast.LENGTH_LONG).show();
                // Navigate back or to appointments list
                Navigation.findNavController(view).navigateUp();
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Confirm booking button
        confirmBookingButton.setOnClickListener(v -> confirmBooking());
    }

    private void setupTimeSlots() {
        timeSlotsGrid.removeAllViews();

        for (String timeSlot : timeSlots) {
            Button timeButton = new Button(requireContext());

            // Set button properties
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

            // Click listener
            timeButton.setOnClickListener(v -> selectTimeSlot(timeButton, timeSlot));

            timeSlotsGrid.addView(timeButton);
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
        selectedTime = time;
        button.setBackgroundColor(Color.parseColor("#4CAF50"));
        button.setTextColor(Color.WHITE);

        // Show selected time
        selectedTimeText.setText("Heure sélectionnée: " + time);
        selectedTimeText.setVisibility(View.VISIBLE);
    }

    private void confirmBooking() {
        // Validate inputs
        if (selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez sélectionner un créneau horaire", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Vous devez être connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String patientId = auth.getCurrentUser().getUid();
        String patientName = auth.getCurrentUser().getDisplayName();
        if (patientName == null || patientName.isEmpty()) {
            patientName = auth.getCurrentUser().getEmail();
        }

        String reason = reasonEditText.getText().toString().trim();
        if (reason.isEmpty()) {
            reason = "Consultation générale";
        }

        // Create appointment
        Appointment appointment = new Appointment(
            null, // ID will be generated by Firestore
            patientId,
            patientName,
            doctorId,
            doctorName,
            doctorSpecialty,
            selectedDate,
            selectedTime,
            "pending",
            reason,
            consultationFee,
            null, // createdAt will be set by repository
            null  // updatedAt will be set by repository
        );

        // Book appointment
        viewModel.bookAppointment(appointment);
    }

    private String formatDate(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(timeInMillis);
    }
}