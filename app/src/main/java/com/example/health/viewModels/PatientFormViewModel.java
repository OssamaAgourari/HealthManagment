package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.PatientForm;
import com.example.health.repository.PatientRepository;

public class PatientFormViewModel extends ViewModel {

    private final PatientRepository repository;
    public MutableLiveData<Boolean> submitResult = new MutableLiveData<>();

    public PatientFormViewModel() {
        repository = new PatientRepository();
    }

    public void submitPatientForm(PatientForm form) {
        repository.submitForm(form, submitResult);
    }
}