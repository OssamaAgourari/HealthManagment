package com.example.health.ui.appointment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.health.R;
import com.example.health.adapters.AppointmentAdapter;
import com.example.health.model.Appointment;
import com.example.health.viewModels.AppointmentViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MyAppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentActionListener {

    private AppointmentViewModel viewModel;
    private AppointmentAdapter adapter;

    // UI Components
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgressBar;
    private TextView appointmentsCountText;

    public MyAppointmentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_appointments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);

        // Initialize views
        recyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        appointmentsCountText = view.findViewById(R.id.appointmentsCountText);

        // Setup RecyclerView
        adapter = new AppointmentAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Observe appointments
        viewModel.getAppointments().observe(getViewLifecycleOwner(), this::displayAppointments);

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingProgressBar.setVisibility(View.VISIBLE);
            } else {
                loadingProgressBar.setVisibility(View.GONE);
            }
        });

        // Observe cancel success
        viewModel.getCancelSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Snackbar.make(view, "Rendez-vous annulé avec succès", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#4CAF50"))
                    .setTextColor(Color.WHITE)
                    .show();
                // Reload appointments
                loadAppointments();
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(view, error, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#F44336"))
                    .setTextColor(Color.WHITE)
                    .show();
            }
        });

        // Load appointments
        loadAppointments();
    }

    private void loadAppointments() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String patientId = auth.getCurrentUser().getUid();
            viewModel.loadPatientAppointments(patientId);
        } else {
            View view = getView();
            if (view != null) {
                Snackbar.make(view, "Vous devez etre connecte", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#FF9800"))
                        .setTextColor(Color.WHITE)
                        .show();
            }
        }
    }

    private void displayAppointments(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            // Show empty state
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            appointmentsCountText.setText("0 rendez-vous");
        } else {
            // Show appointments
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            adapter.setAppointments(appointments);

            // Update count
            int count = appointments.size();
            appointmentsCountText.setText(count + (count > 1 ? " rendez-vous" : " rendez-vous"));
        }
    }

    @Override
    public void onCancelAppointment(Appointment appointment) {
        // Show Material confirmation dialog
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Annuler le rendez-vous")
            .setMessage("Êtes-vous sûr de vouloir annuler ce rendez-vous avec " +
                appointment.getDoctorName() + " le " + appointment.getDate() +
                " à " + appointment.getTime() + "?")
            .setPositiveButton("Oui, annuler", (dialog, which) -> {
                viewModel.cancelAppointment(appointment.getId());
            })
            .setNegativeButton("Non", null)
            .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload appointments when fragment is resumed
        loadAppointments();
    }
}