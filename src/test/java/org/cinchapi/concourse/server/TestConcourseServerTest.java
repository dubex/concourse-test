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
package org.cinchapi.concourse.server;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.cinchapi.concourse.Concourse;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Unit tests for {@link TestConcourseServer}.
 * 
 * @author jnelson
 */
public class TestConcourseServerTest {
    
    private TestConcourseServer server = null;

    @Rule
    public TestWatcher watcher = new TestWatcher() {

        @Override
        protected void starting(Description description) {
            server = TestConcourseServer.create();
        }

        @Override
        protected void finished(Description description) {
            server.destroy();
            server = null;
        }
    };

    @Test
    public void testStart() {
        server.start();
        Assert.assertTrue(server.isRunning());
    }

    @Test
    public void testStop() {
        server.start();
        server.stop();
        Assert.assertFalse(server.isRunning());
    }
    
    @Test
    public void testIsRunning(){
        Assert.assertFalse(server.isRunning());
        server.start();
        Assert.assertTrue(server.isRunning());
    }
    
    @Test
    public void testDestroy(){
        server.destroy();
        Assert.assertFalse(Files.exists(Paths.get(server.getInstallDirectory())));
    }
    
    @Test
    public void testConnectAndInteract(){
        server.start();
        Concourse concourse = server.connect();
        concourse.add("foo", "bar", 1);
        Assert.assertEquals("bar", concourse.get("foo", 1));
    }

}
