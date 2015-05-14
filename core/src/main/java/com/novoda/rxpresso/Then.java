package com.novoda.rxpresso;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;

public class Then {

    public ViewInteraction thenOnView(Matcher<View> viewMatcher) {
        return onView(viewMatcher);
    }

    public DataInteraction thenOnData(Matcher<Object> dataMatcher) {
        return onData(dataMatcher);
    }

}
