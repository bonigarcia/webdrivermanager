/*
 * (C) Copyright 2020 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.versions;

import static io.github.bonigarcia.wdm.etc.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.versions.Shell.getVersionFromPosixOutput;
import static io.github.bonigarcia.wdm.versions.Shell.getVersionFromWmicOutput;
import static io.github.bonigarcia.wdm.versions.Shell.runAndWait;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.etc.Config;
import io.github.bonigarcia.wdm.online.HttpClient;

/**
 * Driver and browser version detector.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 4.0.0
 */
public class VersionDetector {

    static final String REG_SZ = "REG_SZ";
    static final String ONLINE = "online";
    static final String LOCAL = "local";

    final Logger log = getLogger(lookup().lookupClass());

    Config config;
    HttpClient httpClient;
    Properties versionsProperties;
    boolean isSnap;

    public VersionDetector(Config config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    public Optional<String> getDriverVersionFromProperties(String key) {
        // Chromium values are the same than Chrome
        if (key.contains("chromium")) {
            key = key.replace("chromium", "chrome");
        }

        boolean online = config.getVersionsPropertiesOnlineFirst();
        String onlineMessage = online ? ONLINE : LOCAL;
        log.debug("Getting driver version for {} from {} versions.properties",
                key, onlineMessage);
        String value = getVersionFromProperties(online).getProperty(key);
        if (value == null) {
            String notOnlineMessage = online ? LOCAL : ONLINE;
            log.debug(
                    "Driver for {} not found in {} properties (using {} version.properties)",
                    key, onlineMessage, notOnlineMessage);
            versionsProperties = null;
            value = getVersionFromProperties(!online).getProperty(key);
        }
        return value == null ? empty() : Optional.of(value);
    }

    public Properties getVersionFromProperties(boolean online) {
        if (versionsProperties != null) {
            log.trace("Already created versions.properties");
            return versionsProperties;
        } else {
            try (InputStream inputStream = getVersionsInputStream(online)) {
                versionsProperties = new Properties();
                versionsProperties.load(inputStream);
            } catch (Exception e) {
                versionsProperties = null;
                throw new IllegalStateException(
                        "Cannot read versions.properties", e);
            }
            return versionsProperties;
        }
    }

    public InputStream getVersionsInputStream(boolean online)
            throws IOException {
        String onlineMessage = online ? ONLINE : LOCAL;
        log.trace("Reading {} version.properties to find out driver version",
                onlineMessage);
        InputStream inputStream;
        try {
            if (online) {
                inputStream = getOnlineVersionsInputStream();
            } else {
                inputStream = getLocalVersionsInputStream();
            }
        } catch (Exception e) {
            String exceptionMessage = online ? LOCAL : ONLINE;
            log.warn("Error reading version.properties, using {} instead",
                    exceptionMessage);
            if (online) {
                inputStream = getLocalVersionsInputStream();
            } else {
                inputStream = getOnlineVersionsInputStream();
            }
        }
        return inputStream;
    }

    public InputStream getLocalVersionsInputStream() {
        InputStream inputStream;
        inputStream = Config.class.getResourceAsStream("/versions.properties");
        return inputStream;
    }

    public InputStream getOnlineVersionsInputStream() throws IOException {
        return httpClient
                .execute(httpClient
                        .createHttpGet(config.getVersionsPropertiesUrl()))
                .getEntity().getContent();
    }

    public Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion, URL driverUrl,
            Charset versionCharset, String driverName, String versionLabel,
            String latestLabel) {
        String url = driverVersion.isPresent()
                ? driverUrl + latestLabel + "_" + driverVersion.get()
                : driverUrl + versionLabel;
        Optional<String> result = Optional.empty();
        try (InputStream response = httpClient
                .execute(httpClient.createHttpGet(new URL(url))).getEntity()
                .getContent()) {
            result = Optional.of(IOUtils.toString(response, versionCharset)
                    .replaceAll("\r\n", ""));
        } catch (Exception e) {
            log.warn("Exception reading {} to get latest version of {} ({})",
                    url, driverName, e.getMessage());
        }
        if (result.isPresent()) {
            log.debug("Latest version of {} according to {} is {}", driverName,
                    url, result.get());
        }
        return result;
    }

