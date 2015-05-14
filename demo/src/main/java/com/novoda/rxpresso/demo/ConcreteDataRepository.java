package com.novoda.rxpresso.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import rx.Observable;
import rx.Subscriber;

public class ConcreteDataRepository implements DataRepository {
    @Override
    public Observable<Integer> getRandomNumber(final int max) {
        return Observable.create(
                new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        String urlSpec = "https://www.random.org/integers/?num=1&min=1&max=" + max + "&col=1&base=10&format=plain";
                        try (BufferedReader in = openUrl(urlSpec)) {
                            String inputLine;
                            StringBuilder response = new StringBuilder();
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();
                            subscriber.onNext(Integer.parseInt(response.toString()));
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                    }
                }
        );
    }

    private BufferedReader openUrl(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        URLConnection urlConnection = url.openConnection();
        return new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
    }
}
