package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Appointment;
import com.example.health.model.Doctor;
import com.example.health.model.PatientForm;
import com.example.health.repository.AppointmentRepository;
import com.example.health.repository.PatientRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PatientFormViewModel extends ViewModel {

    private final PatientRepository repository;
    private final AppointmentRepository appointmentRepository;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    // Existing
    public MutableLiveData<Boolean> submitResult = new MutableLiveData<>();

    // New for doctor selection and appointment
    private final MutableLiveData<List<Doctor>> doctors = new MutableLiveData<>();
    private final MutableLiveData<Doctor> selectedDoctor = new MutableLiveData<>();
    private final MutableLiveData<String> selectedDate = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedTime = new MutableLiveData<>("");
    private final MutableLiveData<Set<String>> bookedTimeSlots = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bookingSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public PatientFormViewModel() {
        repository = new PatientRepository();
        appointmentRepository = new AppointmentRepository();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set default date to today
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate.setValue(dateFormat.format(today.getTime()));
    }

    public void submitPatientForm(PatientForm form) {
        repository.submitForm(form, submitResult);
    }

    // Load doctors from Firestore
    public void loadDoctors() {
        isLoading.setValue(true);
        firestore.collection("doctors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Doctor> doctorList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Doctor doctor = doc.toObject(Doctor.class);
                        doctor.setId(doc.getId());
                        doctorList.add(doctor);
                    }
                    doctors.setValue(doctorList);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Erreur lors du chargement des medecins");
                    isLoading.setValue(false);
                });
    }

    // Select a doctor
    public void selectDoctor(Doctor doctor) {
        selectedDoctor.setValue(doctor);
        selectedTime.setValue("");
        String date = selectedDate.getValue();
        if (date != null && !date.isEmpty()) {
            loadBookedTimeSlots(doctor.getId(), date);
        }
    }

    // Select date
    public void selectDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());
        selectedDate.setValue(date);
        selectedTime.setValue("");

        Doctor doctor = selectedDoctor.getValue();
        if (doctor != null) {
            loadBookedTimeSlots(doctor.getId(), date);
        }
    }

    // Select time
    public void selectTime(String time) {
        selectedTime.setValue(time);
    }

    // Load booked time slots
    private void loadBookedTimeSlots(String doctorId, String date) {
        appointmentRepository.getBookedTimeSlots(doctorId, date, bookedTimeSlots, errorMessage);
    }

    // Book appointment for someone else
    public void bookAppointmentForSomeoneElse(String patientName, String patientPhone, String description,
                                              String gender, int age, boolean diabete, boolean hypertension) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("Vous devez etre connecte");
            return;
        }

        Doctor doctor = selectedDoctor.getValue();
        if (doctor == null) {
            errorMessage.setValue("Veuillez selectionner un medecin");
            return;
        }

        String date = selectedDate.getValue();
        if (date == null || date.isEmpty()) {
            errorMessage.setValue("Veuillez selectionner une date");
            return;
        }

        String time = selectedTime.getValue();
        if (time == null || time.isEmpty()) {
            errorMessage.setValue("Veuillez selectionner une heure");
            return;
        }

        if (patientName == null || patientName.trim().isEmpty()) {
            errorMessage.setValue("Veuillez entrer le nom du patient");
            return;
        }

        isLoading.setValue(true);

        // Build reason with patient info
        StringBuilder reason = new StringBuilder();
        reason.append("Patient: ").append(patientName.trim());
        reason.append(", Age: ").append(age);
        reason.append(", Genre: ").append(gender);
        if (patientPhone != null && !patientPhone.trim().isEmpty()) {
            reason.append(", Tel: ").append(patientPhone.trim());
        }
        if (diabete) reason.append(", Diabete");
        if (hypertension) reason.append(", Hypertension");
        if (description != null && !description.trim().isEmpty()) {
            reason.append(", Symptomes: ").append(description.trim());
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(currentUser.getUid());
        appointment.setPatientName(patientName.trim() + " (par " + currentUser.getEmail() + ")");
        appointment.setDoctorId(doctor.getId());
        appointment.setDoctorName(doctor.getFullName());
        appointment.setSpecialty(doctor.getSpecialty());
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReason(reason.toString());
        appointment.setConsultationFee(doctor.getConsultationFee());
        appointment.setStatus("pending");

        appointmentRepository.createAppointmentWithCheck(appointment, bookingSuccess, errorMessage);
    }

    // Getters
    public MutableLiveData<List<Doctor>> getDoctors() {
        return doctors;
    }

    public MutableLiveData<Doctor> getSelectedDoctor() {
        return selectedDoctor;
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

    public MutableLiveData<Boolean> getBookingSuccess() {
        return bookingSuccess;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}