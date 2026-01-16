package com.example.health.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BookingChoiceViewModel extends ViewModel {

    // Navigation events
    private final MutableLiveData<Boolean> navigateToBookForMe = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToBookForSomeoneElse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();

    public BookingChoiceViewModel() {
        // Constructor
    }

    // Book for me clicked
    public void onBookForMeClicked() {
        navigateToBookForMe.setValue(true);
    }

    // Book for someone else clicked
    public void onBookForSomeoneElseClicked() {
        navigateToBookForSomeoneElse.setValue(true);
    }

    // Back clicked
    public void onBackClicked() {
        navigateBack.setValue(true);
    }

    // Reset navigation events
    public void resetNavigationEvents() {
        navigateToBookForMe.setValue(false);
        navigateToBookForSomeoneElse.setValue(false);
        navigateBack.setValue(false);
    }

    // Getters
    public MutableLiveData<Boolean> getNavigateToBookForMe() {
        return navigateToBookForMe;
    }

    public MutableLiveData<Boolean> getNavigateToBookForSomeoneElse() {
        return navigateToBookForSomeoneElse;
    }

    public MutableLiveData<Boolean> getNavigateBack() {
        return navigateBack;
    }
}

