package com.kate.testviewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;


public class ArtistFragmentVM extends ViewModel {

    public MutableLiveData<Artist> artist = new MutableLiveData<>();

}
