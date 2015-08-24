package com.novoda.rxpresso;

import com.novoda.rxpresso.mock.RxMocks;
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

    private TestRepository baseRepo;
    private RxMocks rxMocks;

    @Before
    public void setUp() throws Exception {
        baseRepo = Mockito.mock(TestRepository.class);
        rxMocks = RxMocks.init(baseRepo);
    }

    @Test
    public void itSendsEventsToMockedObservable() throws Exception {
        Observable<Integer> foo = baseRepo.foo(3);

        rxMocks.sendEventsFrom(SimpleEvents.onNext(42))
                .to(foo);

        Integer result = foo.toBlocking().first();

        assertThat(result).isEqualTo(42);
    }

    @Test
    public void itSendsEventsToMockedObservableAccordingToParameter() throws Exception {
        Observable<Integer> foo = baseRepo.foo(3);
        Observable<Integer> bar = baseRepo.foo(1);

        rxMocks.sendEventsFrom(SimpleEvents.onNext(42))
                .to(foo);
        rxMocks.sendEventsFrom(SimpleEvents.onNext(24))
                .to(bar);

        Integer result = foo.toBlocking().first();
        Integer result2 = bar.toBlocking().first();

        assertThat(result).isEqualTo(42);
        assertThat(result2).isEqualTo(24);
    }

    @Test
    public void itDeterminesWetherAnObservableIsProvidedByAGivenRepository() throws Exception {
        Observable<Integer> foo = baseRepo.foo(3);

        boolean result = rxMocks.provides(foo);
        boolean result2 = rxMocks.provides(Observable.just(1));

        assertThat(result).isTrue();
        assertThat(result2).isFalse();
    }

    @Test
    public void itProvidesTheSameObservableForTheSameMethodParamCombination() throws Exception {
        Observable<Integer> foo = baseRepo.foo(3);
        Observable<Integer> bar = baseRepo.foo(3);

        assertThat(foo).isEqualTo(bar);
    }

    @Test
    public void resetMocksResetsPipelines() throws Exception {
        Observable<Integer> foo = baseRepo.foo(3);

        rxMocks.sendEventsFrom(SimpleEvents.onNext(42))
                .to(foo);

        rxMocks.resetMocks();

        Observable<Integer> bar = baseRepo.foo(3);

        rxMocks.sendEventsFrom(SimpleEvents.<Integer>onCompleted())
                .to(bar);

        Boolean result = bar.isEmpty().toBlocking().first();

        assertThat(result).isTrue();
    }

    @Test
    public void getEventsForDoesNotAffectSubscriptionToMockeObservables() throws Exception {
        Observable<Integer> foo = baseRepo.foo(3);

        final Notification<Integer>[] test = (Notification<Integer>[]) Array.newInstance(Notification.class, 1);
        rxMocks.getEventsFor(foo)
                .subscribe(
                        new Action1<Notification<Integer>>() {
                            @Override
                            public void call(Notification<Integer> integerNotification) {
                                test[0] = integerNotification;
                            }
                        });

        rxMocks.sendEventsFrom(SimpleEvents.onNext(42))
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
