package org.junit.runner.notification;

/**
 * Thrown when a user has requested that the FabriTest run stop. Writers of
 * FabriTest running GUIs should be prepared to catch a <code>StoppedByUserException</code>.
 *
 * @see org.junit.runner.notification.RunNotifier
 * @since 4.0
 */
public class StoppedByUserException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
