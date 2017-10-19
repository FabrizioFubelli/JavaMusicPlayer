package junit.framework;

/**
 * A Listener for FabriTest progress
 */
public interface TestListener {
    /**
     * An error occurred.
     */
    public void addError(Test test, Throwable e);

    /**
     * A failure occurred.
     */
    public void addFailure(Test test, AssertionFailedError e);

    /**
     * A FabriTest ended.
     */
    public void endTest(Test test);

    /**
     * A FabriTest started.
     */
    public void startTest(Test test);
}