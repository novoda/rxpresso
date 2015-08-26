package com.novoda.rxpresso;

import com.novoda.rxpresso.mock.RxMock;
import com.novoda.rxpresso.mock.SimpleEvents;

import java.lang.reflect.Array;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;

import static org.fest.assertions.api.Assertions.assertThat;

public class RxMocksTest {

    private TestRepository mockedRepo;
    private RxMock rxMock;

    @Before
    public void setUp() throws Exception {
        mockedRepo = Mockito.mock(TestRepository.class);
        rxMock = RxMock.from(mockedRepo);
    }

    @Test
    public void itSendsEventsToMockedObservable() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);

        rxMock.sendEventsFrom(SimpleEvents.onNext(42))
                .to(foo);

        Integer result = foo.toBlocking().first();

        assertThat(result).isEqualTo(42);
    }

    @Test
    public void itSendsEventsToMockedObservableAccordingToParameter() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);
        Observable<Integer> bar = mockedRepo.foo(1);

        rxMock.sendEventsFrom(SimpleEvents.onNext(42))
                .to(foo);
        rxMock.sendEventsFrom(SimpleEvents.onNext(24))
                .to(bar);

        Integer result = foo.toBlocking().first();
        Integer result2 = bar.toBlocking().first();

        assertThat(result).isEqualTo(42);
        assertThat(result2).isEqualTo(24);
    }

    @Test
    public void itDeterminesWetherAnObservableIsProvidedByAGivenRepository() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);

        boolean result = rxMock.provides(foo);
        boolean result2 = rxMock.provides(Observable.just(1));

        assertThat(result).isTrue();
        assertThat(result2).isFalse();
    }

    @Test
    public void itProvidesTheSameObservableForTheSameMethodParamCombination() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);
        Observable<Integer> bar = mockedRepo.foo(3);

        assertThat(foo).isEqualTo(bar);
    }

    @Test
    public void resetMocksResetsPipelines() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);

        rxMock.sendEventsFrom(SimpleEvents.onNext(42))
                .to(foo);

        rxMock.resetMocks();

        Observable<Integer> bar = mockedRepo.foo(3);

        rxMock.sendEventsFrom(SimpleEvents.<Integer>onCompleted())
                .to(bar);

        Boolean result = bar.isEmpty().toBlocking().first();

        assertThat(result).isTrue();
    }

    @Test
    public void getEventsForDoesNotAffectSubscriptionToMockeObservables() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);

        final Notification<Integer>[] test = (Notification<Integer>[]) Array.newInstance(Notification.class, 1);
        rxMock.getEventsFor(foo)
                .subscribe(
                        new Action1<Notification<Integer>>() {
                            @Override
                            public void call(Notification<Integer> integerNotification) {
                                test[0] = integerNotification;
                            }
                        });

        rxMock.sendEventsFrom(SimpleEvents.onNext(42))
                .to(foo);

        assertThat(test[0]).isNull();

        Integer result = foo.toBlocking().first();

        assertThat(test[0].getKind()).isEqualTo(Notification.Kind.OnNext);
        assertThat(test[0].getValue()).isEqualTo(42);
    }

    public interface TestRepository {
        Observable<Integer> foo(int bar);
    }
}
