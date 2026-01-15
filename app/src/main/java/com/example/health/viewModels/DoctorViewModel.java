package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Doctor;
import com.example.health.repository.DoctorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorViewModel extends ViewModel {

    private final DoctorRepository repository;
    private List<Doctor> allDoctors = new ArrayList<>();
    private MutableLiveData<List<Doctor>> doctors = new MutableLiveData<>();
    private MutableLiveData<Doctor> selectedDoctor = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<List<String>> specialties = new MutableLiveData<>();

    private String currentNameFilter = "";
    private String currentSpecialtyFilter = "";

    public DoctorViewModel() {
        repository = new DoctorRepository();
    }

    // Load all doctors
    public void loadDoctors() {
        isLoading.setValue(true);
        repository.getAllDoctors(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctorsList) {
                allDoctors = doctorsList != null ? doctorsList : new ArrayList<>();
                extractSpecialties();
                applyFilters();
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    private void extractSpecialties() {
        List<String> uniqueSpecialties = allDoctors.stream()
                .map(Doctor::getSpecialty)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        uniqueSpecialties.add(0, "Toutes les spécialités");
        specialties.setValue(uniqueSpecialties);
    }

    // Filter by name
    public void filterByName(String name) {
        currentNameFilter = name != null ? name.toLowerCase().trim() : "";
        applyFilters();
    }

    // Filter by specialty
    public void filterBySpecialty(String specialty) {
        currentSpecialtyFilter = (specialty != null && !specialty.equals("Toutes les spécialités"))
                ? specialty : "";
        applyFilters();
    }

    private void applyFilters() {
        List<Doctor> filtered = allDoctors.stream()
                .filter(doctor -> {
                    boolean matchesName = currentNameFilter.isEmpty() ||
                            doctor.getFullName().toLowerCase().contains(currentNameFilter);
                    boolean matchesSpecialty = currentSpecialtyFilter.isEmpty() ||
                            (doctor.getSpecialty() != null &&
                             doctor.getSpecialty().equals(currentSpecialtyFilter));
                    return matchesName && matchesSpecialty;
                })
                .collect(Collectors.toList());
        doctors.setValue(filtered);
    }

    // Load doctor by ID
    public void loadDoctorById(String doctorId) {
        isLoading.setValue(true);
        repository.getDoctorById(doctorId, selectedDoctor, errorMessage);
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

    public MutableLiveData<List<String>> getSpecialties() {
        return specialties;
    }
}
