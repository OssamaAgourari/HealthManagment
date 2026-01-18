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
import com.example.health.model.Review;
import com.example.health.repository.ReviewRepository;
import com.example.health.utils.NotificationScheduler;
import com.example.health.utils.PdfExportHelper;
import com.example.health.viewModels.AppointmentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.app.Dialog;
import android.widget.RatingBar;

import java.util.ArrayList;
import java.util.List;

public class MyAppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentActionListener {

    private AppointmentViewModel viewModel;
    private AppointmentAdapter adapter;
    private ReviewRepository reviewRepository;

    // UI Components
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgressBar;
    private TextView appointmentsCountText;
    private TabLayout filterTabLayout;
    private MaterialButton exportPdfButton;

    // Filter state
    private int currentFilter = 0; // 0=All, 1=Upcoming, 2=Completed, 3=Cancelled
    private List<Appointment> allAppointments = new ArrayList<>();

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

        // Initialize ReviewRepository
        reviewRepository = new ReviewRepository();

        // Initialize views
        recyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        appointmentsCountText = view.findViewById(R.id.appointmentsCountText);
        filterTabLayout = view.findViewById(R.id.filterTabLayout);
        exportPdfButton = view.findViewById(R.id.exportPdfButton);

        // Setup RecyclerView
        adapter = new AppointmentAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Setup tab filtering
        setupFilterTabs();

        // Setup PDF export
        setupPdfExport();

        // Observe appointments
        viewModel.getAppointments().observe(getViewLifecycleOwner(), appointments -> {
            allAppointments = appointments != null ? appointments : new ArrayList<>();
            displayAppointments(allAppointments);
        });

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

    private void setupFilterTabs() {
        filterTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getPosition();
                applyFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupPdfExport() {
        exportPdfButton.setOnClickListener(v -> {
            if (allAppointments == null || allAppointments.isEmpty()) {
                Snackbar.make(v, "Aucun rendez-vous a exporter", Snackbar.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            String patientName = auth.getCurrentUser() != null ?
                    (auth.getCurrentUser().getDisplayName() != null ?
                            auth.getCurrentUser().getDisplayName() :
                            auth.getCurrentUser().getEmail()) : "Patient";

            boolean success = PdfExportHelper.exportAppointmentsToPdf(
                    requireContext(),
                    allAppointments,
                    patientName
            );

            if (success) {
                Snackbar.make(v, R.string.pdf_exported, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#4CAF50"))
                        .setTextColor(Color.WHITE)
                        .show();
            } else {
                Snackbar.make(v, R.string.pdf_export_error, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#F44336"))
                        .setTextColor(Color.WHITE)
                        .show();
            }
        });
    }

    private void applyFilter() {
        List<Appointment> filteredList = new ArrayList<>();

        for (Appointment apt : allAppointments) {
            String status = apt.getStatus();
            switch (currentFilter) {
                case 0: // All
                    filteredList.add(apt);
                    break;
                case 1: // Upcoming (pending or confirmed)
                    if ("pending".equals(status) || "confirmed".equals(status)) {
                        filteredList.add(apt);
                    }
                    break;
                case 2: // Completed
                    if ("completed".equals(status)) {
                        filteredList.add(apt);
                    }
                    break;
                case 3: // Cancelled
                    if ("cancelled".equals(status)) {
                        filteredList.add(apt);
                    }
                    break;
            }
        }

        displayFilteredAppointments(filteredList);
    }

    private void displayAppointments(List<Appointment> appointments) {
        applyFilter();
    }

    private void displayFilteredAppointments(List<Appointment> appointments) {
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
                // Cancel scheduled notifications for this appointment
                NotificationScheduler.cancelAppointmentReminders(requireContext(), appointment.getId());
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

    @Override
    public void onRateAppointment(Appointment appointment) {
        showRatingDialog(appointment);
    }

    private void showRatingDialog(Appointment appointment) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_review);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextView doctorNameText = dialog.findViewById(R.id.dialogDoctorName);
        RatingBar ratingBar = dialog.findViewById(R.id.dialogRatingBar);
        TextInputEditText commentInput = dialog.findViewById(R.id.dialogCommentInput);
        MaterialButton cancelButton = dialog.findViewById(R.id.dialogCancelButton);
        MaterialButton submitButton = dialog.findViewById(R.id.dialogSubmitButton);

        doctorNameText.setText(appointment.getDoctorName());

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        submitButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();

            if (rating == 0) {
                Snackbar.make(requireView(), "Veuillez donner une note", Snackbar.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Snackbar.make(requireView(), "Vous devez etre connecte", Snackbar.LENGTH_SHORT).show();
                return;
            }

            String patientName = user.getDisplayName();
            if (patientName == null || patientName.isEmpty()) {
                patientName = user.getEmail();
            }

            String comment = "";
            if (commentInput.getText() != null) {
                comment = commentInput.getText().toString().trim();
            }

            Review review = new Review(
                    null,
                    appointment.getDoctorId(),
                    user.getUid(),
                    patientName,
                    appointment.getId(),
                    rating,
                    comment,
                    null
            );

            androidx.lifecycle.MutableLiveData<Boolean> successLiveData = new androidx.lifecycle.MutableLiveData<>();
            androidx.lifecycle.MutableLiveData<String> errorLiveData = new androidx.lifecycle.MutableLiveData<>();

            reviewRepository.addReview(review, successLiveData, errorLiveData);

            successLiveData.observe(getViewLifecycleOwner(), success -> {
                if (success != null && success) {
                    dialog.dismiss();
                    Snackbar.make(requireView(), R.string.review_submitted, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#4CAF50"))
                            .setTextColor(Color.WHITE)
                            .show();
                }
            });

            errorLiveData.observe(getViewLifecycleOwner(), error -> {
                if (error != null && !error.isEmpty()) {
                    Snackbar.make(requireView(), error, Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.parseColor("#F44336"))
                            .setTextColor(Color.WHITE)
                            .show();
                }
            });
        });

        dialog.show();
    }
}