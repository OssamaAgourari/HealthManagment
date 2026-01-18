package com.example.health.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.health.model.Review;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepository {

    private final FirebaseFirestore firestore;
    private static final String REVIEWS_COLLECTION = "reviews";
    private static final String DOCTORS_COLLECTION = "doctors";
    private static final String TAG = "ReviewRepository";

    public ReviewRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Add a new review
    public void addReview(Review review,
                          MutableLiveData<Boolean> successLiveData,
                          MutableLiveData<String> errorLiveData) {

        // First check if patient has already reviewed this doctor for this appointment
        firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("patientId", review.getPatientId())
                .whereEqualTo("appointmentId", review.getAppointmentId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        errorLiveData.setValue("Vous avez deja donne votre avis pour ce rendez-vous");
                        successLiveData.setValue(false);
                        return;
                    }

                    // Create the review
                    Map<String, Object> reviewData = new HashMap<>();
                    reviewData.put("doctorId", review.getDoctorId());
                    reviewData.put("patientId", review.getPatientId());
                    reviewData.put("patientName", review.getPatientName());
                    reviewData.put("appointmentId", review.getAppointmentId());
                    reviewData.put("rating", review.getRating());
                    reviewData.put("comment", review.getComment());
                    reviewData.put("createdAt", FieldValue.serverTimestamp());

                    firestore.collection(REVIEWS_COLLECTION)
                            .add(reviewData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Review added: " + documentReference.getId());
                                // Update doctor's rating
                                updateDoctorRating(review.getDoctorId());
                                successLiveData.setValue(true);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error adding review: " + e.getMessage());
                                errorLiveData.setValue("Erreur lors de l'envoi de l'avis");
                                successLiveData.setValue(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking existing review: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors de la verification");
                });
    }

    // Get reviews for a specific doctor
    public void getDoctorReviews(String doctorId,
                                 MutableLiveData<List<Review>> reviewsLiveData,
                                 MutableLiveData<String> errorLiveData) {
        firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("doctorId", doctorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        review.setId(document.getId());
                        reviews.add(review);
                    }
                    // Sort by createdAt in memory (newest first)
                    reviews.sort((r1, r2) -> {
                        if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                        if (r1.getCreatedAt() == null) return 1;
                        if (r2.getCreatedAt() == null) return -1;

                        long time1 = getTimeFromObject(r1.getCreatedAt());
                        long time2 = getTimeFromObject(r2.getCreatedAt());
                        return Long.compare(time2, time1); // Descending
                    });
                    Log.d(TAG, "Loaded " + reviews.size() + " reviews for doctor " + doctorId);
                    reviewsLiveData.setValue(reviews);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reviews: " + e.getMessage());
                    errorLiveData.setValue("Erreur lors du chargement des avis");
                });
    }

    private long getTimeFromObject(Object timestamp) {
        if (timestamp instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) timestamp).toDate().getTime();
        } else if (timestamp instanceof Long) {
            return (Long) timestamp;
        }
        return 0;
    }

    // Check if patient has already reviewed this appointment
    public void hasReviewedAppointment(String patientId, String appointmentId,
                                       MutableLiveData<Boolean> hasReviewedLiveData) {
        firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("patientId", patientId)
                .whereEqualTo("appointmentId", appointmentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    hasReviewedLiveData.setValue(!queryDocumentSnapshots.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking review status: " + e.getMessage());
                    hasReviewedLiveData.setValue(false);
                });
    }

    // Update doctor's average rating
    private void updateDoctorRating(String doctorId) {
        firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("doctorId", doctorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    double totalRating = 0;
                    int count = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double rating = document.getDouble("rating");
                        if (rating != null) {
                            totalRating += rating;
                            count++;
                        }
                    }

                    if (count > 0) {
                        double averageRating = totalRating / count;

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("rating", averageRating);
                        updates.put("totalReviews", count);

                        firestore.collection(DOCTORS_COLLECTION)
                                .document(doctorId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Doctor rating updated: " + averageRating);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating doctor rating: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating rating: " + e.getMessage());
                });
    }
}
