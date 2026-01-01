package com.example.health.viewModels.auth;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.health.model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

public class RegisterViewModel extends ViewModel {
    private final String TAG = "RegisterViewModel";
    private FirebaseAuth auth ;
    private FirebaseFirestore db;
    private MutableLiveData<String> email = new MutableLiveData<>("");
    private MutableLiveData<String> password = new MutableLiveData<>("");
    public MutableLiveData<Patient> userLiveDate = new MutableLiveData<>(new Patient());
    private MutableLiveData<String> erreurMessage = new MutableLiveData<>("");
    private MutableLiveData<Boolean> isRegistered = new MutableLiveData<>(false);

    // Constructeur
    public RegisterViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Getters et Setters

    public String getTAG() {
        return TAG;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public void setAuth(FirebaseAuth auth) {
        this.auth = auth;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public void setEmail(MutableLiveData<String> email) {
        this.email = email;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public void setPassword(MutableLiveData<String> password) {
        this.password = password;
    }

    public MutableLiveData<Patient> getUserLiveDate() {
        return userLiveDate;
    }

    public void setUserLiveDate(MutableLiveData<Patient> userLiveDate) {
        this.userLiveDate = userLiveDate;
    }

    public MutableLiveData<String> getErreurMessage() {
        return erreurMessage;
    }

    public void setErreurMessage(MutableLiveData<String> erreurMessage) {
        this.erreurMessage = erreurMessage;
    }

    public MutableLiveData<Boolean> getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(MutableLiveData<Boolean> isRegistered) {
        this.isRegistered = isRegistered;
    }

    public void register() {

    }
}
