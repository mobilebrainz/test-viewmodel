package com.kate.testviewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.kate.testviewmodel.event.EventLiveData;
import com.kate.testviewmodel.event.SingleLiveEvent;
import com.kate.testviewmodel.event.ConsumedLiveData;


public class MainViewModel extends ViewModel {

    public MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

    public EventLiveData<String> eventLiveData = new EventLiveData<>();

    public SingleLiveEvent<String> singleLiveEvent = new SingleLiveEvent<>();

    public ConsumedLiveData<String> consumedLiveData = new ConsumedLiveData<>();

}
