package com.novoda.rxpresso.mock;

import rx.Observable;

final class Functions {

    static <T> Observable.Operator<T, T> infinite() {
        return new InfiniteOperator<>();
    }

}
