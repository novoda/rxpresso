package com.novoda.rxpresso.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SampleActivity extends Activity {

    private DataRepository dataRepository;
    private TextView numberView;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataRepository = ((SampleApplication) getApplication()).getRepository();
        numberView = (TextView) findViewById(R.id.number);
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscription = dataRepository.getRandomNumber(10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new IntegerObserver());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    private void displayDialogFor(Throwable e) {
        new AlertDialog.Builder(this)
                .setTitle("Something something error")
                .setMessage(e.getMessage())
                .create()
                .show();
    }

    private class IntegerObserver implements Observer<Integer> {
        @Override
        public void onCompleted() {
            //No op
        }

        @Override
        public void onError(Throwable e) {
            displayDialogFor(e);
        }

        @Override
        public void onNext(Integer integer) {
            numberView.setText("Got the number: " + integer);
        }
    }
}