    public Optional<String> getDefaultBrowserVersion(String[] programFilesEnvs,
            String[] winBrowserNames, String linuxBrowserName,
            String macBrowserName, String versionFlag,
            String browserNameInOutput) {

        String browserBinaryPath = config.getBinaryPath();
        if (IS_OS_WINDOWS) {
            String winName = "";
            for (int i = 0; i < programFilesEnvs.length; i++) {
                winName = winBrowserNames.length > 1 ? winBrowserNames[i]
                        : winBrowserNames[0];
                String browserVersionOutput = getBrowserVersionInWindows(
                        programFilesEnvs[i], winName, browserBinaryPath);
                if (!isNullOrEmpty(browserVersionOutput)) {
                    return Optional
                            .of(getVersionFromWmicOutput(browserVersionOutput));
                }
            }
        } else if (IS_OS_LINUX || IS_OS_MAC) {
            String browserPath = getPosixBrowserPath(linuxBrowserName,
                    macBrowserName, browserBinaryPath);
            String browserVersionOutput = runAndWait(browserPath, versionFlag);
            if (browserVersionOutput.toLowerCase().contains("snap")) {
                isSnap = true;
            }
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional.of(getVersionFromPosixOutput(
                        browserVersionOutput, browserNameInOutput));
            }
        }
        return empty();
    }

    public String getPosixBrowserPath(String linuxBrowserName,
            String macBrowserName, String browserBinaryPath) {
        if (!isNullOrEmpty(browserBinaryPath)) {
            return browserBinaryPath;
        } else {
            return IS_OS_LINUX ? linuxBrowserName : macBrowserName;
        }
    }

    public String getBrowserVersionInWindows(String programFilesEnv,
            String winBrowserName, String browserBinaryPath) {
        String programFiles = System.getenv(programFilesEnv).replaceAll("\\\\",
                "\\\\\\\\");
        String browserPath = isNullOrEmpty(browserBinaryPath)
                ? programFiles + winBrowserName
                : browserBinaryPath;
        String wmic = "wmic.exe";
        return runAndWait(findFileLocation(wmic), wmic, "datafile", "where",
                "name='" + browserPath + "'", "get", "Version", "/value");
    }

    public Optional<String> getBrowserVersionFromWinRegistry(String key,
            String value) {
        Optional<String> browserVersionFromRegistry = empty();
        String regQueryResult = Shell.runAndWait("REG", "QUERY", key, "/v",
                value);
        int i = regQueryResult.indexOf(REG_SZ);
        int j = regQueryResult.indexOf('.', i);
        if (i != -1 && j != -1) {
            browserVersionFromRegistry = Optional.of(
                    regQueryResult.substring(i + REG_SZ.length(), j).trim());
        }
        return browserVersionFromRegistry;
    }

    public File findFileLocation(String filename) {
        // Alternative #1: in System32 folder
        File system32Folder = new File(System.getenv("SystemRoot"), "System32");
        File system32File = new File(system32Folder, filename);
        if (checkFileAndFolder(system32Folder, system32File)) {
            return system32Folder;
        }
        // Alternative #2: in wbem folder
        File wbemFolder = new File(system32Folder, "wbem");
        File wbemFile = new File(wbemFolder, filename);
        if (checkFileAndFolder(wbemFolder, wbemFile)) {
            return wbemFolder;
        }
        return new File(".");
    }

    public boolean checkFileAndFolder(File folder, File file) {
        return folder.exists() && folder.isDirectory() && file.exists()
                && file.isFile();
    }

    public boolean isSnap() {
        return isSnap;
    }

}
