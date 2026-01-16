package com.example.health.ui.appointment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.databinding.FragmentBookingChoiceBinding;
import com.example.health.viewModels.BookingChoiceViewModel;

public class BookingChoiceFragment extends Fragment {

    private FragmentBookingChoiceBinding binding;
    private BookingChoiceViewModel viewModel;
    private NavController navController;

    public BookingChoiceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingChoiceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(BookingChoiceViewModel.class);
        binding.setVm(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener(v ->
                navController.navigateUp()
        );

        // Book for me - Navigate to doctors list
        binding.bookForMeCard.setOnClickListener(v ->
                navController.navigate(R.id.action_bookingChoiceFragment_to_doctorsListFragment)
        );

        // Book for someone else - Navigate to patient form
        binding.bookForSomeoneElseCard.setOnClickListener(v ->
                navController.navigate(R.id.action_bookingChoiceFragment_to_patientFormFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

