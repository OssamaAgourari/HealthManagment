package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Patient;
import com.example.health.repository.ProfileRepository;

public class EditProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepository;

    private final MutableLiveData<Patient> patient = new MutableLiveData<>();
    private final MutableLiveData<String> firstName = new MutableLiveData<>("");
    private final MutableLiveData<String> lastName = new MutableLiveData<>("");
    private final MutableLiveData<String> phone = new MutableLiveData<>("");
    private final MutableLiveData<String> birthDate = new MutableLiveData<>("");
    private final MutableLiveData<String> gender = new MutableLiveData<>("");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public EditProfileViewModel() {
        profileRepository = new ProfileRepository();
    }

    public void loadProfile() {
        profileRepository.getProfile(patient, errorMessage, isLoading);
    }

    public void setFieldsFromPatient(Patient p) {
        if (p != null) {
            firstName.setValue(p.getFirstName() != null ? p.getFirstName() : "");
            lastName.setValue(p.getLastName() != null ? p.getLastName() : "");
            phone.setValue(p.getPhone() != null ? p.getPhone() : "");
            birthDate.setValue(p.getBirthDate() != null ? p.getBirthDate() : "");
            gender.setValue(p.getGender() != null ? p.getGender() : "");
        }
    }

    public void updateProfile() {
        String fName = firstName.getValue();
        String lName = lastName.getValue();
        String phoneValue = phone.getValue();
        String birthDateValue = birthDate.getValue();
        String genderValue = gender.getValue();

        // Validation
        if (fName == null || fName.trim().isEmpty()) {
            errorMessage.setValue("Le prenom est requis");
            return;
        }

        if (lName == null || lName.trim().isEmpty()) {
            errorMessage.setValue("Le nom est requis");
            return;
        }

        if (phoneValue == null || phoneValue.trim().isEmpty()) {
            errorMessage.setValue("Le numero de telephone est requis");
            return;
        }

        // Phone validation (simple check for digits)
        if (!phoneValue.matches("^[0-9+\\-\\s]{8,15}$")) {
            errorMessage.setValue("Numero de telephone invalide");
            return;
        }

        Patient updatedPatient = new Patient();
        updatedPatient.setFirstName(fName.trim());
        updatedPatient.setLastName(lName.trim());
        updatedPatient.setPhone(phoneValue.trim());
        updatedPatient.setBirthDate(birthDateValue != null ? birthDateValue.trim() : "");
        updatedPatient.setGender(genderValue != null ? genderValue.trim() : "");

        profileRepository.updateProfile(updatedPatient, updateSuccess, errorMessage, isLoading);
    }

    public void setFirstName(String value) {
        firstName.setValue(value);
    }

    public void setLastName(String value) {
        lastName.setValue(value);
    }

    public void setPhone(String value) {
        phone.setValue(value);
    }

    public void setBirthDate(String value) {
        birthDate.setValue(value);
    }

    public void setGender(String value) {
        gender.setValue(value);
    }

    // Getters
    public MutableLiveData<Patient> getPatient() {
        return patient;
    }

    public MutableLiveData<String> getFirstName() {
        return firstName;
    }

    public MutableLiveData<String> getLastName() {
        return lastName;
    }

    public MutableLiveData<String> getPhone() {
        return phone;
    }

    public MutableLiveData<String> getBirthDate() {
        return birthDate;
    }

    public MutableLiveData<String> getGender() {
        return gender;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }
}
