package com.example.cse_4095_final_project.ui.Display;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DisplayViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DisplayViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}