package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Doctor;
import com.example.health.repository.DoctorRepository;

import java.util.List;

public class DoctorViewModel extends ViewModel {

    private final DoctorRepository repository;
    private MutableLiveData<List<Doctor>> doctors = new MutableLiveData<>();
    private MutableLiveData<Doctor> selectedDoctor = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public DoctorViewModel() {
        repository = new DoctorRepository();
    }

    // Load all doctors
    public void loadDoctors() {
        isLoading.setValue(true);
        repository.getAllDoctors(doctors, errorMessage);
        isLoading.setValue(false);
    }

    // Load doctor by ID
    public void loadDoctorById(String doctorId) {
        isLoading.setValue(true);
        repository.getDoctorById(doctorId, selectedDoctor, errorMessage);
        isLoading.setValue(false);
    }

    // Search by specialty
    public void searchBySpecialty(String specialty) {
        isLoading.setValue(true);
        repository.searchBySpecialty(specialty, doctors, errorMessage);
        isLoading.setValue(false);
    }

    // Getters
    public MutableLiveData<List<Doctor>> getDoctors() {
        return doctors;
    }

    public MutableLiveData<Doctor> getSelectedDoctor() {
        return selectedDoctor;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
