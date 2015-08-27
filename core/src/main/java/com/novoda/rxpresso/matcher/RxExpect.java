package com.novoda.rxpresso.matcher;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;

public final class RxExpect {

    private RxExpect() {
    }

    /**
     * Asserts that a given {@code observable} emits an element matching a given {@code matcher}
     *
     * @param matcher    The matcher to use for the assertion
     * @param observable The observable to assert against
     * @param <T>        The type of the observable
     */
    public static <T> void expect(RxMatcher<Notification<T>> matcher, Observable<T> observable) {
        observable.materialize()
                .subscribe(expect(matcher));
    }

    /**
     * Asserts that a given {@code observable} emits only elements matching a given {@code matcher}
     *
     * @param matcher    The matcher to use for the assertion
     * @param observable The observable to assert against
     * @param <T>        The type of the observable
     */
    public static <T> void expectOnly(RxMatcher<Notification<T>> matcher, Observable<T> observable) {
        observable.materialize()
                .subscribe(expectOnly(matcher));
    }

    /**
     * Asserts that a given {@code observable} emits an element matching a given {@code matcher}     *
     *
     * @param matcher    The matcher to use for the assertion
     * @param observable The observable to assert against
     * @param matched    A callback for when the assertion is matched
     * @param <T>        The type of the observable
     */
    public static <T> void expect(final RxMatcher<Notification<T>> matcher, final Observable<T> observable, final Action1<Notification<T>> matched) {
        observable.materialize()
                .subscribe(expect(matcher, matched));
    }

    /**
     * Asserts that a given {@code observable} emits only elements matching a given {@code matcher}     *
     *
     * @param matcher    The matcher to use for the assertion
     * @param observable The observable to assert against
     * @param matched    A callback for when the assertion is matched
     * @param <T>        The type of the observable
     */
    public static <T> void expectOnly(final RxMatcher<Notification<T>> matcher, final Observable<T> observable, final Action1<Notification<T>> matched) {
        observable.materialize()
                .subscribe(expectOnly(matcher, matched));
    }

    /**
     * Returns an action to subscribe to an observable to assert if it emits an element matching a given {@code matcher}
     *
     * @param matcher The matcher to use for the assertion
     * @param <T>     The type of the observable
     * @return The action to subscribe to a materialized observable to assert if a given event is emitted.
     */
    public static <T> Action1<Notification<T>> expect(final RxMatcher<Notification<T>> matcher) {
        return expect(matcher, doNothing);
    }

    /**
     * Returns an action to subscribe to an observable to assert if it emits only elements matching a given {@code matcher}
     *
     * @param matcher The matcher to use for the assertion
     * @param <T>     The type of the observable
     * @return The action to subscribe to a materialized observable to assert if a given event is emitted.
     */
    public static <T> Action1<Notification<T>> expectOnly(final RxMatcher<Notification<T>> matcher) {
        return expectOnly(matcher, doNothing);
    }

    /**
     * Returns an action to subscribe to an observable to assert if it emits only elements matching a given {@code matcher}     *
     *
     * @param matcher The matcher to use for the assertion
     * @param matched A callback for when the assertion is matched
     * @param <T>     The type of the observable
     * @return The action to subscribe to a materialized observable to assert if a given event is emitted.
     */
    public static <T> Action1<Notification<T>> expectOnly(final RxMatcher<Notification<T>> matcher, final Action1<Notification<T>> matched) {
        return new Action1<Notification<T>>() {
            @Override
            public void call(Notification<T> notification) {
                if (matcher.matches(notification)) {
                    matched.call(notification);
                } else {
                    throw new RuntimeException("Expected " + matcher.description() + " but got " + notification);
                }
            }
        };
    }

    /**
     * Returns an action to subscribe to an observable to assert if it emits an element matching a given {@code matcher}     *
     *
     * @param matcher The matcher to use for the assertion
     * @param matched A callback for when the assertion is matched
     * @param <T>     The type of the observable
     * @return The action to subscribe to a materialized observable to assert if a given event is emitted.
     */
    public static <T> Action1<Notification<T>> expect(final RxMatcher<Notification<T>> matcher, final Action1<Notification<T>> matched) {
        return new Action1<Notification<T>>() {

            private boolean noMatch = true;

            @Override
            public void call(Notification<T> notification) {
                if (matcher.matches(notification)) {
                    noMatch = false;
                    matched.call(notification);
                }
                if (notification.getKind() == Notification.Kind.OnCompleted && noMatch) {
                    throw new RuntimeException("Expected " + matcher.description() + " but completed without matching");
                }
            }
        };
    }

    /**
     * @param clazz The class of the type {@code T} to match
     * @param <T>   The type to match
     * @return a matcher matching any onNext event of a given type {@code T}
     */
    public static <T> RxMatcher<Notification<T>> any(Class<T> clazz) {
        return new RxMatcher<Notification<T>>() {
            @Override
            public boolean matches(Notification<T> actual) {
                return actual.getKind() == Notification.Kind.OnNext;
            }

            @Override
            public String description() {
                return "Notification with kind " + Notification.Kind.OnNext;
            }
        };
    }

    /**
     * @param clazz The class of the type {@code T} of the observable to assert against
     * @param <T>   The type of the observable to assert against
     * @return a matcher matching any onError event
     */
    public static <T> RxMatcher<Notification<T>> anyError(Class<T> clazz) {
        return new RxMatcher<Notification<T>>() {
            @Override
            public boolean matches(Notification<T> actual) {
                return actual.getKind() == Notification.Kind.OnError;
            }

            @Override
            public String description() {
                return "Notification with kind " + Notification.Kind.OnError;
            }
        };
    }

    /**
     * @param clazz      The class of the type {@code T} of the observable to assert against
     * @param errorClazz The class of the type {@code V} of the error to match
     * @param <T>        The type of the observable to assert against
     * @param <V>
     * @return a matcher matching any onError event with an error a given type {@code V}
     */
    public static <T, V extends Throwable> RxMatcher<Notification<T>> anyError(Class<T> clazz, final Class<V> errorClazz) {
        return new RxMatcher<Notification<T>>() {
            @Override
            public boolean matches(Notification<T> actual) {
                return actual.hasThrowable() && actual.getThrowable().getClass().isAssignableFrom(errorClazz);
            }

            @Override
            public String description() {
                return "Notification with error of type " + errorClazz.getName();
            }
        };
    }

    private static final Action1 doNothing = new Action1() {
        @Override
        public void call(Object o) {
            //Do Nothing
        }
    };

}
