package com.novoda.rxpresso;

import android.support.test.espresso.IdlingResource;

import com.novoda.rxpresso.mock.RxMock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public final class RxPresso implements IdlingResource {

    private final List<RxMock> repositories;
    private final List<IdlingResource> pendingResources = Collections.synchronizedList(new ArrayList<IdlingResource>());

    private ResourceCallback resourceCallback;

    /**
     * @param repositories The different mocked repositories you want to control in your tests
     */
    public static RxPresso init(Object... repositories) {
        return new RxPresso(Observable.from(repositories).map(asRxMocks).toList().toBlocking().first());
    }

    /**
     * @param repositories The different mocked repositories you want to control in your tests
     */
    private RxPresso(List<RxMock> repositories) {
        this.repositories = repositories;
    }

    public <T> With<T> given(Observable<T> observable) {
        RxMock repo = Observable.from(repositories).filter(provides(observable)).toBlocking().first();
        final With<T> with = new With<>(repo, observable);
        pendingResources.add(with);
        with.registerIdleTransitionCallback(
                new ResourceCallback() {
                    @Override
                    public void onTransitionToIdle() {
                        pendingResources.remove(with);
                        if (pendingResources.isEmpty()) {
                            resourceCallback.onTransitionToIdle();
                        }
                    }
                });
        return with;
    }

    private static <T> Func1<? super RxMock, Boolean> provides(final Observable<T> observable) {
        return new Func1<RxMock, Boolean>() {
            @Override
            public Boolean call(RxMock rxMock) {
                return rxMock.provides(observable);
            }
        };
    }

    @Override
    public String getName() {
        return "RxPresso";
    }

    @Override
    public boolean isIdleNow() {
        if (pendingResources.isEmpty()) {
            return true;
        }
        return Observable.from(pendingResources).all(isIdle).toBlocking().first();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    public void resetMocks() {
        for (RxMock repository : repositories) {
            repository.resetMocks();
        }
    }

    private static final Func1<IdlingResource, Boolean> isIdle = new Func1<IdlingResource, Boolean>() {
        @Override
        public Boolean call(IdlingResource resource) {
            return resource.isIdleNow();
        }
    };

    private static final Func1<Object, RxMock> asRxMocks = new Func1<Object, RxMock>() {
        @Override
        public RxMock call(Object object) {
            return RxMock.init(object);
        }
    };
}
