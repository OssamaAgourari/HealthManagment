package com.example.health.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.health.R;
import com.example.health.databinding.FragmentLoginBinding;
import com.example.health.viewModels.UserViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {
    private NavController navController;
    private FragmentLoginBinding binding;
    private UserViewModel viewModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentLoginBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        binding.setVm(viewModel);
        binding.setLifecycleOwner(this);
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        observeViewModel(container);
        // redirection to forgot password onclick
        binding.forgotPassword.setOnClickListener(view->{
            navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
        });

        // redirection to register onclick
        binding.goToRegister.setOnClickListener(view->{
            navController.navigate(R.id.action_loginFragment_to_registerFragment);
        });
        return binding.getRoot();
    }
    private void observeViewModel(ViewGroup container){
        viewModel.getIsLogedIn().observe(getViewLifecycleOwner(), isLogedIn ->{
            if(isLogedIn){
                navController.navigate(R.id.action_loginFragment_to_homeFragment);
            }
        });

        viewModel.getErreurMessage().observe(getViewLifecycleOwner(), erreurMessage->{
            if(erreurMessage !=null && !erreurMessage.isEmpty()){
                Snackbar.make(container, erreurMessage, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}