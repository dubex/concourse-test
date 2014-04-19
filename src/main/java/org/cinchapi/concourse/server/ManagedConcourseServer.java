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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jline.TerminalFactory;

import org.cinchapi.concourse.Concourse;
import org.cinchapi.concourse.config.ConcoursePreferences;
import org.cinchapi.concourse.time.Time;
import org.cinchapi.concourse.util.ConcourseServerDownloader;
import org.cinchapi.concourse.util.Processes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * A {@link ManagedConcourseServer} is an external server process that can be
 * programmatically controlled within another application. This class is useful
 * for applications that want to "embed" a Concourse Server for the duration of
 * the application's life cycle and then forget about its existence afterwards.
 * 
 * @author jnelson
 */
public class ManagedConcourseServer {

    /**
     * Create an {@link ManagedConcourseServer from the {@code installer}.
     * 
     * @param installer
     * @return the EmbeddedConcourseServer
     */
    public static ManagedConcourseServer manageNewServer(File installer) {
        return manageNewServer(installer, DEFAULT_INSTALL_HOME + File.separator
                + Time.now());
    }

    /**
     * Create an {@link ManagedConcourseServer} from the {@code installer} in
     * {@code directory}.
     * 
     * @param installer
     * @param directory
     * @return the EmbeddedConcourseServer
     */
    public static ManagedConcourseServer manageNewServer(File installer,
            String directory) {
        return new ManagedConcourseServer(install(installer.getAbsolutePath(),
                directory));
    }

    /**
     * Create an {@link ManagedConcourseServer} at {@code version}.
     * 
     * @param version
     * @return the EmbeddedConcourseServer
     */
    public static ManagedConcourseServer manageNewServer(String version) {
        return manageNewServer(version, DEFAULT_INSTALL_HOME + File.separator
                + Time.now());
    }

    /**
     * Create an {@link ManagedConcourseServer} at {@code version} in
     * {@code directory}.
     * 
     * @param version
     * @param directory
     * @return the EmbeddedConcourseServer
     */
    public static ManagedConcourseServer manageNewServer(String version,
            String directory) {
        return manageNewServer(
                new File(ConcourseServerDownloader.download(version)),
                directory);
    }

    /**
     * Tweak some of the preferences to make this more palatable for testing
     * (i.e. reduce the possibility of port conflicts, etc).
     * 
     * @param installDirectory
     */
    private static void configure(String installDirectory) {
        ConcoursePreferences prefs = ConcoursePreferences.load(installDirectory
                + File.separator + CONF + File.separator + "concourse.prefs");
        String data = installDirectory + File.separator + "data";
        prefs.setBufferDirectory(data + File.separator + "buffer");
        prefs.setDatabaseDirectory(data + File.separator + "database");
        prefs.setClientPort(getOpenPort());
        prefs.setLogLevel(Level.DEBUG);
        prefs.setShutdownPort(getOpenPort());
    }

    /**
     * Get an open port.
     * 
     * @return the port
     */
    private static int getOpenPort() {
        int min = 49512;
        int max = 65535;
        int port = RAND.nextInt(min) + (max - min);
        return isPortAvailable(port) ? port : getOpenPort();
    }

    /**
     * Install a Concourse Server in {@code directory} using {@code installer}.
     * 
     * @param installer
     * @param directory
     * @return the server install directory
     */
    private static String install(String installer, String directory) {
        try {
            Files.createDirectories(Paths.get(directory));
            Path binary = Paths.get(directory + File.separator
                    + TARGET_BINARY_NAME);
            Files.deleteIfExists(binary);
            Files.copy(Paths.get(installer), binary);
            ProcessBuilder builder = new ProcessBuilder(Lists.newArrayList(
                    "sh", binary.toString()));
            builder.directory(new File(directory));
            builder.redirectErrorStream();
            Process process = builder.start();
            // The concourse-server installer prompts for an admin password in
            // order to make optional system wide In order to get around this
            // prompt, we have to "kill" the process, otherwise the server
            // install will hang.
            Stopwatch watch = Stopwatch.createStarted();
            while (watch.elapsed(TimeUnit.SECONDS) < 1) {
                continue;
            }
            watch.stop();
            process.destroy();
            TerminalFactory.get().restore();
            String application = directory + File.separator
                    + "concourse-server"; // the install directory for the
                                          // concourse-server application
            process = Runtime.getRuntime().exec("ls " + application);
            List<String> output = Processes.getStdOut(process);
            if(!output.isEmpty()) {
                configure(application);
                log.info("Successfully installed server in {}", application);
                return application;
            }
            else {
                throw new RuntimeException(
                        MessageFormat
                                .format("Unsuccesful attempt to "
                                        + "install server at {0} "
                                        + "using binary from {1}", directory,
                                        installer));
            }

        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }

    }

