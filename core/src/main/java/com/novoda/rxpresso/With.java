package com.novoda.rxpresso;

import android.support.test.espresso.IdlingResource;

import com.novoda.rxpresso.mock.RxMock;

import rx.Observable;

public class With<T> implements IdlingResource {

    private final RxMock repo;
    private final Observable<T> observable;
    private ResourceCallback resourceCallback;
    private Expect<T> expect;

    With(RxMock repo, Observable<T> observable) {
        this.repo = repo;
        this.observable = observable;
    }

    /**
     * Setup the injection of the events from the {@code source} into the mocked {@code observable}
     *
     * @param source An observable providing the events to inject
     * @return An Expect object to trigger the injection and setup what event to expect and wait for.
     */
    public Expect<T> withEventsFrom(Observable<T> source) {
        expect = new Expect<>(repo, source, observable);
        expect.registerIdleTransitionCallback(
                new ResourceCallback() {
                    @Override
                    public void onTransitionToIdle() {
                        resourceCallback.onTransitionToIdle();
                    }
                }
        );
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
