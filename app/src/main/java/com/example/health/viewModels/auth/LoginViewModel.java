package com.example.health.viewModels.auth;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginViewModel extends ViewModel {
    private final String TAG = "UserViewModel";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private MutableLiveData<String> email = new MutableLiveData<>("");
    private MutableLiveData<String> password = new MutableLiveData<>("");
    private MutableLiveData<String> erreurMessage = new MutableLiveData<>("");
    private MutableLiveData<Boolean> isLogedIn = new MutableLiveData<>(false);

    // Constructeur
    public LoginViewModel(){
        auth = FirebaseAuth.getInstance();
    }

    // Getters et Setters
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

    public MutableLiveData<String> getErreurMessage() {
        return erreurMessage;
    }

    public void setErreurMessage(MutableLiveData<String> erreurMessage) {
        this.erreurMessage = erreurMessage;
    }

    public MutableLiveData<Boolean> getIsLogedIn() {
        return isLogedIn;
    }

    public void setIsLogedIn(MutableLiveData<Boolean> isLogedIn) {
        this.isLogedIn = isLogedIn;
    }

    // Methode de login
    public void login(){
        String emailString = email.getValue();
        String passwordString = password.getValue();

        auth.signInWithEmailAndPassword(emailString,passwordString)
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()){
                       isLogedIn.setValue(true);
                       Log.d(TAG, "Login : success");
                   }else{
                       Log.d(TAG, "Login " + task.getException().getMessage());
                       erreurMessage.setValue("Email ou mot de passe incorrect");
                   }
                });
    }
}
