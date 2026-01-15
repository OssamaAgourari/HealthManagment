package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Appointment;
import com.example.health.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AppointmentViewModel extends ViewModel {

    private final AppointmentRepository repository;
    private final FirebaseAuth auth;

    // LiveData pour les listes et états
    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bookingSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cancelSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Données du médecin
    private final MutableLiveData<String> doctorId = new MutableLiveData<>();
    private final MutableLiveData<String> doctorName = new MutableLiveData<>();
    private final MutableLiveData<String> doctorSpecialty = new MutableLiveData<>();
    private final MutableLiveData<Double> consultationFee = new MutableLiveData<>(0.0);

    // Données de sélection
    private final MutableLiveData<String> selectedDate = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedTime = new MutableLiveData<>("");
    private final MutableLiveData<String> reason = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedTimeDisplay = new MutableLiveData<>("");

    // Créneaux horaires disponibles
    private final String[] timeSlots = {
            "09:00", "10:00", "11:00",
            "14:00", "15:00", "16:00",
            "17:00", "18:00", "19:00"
    };

    public AppointmentViewModel() {
        repository = new AppointmentRepository();
        auth = FirebaseAuth.getInstance();

        // Initialiser la date par défaut à aujourd'hui
        Calendar today = Calendar.getInstance();
        selectedDate.setValue(formatDate(today.getTimeInMillis()));
    }

    // Initialiser les données du médecin depuis les arguments
    public void initDoctorData(String id, String name, String specialty, double fee) {
        doctorId.setValue(id);
        doctorName.setValue(name);
        doctorSpecialty.setValue(specialty);
        consultationFee.setValue(fee);
    }

    // Sélectionner une date
    public void selectDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        selectedDate.setValue(formatDate(calendar.getTimeInMillis()));
    }

    // Sélectionner un créneau horaire
    public void selectTime(String time) {
        selectedTime.setValue(time);
        selectedTimeDisplay.setValue("Heure sélectionnée: " + time);
    }

    // Définir la raison de la consultation
    public void setReason(String reasonText) {
        reason.setValue(reasonText);
    }

    // Confirmer la réservation
    public void confirmBooking() {
        String date = selectedDate.getValue();
        String time = selectedTime.getValue();

        // Validation de la date
        if (date == null || date.isEmpty()) {
            errorMessage.setValue("Veuillez sélectionner une date");
            return;
        }

        // Validation de l'heure
        if (time == null || time.isEmpty()) {
            errorMessage.setValue("Veuillez sélectionner un créneau horaire");
            return;
        }

        // Vérifier l'authentification
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("Vous devez être connecté");
            return;
        }

        String patientId = user.getUid();
        String patientName = user.getDisplayName();
        if (patientName == null || patientName.isEmpty()) {
            patientName = user.getEmail();
        }

        String reasonText = reason.getValue();
        if (reasonText == null || reasonText.trim().isEmpty()) {
            reasonText = "Consultation générale";
        }

        // Créer le rendez-vous
        Appointment appointment = new Appointment(
                null,
                patientId,
                patientName,
                doctorId.getValue(),
                doctorName.getValue(),
                doctorSpecialty.getValue(),
                date,
                time,
                "pending",
                reasonText,
                consultationFee.getValue() != null ? consultationFee.getValue() : 0.0,
                null,
                null
        );

        // Réserver le rendez-vous
        bookAppointment(appointment);
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

    // Formater la date
    private String formatDate(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(timeInMillis);
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

    public MutableLiveData<String> getDoctorName() {
        return doctorName;
    }

    public MutableLiveData<String> getDoctorSpecialty() {
        return doctorSpecialty;
    }

    public MutableLiveData<Double> getConsultationFee() {
        return consultationFee;
    }

    public MutableLiveData<String> getSelectedDate() {
        return selectedDate;
    }

    public MutableLiveData<String> getSelectedTime() {
        return selectedTime;
    }

    public MutableLiveData<String> getSelectedTimeDisplay() {
        return selectedTimeDisplay;
    }

    public String[] getTimeSlots() {
        return timeSlots;
    }
}
