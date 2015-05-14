package com.novoda.rxpresso.demo;

import rx.Observable;

public interface DataRepository {

    Observable<Integer> getRandomNumber(int max);

}
