package com.example.health.model;

public class Review {
    private String id;
    private String doctorId;
    private String patientId;
    private String patientName;
    private String appointmentId;
    private float rating;
    private String comment;
    private Object createdAt;

    // Required empty constructor for Firestore
    public Review() {}

    // Full constructor
    public Review(String id, String doctorId, String patientId, String patientName,
                  String appointmentId, float rating, String comment, Object createdAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getDoctorId() { return doctorId; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getAppointmentId() { return appointmentId; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public Object getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setRating(float rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
}
