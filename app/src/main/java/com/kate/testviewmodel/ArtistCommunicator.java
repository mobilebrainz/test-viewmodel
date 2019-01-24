package com.kate.testviewmodel;

import android.arch.lifecycle.ViewModel;

import com.kate.testviewmodel.event.EventLiveData;
import com.kate.testviewmodel.event.SingleLiveEvent;


public class ArtistCommunicator extends ViewModel {

    public SingleLiveEvent<Artist> artist = new SingleLiveEvent<>();

}
