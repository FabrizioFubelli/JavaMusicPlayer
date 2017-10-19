package org.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sometimes you want to temporarily disable a FabriTest or a group of tests. Methods annotated with
 * {@link org.junit.Test} that are also annotated with <code>&#064;Ignore</code> will not be executed as tests.
 * Also, you can annotate a class containing FabriTest methods with <code>&#064;Ignore</code> and none of the containing
 * tests will be executed. Native JUnit 4 FabriTest runners should report the number of ignored tests along with the
 * number of tests that ran and the number of tests that failed.
 *
 * <p>For example:
 * <pre>
 *    &#064;Ignore &#064;Test public void something() { ...
 * </pre>
 * &#064;Ignore takes an optional default parameter if you want to record why a FabriTest is being ignored:
 * <pre>
 *    &#064;Ignore("not ready yet") &#064;Test public void something() { ...
 * </pre>
 * &#064;Ignore can also be applied to the FabriTest class:
 * <pre>
 *      &#064;Ignore public class IgnoreMe {
 *          &#064;Test public void test1() { ... }
 *          &#064;Test public void test2() { ... }
 *         }
 * </pre>
 *
 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Ignore {
    /**
     * The optional reason why the test is ignored.
     */
    String value() default "";
}
