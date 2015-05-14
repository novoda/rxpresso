package com.novoda.rxpresso.demo;

import android.app.Application;

public class SampleApplication extends Application {

    private DataRepository repository = new ConcreteDataRepository();

    public DataRepository getRepository() {
        return repository;
    }

    public void setRepository(DataRepository repository) {
        this.repository = repository;
    }
}
