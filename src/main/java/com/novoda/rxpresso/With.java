package com.novoda.rxpresso;

import android.support.test.espresso.IdlingResource;

import rx.Observable;

public class With<T> implements IdlingResource {

    private final Object repo;
    private final Observable<T> observable;
    private ResourceCallback resourceCallback;
    private Expect<T> expect;

    public With(Object repo, Observable<T> observable) {
        this.repo = repo;
        this.observable = observable;
    }

    public Expect<T> withEventsFrom(Observable<T> source) {
        expect = new Expect<>(repo, source, observable);
        expect.registerIdleTransitionCallback(new ResourceCallback() {
            @Override
            public void onTransitionToIdle() {
                resourceCallback.onTransitionToIdle();
            }
        });
        return expect;
    }

    @Override
    public String getName() {
        return "With";
    }

    @Override
    public boolean isIdleNow() {
        return expect == null || expect.isIdleNow();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

}
