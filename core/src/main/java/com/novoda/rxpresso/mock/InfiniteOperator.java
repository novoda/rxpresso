package com.novoda.rxpresso.mock;

import rx.Observable;
import rx.Subscriber;

class InfiniteOperator<T> implements Observable.Operator<T, T> {
    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
        return new Subscriber<T>() {
            @Override
            public void onCompleted() {
                //Swallow
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(T t) {
                subscriber.onNext(t);
            }
        };
    }
}
