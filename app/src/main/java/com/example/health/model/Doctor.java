package com.example.health.model;

public class Doctor {
    private String id;              // Firestore document ID
    private String firstName;
    private String lastName;
    private String specialty;       // Cardiologue, Dermatologue, etc.
    private String photoUrl;        // URL of doctor's photo
    private String address;         // Cabinet address
    private String city;
    private String phone;
    private String email;
    private int experience;         // Years of experience
    private double consultationFee; // Price in €
    private double rating;          // Average rating (0-5)
    private int totalReviews;       // Number of reviews
    private String description;     // Brief bio
    private boolean available;      // Currently accepting patients
    private Object createdAt;       // Can be Long or Timestamp from Firestore

    // Required empty constructor for Firestore
    public Doctor() {}

    // Full constructor
    public Doctor(String id, String firstName, String lastName, String specialty,
                  String photoUrl, String address, String city, String phone,
                  String email, int experience, double consultationFee,
                  double rating, int totalReviews, String description,
                  boolean available, Object createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialty = specialty;
        this.photoUrl = photoUrl;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.experience = experience;
        this.consultationFee = consultationFee;
        this.rating = rating;
        this.totalReviews = totalReviews;
        this.description = description;
        this.available = available;
        this.createdAt = createdAt;
    }

    // Helper method to get full name
    public String getFullName() {
        return "Dr. " + firstName + " " + lastName;
    }

    // Getters
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialty() { return specialty; }
    public String getPhotoUrl() { return photoUrl; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getExperience() { return experience; }
    public double getConsultationFee() { return consultationFee; }
    public double getRating() { return rating; }
    public int getTotalReviews() { return totalReviews; }
    public String getDescription() { return description; }
    public boolean isAvailable() { return available; }
    public Object getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setExperience(int experience) { this.experience = experience; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }
    public void setRating(double rating) { this.rating = rating; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
    public void setDescription(String description) { this.description = description; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
}
