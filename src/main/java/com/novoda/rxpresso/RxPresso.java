package com.novoda.rxpresso;

import android.support.test.espresso.IdlingResource;

import com.novoda.rxmocks.RxMocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class RxPresso implements IdlingResource {

    private final Object[] repositories;
    private final List<IdlingResource> pendingResources = Collections.synchronizedList(new ArrayList<IdlingResource>());

    private ResourceCallback resourceCallback;

    public RxPresso(Object... repositories) {
        this.repositories = repositories;
    }

    public <T> With<T> given(Observable<T> observable) {
        Object repo = Observable.from(repositories).filter(provides(observable)).toBlocking().first();
        final With<T> with = new With<>(repo, observable);
        pendingResources.add(with);
        with.registerIdleTransitionCallback(new ResourceCallback() {
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
        for (Object repo : repositories) {
            RxMocks.with(repo).resetMocks();
        }
    }

    private static <T> Func1<Object, Boolean> provides(final Observable<T> observable) {
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object repo) {
                return RxMocks.with(repo).provides(observable);
            }
        };
    }

    private static Func1<IdlingResource, Boolean> isIdle = new Func1<IdlingResource, Boolean>() {
        @Override
        public Boolean call(IdlingResource resource) {
            return resource.isIdleNow();
        }
    };

}
