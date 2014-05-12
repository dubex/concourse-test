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

import org.cinchapi.concourse.test.runners.CrossVersionTestRunner;
import org.cinchapi.concourse.test.runners.CrossVersionTestRunner.Versions;
import org.cinchapi.concourse.util.TLinkedTableMap;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link CrossVersionTest} is one that runs against multiple versions of
 * Concourse. This is beneficial for comparing performance and functionality
 * across different versions of the product in a head-to-head manner.
 * <p>
 * To simultaneously run a test case against multiple versions, make sure the
 * test class extends {@link CrossVersionTest} and uses the {@link Versions}
 * annotation to specify the release versions or paths to local installer files
 * to test against.
 * </p>
 * 
 * @author jnelson
 */
@RunWith(CrossVersionTestRunner.class)
public abstract class CrossVersionTest extends ClientServerTest {

    private String version = ""; // this is reflectively set by the test runner
                                 // for each versions that we run against

    /**
     * A {@link Logger} to print information about the test case.
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * A collection of stats that are recorded during the test and sorted by
     * version. These stats are printed at the end of the entire test run to
     * provide an easy view of comparative metrics.
     */
    private static TLinkedTableMap<String, String, Object> stats = TLinkedTableMap
            .newTLinkedTableMap("Version");

    @Override
    protected void beforeEachTest() {
        log.info("Running against version {}", version);
    }

    @Override
    protected final String getServerVersion() {
        return version;
    }

    /**
     * Record a stat described by the {@code key} and {@code value}. The stat
     * will be recorded for each version and printed out at the end for
     * comparison.
     * 
     * @param key
     * @param value
     */
    protected final void record(String key, Object value) {
        stats.put(version, key, value);
    }

}
