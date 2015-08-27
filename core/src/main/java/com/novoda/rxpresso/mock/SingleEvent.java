package com.novoda.rxpresso.mock;

import rx.Observable;

public final class SingleEvent {

    private SingleEvent() {
    }

    /**
     * @param value The value to emit
     * @param <T> The type of the observable
     * @return An observable emitting only one onNext event and never completing. (Useful to inject single events in the mocked observables)
     */
    public static <T> Observable<T> onNext(T value) {
        return Observable.just(value).lift(Functions.<T>infinite());
    }

    /**
     * @param error The error to emit
     * @param <T>   The type of the observable
     * @return An observable emitting only one onError event
     */
    public static <T> Observable<T> onError(Throwable error) {
        return Observable.error(error);
    }

    /**
     * @param <T>   The type of the observable
     * @return An observable emitting only one onCompleted event
     */
    public static <T> Observable<T> onCompleted() {
        return Observable.empty();
    }

}
