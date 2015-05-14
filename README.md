# RxPresso [![](https://ci.novoda.com/buildStatus/icon?job=rxpresso)](https://ci.novoda.com/job/rxpresso/lastBuild/console) [![Apache Licence 2.0](https://raw.githubusercontent.com/novoda/novoda/master/assets/btn_apache_lisence.png)](LICENSE.txt)

Mocks/Assertions for RxJava testing

## Description

RxPresso makes Espresso testing easy for applications using RxJava.

RxPresso uses RxMocks to generate mocks of your repositories that you can use with RxPresso to control data in your Espresso tests.
The binding with Espresso Idling resource is handled for you so Espresso will wait until the data you expect to inject in your UI
has been delivered to you UI.

No more data you don't control in your Espresso test.

At the moment this will only mock methods from the interface returning observables (see Future improvements section).

This project is in its early stages, feel free to comment, and contribute back to help us improve it.

## Adding to your project

To integrate RxPresso into your project, add the following at the beginning of the `build.gradle` of your project:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        androidTestCompile 'com.novoda:rxpresso:0.1.2'
    }
}
```


## Simple usage

To generate a mocked repo use an interface providing Observables as an abstraction for your repo.
You can now use this interface to generate a mock as shown below.

**Interfaced repository**
```java
public interface DataRepository {

    Observable<User> getUser(String name);

    Observable<Articles> getArticles();

}
```

**Mocking this repository**
```java
DataRepository mockedRepo = RxMocks.mock(DataRepository.class)
```

You should then replace the repository used by your activities by this mocked one.
If you use Dagger or Dagger2 you can replace the module by a test one providing the mock.
If your repo lives in the application class you can have a setter or user reflection to set it during tests.
Any other option as long as your UI reads from the mocked repo.

**Set up RxPresso in your tests**
```java
DataRepository mockedRepo = getSameRepoUsedByUi();

RxPresso rxpresso = new RxPresso(mockedRepo);
Espresso.registerIdlingResources(rxPresso);
```

**Use it to inject data in your UI**
```java
rxPresso.given(mockedRepo.getUser("some name"))
           .withEventsFrom(Observable.just(new User("some name")))
           .expect(any(User.class))
           .thenOnView(withText(containsString("some name")))
           .perform(click());
```

**Use it to inject data from local sources**
```java
Observable<User> testAssetObservable = testAssetRepo.getUser("some name");

rxPresso.given(mockedRepo.getUser("some name"))
           .withEventsFrom(testAssetObservable)
           .expect(any(User.class))
           .thenOnView(withText(containsString("some name")))
           .perform(click());
```

**Use custom matchers**
```java
Observable<User> testAssetObservable = testAssetRepo.getUser("some name");

rxPresso.given(mockedRepo.getUser("some name"))
           .withEventsFrom(testAssetObservable)
           .expect(new RxMatcher<Notification<User>>() {
                   @Override
                   public boolean matches(Notification<User> actual) {
                       return actual.getValue().name().equals("some name");
                   }

                   @Override
                   public String description() {
                       return "User with name " + "some name";
                   }
           })
           .thenOnView(withText(containsString("some name")))
           .perform(click());
```

**Use it to inject errors in your UI**
```java
rxPresso.given(mockedRepo.getUser("some name"))
           .withEventsFrom(Observable.error(new CustomError()))
           .expect(anyError(User.class, CustomError.class))
           .thenOnView(withText(containsString("Custom Error Message")))
           .matches(isDisplayed());
```

**Reset mocks between tests**
```java
rxPresso.resetMocks();
```

You can also use RxPresso with multiple repositories.
Just setup using all the repositories your UI is using.
The usage doesn't change RxPresso will detect from what repo the observable provided comes from and send the data to the correct pipeline.

**Setup with multiple repositories**
```java
DataRepository mockedRepo = getSameRepoUsedByUi();
AnotherDataRepository mockedRepo2 = getSameSecondRepoUsedByUi();


RxPresso rxpresso = new RxPresso(mockedRepo, mockedRepo2);
Espresso.registerIdlingResources(rxPresso);
```

## Future improvements

- Support "spying" to allow for non mocked calls to be forwarded to actual implementation.

## Links

Here are a list of useful links:

 * We always welcome people to contribute new features or bug fixes, [here is how](https://github.com/novoda/novoda/blob/master/CONTRIBUTING.md)
 * If you have a problem check the [Issues Page](https://github.com/novoda/rxpresso/issues) first to see if we are working on it
 * Looking for community help, browse the already asked [Stack Overflow Questions](http://stackoverflow.com/questions/tagged/support-rxpresso) or use the tag: `support-rxpresso` when posting a new question
