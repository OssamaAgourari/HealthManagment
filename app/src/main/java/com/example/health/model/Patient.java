package com.example.health.model;

public class Patient {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String birthDate;
    private String gender;
    private long createdAt;

    // Constructeur vide (requis pour Firestore)
    public Patient() {}

    // Constructeur complet
    public Patient(String uid, String firstName, String lastName, String email,
                   String phone, String birthDate, String gender, long createdAt) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.createdAt = createdAt;
    }

    // Getters
    public String getUid() { return uid; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBirthDate() { return birthDate; }
    public String getGender() { return gender; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setUid(String uid) { this.uid = uid; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public void setGender(String gender) { this.gender = gender; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
