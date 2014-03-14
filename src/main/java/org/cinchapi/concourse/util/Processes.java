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
package org.cinchapi.concourse.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.cinchapi.concourse.annotate.UtilityClass;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Utilities for dealing with {@link Process} objects.
 * 
 * @author jnelson
 */
@UtilityClass
public final class Processes {

    /**
     * Get the stdout for {@code process}.
     * 
     * @param process
     * @return a collection of output lines
     */
    public static List<String> getStdOut(Process process) {
        return readStream(process.getInputStream());

    }

    /**
     * Read an input stream.
     * @param stream
     * @return the lines in the stream
     */
    private static List<String> readStream(InputStream stream) {
        try {
            BufferedReader out = new BufferedReader(new InputStreamReader(
                    stream));
            String line;
            List<String> output = Lists.newArrayList();
            while ((line = out.readLine()) != null) {
                output.add(line);
            }
            return output;
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Get the stderr for {@code process}.
     * @param process
     * @return a collection of error lines
     */
    public static List<String> getStdErr(Process process) {
        return readStream(process.getErrorStream());
    }

    private Processes() {} /* noinit */

}
