/*
 * (C) Copyright 2020 Boni Garcia (https://bonigarcia.github.io/)
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
 * limitations under the License..
 *
 */
package io.github.bonigarcia.wdm.online;

import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static java.io.File.separator;
import static java.lang.Integer.signum;
import static java.lang.Integer.valueOf;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.copyOf;
import static java.util.Collections.singletonList;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

/**
 * Handler for URLs (filtering, version selection).
 *
 * @author Boni Garcia
 * @since 4.0.0
 */
public class UrlHandler {

    public static final String ALPHA = "alpha";
    public static final String BETA = "beta";

    final Logger log = getLogger(lookup().lookupClass());

    Config config;
    List<URL> candidateUrls;
    String driverVersion;
    String shortDriverName;
    Function<String, Optional<URL>> buildUrlFunction;

    public UrlHandler(Config config, List<URL> candidateUrls,
            String driverVersion, String shortDriverName,
            Function<String, Optional<URL>> buildUrlFunction) {
        this.config = config;
        this.candidateUrls = candidateUrls;
        this.driverVersion = driverVersion;
        this.shortDriverName = shortDriverName;
        this.buildUrlFunction = buildUrlFunction;
    }

    public void filterByDriverName(String driverName) {
        candidateUrls = candidateUrls.stream()
                .filter(url -> url.getFile().contains(driverName)
                        && !url.getFile().contains("-symbols"))
                .collect(toList());
    }

    public void filterByVersion(String driverVersion) {
        this.driverVersion = driverVersion;
        candidateUrls = candidateUrls.stream()
                .filter(url -> url.getFile().contains(driverVersion))
                .collect(toList());
    }

    public void filterByLatestVersion(Function<URL, String> getCurrentVersion) {
        log.trace("Checking the latest version using URL list {}",
                candidateUrls);
        List<URL> out = new ArrayList<>();
        List<URL> copyOfList = new ArrayList<>(candidateUrls);
        String foundFriverVersion = null;

        for (URL url : copyOfList) {
            try {
                if (isNotStable(url)) {
                    continue;
                }
                String currentVersion = getCurrentVersion.apply(url);
                if (isNullOrEmpty(foundFriverVersion)) {
                    foundFriverVersion = currentVersion;
                }
                if (versionCompare(currentVersion, foundFriverVersion) > 0) {
                    foundFriverVersion = currentVersion;
                    out.clear();
                }
                if (url.getFile().contains(foundFriverVersion)) {
                    out.add(url);
                }
            } catch (Exception e) {
                log.trace("There was a problem with URL {} : {}", url,
                        e.getMessage());
                candidateUrls.remove(url);
            }
        }

        this.driverVersion = foundFriverVersion;
        this.candidateUrls = out;
    }

    public void filterByBeta(boolean useBeta) {
        if (!useBeta) {
            log.trace("URLs before filtering by beta versions: {}",
                    candidateUrls);
            candidateUrls = candidateUrls.stream().filter(url -> {
                String fileLowerCase = url.getFile().toLowerCase(ROOT);
                return !fileLowerCase.contains(BETA)
                        && !fileLowerCase.contains(ALPHA);
            }).collect(toList());
            log.trace("URLs after filtering by beta versions: {}",
                    candidateUrls);
        }
    }

    public void filterByOs(String driverName, String osName) {
        if (!driverName.equalsIgnoreCase("IEDriverServer")) {
            log.trace("URLs before filtering by OS ({}): {}", osName,
                    candidateUrls);
            candidateUrls = candidateUrls.stream().filter(url -> OperatingSystem
                    .valueOf(osName).matchOs(url.getFile())).collect(toList());
            log.trace("URLs after filtering by OS ({}): {}", osName,
                    candidateUrls);
        }
    }

    public void filterByArch(Architecture arch) {
        log.trace("URLs before filtering by architecture ({}): {}", arch,
                candidateUrls);

        candidateUrls = config.getArchitecture().filterArm64(candidateUrls);

        if (candidateUrls.size() > 1 && arch != null) {
            candidateUrls = candidateUrls.stream().filter(arch::matchUrl)
                    .collect(toList());
        }
        log.trace("URLs after filtering by architecture ({}): {}", arch,
                candidateUrls);

        if (candidateUrls.isEmpty() && !candidateUrls.isEmpty()) {
            candidateUrls = singletonList(
                    candidateUrls.get(candidateUrls.size() - 1));
            log.trace(
                    "Empty URL list after filtering by architecture ... using last candidate: {}",
                    candidateUrls);
        }
    }

    public void filterByIgnoredVersions(List<String> ignoredVersions) {
        if (!ignoredVersions.isEmpty() && !candidateUrls.isEmpty()) {
            log.trace("URLs before filtering by ignored versions ({}): {}",
                    ignoredVersions, candidateUrls);
            candidateUrls = candidateUrls.stream()
                    .filter(url -> ignoredVersions.stream().noneMatch(
                            version -> url.getFile().contains(version)))
                    .collect(toList());

            log.trace("URLs after filtering by ignored versions ({}): {}",
                    ignoredVersions, candidateUrls);
        }
    }

    public String getDistroName() throws IOException {
        String out = "";
        final String key = "UBUNTU_CODENAME";
        File dir = new File(separator + "etc");
        File[] fileList = new File[0];
        if (dir.exists()) {
            fileList = dir.listFiles(
                    (path, filename) -> filename.endsWith("-release"));
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

    public void resetList(List<URL> newCandidateUrls) {
        candidateUrls = newCandidateUrls.stream()
                .filter(url -> !url.getFile().contains(driverVersion))
                .collect(toList());
    }

    public boolean isNotStable(URL url) {
        String fileLowerCase = url.getFile().toLowerCase(ROOT);
        return !config.isUseMirror() && (fileLowerCase.contains(BETA)
                || fileLowerCase.contains(ALPHA));
    }

    public Integer versionCompare(String str1, String str2) {
        String versionRegex = config.getBrowserVersionDetectionRegex();
        String[] vals1 = str1.replaceAll(versionRegex, "").split("\\.");
        String[] vals2 = str2.replaceAll(versionRegex, "").split("\\.");

        if (vals1[0].equals("")) {
            vals1[0] = "0";
        }
        if (vals2[0].equals("")) {
            vals2[0] = "0";
        }

        int i = 0;
        while (i < vals1.length && i < vals2.length
                && vals1[i].equals(vals2[i])) {
            i++;
        }

        if (i < vals1.length && i < vals2.length) {
            return signum(valueOf(vals1[i]).compareTo(valueOf(vals2[i])));
        } else {
            return signum(vals1.length - vals2.length);
        }
    }

    public List<URL> getCandidateUrls() {
        return candidateUrls;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public boolean hasNoCandidateUrl() {
        return candidateUrls.isEmpty();
    }

    public URL getCandidateUrl() {
        if (hasNoCandidateUrl()) {
            Optional<URL> buildUrl = buildUrlFunction.apply(driverVersion);

            if (buildUrl.isPresent()) {
                URL url = buildUrl.get();

                // Check ignored versions
                Stream<String> ignoredVersionsStream = config
                        .getIgnoreVersions().stream();
                if (ignoredVersionsStream.noneMatch(url.getFile()::contains)) {
                    return url;
                }
            }

            String driverVersionLabel = isNullOrEmpty(driverVersion) ? ""
                    : " " + driverVersion;
            String errorMessage = String.format(
                    "No proper candidate URL to download %s%s", shortDriverName,
                    driverVersionLabel);
            throw new WebDriverManagerException(errorMessage);
        }

        return candidateUrls.iterator().next();
    }

}
