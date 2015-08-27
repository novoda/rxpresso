package com.novoda.rxpresso;

import android.support.test.espresso.IdlingResource;

import com.novoda.rxpresso.matcher.RxExpect;
import com.novoda.rxpresso.matcher.RxMatcher;
import com.novoda.rxpresso.mock.RxMock;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.plugins.RxErrorRethrower;

public class Expect<T> implements IdlingResource {

    private final Observable<T> observable;
    private final RxMock mock;
    private final Observable<T> source;
    private final AtomicBoolean idle = new AtomicBoolean(true);

    private Subscription subscription;
    private ResourceCallback resourceCallback;

    Expect(RxMock mock, Observable<T> source, Observable<T> observable) {
        this.mock = mock;
        this.source = source;
        this.observable = observable;
    }

    /**
     * Injects the events from {@code source} into the mocked {@code observable} and wait for an event matching {@code matcher}
     * If no event matching {@code matcher} goes through the {@code observable} then the test hangs until an event {@code onCompleted}
     * is emitted by the Observable at this points an exception is thrown.
     *
     * @param matcher A matcher defining what event we are expecting to receive.
     * @return A Then object to chain any Espresso actions to execute once {@code observable} received an event matching {@code matcher}
     */
    public Then expect(RxMatcher<Notification<T>> matcher) {
        expectAnyMatching(matcher);
        mock.sendEventsFrom(source).to(observable);
        return new Then();
    }

    /**
     * Injects the events from {@code source} into the mocked {@code observable} and wait for an event matching {@code matcher}
     * If an event not matching {@code matcher} is received an exception is thrown.
     *
     * @param matcher A matcher defining what event we are expecting to receive.
     * @return A Then object to chain any Espresso actions to execute once {@code observable} received an event matching {@code matcher}
     */
    public Then expectOnly(RxMatcher<Notification<T>> matcher) {
        expectOnlyMatching(matcher);
        mock.sendEventsFrom(source).to(observable);
        return new Then();
    }

    private void expectAnyMatching(RxMatcher<Notification<T>> matcher) {
        RxErrorRethrower.register();
        idle.compareAndSet(true, false);

        subscription = mock.getEventsFor(observable).subscribe(
                RxExpect.expect(
                        matcher, new Action1<Notification<T>>() {
                            @Override
                            public void call(Notification<T> tNotification) {
                                subscription.unsubscribe();
                                RxErrorRethrower.unregister();
                                transitionToIdle();
                            }
                        }
                )
        );
    }

    private void expectOnlyMatching(RxMatcher<Notification<T>> matcher) {
        RxErrorRethrower.register();
        idle.compareAndSet(true, false);

        subscription = mock.getEventsFor(observable).subscribe(
                RxExpect.expectOnly(
                        matcher, new Action1<Notification<T>>() {
                            @Override
                            public void call(Notification<T> tNotification) {
                                subscription.unsubscribe();
                                RxErrorRethrower.unregister();
                                transitionToIdle();
                            }
                        }
                )
        );
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
