package com.example.health.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.databinding.FragmentEditProfileBinding;
import com.example.health.viewModels.EditProfileViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private EditProfileViewModel viewModel;
    private final Calendar calendar = Calendar.getInstance();

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        viewModel.loadProfile();

        setupTextWatchers();
        setupClickListeners();
        observeViewModel();
    }

    private void setupTextWatchers() {
        binding.editFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setFirstName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setLastName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setPhone(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        binding.editBirthDate.setOnClickListener(v -> showDatePicker());

        binding.birthDateLayout.setEndIconOnClickListener(v -> showDatePicker());

        binding.genderRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMale) {
                viewModel.setGender("Homme");
            } else if (checkedId == R.id.radioFemale) {
                viewModel.setGender("Femme");
            }
        });

        binding.saveButton.setOnClickListener(v -> viewModel.updateProfile());

        binding.cancelButton.setOnClickListener(v -> navigateBack());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateBirthDateField();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateBirthDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(calendar.getTime());
        binding.editBirthDate.setText(dateStr);
        viewModel.setBirthDate(dateStr);
    }

    private void observeViewModel() {
        viewModel.getPatient().observe(getViewLifecycleOwner(), patient -> {
            if (patient != null) {
                viewModel.setFieldsFromPatient(patient);

                if (patient.getFirstName() != null) {
                    binding.editFirstName.setText(patient.getFirstName());
                }
                if (patient.getLastName() != null) {
                    binding.editLastName.setText(patient.getLastName());
                }
                if (patient.getPhone() != null) {
                    binding.editPhone.setText(patient.getPhone());
                }
                if (patient.getBirthDate() != null && !patient.getBirthDate().isEmpty()) {
                    binding.editBirthDate.setText(patient.getBirthDate());
                }
                if (patient.getGender() != null) {
                    if ("Homme".equals(patient.getGender())) {
                        binding.radioMale.setChecked(true);
                    } else if ("Femme".equals(patient.getGender())) {
                        binding.radioFemale.setChecked(true);
                    }
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.saveButton.setEnabled(!isLoading);
                binding.cancelButton.setEnabled(!isLoading);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Snackbar.make(binding.getRoot(), "Profil mis a jour avec succes", Snackbar.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }

    private void navigateBack() {
        Navigation.findNavController(requireView()).popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
