package com.example.health.viewModels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.health.R;
import com.example.health.model.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HomeViewModel extends AndroidViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final Application application;

    // LiveData
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<String> healthTip = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasUpcomingAppointment = new MutableLiveData<>(false);
    private final MutableLiveData<String> nextAppointmentDoctorName = new MutableLiveData<>();
    private final MutableLiveData<String> nextAppointmentSpecialty = new MutableLiveData<>();
    private final MutableLiveData<String> nextAppointmentDateTime = new MutableLiveData<>();
    private final MutableLiveData<Appointment> nextAppointment = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentLanguage = new MutableLiveData<>();

    private static final String PREFS_NAME = "HealthAppPrefs";
    private static final String LANGUAGE_KEY = "language";

    public HomeViewModel(Application application) {
        super(application);
        this.application = application;
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Load saved language
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedLanguage = prefs.getString(LANGUAGE_KEY, "fr");
        currentLanguage.setValue(savedLanguage);
    }

    // Load all home data
    public void loadHomeData() {
        loadUserName();
        loadHealthTip();
        loadNextAppointment();
    }

    // Load user name from Firestore
    private void loadUserName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            userName.setValue(application.getString(R.string.user_default));
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        StringBuilder name = new StringBuilder();
                        if (firstName != null && !firstName.isEmpty()) {
                            name.append(firstName);
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            if (name.length() > 0) name.append(" ");
                            name.append(lastName);
                        }

                        if (name.length() > 0) {
                            userName.setValue(name.toString());
                        } else if (user.getEmail() != null) {
                            userName.setValue(user.getEmail().split("@")[0]);
                        } else {
                            userName.setValue(application.getString(R.string.user_default));
                        }
                    } else {
                        // Fallback to Firebase Auth display name or email
                        String displayName = user.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            userName.setValue(displayName);
                        } else if (user.getEmail() != null) {
                            userName.setValue(user.getEmail().split("@")[0]);
                        } else {
                            userName.setValue(application.getString(R.string.user_default));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    userName.setValue(application.getString(R.string.user_default));
                });
    }

    // Load random health tip
    private void loadHealthTip() {
        String[] tips = application.getResources().getStringArray(R.array.health_tips);
        if (tips.length > 0) {
            Random random = new Random();
            int index = random.nextInt(tips.length);
            healthTip.setValue(tips[index]);
        }
    }

    // Load next upcoming appointment
    private void loadNextAppointment() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            hasUpcomingAppointment.setValue(false);
            return;
        }

        isLoading.setValue(true);

        firestore.collection("appointments")
                .whereEqualTo("patientId", user.getUid())
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Appointment closestAppointment = null;
                    Date closestDate = null;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        appointment.setId(doc.getId());

                        try {
                            Date appointmentDate = dateFormat.parse(appointment.getDate());
                            Date today = new Date();

                            // Only consider future appointments
                            if (appointmentDate != null && !appointmentDate.before(getStartOfDay(today))) {
                                if (closestDate == null || appointmentDate.before(closestDate)) {
                                    closestDate = appointmentDate;
                                    closestAppointment = appointment;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (closestAppointment != null) {
                        hasUpcomingAppointment.setValue(true);
                        nextAppointment.setValue(closestAppointment);
                        nextAppointmentDoctorName.setValue(closestAppointment.getDoctorName());
                        nextAppointmentSpecialty.setValue(closestAppointment.getSpecialty());

                        // Format date/time display
                        String dateTimeDisplay = formatAppointmentDateTime(closestAppointment.getDate(), closestAppointment.getTime());
                        nextAppointmentDateTime.setValue(dateTimeDisplay);
                    } else {
                        hasUpcomingAppointment.setValue(false);
                    }

                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    hasUpcomingAppointment.setValue(false);
                    errorMessage.setValue("Erreur lors du chargement des rendez-vous");
                    isLoading.setValue(false);
                });
    }

    // Format appointment date/time for display
    private String formatAppointmentDateTime(String date, String time) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date appointmentDate = dateFormat.parse(date);
            Date today = getStartOfDay(new Date());

            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            Date tomorrow = cal.getTime();

            String dateStr;
            if (appointmentDate != null) {
                if (isSameDay(appointmentDate, today)) {
                    dateStr = application.getString(R.string.today);
                } else if (isSameDay(appointmentDate, tomorrow)) {
                    dateStr = application.getString(R.string.tomorrow);
                } else {
                    dateStr = date;
                }
            } else {
                dateStr = date;
            }

            return dateStr + " – " + time;
        } catch (ParseException e) {
            return date + " – " + time;
        }
    }

    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // Change language
    public void setLanguage(String languageCode) {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply();
        currentLanguage.setValue(languageCode);
    }

    // Get current user ID
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Getters
    public MutableLiveData<String> getUserName() {
        return userName;
    }

    public MutableLiveData<String> getHealthTip() {
        return healthTip;
    }

    public MutableLiveData<Boolean> getHasUpcomingAppointment() {
        return hasUpcomingAppointment;
    }

    public MutableLiveData<String> getNextAppointmentDoctorName() {
        return nextAppointmentDoctorName;
    }

    public MutableLiveData<String> getNextAppointmentSpecialty() {
        return nextAppointmentSpecialty;
    }

    public MutableLiveData<String> getNextAppointmentDateTime() {
        return nextAppointmentDateTime;
    }

    public MutableLiveData<Appointment> getNextAppointment() {
        return nextAppointment;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getCurrentLanguage() {
        return currentLanguage;
    }
}

