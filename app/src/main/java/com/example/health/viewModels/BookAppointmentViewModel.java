package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Appointment;
import com.example.health.model.Patient;
import com.example.health.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Set;

public class BookAppointmentViewModel extends ViewModel {

    private final AppointmentRepository appointmentRepository;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    private final MutableLiveData<Patient> currentPatient = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bookingSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Selected date and time
    private final MutableLiveData<String> selectedDate = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedTime = new MutableLiveData<>("");

    // Booked time slots for current doctor and date
    private final MutableLiveData<Set<String>> bookedTimeSlots = new MutableLiveData<>();

    // Current doctor ID
    private String currentDoctorId;

    public BookAppointmentViewModel() {
        appointmentRepository = new AppointmentRepository();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Set the current doctor ID
    public void setDoctorId(String doctorId) {
        this.currentDoctorId = doctorId;
    }

    // Load booked time slots for a specific date
    public void loadBookedTimeSlots(String date) {
        if (currentDoctorId == null || currentDoctorId.isEmpty()) {
            return;
        }
        appointmentRepository.getBookedTimeSlots(currentDoctorId, date, bookedTimeSlots, errorMessage);
    }

    // Load current patient info from Firestore
    public void loadCurrentPatientInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("Utilisateur non connecte");
            return;
        }

        isLoading.setValue(true);
        // Les utilisateurs sont stockes dans la collection "users"
        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Mapper manuellement les donnees car la structure peut differer
                        Patient patient = new Patient();
                        patient.setUid(user.getUid());
                        patient.setFirstName(documentSnapshot.getString("firstName"));
                        patient.setLastName(documentSnapshot.getString("lastName"));
                        patient.setEmail(documentSnapshot.getString("email"));
                        patient.setPhone(documentSnapshot.getString("phone"));
                        patient.setBirthDate(documentSnapshot.getString("birthDate"));
                        patient.setGender(documentSnapshot.getString("gender"));

                        currentPatient.setValue(patient);
                    } else {
                        // Si le document n'existe pas, creer depuis les infos Firebase Auth
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

    // Book appointment with slot check
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
            errorMessage.setValue("Veuillez selectionner une date");
            return;
        }

        if (time == null || time.isEmpty()) {
            errorMessage.setValue("Veuillez selectionner une heure");
            return;
        }

        isLoading.setValue(true);

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getUid());

        String patientName = "";
        if (patient.getFirstName() != null) {
            patientName = patient.getFirstName();
        }
        if (patient.getLastName() != null) {
            patientName += " " + patient.getLastName();
        }
        appointment.setPatientName(patientName.trim());

        appointment.setDoctorId(doctorId);
        appointment.setDoctorName(doctorName);
        appointment.setSpecialty(specialty);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReason(reason);
        appointment.setConsultationFee(consultationFee);
        appointment.setStatus("pending");

        // Use the new method that checks slot availability
        appointmentRepository.createAppointmentWithCheck(appointment, bookingSuccess, errorMessage);

        // Observe the result to update loading state
        // Note: isLoading will be set to false when success or error is received
    }

    // Set selected date and load booked slots
    public void setSelectedDate(String date) {
        selectedDate.setValue(date);
        // Reset selected time when date changes
        selectedTime.setValue("");
        // Load booked slots for this date
        loadBookedTimeSlots(date);
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

    public MutableLiveData<Set<String>> getBookedTimeSlots() {
        return bookedTimeSlots;
    }
}

