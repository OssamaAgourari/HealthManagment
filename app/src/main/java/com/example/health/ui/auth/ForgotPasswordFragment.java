package com.example.health.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.health.databinding.FragmentForgotPasswordBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();

        binding.resetButton.setOnClickListener(v -> resetPassword());
        binding.goToLogin.setOnClickListener(v ->
            Navigation.findNavController(v).popBackStack()
        );
    }

    private void resetPassword() {
        String email = binding.emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            binding.emailLayout.setError("Veuillez entrer votre email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Email invalide");
            return;
        }

        binding.emailLayout.setError(null);
        setLoading(true);

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener(v -> {
                setLoading(false);
                Snackbar.make(binding.getRoot(),
                    "Lien envoye a " + email,
                    Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark))
                    .show();
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                Snackbar.make(binding.getRoot(),
                    "Erreur: " + getErrorMessage(e),
                    Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                    .show();
            });
    }

    private void setLoading(boolean loading) {
        binding.resetButton.setEnabled(!loading);
        binding.resetButton.setText(loading ? "Envoi en cours..." : "Envoyer le lien");
    }

    private String getErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("no user record")) {
            return "Aucun compte avec cet email";
        }
        return "Une erreur s'est produite";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
