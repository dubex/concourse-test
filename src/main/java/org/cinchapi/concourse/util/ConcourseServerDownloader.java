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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * A utility class that can download Concourse Server binaries from Github.
 * 
 * @author jnelson
 */
public final class ConcourseServerDownloader {

    /**
     * Download the Concourse Server binary of the specified {@code version} to
     * the user's home directory.
     * 
     * @param version
     * @return the absolute path to the downloaded file
     */
    public static String download(String version) {
        return download(version, System.getProperty("user.home"));
    }

    /**
     * Download the Concourse Server binary of the specified {@code version} to
     * the
     * specified {@code location}.
     * 
     * @param version
     * @param location
     * @return the absolute path to the downloaded file
     */
    public static String download(String version, String location) {
        String file = location + File.separator + "concourse-server-" + version
                + ".bin";
        if(!Files.exists(Paths.get(file))) {
            log.info(MessageFormat.format("Did not find an installer for "
                    + "ConcourseServer v{0} in {1}", version, location));
            URL url;
            try (FileOutputStream stream = new FileOutputStream(file)) {
                url = new URI(getDownloadUrl(version)).toURL();
                ReadableByteChannel channel = Channels.newChannel(url
                        .openStream());
                stream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
                log.info(MessageFormat.format("Downloaded the installer for "
                        + "Concourse Server v{0} from {1}. The installer is "
                        + "stored in {2}", version, url.toString(), location));
            }
            catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        return file;
    }

    /**
     * Return the download URL for the specified {@code version} of Concourse
     * Server.
     * 
     * @param version
     * @return the download URL
     */
    private static String getDownloadUrl(String version) {
        String page = RELEASE_PAGE_URL_BASE + version;
        try {
            Document doc = Jsoup.parse(new URI(page).toURL(), 10000);
            Elements links = doc.select("a[href]");
            Iterator<Element> it = links.iterator();
            while (it.hasNext()) {
                Element element = it.next();
                String url = element.attr("href");
                if(url.endsWith(".bin")) {
                    return DOWNLOAD_URL_BASE + url;
                }
            }
            throw new Exception("Could not determine download URL for version "
                    + version);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * The base Github URL for the release page.
     */
    private static final String RELEASE_PAGE_URL_BASE = "https://github.com/cinchapi/concourse/releases/tag/v";

    /**
     * The base Github URL for the download page.
     */
    private static final String DOWNLOAD_URL_BASE = "https://github.com";

    // ---logger
    private static final Logger log = LoggerFactory
            .getLogger(ConcourseServerDownloader.class);

    private ConcourseServerDownloader() {}

}
