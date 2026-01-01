package com.example.health.model;

import java.util.List;

public class PatientForm {

    private String id; // Firestore document id
    private String fullName;
    private int age;
    private String gender;
    private String consultationType;
    private String description;

    private boolean diabete;
    private boolean hypertension;

    private long createdAt;

    // 🔹 Required empty constructor for Firestore
    public PatientForm() {}

    public PatientForm(String fullName, int age, String gender,
                       String consultationType, String description,
                       boolean diabete, boolean hypertension,
                       long createdAt) {
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.consultationType = consultationType;
        this.description = description;
        this.diabete = diabete;
        this.hypertension = hypertension;
        this.createdAt = createdAt;
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getConsultationType() { return consultationType; }
    public String getDescription() { return description; }
    public boolean isDiabete() { return diabete; }
    public boolean isHypertension() { return hypertension; }
    public long getCreatedAt() { return createdAt; }
}

