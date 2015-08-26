package com.novoda.rxpresso;

import android.support.test.espresso.IdlingResource;

import com.novoda.rxpresso.matcher.RxMatcher;
import com.novoda.rxpresso.mock.SimpleEvents;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import rx.Notification;
import rx.Observable;

import static com.novoda.rxpresso.matcher.RxExpect.any;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RxPressoTest {

    public @Rule ExpectedException expectedException = ExpectedException.none();

    private TestRepository mockedRepo;
    private RxPresso rxPresso;
    private IdlingResource.ResourceCallback resourceCallback;

    @Before
    public void setUp() throws Exception {
        mockedRepo = Mockito.mock(TestRepository.class);
        resourceCallback = mock(IdlingResource.ResourceCallback.class);
        rxPresso = RxPresso.from(mockedRepo);
        rxPresso.registerIdleTransitionCallback(resourceCallback);
    }

    @Test
    public void itSendsEventsToMockedObservable() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);

        rxPresso.given(foo)
                .withEventsFrom(SimpleEvents.onNext(42))
                .expect(any(Integer.class));

        Integer result = foo.toBlocking().first();

        assertThat(result).isEqualTo(42);
    }

    @Test
    public void itSendsEventsToMockedObservableAccordingToParameter() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);
        Observable<Integer> bar = mockedRepo.foo(1);

        rxPresso.given(foo)
                .withEventsFrom(SimpleEvents.onNext(42))
                .expect(any(Integer.class));
        rxPresso.given(bar)
                .withEventsFrom(SimpleEvents.onNext(24))
                .expect(any(Integer.class));

        Integer result = foo.toBlocking().first();
        Integer result2 = bar.toBlocking().first();

        assertThat(result).isEqualTo(42);
        assertThat(result2).isEqualTo(24);
    }

    @Test
    public void resetMocksResetsPipelines() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);

        rxPresso.given(foo)
                .withEventsFrom(SimpleEvents.onNext(42))
                .expect(any(Integer.class));

        rxPresso.resetMocks();

        Observable<Integer> bar = mockedRepo.foo(3);

        rxPresso.given(bar)
                .withEventsFrom(SimpleEvents.<Integer>onCompleted())
                .expect(
                        new RxMatcher<Notification<Integer>>() {
                            @Override
                            public boolean matches(Notification<Integer> actual) {
                                return actual.getKind() == Notification.Kind.OnCompleted;
                            }

                            @Override
                            public String description() {
                                return "Completed event";
                            }
                        }
                );

        Boolean result = bar.isEmpty().toBlocking().first();

        assertThat(result).isTrue();
    }

    @Test
    public void idlingRessourceTransitionsToIdleWhenDataIsDelivered() throws Exception {
        Observable<Integer> foo = mockedRepo.foo(3);

        rxPresso.given(foo)
                .withEventsFrom(SimpleEvents.onNext(42))
                .expect(any(Integer.class));

        assertThat(rxPresso.isIdleNow()).isFalse();

        Integer result = foo.toBlocking().first();

        assertThat(rxPresso.isIdleNow()).isTrue();
    }

    @Test
    public void itFailsIfNoEventMatchingMatcherIsReceived() throws Exception {
        expectedException.expectMessage("Expected Notification with kind OnNext but completed without matching");
        Observable<Integer> foo = mockedRepo.foo(3);

        rxPresso.given(foo)
                .withEventsFrom(SimpleEvents.<Integer>onCompleted())
                .expect(any(Integer.class));

        Integer result = foo.toBlocking().first();

        assertThat(result).isEqualTo(42);
    }

    @Test
    public void itFailsIfAnEventNotMatchingMatcherIsReceivedWhenUsingExpectOnly() throws Exception {
        expectedException.expectMessage("Expected Notification with kind OnNext but got");
        Observable<Integer> foo = mockedRepo.foo(3);

        rxPresso.given(foo)
                .withEventsFrom(Observable.just(42))
                .expectOnly(any(Integer.class));

        Integer result = foo.toBlocking().first();

        assertThat(result).isEqualTo(42);
    }

    public interface TestRepository {
        Observable<Integer> foo(int bar);
    }

}
