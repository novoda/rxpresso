package com.novoda.rxpresso;

import com.novoda.rxpresso.mock.SimpleEvents;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import rx.Observable;

import static com.novoda.rxpresso.matcher.RxExpect.*;

public class RxExpectTest {

    public @Rule
    ExpectedException expectedException = ExpectedException.none();

    @Test
    public void expectMatchesAccordingToMatcher() throws Exception {
        Observable<Integer> observableToTest = Observable.just(42);

        observableToTest.materialize()
                .subscribe(expect(any(Integer.class)));
    }

    @Test
    public void expectFailsIfNoEventMatchesMatcher() throws Exception {
        expectedException.expectMessage("Expected Notification with kind OnError but completed without matching");

        Observable<Integer> observableToTest = Observable.just(42);

        observableToTest.materialize()
                .subscribe(expect(anyError(Integer.class)));
    }



    @Test
    public void expectOnlyMatchesAccordingToMatcher() throws Exception {
        Observable<Integer> observableToTest = SimpleEvents.onNext(42);

        observableToTest.materialize()
                .subscribe(expectOnly(any(Integer.class)));
    }

    @Test
    public void expectOnlyFailsIfAnEventDoesNotMatchMatcher() throws Exception {
        expectedException.expectMessage("Expected Notification with kind OnNext but got");
        Observable<Integer> observableToTest = Observable.just(42);

        observableToTest.materialize()
                .subscribe(expectOnly(any(Integer.class)));
    }
}
