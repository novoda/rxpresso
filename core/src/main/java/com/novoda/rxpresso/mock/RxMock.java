package com.novoda.rxpresso.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.subjects.ClearableBehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.BooleanSubscription;

import static com.novoda.rxpresso.mock.Functions.infinite;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public final class RxMock {

    private final Object mock;
    private final Map<String, Observable> observableHashMap = new HashMap<>();
    private final Map<Observable, Pair<ClearableBehaviorSubject<Notification>, PublishSubject<Notification>>> mapSubject = new HashMap<>();

    public static RxMock mock(Class clazz) {
        return from(Mockito.mock(clazz));
    }

    public static RxMock from(Object mock) {
        RxMock rxMock = new RxMock(mock);
        rxMock.setMockResponses();
        return rxMock;
    }

    private RxMock(Object mock) {
        this.mock = mock;
    }

    private void setMockResponses() {
        for (Method method : mock.getClass().getMethods()) {
            if (method.getReturnType().equals(Observable.class) && isMockable(method)) {
                setupMockResponseFor(method);
            }
        }
    }

    private boolean isMockable(Method method) {
        return !Modifier.isPrivate(method.getModifiers())
                && !Modifier.isProtected(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers());
    }

    public <T> Boolean provides(Observable<T> observable) {
        return observableHashMap.containsValue(observable);
    }

    public <T> Observable<Notification<T>> getEventsFor(Observable<T> observable) {
        Pair<ClearableBehaviorSubject<Notification>, PublishSubject<Notification>> subjectPair = mapSubject.get(observable);
        if (subjectPair == null) {
            throw new IllegalArgumentException(
                    "The observable " + observable +
                            " is not provided by this repo use the provides(Observable o) method to check first");
        }
        return Observable.zip(subjectPair.first, subjectPair.second, unzip())
                .lift(clearOnUnsubscribe(observable));
    }

    /**
     * Inject the events from given {@code source} into a mocked pipeline
     *
     * @param source The observable producing the events to inject
     * @param <T>    The type of this observable
     * @return A sender object to define into which pipeline to inject the events.
     */
    public <T> RxObservableSender<T> sendEventsFrom(Observable<T> source) {
        return new RxObservableSender<>(source);
    }

    public class RxObservableSender<T> {

        private final Observable<T> source;

        public RxObservableSender(Observable<T> source) {
            this.source = source;
        }

        /**
         * Send the events from {@code source} to the given mocked {@code observable}
         *
         * @param observable The mocked observable to inject events into.
         */
        public void to(Observable<T> observable) {
            ((Observable) source).materialize().lift(infinite()).subscribe(mapSubject.get(observable).first);
        }

    }

    public void resetMocks() {
        observableHashMap.clear();
        mapSubject.clear();
    }

    private void setupMockResponseFor(Method method) {
        try {
            when(method.invoke(mock, getArgumentsFor(method)))
                    .thenAnswer(
                            new Answer<Observable>() {
                                @Override
                                public Observable answer(InvocationOnMock invocation) throws Throwable {
                                    String key = getKeyFor(invocation.getMethod(), invocation.getArguments());
                                    if (!observableHashMap.containsKey(key)) {
                                        initialiseMockedObservable(invocation.getMethod(), invocation.getArguments());
                                    }
                                    return observableHashMap.get(key);
                                }
                            }
                    );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Object[] getArgumentsFor(Method method) {
        List<Object> arguments = new ArrayList<>();
        for (Class<?> aClass : method.getParameterTypes()) {
            arguments.add(any(aClass));
        }
        return arguments.toArray();
    }

    private void initialiseMockedObservable(Method method, Object[] args) {
        ClearableBehaviorSubject<Notification> subject = ClearableBehaviorSubject.create();
        PublishSubject<Notification> notificationSubject = PublishSubject.create();
        final String keyForArgs = getKeyFor(method, args);
        final Observable observable = subject
                .dematerialize()
                .doOnEach(new NotifyDataEvent(notificationSubject))
                .lift(new SwallowUnsubscribe());
        observableHashMap.put(keyForArgs, observable);
        mapSubject.put(observable, new Pair<>(subject, notificationSubject));
    }

    private String getKeyFor(Method method, Object[] args) {
        StringBuilder keyBuilder = new StringBuilder(method.getName());
        int index = 0;
        for (Class<?> type : method.getParameterTypes()) {
            keyBuilder.append('#').append(type.getSimpleName()).append('-').append(args[index++].hashCode());
        }
        return keyBuilder.toString();
    }

    private AddUnsubscribe clearOnUnsubscribe(final Object observable) {
        return new AddUnsubscribe(
                BooleanSubscription.create(
                        new Action0() {
                            @Override
                            public void call() {
                                mapSubject.get(observable).first.clear();
                            }
                        }
                )
        );
    }

    private static class NotifyDataEvent<T> implements Action1<Notification<? super T>> {

        private final PublishSubject<Notification<T>> publishSubject;

        public NotifyDataEvent(PublishSubject<Notification<T>> publishSubject) {
            this.publishSubject = publishSubject;
        }

        @Override
        public void call(Notification<? super T> notification) {
            publishSubject.onNext((Notification<T>) notification);
        }
    }

    private static class SwallowUnsubscribe<T> implements Observable.Operator<T, T> {

        @Override
        public Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
            return new Subscriber<T>() {
                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
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

    private static class AddUnsubscribe<T> implements Observable.Operator<T, T> {

        private final Subscription unsubscribe;

        private AddUnsubscribe(Subscription unsubscribe) {
            this.unsubscribe = unsubscribe;
        }

        @Override
        public Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
            subscriber.add(unsubscribe);
            return subscriber;
        }

    }

    private static Func2<Notification, Notification, Notification> unzip() {
        return new Func2<Notification, Notification, Notification>() {
            @Override
            public Notification call(Notification first, Notification second) {
                return second;
            }
        };
    }
}
