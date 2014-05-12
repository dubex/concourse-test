/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Jeff Nelson, Cinchapi Software Collective
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.cinchapi.concourse.test.runners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;

import org.cinchapi.concourse.test.CrossVersionTest;
import org.cinchapi.concourse.util.TLinkedTableMap;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * A test {@link Runner} that takes a single test class that extends
 * {@link CrossVersionTest} and runs all the test methods against all the
 * versions specified in the {@link Versions} annotation.
 * 
 * @author jnelson
 */
public class CrossVersionTestRunner extends ParentRunner<Runner> {

    /**
     * Return all the versions that are specified in the {@link Versions}
     * annotation.
     * 
     * @param klass
     * @return the specified version
     * @throws InitializationError
     */
    private static String[] getAnnotatedVersions(Class<?> klass)
            throws InitializationError {
        Versions annotation = klass.getAnnotation(Versions.class);
        if(annotation == null) {
            throw new InitializationError(String.format(
                    "class '%s' must have a Versions annotation",
                    klass.getName()));
        }
        return annotation.value();
    }

    /**
     * A collection of runners, each of which represents the test class running
     * against a certain version.
     */
    private final List<Runner> runners;

    /**
     * Construct a new instance.
     * 
     * @param testClass
     * @throws Throwable
     */
    public CrossVersionTestRunner(Class<?> testClass) throws Throwable {
        super(testClass);
        runners = Lists.newArrayList();
        for (final String version : getAnnotatedVersions(testClass)) {
            runners.add(new BlockJUnit4ClassRunner(testClass) {

                @Override
                protected Object createTest() throws Exception {
                    Object instance = super.createTest();
                    Field field = null;
                    Class<?> c = instance.getClass();
                    while (field == null && c != Object.class) {
                        try {
                            field = c.getDeclaredField("version");
                            field.setAccessible(true);
                            field.set(instance, version);
                            return instance;
                        }
                        catch (NoSuchFieldException e) {
                            c = c.getSuperclass();
                        }
                    }
                    String ex = "Any test class that is utilizes %s must extend %s";
                    throw new Exception(MessageFormat.format(ex,
                            CrossVersionTestRunner.class,
                            CrossVersionTest.class));
                }

                @Override
                protected String getName() {
                    return MessageFormat.format("{0} [{1}]", super.getName(),
                            version);
                }

                @Override
                protected String testName(FrameworkMethod method) {
                    return MessageFormat.format("{0} [{1}]", method.getName(),
                            version);
                }

            });
        }
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(super
                .getDescription().getTestClass());
        for (Runner runner : runners) {
            description.addChild(runner.getDescription());
        }
        return description;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
        if(runners.indexOf(child) == runners.size() - 1) {
            Field field = null;
            Class<?> clazz = ((BlockJUnit4ClassRunner) child).getTestClass()
                    .getJavaClass();
            while (field == null && clazz != Object.class) {
                try {
                    field = clazz.getDeclaredField("stats");
                    field.setAccessible(true);
                }
                catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if(field != null) {
                try {
                    @SuppressWarnings("unchecked") TLinkedTableMap<String, String, Object> stats = (TLinkedTableMap<String, String, Object>) field
                            .get(null);
                    if(!stats.isEmpty()) {

                        System.out.print("CROSS VERSION STATS:");
                        System.out.println(field.get(null));

                    }
                }
                catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        }
    }

    /**
     * The {@code Versions} annotation specifies the version numbers or paths to
     * installer files against which the test should run.
     * 
     * @author jnelson
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface Versions {

        /**
         * @return the versions against which to test
         */
        public String[] value();

    }

}