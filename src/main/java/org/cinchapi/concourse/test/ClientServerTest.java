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
package org.cinchapi.concourse.test;

import java.io.File;

import javax.annotation.Nullable;

import org.cinchapi.concourse.Concourse;
import org.cinchapi.concourse.server.ManagedConcourseServer;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * A {@link ClientServerTest} is one that interacts with a Concourse server via
 * the public client API. This base class handles boilerplate logic for creating
 * a new server for each test and managing resources.
 * <ul>
 * <li>Specify the server version to test against using the
 * {@link #getServerVersion()} method or specify a custom installer file using
 * the {@link #installerPath()} method.</li>
 * <li>Specify actions to take before each test using the
 * {@link #beforeEachTest()} method.</li>
 * <li>Specify actions to take after each test using the
 * {@link #afterEachTest()} method.</li>
 * </ul>
 * 
 * @author jnelson
 */
public abstract class ClientServerTest {

    // Initialization for all tests
    static {
        System.setProperty("test", "true");
    }

    /**
     * The client allows the subclass to define tests that perform actions
     * against the test {@link #server} using the public API.
     */
    protected Concourse client = null;

    /**
     * A new server is created for every test. The subclass can perform
     * lifecycle and management operations on the server using this variable and
     * may also interact via the {@link #client} API.
     */
    protected ManagedConcourseServer server = null;

    /**
     * This watcher clears previously registered {@link Variables} on startup
     * and dumps them in the event of failure.
     */
    @Rule
    public final TestWatcher __watcher = new TestWatcher() {

        @Override
        protected void failed(Throwable t, Description description) {
            System.out.println("TEST FAILURE in " + description.getMethodName()
                    + ": " + t.getMessage());
            System.out.println("---");
            System.out.println(Variables.dump());
            System.out.println("");
        }

        @Override
        protected void finished(Description description) {
            client.exit();
            server.destroy();
            client = null;
            server = null;
            afterEachTest();
        }

        @Override
        protected void starting(Description description) {
            Variables.clear();
            if(installerPath() == null) {
                server = ManagedConcourseServer
                        .manageNewServer(getServerVersion());
            }
            else {
                server = ManagedConcourseServer
                        .manageNewServer(installerPath());
            }
            server.start();
            client = server.connect();
            beforeEachTest();
        }

    };

    /**
     * This method is provided for the subclass to specify additional behaviour
     * to be run after each test is done. The subclass should define such logic
     * in this method as opposed to a test watcher.
     */
    protected void afterEachTest() {}

    /**
     * This method is provided for the subclass to specify additional behaviour
     * to be run before each test begins. The subclass should define such logic
     * in this method as opposed to a test watcher.
     */
    protected void beforeEachTest() {}

    /**
     * This method is provided for the subclass to specify the appropriate
     * version number to test against.
     * 
     * @return the version number
     */
    protected abstract String getServerVersion();

    /**
     * This method is provided for the subclass to specify the path to a custom
     * installer (i.e. testing against a SNAPSHOT version of the server). If
     * this method returns a null or empty string, the default installer is
     * used.
     * 
     * @return the custom installer path
     */
    @Nullable
    protected File installerPath() {
        return null;
    }

}
