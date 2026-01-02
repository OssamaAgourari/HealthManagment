package com.example.health.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.health.R;
import com.example.health.model.Doctor;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors = new ArrayList<>();
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(OnDoctorClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.bind(doctor, listener);
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
        notifyDataSetChanged();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView doctorPhoto;
        private TextView doctorName;
        private TextView doctorSpecialty;
        private TextView doctorExperience;
        private TextView doctorRating;
        private TextView doctorPrice;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorPhoto = itemView.findViewById(R.id.doctorPhoto);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorSpecialty = itemView.findViewById(R.id.doctorSpecialty);
            doctorExperience = itemView.findViewById(R.id.doctorExperience);
            doctorRating = itemView.findViewById(R.id.doctorRating);
            doctorPrice = itemView.findViewById(R.id.doctorPrice);
        }

        public void bind(Doctor doctor, OnDoctorClickListener listener) {
            doctorName.setText(doctor.getFullName());
            doctorSpecialty.setText(doctor.getSpecialty());
            doctorExperience.setText(doctor.getExperience() + " ans d'expérience");
            doctorRating.setText("⭐ " + String.format("%.1f", doctor.getRating()));
            doctorPrice.setText(String.format("%.0f€", doctor.getConsultationFee()));

            // TODO: Load photo from URL using Glide or Picasso
            // For now, using default profile image

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDoctorClick(doctor);
                }
            });
        }
    }
}
