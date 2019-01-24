package com.kate.testviewmodel;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/*
Порядок старта:
    setData()
    onAttach:
        getArguments() доступен
        можно вызывать инициализацию fragmentVieModel
        getActivity() уже доступен, поэтому можно инициализировать activityVieModel
    onCreate
    onCreateView
    onActivityCreated
    onStart:
        вызываются fragmentVieModel.observe() и activityVieModel.observe()
        при первом запуске фрагмента и после дестроя-пересоздания фрагмента
        (напр. вращении экрана). При выходе из бэкграунда (onStop - onStart)
        fragmentVieModel.observe() и activityVieModel.observe() не вызываются
    onResume

Поворот экрана:
    уничтожение объекта фрагмента:
    onPause
    onStop
    onDestroyView
    onDestroy
    onDetach - уничтожается объект с данными, но данные в vieModel сохраняются

    пересоздание объекта фрагмента:
    new Fragment() - пересоздаётся обект фрагмента с пустыми полями, но доступным getArguments()
    onAttach:
        данные в объекте фрагмента обнулены, поэтому надо снова вызывать setData() или сохранять данные
        из setData() в бандле для последующего восстановления из бандла, но это только для примитивных типов
        getArguments() доступны (в mArguments сохраняется первоначальный Bundle)
    onCreate
    onCreateView
    onActivityCreated
    onStart:
        вызываются vieModel.observe()
    onResume

    Вывод: для передачи данных в фрагмент использовать:
        - newInstance(String param1) и тогда данные хранятся в бандле и доступны после пересоздания фрагмента
        - set() - очень неуклюже, потому что надо потом сохранять данные в бандле или вьюмоделе
        - viewModel, но надо учесть, что данные будут применены только после onStart() и следовательно всю логику
            запуска фрагмента с использованием данных надо переносить в observe() метод,
            а в onAttach, onCreate, onCreateView и onActivityCreated только инициализировать вьюхи без использования данных,
            передаваемых из viewModel.

        Проблемы:
        1. LiveData передаёт только 1 объект, и если надо передать/сохранить несколько параметров, надо создавать
        обёртку данных. Напр. для локального сохранения данных фрагмента создать объект напр. FragmentStorage и
        туда положить все данные из фрагмента, а потом его отправить в viewModel:LiveData.

        2. Если надо обсервить несколько LiveData, как их синхронизировать?
        В каком порядке выполняюся observe() их разных вьюмоделей? Как их выполнение синхронизировать?
        И если есть две viewModel из активности и фрагмента, то как заставить их выполнять только 1 запуск?

        Вариант решения снхронизации передачи данных из ативности в фрагмент:
        Использовать для передачи двнных из вьюмодели активности SingleLiveEvent<>.
        В обсервере доставать данные из SingleLiveEvent и сохранять в MutableLiveData<>
        в локальной вьюмодели фрагмента. И при пересоздании фрагмента двнные будут браться из MutableLiveData<>.

        Главная проблема: передавать в фрагмент несколько объектов, причём некоторые фрагменты используют одни объекты
        (напр username и address), а другие - другие (username и phone). Тогда придётся делать один гигантский враппер на
        все объекты, но это неэффективно. А передавать по отдельности объекты каждый в свём коммуникаторе - трудно
        потом их синхронирзировать в нескольких observe() в фрагменте.

    Глобальный Вывод:
        1. Статическая передача: когда передаваемые параметры известны перед созданием фрагментов.
            При изменении данных в активности, фрагменты полностью пересоздаются заново с новыми
            входными параметрами.

            а) через newInstance(params) и при пересоздании фрагмента доставать из бандла через getArguments()
            б) через интерфейсы или сеттеры и сохранять потом в бандле или viewModel фрагмента в враппере параметров,
                если их несколько

        2. Динамическая передача: когда фрагменты создаются вмести с активностью без данных,
         при пересоздании активность фрагменты не пересоздаются вручную, а пересоздаются
         фрагмент-менеджером и обновляются динамически.
            Фрагменты не пересоздаются при изменении данных в активности!
            - через LiveData

    Важно определить какую стратегию будет применять активность динамическую или статическую.
    Если изменяемые параметры активности ведут к полному переконфигурированию (разные меню, расположение,
    вид, размеры, ресайклеры и т.д.), то надо применять статическую передачу.

    Если меняются только отображаемые данные без изменения внешнего вида, то надо применять динамическую
    передачу. Напр. активность артиста. Структура фрагментов будет оставаться одинаковой, только поменяются
    их данные, поэтому такая активность динамическая и передача всех данных через LiveData.

    В динамичесой активности передача данных из активности в фрагменты только одним обЪектом - эвентом!
    Напр. SingleLiveEvent<Artist>.
    Динамическая входная точка должна быть только одна! Можно при первом создании фрагмета передать
    статич данные, но впоследствии можно будет менять только динамич. данные. Если потребуется поменять
    статич. данные, надо будет пересоздавать весь фрагмент.

    Пример: есть активность артиста для логгированного пользователя, и для чужого пользователя.
    Для обоих случаев будут разные меню, разные права доступа, разные фрагменты. Поэтому при
    смене username надо полностью пересоздавать активность со всеми фрагментами.
    И в данном случае стат. данные для фрагментов активности - username. Динамич. данные - artist.
    username передаём в newInstance(), artist - в SingleLiveEvent<Artist>.

    В примере:
     - кнопка User иммитирует смену пользователя, что ведёт к пересозданию фрагмента.
     - кнопка Artist иммитирует смену артиста, что ведёт к динамическому обновлению фрагмента
        без его пересоздания.
 */
public class ArtistFragment extends Fragment {

    private static final String TAG = "ArtistFragment";
    private static final String ARG_USERNAME = "ArtistFragment.ARG_USERNAME";

    private Artist artist;
    private String username;
    private ArtistFragmentVM artistFragmentVM;

    private TextView artistNameView;
    private TextView userNameView;

    public static ArtistFragment newInstance(@NonNull String username) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    public void setData(String data) {
        Log.i(TAG, "onCreateView: setData");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
        }

        if (getActivity() != null) {
            artistFragmentVM = ViewModelProviders.of(this).get(ArtistFragmentVM.class);
            ArtistCommunicator artistCommunicator = ViewModelProviders.of(getActivity()).get(ArtistCommunicator.class);

            //динамическая передача данных в виде эвента из активности в фрагмент
            artistCommunicator.artist.observe(this, artist -> {
                // сохранение данные в вьюмодели фрагмента для возможного перезапуска фрагмента (напр. поворота)
                artistFragmentVM.artist.setValue(artist);
                Log.i(TAG, "onCreateView: ArtistCommunicator - " + artist.getName());
            });

            //динамическое обновление фрагмента в зависмости от данных в artistFragmentVM.artist
            artistFragmentVM.artist.observe(this, artist -> {
                this.artist = artist;
                update();
            });
        }
        Log.i(TAG, "onCreateView: onAttach");
    }

    // работа с динамическими данными
    private void update() {
        artistNameView.setText(artist.getName());
        Log.i(TAG, " update");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreateView: onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        artistNameView = view.findViewById(R.id.artistNameView);
        userNameView = view.findViewById(R.id.userNameView);

        // статич. данные из аргументов
        userNameView.setText(username);

        Log.i(TAG, "onCreateView: onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onCreateView: onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onCreateView: onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onCreateView: onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onCreateView: onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onCreateView: onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onCreateView: onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onCreateView: onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onCreateView: onDetach");
    }
}
