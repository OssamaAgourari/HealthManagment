package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Appointment;
import com.example.health.repository.AppointmentRepository;

import java.util.List;

public class AppointmentViewModel extends ViewModel {

    private final AppointmentRepository repository;
    private MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    private MutableLiveData<Boolean> bookingSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> cancelSuccess = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AppointmentViewModel() {
        repository = new AppointmentRepository();
    }

    // Book a new appointment
    public void bookAppointment(Appointment appointment) {
        isLoading.setValue(true);
        repository.createAppointment(appointment, bookingSuccess, errorMessage);
        isLoading.setValue(false);
    }

    // Load patient appointments
    public void loadPatientAppointments(String patientId) {
        isLoading.setValue(true);
        repository.getPatientAppointments(patientId, appointments, errorMessage);
        isLoading.setValue(false);
    }

    // Cancel appointment
    public void cancelAppointment(String appointmentId) {
        isLoading.setValue(true);
        repository.cancelAppointment(appointmentId, cancelSuccess, errorMessage);
        isLoading.setValue(false);
    }

    // Getters
    public MutableLiveData<List<Appointment>> getAppointments() {
        return appointments;
    }

    public MutableLiveData<Boolean> getBookingSuccess() {
        return bookingSuccess;
    }

    public MutableLiveData<Boolean> getCancelSuccess() {
        return cancelSuccess;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
