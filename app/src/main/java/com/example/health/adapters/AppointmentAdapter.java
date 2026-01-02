package com.example.health.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.health.R;
import com.example.health.model.Appointment;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onCancelAppointment(Appointment appointment);
    }

    public AppointmentAdapter(OnAppointmentActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private TextView doctorName;
        private TextView specialty;
        private TextView status;
        private TextView date;
        private TextView time;
        private TextView fee;
        private TextView reason;
        private Button cancelButton;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.appointmentDoctorName);
            specialty = itemView.findViewById(R.id.appointmentSpecialty);
            status = itemView.findViewById(R.id.appointmentStatus);
            date = itemView.findViewById(R.id.appointmentDate);
            time = itemView.findViewById(R.id.appointmentTime);
            fee = itemView.findViewById(R.id.appointmentFee);
            reason = itemView.findViewById(R.id.appointmentReason);
            cancelButton = itemView.findViewById(R.id.cancelAppointmentButton);
        }

        public void bind(Appointment appointment, OnAppointmentActionListener listener) {
            doctorName.setText(appointment.getDoctorName());
            specialty.setText(appointment.getSpecialty());
            date.setText(appointment.getDate());
            time.setText(appointment.getTime());
            fee.setText(String.format("%.0f€", appointment.getConsultationFee()));

            // Display reason if provided
            if (appointment.getReason() != null && !appointment.getReason().isEmpty()) {
                reason.setText("Motif: " + appointment.getReason());
                reason.setVisibility(View.VISIBLE);
            } else {
                reason.setVisibility(View.GONE);
            }

            // Set status with color coding
            String statusText = "";
            int statusColor = Color.parseColor("#FF9800"); // Orange by default

            switch (appointment.getStatus().toLowerCase()) {
                case "pending":
                    statusText = "En attente";
                    statusColor = Color.parseColor("#FF9800"); // Orange
                    break;
                case "confirmed":
                    statusText = "Confirmé";
                    statusColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case "cancelled":
                    statusText = "Annulé";
                    statusColor = Color.parseColor("#F44336"); // Red
                    break;
                case "completed":
                    statusText = "Terminé";
                    statusColor = Color.parseColor("#2196F3"); // Blue
                    break;
            }

            status.setText(statusText);
            status.setBackgroundColor(statusColor);

            // Show/hide cancel button based on status
            if (appointment.getStatus().equalsIgnoreCase("cancelled") ||
                appointment.getStatus().equalsIgnoreCase("completed")) {
                cancelButton.setVisibility(View.GONE);
            } else {
                cancelButton.setVisibility(View.VISIBLE);
                cancelButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCancelAppointment(appointment);
                    }
                });
            }
        }
    }
}
