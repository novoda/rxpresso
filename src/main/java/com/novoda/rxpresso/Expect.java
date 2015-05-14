package com.novoda.rxpresso;

import android.support.test.espresso.IdlingResource;

import com.novoda.rxmocks.RxExpect;
import com.novoda.rxmocks.RxMatcher;
import com.novoda.rxmocks.RxMocks;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.plugins.RxErrorRethrower;

public class Expect<T> implements IdlingResource {

    private final Object repo;
    private final Observable<T> observable;
    private final Observable<T> source;
    private final AtomicBoolean idle = new AtomicBoolean(true);

    private Subscription subscription;
    private ResourceCallback resourceCallback;

    public Expect(Object repo, Observable<T> source, Observable<T> observable) {
        this.repo = repo;
        this.source = source;
        this.observable = observable;
    }

    public Then expect(RxMatcher<Notification<T>> matcher) {
        expectAnyMatching(matcher);
        RxMocks.with(repo)
                .sendEventsFrom(source)
                .to(observable);
        return new Then();
    }

    private void expectAnyMatching(RxMatcher<Notification<T>> matcher) {
        RxErrorRethrower.register();
        idle.compareAndSet(true, false);
        subscription = RxMocks.with(repo)
                .getEventsFor(observable)
                .subscribe(RxExpect.expect(matcher, new Action1<Notification<T>>() {
                    @Override
                    public void call(Notification<T> notification) {
                        subscription.unsubscribe();
                        RxErrorRethrower.unregister();
                        transitionToIdle();
                    }
                }));
    }

    private void transitionToIdle() {
        if (idle.compareAndSet(false, true)) {
            resourceCallback.onTransitionToIdle();
        }
    }

    @Override
    public String getName() {
        return "When";
    }

    @Override
    public boolean isIdleNow() {
        return idle.get();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

}
