package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Appointment;
import com.example.health.model.Patient;
import com.example.health.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookAppointmentViewModel extends ViewModel {

    private final AppointmentRepository appointmentRepository;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    private MutableLiveData<Patient> currentPatient = new MutableLiveData<>();
    private MutableLiveData<Boolean> bookingSuccess = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Selected date and time
    private MutableLiveData<String> selectedDate = new MutableLiveData<>("");
    private MutableLiveData<String> selectedTime = new MutableLiveData<>("");

    public BookAppointmentViewModel() {
        appointmentRepository = new AppointmentRepository();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Load current patient info from Firestore
    public void loadCurrentPatientInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("Utilisateur non connecté");
            return;
        }

        isLoading.setValue(true);
        firestore.collection("patients")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Patient patient = documentSnapshot.toObject(Patient.class);
                        if (patient != null) {
                            patient.setUid(user.getUid());
                            currentPatient.setValue(patient);
                        }
                    } else {
                        // If patient document doesn't exist, create from auth info
                        Patient patient = new Patient();
                        patient.setUid(user.getUid());
                        patient.setEmail(user.getEmail());
                        String displayName = user.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            String[] names = displayName.split(" ");
                            patient.setFirstName(names[0]);
                            if (names.length > 1) {
                                patient.setLastName(names[names.length - 1]);
                            }
                        }
                        currentPatient.setValue(patient);
                    }
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors du chargement des informations");
                    isLoading.setValue(false);
                });
    }

    // Book appointment
    public void bookAppointment(String doctorId, String doctorName, String specialty,
                                double consultationFee, String reason) {
        Patient patient = currentPatient.getValue();
        if (patient == null) {
            errorMessage.setValue("Informations patient non disponibles");
            return;
        }

        String date = selectedDate.getValue();
        String time = selectedTime.getValue();

        if (date == null || date.isEmpty()) {
            errorMessage.setValue("Veuillez sélectionner une date");
            return;
        }

        if (time == null || time.isEmpty()) {
            errorMessage.setValue("Veuillez sélectionner une heure");
            return;
        }

        isLoading.setValue(true);

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getUid());
        appointment.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        appointment.setDoctorId(doctorId);
        appointment.setDoctorName(doctorName);
        appointment.setSpecialty(specialty);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReason(reason);
        appointment.setConsultationFee(consultationFee);
        appointment.setStatus("pending");

        appointmentRepository.createAppointment(appointment, bookingSuccess, errorMessage);
        isLoading.setValue(false);
    }

    // Set selected date
    public void setSelectedDate(String date) {
        selectedDate.setValue(date);
    }

    // Set selected time
    public void setSelectedTime(String time) {
        selectedTime.setValue(time);
    }

    // Getters
    public MutableLiveData<Patient> getCurrentPatient() {
        return currentPatient;
    }

    public MutableLiveData<Boolean> getBookingSuccess() {
        return bookingSuccess;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getSelectedDate() {
        return selectedDate;
    }

    public MutableLiveData<String> getSelectedTime() {
        return selectedTime;
    }
}

