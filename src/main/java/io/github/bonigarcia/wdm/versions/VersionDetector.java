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
package io.github.bonigarcia.wdm.versions;

import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.versions.Shell.runAndWait;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.online.HttpClient;

/**
 * Driver and browser version detector.
 *
 * @author Boni Garcia
 * @since 4.0.0
 */
public class VersionDetector {

    static final String ONLINE = "online";
    static final String LOCAL = "local";
    static final String VERSIONS_PROPERTIES = "versions.properties";
    static final String COMMANDS_PROPERTIES = "commands.properties";
    static final String FILE_PROTOCOL = "file";

    final Logger log = getLogger(lookup().lookupClass());

    Config config;
    HttpClient httpClient;
    Map<String, Properties> propertiesMap;
    boolean isSnap;

    public VersionDetector(Config config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        propertiesMap = new HashMap<>();
    }

    public Optional<String> getDriverVersionFromProperties(String key) {
        // Chromium values are the same than Chrome
        if (key.contains("chromium")) {
            key = key.replace("chromium", "chrome");
        }

        boolean online = config.isVersionsPropertiesOnlineFirst();
        String propertiesName = VERSIONS_PROPERTIES;
        String onlineMessage = online ? ONLINE : LOCAL;
        log.debug("Getting driver version for {} from {} {}", key,
                onlineMessage, propertiesName);
        String value = getValueFromProperties(
                getProperties(propertiesName, online), key);
        if (value == null) {
            String notOnlineMessage = online ? LOCAL : ONLINE;
            log.debug("Driver for {} not found in {} properties (using {} {})",
                    key, onlineMessage, notOnlineMessage, propertiesName);
            propertiesMap.remove(propertiesName);
            value = getProperties(propertiesName, !online).getProperty(key);
        }
        return value == null ? empty() : Optional.of(value);
    }

    public String getValueFromProperties(Properties properties, String key) {
        String keyWithOs = key + "." + config.getOperatingSystem().getName();
        return Optional.ofNullable(properties.getProperty(keyWithOs))
                .orElse(properties.getProperty(key));
    }

