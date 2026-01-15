package com.example.health.ui.auth;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.health.R;
import com.example.health.databinding.FragmentRegisterBinding;
import com.example.health.model.Patient;
import com.example.health.viewModels.auth.RegisterViewModel;

import java.util.Calendar;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupDatePicker();
        setupRegisterButton();
        observeViewModel();
    }

    private void setupDatePicker() {
        binding.birthDateInput.setOnClickListener(v -> showDatePicker());
        binding.birthDateLayout.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 20; // Default to 20 years ago
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    binding.birthDateInput.setText(date);
                },
                year, month, day
        );

        // Set max date to today (can't be born in the future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void setupRegisterButton() {
        binding.registerButton.setOnClickListener(v -> {
            // Get form data
            String firstName = binding.firstNameInput.getText().toString().trim();
            String lastName = binding.lastNameInput.getText().toString().trim();
            String email = binding.emailInput.getText().toString().trim();
            String phone = binding.phoneInput.getText().toString().trim();
            String birthDate = binding.birthDateInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString();

            // Get selected gender
            String gender = "";
            if (binding.radioMale.isChecked()) {
                gender = "Homme";
            } else if (binding.radioFemale.isChecked()) {
                gender = "Femme";
            }

            // Validate
            if (firstName.isEmpty()) {
                binding.firstNameLayout.setError("Le prénom est requis");
                return;
            }
            if (lastName.isEmpty()) {
                binding.lastNameLayout.setError("Le nom est requis");
                return;
            }
            if (email.isEmpty()) {
                binding.emailLayout.setError("L'email est requis");
                return;
            }
            if (password.isEmpty()) {
                binding.passwordLayout.setError("Le mot de passe est requis");
                return;
            }
            if (password.length() < 6) {
                binding.passwordLayout.setError("Au moins 6 caractères");
                return;
            }
            if (gender.isEmpty()) {
                Snackbar.make(binding.getRoot(), "Veuillez selectionner un genre", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#FF9800"))
                    .setTextColor(Color.WHITE)
                    .show();
                return;
            }

            // Clear errors
            binding.firstNameLayout.setError(null);
            binding.lastNameLayout.setError(null);
            binding.emailLayout.setError(null);
            binding.passwordLayout.setError(null);

            // Create Patient object
            Patient patient = new Patient();
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setPhone(phone);
            patient.setBirthDate(birthDate);
            patient.setGender(gender);

            // Set data in ViewModel
            viewModel.getEmail().setValue(email);
            viewModel.getPassword().setValue(password);
            viewModel.getUserLiveDate().setValue(patient);

            // Disable button during registration
            binding.registerButton.setEnabled(false);
            binding.registerButton.setText("Création en cours...");

            // Register
            viewModel.register();
        });
    }

    private void observeViewModel() {
        // Observe registration success
        viewModel.getIsRegistered().observe(getViewLifecycleOwner(), isRegistered -> {
            if (isRegistered != null && isRegistered) {
                Snackbar.make(binding.getRoot(), "Compte cree avec succes!", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#4CAF50"))
                    .setTextColor(Color.WHITE)
                    .show();

                // Navigate back to login screen after delay
                binding.getRoot().postDelayed(() ->
                    Navigation.findNavController(requireView()).popBackStack(), 1500);
            }
        });

        // Observe errors
        viewModel.getErreurMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#F44336"))
                    .setTextColor(Color.WHITE)
                    .show();

                // Re-enable button
                binding.registerButton.setEnabled(true);
                binding.registerButton.setText("Creer mon compte");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}