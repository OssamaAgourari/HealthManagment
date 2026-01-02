package com.example.health.model;

public class Appointment {
    private String id;              // Firestore document ID
    private String patientId;       // Reference to patient (Firebase Auth UID)
    private String patientName;     // Patient full name
    private String doctorId;        // Reference to doctor document
    private String doctorName;      // Doctor full name
    private String specialty;       // Doctor's specialty
    private String date;            // Date in format: "dd/MM/yyyy"
    private String time;            // Time slot: "09:00", "10:00", etc.
    private String status;          // "pending", "confirmed", "cancelled", "completed"
    private String reason;          // Reason for consultation (optional)
    private double consultationFee; // Price
    private Object createdAt;       // Timestamp
    private Object updatedAt;       // Timestamp

    // Required empty constructor for Firestore
    public Appointment() {}

    // Full constructor
    public Appointment(String id, String patientId, String patientName, String doctorId,
                      String doctorName, String specialty, String date, String time,
                      String status, String reason, double consultationFee,
                      Object createdAt, Object updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.date = date;
        this.time = time;
        this.status = status;
        this.reason = reason;
        this.consultationFee = consultationFee;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialty() { return specialty; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public double getConsultationFee() { return consultationFee; }
    public Object getCreatedAt() { return createdAt; }
    public Object getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setStatus(String status) { this.status = status; }
    public void setReason(String reason) { this.reason = reason; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }
}
