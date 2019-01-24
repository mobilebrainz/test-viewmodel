package com.kate.testviewmodel.event;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * При использовании setValue() или postValue() ConsumedLiveData ведёт себя как обычный MutableLiveData,
 * т.е. информирует всех наблюдателей и каждый раз после изменения конфигурации приложения.
 * При использовании setEvent() или postEvent() ConsumedLiveData ведёт себя как событие,
 * т.е. информирует только одного наблюдателя и только один раз, игнорируя изменения конфигурации приложения.
 */
public class ConsumedLiveData<T> extends MutableLiveData<T> {

    private static final String TAG = "SingleLiveEvent";

    private final AtomicBoolean mPending = new AtomicBoolean(false);
    private final AtomicBoolean mConsumed = new AtomicBoolean(false);

    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        super.observe(owner, t -> {
            if (!mConsumed.get() || (mConsumed.get() && mPending.compareAndSet(true, false))) {
                observer.onChanged(t);
            }
        });
    }

    @MainThread
    @Override
    public void setValue(@Nullable T t) {
        mConsumed.set(false);
        super.setValue(t);
    }

    @MainThread
    public void setEvent(@Nullable T t) {
        mConsumed.set(true);
        mPending.set(true);
        super.setValue(t);
    }

    /*
    // postValue(t) работают только без потребления события
    @Override
    public void postValue(@Nullable T t) {
        mConsumed.set(false);
        super.postValue(t);
    }

    public void postEvent(@Nullable T t) {
        mConsumed.set(true);
        mPending.set(true);
        super.postValue(t);
    }
    */

}
