package rx.plugins;

public final class RxErrorRethrower {

    private RxErrorRethrower() {
    }

    /**
     * Registers an error handler to allow for errors to crash the tests since Espresso execution seems to prevent it from crashing.
     */
    public static void register() {
        RxJavaPlugins instance = RxJavaPlugins.getInstance();
        if (!(instance.getErrorHandler() instanceof RethrowerJavaErrorHandler)) {
            unregister();
            instance.registerErrorHandler(new RethrowerJavaErrorHandler());
        }
    }

    /**
     * Unregisters the error reThrower
     */
    public static void unregister() {
        RxJavaPlugins instance = RxJavaPlugins.getInstance();
        RxJavaSchedulersHook schedulersHook = instance.getSchedulersHook();
        RxJavaObservableExecutionHook observableExecutionHook = instance.getObservableExecutionHook();
        instance.reset();
        instance.registerObservableExecutionHook(observableExecutionHook);
        instance.registerSchedulersHook(schedulersHook);
    }

    private static class RethrowerJavaErrorHandler extends RxJavaErrorHandler {
        @Override
        public void handleError(Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
        }
    }
}