    /**
     * Return {@code true} if the {@code port} is available on the local
     * machine.
     * 
     * @param port
     * @return {@code true} if the port is available
     */
    private static boolean isPortAvailable(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        }
        catch (SocketException e) {
            return false;
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * The filename of the binary installer from which the test server will be
     * created.
     */
    private static final String TARGET_BINARY_NAME = "concourse-server.bin";

    /**
     * The default location where the the test server is installed if a
     * particular location is not specified.
     */
    private static final String DEFAULT_INSTALL_HOME = System
            .getProperty("user.home") + File.separator + ".concourse-testing";

    // ---relative paths
    private static final String CONF = "conf";
    private static final String BIN = "bin";

    // ---logger
    private static final Logger log = LoggerFactory
            .getLogger(ManagedConcourseServer.class);

    // ---random
    private static final Random RAND = new Random();

    /**
     * The server application install directory;
     */
    private final String installDirectory;

    /**
     * The handler for the server's preferences.
     */
    private final ConcoursePreferences prefs;

    /**
     * Construct a new instance.
     * 
     * @param installDirectory
     */
    private ManagedConcourseServer(String installDirectory) {
        this.installDirectory = installDirectory;
        this.prefs = ConcoursePreferences.load(installDirectory
                + File.separator + CONF + File.separator + "concourse.prefs");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                destroy();
            }

        }));
    }

    /**
     * Return a connection handler to the server using the default "admin"
     * credentials.
     * 
     * @return the connection handler
     */
    public Concourse connect() {
        return connect("admin", "admin");
    }

    /**
     * Return a connection handler to the server using the specified
     * {@code username} and {@code password}.
     * 
     * @param username
     * @param password
     * @return the connection handler
     */
    public Concourse connect(String username, String password) {
        return Concourse.connect("localhost", prefs.getClientPort(), username,
                password);
    }

    /**
     * Stop the server, if it is running, and permanently delete the application
     * files and associated data.
     */
    public void destroy() {
        if(Files.exists(Paths.get(installDirectory))) { // check if server has
                                                        // been manually
                                                        // destroyed
            if(isRunning()) {
                stop();
            }
            try {
                deleteDirectory(Paths.get(installDirectory).getParent()
                        .toString());
                log.info("Deleted server install directory at {}",
                        installDirectory);
            }
            catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

    }

    /**
     * Return the client port for this server.
     * 
     * @return the client port
     */
    public int getClientPort() {
        return prefs.getClientPort();
    }

    /**
     * Return the {@link #installDirectory} for this server.
     * 
     * @return the install directory
     */
    public String getInstallDirectory() {
        return installDirectory;
    }

    /**
     * Return {@code true} if the server is currently running.
     * 
     * @return {@code true} if the server is running
     */
    public boolean isRunning() {
        return Iterables.get(execute("concourse", "status"), 0).contains(
                "is running");
    }

    /**
     * Start the server.
     */
    public void start() {
        try {
            for (String line : execute("start")) {
                log.info(line);
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }

    }

    /**
     * Stop the server.
     */
    public void stop() {
        try {
            for (String line : execute("stop")) {
                log.info(line);
            }
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }

    }

    /**
     * Recursively delete a directory and all of its contents.
     * 
     * @param directory
     */
    private void deleteDirectory(String directory) {
        try {
            File dir = new File(directory);
            for (File file : dir.listFiles()) {
                if(file.isDirectory()) {
                    deleteDirectory(file.getAbsolutePath());
                }
                else {
                    file.delete();
                }
            }
            dir.delete();
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Execute a command line interface script and return the output.
     * 
     * @param cli
     * @param args
     * @return the script output
     */
    private List<String> execute(String cli, String... args) {
        try {
            String command = "sh " + cli;
            for (String arg : args) {
                command += " " + arg;
            }
            Process process = Runtime.getRuntime().exec(command, null,
                    new File(installDirectory + File.separator + BIN));
            return Processes.getStdOut(process);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