    public Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion, URL driverUrl,
            Charset versionCharset, String driverName, String versionLabel,
            String latestLabel, Optional<String> optOsLabel) {
        String osLabel = optOsLabel.isPresent() ? optOsLabel.get() : "";
        String url = driverVersion.isPresent()
                ? driverUrl + latestLabel + "_" + driverVersion.get() + osLabel
                : driverUrl + versionLabel;
        Optional<String> result = Optional.empty();
        try (InputStream response = httpClient
                .execute(httpClient.createHttpGet(new URL(url))).getEntity()
                .getContent()) {
            result = Optional.of(IOUtils.toString(response, versionCharset)
                    .replace("\r\n", ""));
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

    public Optional<Path> getBrowserPath(String browserName) {
        log.debug("Detecting {} path using the commands database", browserName);

        String pathStr = "";
        Properties commandsProperties = getProperties(COMMANDS_PROPERTIES,
                config.isCommandsPropertiesOnlineFirst());
        List<String> commandsPerOs = getCommandsList(browserName,
                commandsProperties);

        for (String commandKey : commandsPerOs) {
            String command = commandsProperties.get(commandKey).toString();
            int lastSpaceIndex = command.lastIndexOf(" ");
            String firstCommand = command;
            if (lastSpaceIndex != -1) {
                firstCommand = command.substring(0, lastSpaceIndex);
            }

            OperatingSystem operatingSystem = config.getOperatingSystem();
            switch (operatingSystem) {
            case WIN:
                if (command.toLowerCase(ROOT).contains("wmic")) {
                    File wmicLocation = findFileLocation("wmic.exe");
                    String newCommand = command.replace("Version", "Caption");
                    String captionOutput = runAndWait(wmicLocation,
                            newCommand.split(" "));
                    int iCaption = captionOutput.indexOf("=");
                    if (iCaption != -1) {
                        pathStr = captionOutput.substring(iCaption + 1);
                    }
                }
                break;

            case MAC:
            case LINUX:
            default:
                if (firstCommand.contains("/")) {
                    pathStr = firstCommand;
                } else {
                    String[] commandArray = new String[] { "bash", "-c",
                            "type -p " + firstCommand };
                    pathStr = runAndWait(commandArray);
                }
                break;
            }
            Path path = Paths.get(pathStr);
            if (!isNullOrEmpty(pathStr) && Files.exists(path)) {
                log.debug("The path of {} is {}", browserName, pathStr);
                return Optional.of(path);
            }
        }

        log.info("Browser {} is not available in the system", browserName);
        return empty();
    }

    public Optional<String> getBrowserVersionFromTheShell(String browserName) {
        Optional<String> browserVersionUsingProperties = empty();
        String browserVersionDetectionCommand = config
                .getBrowserVersionDetectionCommand();
        if (!isNullOrEmpty(browserVersionDetectionCommand)) {
            browserVersionUsingProperties = getBrowserVersionUsingCommand(
                    browserVersionDetectionCommand);
        }
        if (browserVersionUsingProperties.isPresent()) {
            return browserVersionUsingProperties;
        }

        boolean online = config.isCommandsPropertiesOnlineFirst();
        String propertiesName = COMMANDS_PROPERTIES;
        Properties commandsProperties = getProperties(propertiesName, online);

        String onlineMessage = online ? ONLINE : LOCAL;
        log.debug("Detecting {} version using {} {}", browserName,
                onlineMessage, propertiesName);

        browserVersionUsingProperties = getBrowserVersionUsingProperties(
                browserName, commandsProperties);

        if (!browserVersionUsingProperties.isPresent()) {
            String notOnlineMessage = online ? LOCAL : ONLINE;
            log.debug(
                    "Browser version for {} not detected using {} properties (using {} {})",
                    browserName, onlineMessage, notOnlineMessage,
                    propertiesName);

            commandsProperties = getProperties(propertiesName, !online);
            browserVersionUsingProperties = getBrowserVersionUsingProperties(
                    browserName, commandsProperties);
        }

        return browserVersionUsingProperties;
    }

    protected Optional<String> getBrowserVersionUsingProperties(
            String browserName, Properties commandsProperties) {
        List<String> commandsPerOs = getCommandsList(browserName,
                commandsProperties);

        for (String commandKey : commandsPerOs) {
            String command = commandsProperties.get(commandKey).toString();

            Optional<String> browserVersionUsingCommand = getBrowserVersionUsingCommand(
                    command);
            if (browserVersionUsingCommand.isPresent()) {
                return browserVersionUsingCommand;
            }
        }

        return empty();
    }

    protected List<String> getCommandsList(String browserName,
            Properties commandsProperties) {
        OperatingSystem operatingSystem = config.getOperatingSystem();
        return Collections.list(commandsProperties.keys()).stream()
                .map(Object::toString).filter(s -> s.contains(browserName))
                .filter(operatingSystem::matchOs).sorted()
                .collect(Collectors.toList());
    }

    protected Optional<String> getBrowserVersionUsingCommand(String command) {
        String commandLowerCase = command.toLowerCase(ROOT);
        boolean isWmic = commandLowerCase.contains("wmic");
        boolean isRegQuery = commandLowerCase.contains("reg query");
        int lastSpaceIndex = command.lastIndexOf(" ");

        String[] commandArray;
        if (!isWmic && !isRegQuery && lastSpaceIndex != -1) {
            // For non-windows (wmic or reg query), the command is splitted into
            // two parts: {"browserPath", "--version"}
            commandArray = new String[] { command.substring(0, lastSpaceIndex),
                    command.substring(lastSpaceIndex + 1) };
        } else {
            commandArray = command.split(" ");
        }

        String browserVersionOutput;
        if (isWmic) {
            File wmicLocation = findFileLocation("wmic.exe");
            browserVersionOutput = runAndWait(wmicLocation, commandArray);
        } else {
            browserVersionOutput = runAndWait(commandArray);
        }

        if (!isNullOrEmpty(browserVersionOutput)) {
            if (browserVersionOutput.toLowerCase(ROOT).contains("snap")) {
                isSnap = true;
            }

            String parsedBrowserVersion = browserVersionOutput
                    .replaceAll(config.getBrowserVersionDetectionRegex(), "");
            log.trace("Detected browser version is {}", parsedBrowserVersion);
            return Optional.of(getMajorVersion(parsedBrowserVersion));
        } else {

            return empty();
        }
    }

    protected Properties getProperties(String propertiesName, boolean online) {
        if (propertiesMap.containsKey(propertiesName)) {
            log.trace("Already created {}", propertiesName);
            return propertiesMap.get(propertiesName);
        } else {
            Properties properties = null;
            try (InputStream inputStream = getVersionsInputStream(
                    propertiesName, online)) {
                properties = new Properties();
                properties.load(new StringReader(IOUtils
                        .toString(inputStream, UTF_8).replace("\\", "\\\\")));
                propertiesMap.put(propertiesName, properties);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot read " + propertiesName,
                        e);
            }
            return properties;
        }
    }

    protected InputStream getVersionsInputStream(String propertiesName,
            boolean online) throws IOException {
        String onlineMessage = online ? ONLINE : LOCAL;
        log.trace("Reading {} {} to find out driver version", onlineMessage,
                propertiesName);
        InputStream inputStream;
        try {
            if (online) {
                inputStream = getOnlineInputStream(propertiesName);
            } else {
                inputStream = getLocalInputStream(propertiesName);
            }
        } catch (Exception e) {
            String exceptionMessage = online ? LOCAL : ONLINE;
            log.warn("Error reading {}, using {} instead", propertiesName,
                    exceptionMessage);
            if (online) {
                inputStream = getLocalInputStream(propertiesName);
            } else {
                inputStream = getOnlineInputStream(propertiesName);
            }
        }
        return inputStream;
    }

    protected InputStream getLocalInputStream(String propertiesName) {
        InputStream inputStream;
        inputStream = Config.class.getResourceAsStream("/" + propertiesName);
        return inputStream;
    }

    protected InputStream getOnlineInputStream(String propertiesName)
            throws IOException {
        URL propertiesUrl = propertiesName.equals(VERSIONS_PROPERTIES)
                ? config.getVersionsPropertiesUrl()
                : config.getCommandsPropertiesUrl();

        InputStream inputStream;
        if (propertiesUrl.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
            inputStream = new FileInputStream(
                    new File(propertiesUrl.getFile()));
        } else {
            inputStream = httpClient
                    .execute(httpClient.createHttpGet(propertiesUrl))
                    .getEntity().getContent();
        }

        return inputStream;
    }

    protected String getMajorVersion(String version) {
        int i = version.indexOf('.');
        return i != -1 ? version.substring(0, i) : version;
    }

    protected File findFileLocation(String filename) {
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

    protected boolean checkFileAndFolder(File folder, File file) {
        return folder.exists() && folder.isDirectory() && file.exists()
                && file.isFile();
    }

    public boolean isSnap() {
        return isSnap;
    }

}
