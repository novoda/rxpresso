package com.novoda.rxpresso.matcher;

public interface RxMatcher<T> {

    boolean matches(T actual);

    String description();

}
