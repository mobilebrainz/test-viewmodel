package com.kate.testviewmodel;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;

/*
EventLiveData и ConsumedLiveData ведут себя аналогично. EventLiveData даёт некоторые возможности по работе с Event - враппере
контента, а ConsumedLiveData - нет. ConsumedLiveData и SingleLiveEvent не предоставляют возможности работать с потреблением
события в postValue(), что является большим минусом.

Вывод: EventLiveData - лучшая реализация события через LiveData!
 */
public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private FrameLayout frameContainerView;
    private Button artistButton;
    private Button userButton;
    private int counter;
    private boolean created;

    private static Artist[] artists = {
            new Artist("Deep Purple", 30, "England"),
            new Artist("Nirvana", 20, "USA"),
            new Artist("Abba", 35, "Sweden")};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameContainerView = findViewById(R.id.frameContainerView);

        //кнопка User иммитирует смену пользователя, что ведёт к пересозданию фрагмента.
        Button userButton = findViewById(R.id.userButton);
        userButton.setOnClickListener(v -> {
            String username = created ? "Alex" : "guest";
            created = ! created;
            counter = 0;
            loadFragment(ArtistFragment.newInstance(username));
        });

        //кнопка Artist иммитирует смену артиста, что ведёт к динамическому обновлению фрагмента без его пересоздания.
        Button artistButton = findViewById(R.id.artistButton);
        artistButton.setOnClickListener(v -> {
            if (counter < artists.length) {
                Artist artist = artists[counter];
                // динамическая передача данных
                ArtistCommunicator artistCommunicator = ViewModelProviders.of(this).get(ArtistCommunicator.class);
                artistCommunicator.artist.setValue(artist);
                counter++;
            }
        });

        //testLiveData();
    }

    protected void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameContainerView, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void testLiveData() {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        //MutableLiveData
        mainViewModel.mutableLiveData.observe(this, s -> Log.i("mutableLiveData: ", s));

        //EventLiveData
        mainViewModel.eventLiveData.observeEvent(this, s -> Log.i("eventLiveData1: ", s));
        //не выполняется!
        mainViewModel.eventLiveData.observeEvent(this, s -> Log.i("eventLiveData2: ", s));

        //SingleLiveEvent
        mainViewModel.singleLiveEvent.observe(this, s -> Log.i("singleLiveEvent1: ", s));
        //не выполняется!
        mainViewModel.singleLiveEvent.observe(this, s -> Log.i("singleLiveEvent2: ", s));

        //ConsumedLiveData
        mainViewModel.consumedLiveData.observe(this, s -> Log.i("consumedLiveData1: ", s));
        //не выполняется!
        mainViewModel.consumedLiveData.observe(this, s -> Log.i("consumedLiveData2: ", s));

        artistButton.setOnClickListener(v -> {
            mainViewModel.mutableLiveData.setValue("Hello!");

            mainViewModel.eventLiveData.postEvent("Bye1!");
            //без потребления события:
            //mainViewModel.eventLiveData.postData("Bye2!");

            //mainViewModel.singleLiveEvent.setValue("Bye-Bye!");

            //mainViewModel.consumedLiveData.setEvent("Bye-Bye-Bye1!");
            //без потребления события:
            //mainViewModel.consumedLiveData.setValue("Bye-Bye-Bye2!");
        });
    }

}
