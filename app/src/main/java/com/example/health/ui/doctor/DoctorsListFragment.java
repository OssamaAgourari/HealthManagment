package com.example.health.ui.doctor;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.health.R;
import com.example.health.adapters.DoctorAdapter;
import com.example.health.model.Doctor;
import com.example.health.viewModels.DoctorViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textfield.TextInputEditText;

public class DoctorsListFragment extends Fragment implements DoctorAdapter.OnDoctorClickListener {

    private DoctorViewModel viewModel;
    private DoctorAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private TextInputEditText searchNameInput;
    private AutoCompleteTextView specialtyFilter;
    private ShimmerFrameLayout shimmerLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctors_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupFilters();
        viewModel.loadDoctors();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.doctorsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        searchNameInput = view.findViewById(R.id.searchNameInput);
        specialtyFilter = view.findViewById(R.id.specialtyFilter);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);

        adapter = new DoctorAdapter(this);
        recyclerView.setAdapter(adapter);

        // Start shimmer animation
        shimmerLayout.startShimmer();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        viewModel.getDoctors().observe(getViewLifecycleOwner(), doctors -> {
            // Hide shimmer when data arrives
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);

            if (doctors != null && !doctors.isEmpty()) {
                adapter.setDoctors(doctors);
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                shimmerLayout.setVisibility(View.VISIBLE);
                shimmerLayout.startShimmer();
                recyclerView.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && getView() != null) {
                Snackbar.make(getView(), error, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#F44336"))
                    .setTextColor(Color.WHITE)
                    .show();
            }
        });

        viewModel.getSpecialties().observe(getViewLifecycleOwner(), specialties -> {
            if (specialties != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        specialties
                );
                specialtyFilter.setAdapter(adapter);
            }
        });
    }

    private void setupFilters() {
        searchNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterByName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        specialtyFilter.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            viewModel.filterBySpecialty(selected);
        });
    }

    @Override
    public void onDoctorClick(Doctor doctor) {
        Bundle bundle = new Bundle();
        bundle.putString("doctorId", doctor.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_doctorsListFragment_to_doctorDetailsFragment, bundle);
    }
}