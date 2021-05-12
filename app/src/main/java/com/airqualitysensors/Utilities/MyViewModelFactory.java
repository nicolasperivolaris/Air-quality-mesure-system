package com.airqualitysensors.Utilities;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MyViewModelFactory implements ViewModelProvider.Factory {

    private Application application;
    public MyViewModelFactory(Application app){
        application = app;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DataManager(application);
    }
}
