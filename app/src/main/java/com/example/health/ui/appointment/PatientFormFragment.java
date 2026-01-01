package com.example.health.ui.appointment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.health.R;
import com.example.health.databinding.FragmentPatientFormBinding;
import com.example.health.model.PatientForm;
import com.example.health.viewModels.PatientFormViewModel;

public class PatientFormFragment extends Fragment {

    private FragmentPatientFormBinding binding;
    private PatientFormViewModel viewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentPatientFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PatientFormViewModel.class);

        setupDropdown();
        observeViewModel();

        binding.envoyer.setOnClickListener(v -> submitForm());
        binding.fermer.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.radioMale.setOnClickListener(v -> {
            binding.radioMale.setChecked(true);
            binding.radioFemale.setChecked(false);
        });

        binding.radioFemale.setOnClickListener(v -> {
            binding.radioFemale.setChecked(true);
            binding.radioMale.setChecked(false);
        });

    }

    private void setupDropdown() {
        String[] types = getResources().getStringArray(R.array.specialites);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_list_item_1, types);
        binding.menuAuto.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.submitResult.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(),
                        "✔ Formulaire envoyé avec succès",
                        Toast.LENGTH_LONG).show();
                clearForm();
            } else {
                Toast.makeText(requireContext(),
                        "❌ Erreur lors de l’envoi",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitForm() {

        String name = binding.editName.getText().toString().trim();
        String ageText = binding.editAge.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();
        String type = binding.menuAuto.getText().toString().trim();

        if (name.isEmpty() || ageText.isEmpty() || description.isEmpty() || type.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Veuillez remplir tous les champs",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageText);

        String gender = binding.radioMale.isChecked() ? "Homme" :
                binding.radioFemale.isChecked() ? "Femme" : "Non renseigné";

        boolean diabete = binding.diabete.isChecked();
        boolean hypertension = binding.hypertension.isChecked();

        PatientForm form = new PatientForm(
                name,
                age,
                gender,
                type,
                description,
                diabete,
                hypertension,
                System.currentTimeMillis()
        );

        viewModel.submitPatientForm(form);
    }

    private void clearForm() {
        binding.editName.setText("");
        binding.editAge.setText("");
        binding.editDescription.setText("");
        binding.menuAuto.setText("");
        binding.radioMale.setChecked(false);
        binding.radioFemale.setChecked(false);
        binding.diabete.setChecked(false);
        binding.hypertension.setChecked(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
