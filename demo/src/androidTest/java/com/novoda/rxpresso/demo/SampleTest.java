package com.novoda.rxpresso.demo;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.novoda.rxmocks.RxMocks;
import com.novoda.rxmocks.SimpleEvents;
import com.novoda.rxpresso.RxPresso;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.novoda.rxmocks.RxExpect.any;
import static com.novoda.rxmocks.RxExpect.anyError;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class SampleTest {

    private RxPresso rxPresso;
    private DataRepository mockedRepo;

    @Rule
    public ActivityTestRule<SampleActivity> rule = new ActivityTestRule<SampleActivity>(SampleActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            SampleApplication application = (SampleApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();

            mockedRepo = RxMocks.mock(DataRepository.class);
            application.setRepository(mockedRepo);

            rxPresso = new RxPresso(mockedRepo);
            Espresso.registerIdlingResources(rxPresso);
        }

        @Override
        protected void afterActivityFinished() {
            super.afterActivityFinished();
            Espresso.unregisterIdlingResources(rxPresso);
            rxPresso.resetMocks();
        }
    };

    @Test
    public void randomIntegerIsDisplayed() throws Exception {
        rxPresso.given(mockedRepo.getRandomNumber(10))
                .withEventsFrom(SimpleEvents.onNext(3))
                .expect(any(Integer.class))
                .thenOnView(withId(R.id.number))
                .check(matches(withText(containsString(String.valueOf(3)))));
    }

    @Test
    public void whenAnErrorOccursAnErrorDialogIsDisplayedShowingErrorMessage() throws Exception {
        rxPresso.given(mockedRepo.getRandomNumber(10))
                .withEventsFrom(SimpleEvents.<Integer>onError(new IOException("Not random enough ?!")))
                .expect(anyError(Integer.class, IOException.class))
                .thenOnView(withText("Not random enough ?!"))
                .check(matches(isDisplayed()));
    }

}
