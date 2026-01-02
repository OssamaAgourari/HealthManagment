package com.example.health.ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class DoctorsListFragment extends Fragment implements DoctorAdapter.OnDoctorClickListener {

    private DoctorViewModel viewModel;
    private DoctorAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;

    public DoctorsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctors_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.doctorsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        // Setup RecyclerView
        adapter = new DoctorAdapter(this);
        recyclerView.setAdapter(adapter);

        // Observe data
        observeViewModel();

        // Load doctors
        viewModel.loadDoctors();
    }

    private void observeViewModel() {
        // Observe doctors list
        viewModel.getDoctors().observe(getViewLifecycleOwner(), doctors -> {
            if (doctors != null && !doctors.isEmpty()) {
                adapter.setDoctors(doctors);
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDoctorClick(Doctor doctor) {
        // Navigate to doctor details
        Bundle bundle = new Bundle();
        bundle.putString("doctorId", doctor.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_doctorsListFragment_to_doctorDetailsFragment, bundle);
    }
}