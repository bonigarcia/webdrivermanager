/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia.wdm;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.io.File.separator;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.copyOf;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * URL filtering logic.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.7.2
 */
public class UrlFilter {

    final Logger log = getLogger(lookup().lookupClass());

    public List<URL> filterByOs(List<URL> list, String osName) {
        log.trace("URLs before filtering by OS ({}): {}", osName, list);
        List<URL> out = new ArrayList<>();

        for (URL url : list) {
            for (OperativeSystem os : OperativeSystem.values()) {
                if (((osName.contains(os.name())
                        && url.getFile().toUpperCase().contains(os.name()))
                        || (osName.equalsIgnoreCase("mac")
                        && url.getFile().toLowerCase().contains("osx")))
                        && !out.contains(url)) {
                    out.add(url);
                }
            }
        }

        log.trace("URLs after filtering by OS ({}): {}", osName, out);
        return out;
    }

    public List<URL> filterByArch(List<URL> list, Architecture arch) {
        log.trace("URLs before filtering by architecture ({}): {}", arch, list);
        List<URL> out = new ArrayList<>(list);

        if (out.size() > 0 && arch != null) {
            for (URL url : list) {
                final String urlFile = url.getFile();
                final String fileName = urlFile.substring(urlFile.lastIndexOf("/"), urlFile.length());
                if (!fileName.contains("x86")
                        && !url.getFile().contains("64")
                        && !url.getFile().contains("i686")
                        && !url.getFile().contains("32")) {
                    log.debug("URL doesn't contain any architecture symbols. No filtering will be done.");
                    continue;
                }
                if (!fileName.contains(arch.toString() + ".") && !fileName.contains(arch.toString() + "_")) {
                    out.remove(url);
                }
            }
        }

        log.trace("URLs after filtering by architecture ({}): {}", arch, out);
        return out;
    }

    public List<URL> filterByDistro(List<URL> list, String version)
            throws IOException {
        String distro = getDistroName();
        log.trace("URLs before filtering by Linux distribution ({}): {}",
                distro, list);
        List<URL> out = new ArrayList<>(list);

        for (URL url : list) {
            if (url.getFile().contains(version)
                    && !url.getFile().contains(distro)) {
                out.remove(url);
            }
        }

        log.trace("URLs after filtering by Linux distribution ({}): {}", distro,
                out);
        return out;
    }

    public List<URL> filterByIgnoredVersions(List<URL> list,
                                             String... ignoredVersions) {
        if (log.isTraceEnabled()) {
            log.trace("URLs before filtering by ignored versions ({}): {}",
                    Arrays.toString(ignoredVersions), list);
        }
        List<URL> out = new ArrayList<>(list);

        for (URL url : list) {
            for (String s : ignoredVersions) {
                if (url.getFile().contains(s)) {
                    log.info("Ignoring version {}", s);
                    out.remove(url);
                }
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("URLs after filtering by ignored versions ({}): {}",
                    Arrays.toString(ignoredVersions), out);
        }
        return out;
    }

    private String getDistroName() throws IOException {
        String out = "";
        final String key = "UBUNTU_CODENAME";
        File dir = new File(separator + "etc");
        File[] fileList = new File[0];
        if (dir.exists()) {
            fileList = dir.listFiles((dir1, filename) -> filename.endsWith("-release"));
        }
        File fileVersion = new File(separator + "proc", "version");
        if (fileVersion.exists()) {
            fileList = copyOf(fileList, fileList.length + 1);
            fileList[fileList.length - 1] = fileVersion;
        }
        for (File f : fileList) {
            if (f.isDirectory()) {
                continue;
            }
            try (BufferedReader myReader = new BufferedReader(
                    new FileReader(f))) {
                String strLine = null;
                while ((strLine = myReader.readLine()) != null) {
                    if (strLine.contains(key)) {
                        int beginIndex = key.length();
                        out = strLine.substring(beginIndex + 1);
                    }
                }
            }
        }
        return out;
    }

}
