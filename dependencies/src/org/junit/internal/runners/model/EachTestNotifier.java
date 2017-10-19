package org.junit.internal.runners.model;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.MultipleFailureException;

public class EachTestNotifier {
    private final RunNotifier notifier;

    private final Description description;

    public EachTestNotifier(RunNotifier notifier, Description description) {
        this.notifier = notifier;
        this.description = description;
    }

    public void addFailure(Throwable targetException) {
        if (targetException instanceof org.junit.internal.runners.model.MultipleFailureException) {
            addMultipleFailureException((org.junit.internal.runners.model.MultipleFailureException) targetException);
        } else {
            notifier.fireTestFailure(new Failure(description, targetException));
        }
    }

    private void addMultipleFailureException(org.junit.internal.runners.model.MultipleFailureException mfe) {
        for (Throwable each : mfe.getFailures()) {
            addFailure(each);
        }
    }

    public void addFailedAssumption(AssumptionViolatedException e) {
        notifier.fireTestAssumptionFailed(new Failure(description, e));
    }

    public void fireTestFinished() {
        notifier.fireTestFinished(description);
    }

    public void fireTestStarted() {
        notifier.fireTestStarted(description);
    }

    public void fireTestIgnored() {
        notifier.fireTestIgnored(description);
    }
}