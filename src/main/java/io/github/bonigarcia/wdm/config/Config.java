/*
 * (C) Copyright 2018 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.config;

import static io.github.bonigarcia.wdm.config.OperatingSystem.LINUX;
import static io.github.bonigarcia.wdm.config.OperatingSystem.MAC;
import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;
import static java.lang.String.join;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Locale.ROOT;
import static org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Configuration class.
 *
 * @author Boni Garcia
 * @since 2.2.0
 */
public class Config {

    final Logger log = getLogger(lookup().lookupClass());

    static final String HOME = "~";
    static final String PWD = ".";
    static final String SCREEN_RESOLUTION_SEPARATOR = "x";

    ConfigKey<String> properties = new ConfigKey<>("wdm.properties",
            String.class, "webdrivermanager.properties");

    ConfigKey<String> cachePath = new ConfigKey<>("wdm.cachePath",
            String.class);
    ConfigKey<String> resolutionCachePath = new ConfigKey<>(
            "wdm.resolutionCachePath", String.class);
    ConfigKey<Boolean> forceDownload = new ConfigKey<>("wdm.forceDownload",
            Boolean.class);
    ConfigKey<Boolean> useMirror = new ConfigKey<>("wdm.useMirror",
            Boolean.class);
    ConfigKey<Boolean> useBetaVersions = new ConfigKey<>("wdm.useBetaVersions",
            Boolean.class);
    ConfigKey<Boolean> avoidExport = new ConfigKey<>("wdm.avoidExport",
            Boolean.class);
    ConfigKey<Boolean> avoidOutputTree = new ConfigKey<>("wdm.avoidOutputTree",
            Boolean.class);
    ConfigKey<Boolean> avoidBrowserDetection = new ConfigKey<>(
            "wdm.avoidBrowserDetection", Boolean.class);
    ConfigKey<Boolean> avoidFallback = new ConfigKey<>("wdm.avoidFallback",
            Boolean.class);
    ConfigKey<Boolean> avoidResolutionCache = new ConfigKey<>(
            "wdm.avoidResolutionCache", Boolean.class);
    ConfigKey<Boolean> avoidReadReleaseFromRepository = new ConfigKey<>(
            "wdm.avoidReadReleaseFromRepository", Boolean.class);
    ConfigKey<Boolean> avoidTmpFolder = new ConfigKey<>("wdm.avoidTmpFolder",
            Boolean.class);
    ConfigKey<Integer> timeout = new ConfigKey<>("wdm.timeout", Integer.class);
    ConfigKey<Boolean> versionsPropertiesOnlineFirst = new ConfigKey<>(
            "wdm.versionsPropertiesOnlineFirst", Boolean.class);
    ConfigKey<Boolean> commandsPropertiesOnlineFirst = new ConfigKey<>(
            "wdm.commandsPropertiesOnlineFirst", Boolean.class);
    ConfigKey<URL> versionsPropertiesUrl = new ConfigKey<>(
            "wdm.versionsPropertiesUrl", URL.class);
    ConfigKey<URL> commandsPropertiesUrl = new ConfigKey<>(
            "wdm.commandsPropertiesUrl", URL.class);
    ConfigKey<Boolean> clearResolutionCache = new ConfigKey<>(
            "wdm.clearResolutionCache", Boolean.class);
    ConfigKey<Boolean> clearDriverCache = new ConfigKey<>(
            "wdm.clearDriverCache", Boolean.class);

    ConfigKey<String> architecture = new ConfigKey<>("wdm.architecture",
            String.class, defaultArchitecture());
    ConfigKey<String> os = new ConfigKey<>("wdm.os", String.class,
            defaultOsName());
    ConfigKey<String> proxy = new ConfigKey<>("wdm.proxy", String.class);
    ConfigKey<String> proxyUser = new ConfigKey<>("wdm.proxyUser",
            String.class);
    ConfigKey<String> proxyPass = new ConfigKey<>("wdm.proxyPass",
            String.class);
    ConfigKey<String> ignoreVersions = new ConfigKey<>("wdm.ignoreVersions",
            String.class);
    ConfigKey<String> gitHubToken = new ConfigKey<>("wdm.gitHubToken",
            String.class);
    ConfigKey<String> defaultBrowser = new ConfigKey<>("wdm.defaultBrowser",
            String.class);

    ConfigKey<String> chromeDriverVersion = new ConfigKey<>(
            "wdm.chromeDriverVersion", String.class);
    ConfigKey<String> chromeVersion = new ConfigKey<>("wdm.chromeVersion",
            String.class);
    ConfigKey<String> chromeDriverExport = new ConfigKey<>(
            "wdm.chromeDriverExport", String.class);
    ConfigKey<URL> chromeDriverUrl = new ConfigKey<>("wdm.chromeDriverUrl",
            URL.class);
    ConfigKey<URL> chromeDriverMirrorUrl = new ConfigKey<>(
            "wdm.chromeDriverMirrorUrl", URL.class);
    ConfigKey<String> chromeDownloadUrlPattern = new ConfigKey<>(
            "wdm.chromeDownloadUrlPattern", String.class);

    ConfigKey<String> edgeDriverVersion = new ConfigKey<>(
            "wdm.edgeDriverVersion", String.class);
    ConfigKey<String> edgeVersion = new ConfigKey<>("wdm.edgeVersion",
            String.class);
    ConfigKey<String> edgeDriverExport = new ConfigKey<>("wdm.edgeDriverExport",
            String.class);
    ConfigKey<URL> edgeDriverUrl = new ConfigKey<>("wdm.edgeDriverUrl",
            URL.class);
    ConfigKey<String> edgeDownloadUrlPattern = new ConfigKey<>(
            "wdm.edgeDownloadUrlPattern", String.class);

    ConfigKey<String> geckoDriverVersion = new ConfigKey<>(
            "wdm.geckoDriverVersion", String.class);
    ConfigKey<String> firefoxVersion = new ConfigKey<>("wdm.firefoxVersion",
            String.class);
    ConfigKey<String> firefoxDriverExport = new ConfigKey<>(
            "wdm.geckoDriverExport", String.class);
    ConfigKey<URL> firefoxDriverUrl = new ConfigKey<>("wdm.geckoDriverUrl",
            URL.class);
    ConfigKey<URL> firefoxDriverMirrorUrl = new ConfigKey<>(
            "wdm.geckoDriverMirrorUrl", URL.class);

    ConfigKey<String> iExplorerDriverVersion = new ConfigKey<>(
            "wdm.iExplorerDriverVersion", String.class);
    ConfigKey<String> iExplorerDriverExport = new ConfigKey<>(
            "wdm.iExplorerDriverExport", String.class);
    ConfigKey<URL> iExplorerDriverUrl = new ConfigKey<>(
            "wdm.iExplorerDriverUrl", URL.class);

    ConfigKey<String> operaDriverVersion = new ConfigKey<>(
            "wdm.operaDriverVersion", String.class);
    ConfigKey<String> operaVersion = new ConfigKey<>("wdm.operaVersion",
            String.class);
    ConfigKey<String> operaDriverExport = new ConfigKey<>(
            "wdm.operaDriverExport", String.class);
    ConfigKey<URL> operaDriverUrl = new ConfigKey<>("wdm.operaDriverUrl",
            URL.class);
    ConfigKey<URL> operaDriverMirrorUrl = new ConfigKey<>(
            "wdm.operaDriverMirrorUrl", URL.class);

    ConfigKey<String> chromiumDriverVersion = new ConfigKey<>(
            "wdm.chromiumDriverVersion", String.class);
    ConfigKey<String> chromiumVersion = new ConfigKey<>("wdm.chromiumVersion",
            String.class);
    ConfigKey<String> chromiumDriverSnapPath = new ConfigKey<>(
            "wdm.chromiumDriverSnapPath", String.class);
    ConfigKey<Boolean> useChromiumDriverSnap = new ConfigKey<>(
            "wdm.useChromiumDriverSnap", Boolean.class);

    ConfigKey<Integer> ttl = new ConfigKey<>("wdm.ttl", Integer.class);
    ConfigKey<Integer> ttlForBrowsers = new ConfigKey<>("wdm.ttlForBrowsers",
            Integer.class);
    ConfigKey<String> resolutionCache = new ConfigKey<>("wdm.resolutionCache",
            String.class);
    ConfigKey<String> browserVersionDetectionRegex = new ConfigKey<>(
            "wdm.browserVersionDetectionRegex", String.class);
    ConfigKey<String> browserVersionDetectionCommand = new ConfigKey<>(
            "wdm.browserVersionDetectionCommand", String.class);

    ConfigKey<Integer> serverPort = new ConfigKey<>("wdm.serverPort",
            Integer.class);
    ConfigKey<String> serverPath = new ConfigKey<>("wdm.serverPath",
            String.class);
    ConfigKey<Integer> serverTimeoutSec = new ConfigKey<>(
            "wdm.serverTimeoutSec", Integer.class);

    ConfigKey<String> dockerDaemonUrl = new ConfigKey<>("wdm.dockerDaemonUrl",
            String.class);
    ConfigKey<String> dockerHubUrl = new ConfigKey<>("wdm.dockerHubUrl",
            String.class);
    ConfigKey<String> dockerNetwork = new ConfigKey<>("wdm.dockerNetwork",
            String.class);
    ConfigKey<String> dockerTimezone = new ConfigKey<>("wdm.dockerTimezone",
            String.class);
    ConfigKey<String> dockerLang = new ConfigKey<>("wdm.dockerLang",
            String.class);
    ConfigKey<String> dockerShmSize = new ConfigKey<>("wdm.dockerShmSize",
            String.class);
    ConfigKey<String> dockerTmpfsSize = new ConfigKey<>("wdm.dockerTmpfsSize",
            String.class);
    ConfigKey<String> dockerTmpfsMount = new ConfigKey<>("wdm.dockerTmpfsMount",
            String.class);
    ConfigKey<Integer> dockerStopTimeoutSec = new ConfigKey<>(
            "wdm.dockerStopTimeoutSec", Integer.class);
    ConfigKey<Boolean> dockerEnableVnc = new ConfigKey<>("wdm.dockerEnableVnc",
            Boolean.class);
    ConfigKey<Boolean> dockerViewOnly = new ConfigKey<>("wdm.dockerViewOnly",
            Boolean.class);
    ConfigKey<Boolean> dockerEnableRecording = new ConfigKey<>(
            "wdm.dockerEnableRecording", Boolean.class);
    ConfigKey<String> dockerScreenResolution = new ConfigKey<>(
            "wdm.dockerScreenResolution", String.class);
    ConfigKey<String> dockerVncPassword = new ConfigKey<>(
            "wdm.dockerVncPassword", String.class);
    ConfigKey<Integer> dockerBrowserPort = new ConfigKey<>(
            "wdm.dockerBrowserPort", Integer.class);
    ConfigKey<Integer> dockerVncPort = new ConfigKey<>("wdm.dockerVncPort",
            Integer.class);
    ConfigKey<Integer> dockerNoVncPort = new ConfigKey<>("wdm.dockerNoVncPort",
            Integer.class);
    ConfigKey<Integer> dockerRecordingFrameRate = new ConfigKey<>(
            "wdm.dockerRecordingFrameRate", Integer.class);
    ConfigKey<Path> dockerRecordingOutput = new ConfigKey<>(
            "wdm.dockerRecordingOutput", Path.class);
    ConfigKey<String> dockerRecordingPrefix = new ConfigKey<>(
            "wdm.dockerRecordingPrefix", String.class);
    ConfigKey<String> dockerBrowserSelenoidImageFormat = new ConfigKey<>(
            "wdm.dockerBrowserSelenoidImageFormat", String.class);
    ConfigKey<String> dockerBrowserTwilioImageFormat = new ConfigKey<>(
            "wdm.dockerBrowserTwilioImageFormat", String.class);
    ConfigKey<String> dockerBrowserAerokubeImageFormat = new ConfigKey<>(
            "wdm.dockerBrowserAerokubeImageFormat", String.class);
    ConfigKey<String> dockerBrowserMobileImageFormat = new ConfigKey<>(
            "wdm.dockerBrowserMobileImageFormat", String.class);
    ConfigKey<String> dockerRecordingImage = new ConfigKey<>(
            "wdm.dockerRecordingImage", String.class);
    ConfigKey<String> dockerNoVncImage = new ConfigKey<>("wdm.dockerNoVncImage",
            String.class);
    ConfigKey<String> dockerCustomImage = new ConfigKey<>(
            "wdm.dockerCustomImage", String.class);
    ConfigKey<String> dockerVolumes = new ConfigKey<>("wdm.dockerVolumes",
            String.class);
    ConfigKey<String> dockerExtraHosts = new ConfigKey<>("wdm.dockerExtraHosts",
            String.class);
    ConfigKey<String> dockerEnvVariables = new ConfigKey<>(
            "wdm.dockerEnvVariables", String.class);
    ConfigKey<String> dockerDefaultArgs = new ConfigKey<>(
            "wdm.dockerDefaultArgs", String.class);
    ConfigKey<Boolean> dockerLocalFallback = new ConfigKey<>(
            "wdm.dockerLocalFallback", Boolean.class);

    ConfigKey<String> dockerPrivateEndpoint = new ConfigKey<>(
            "wdm.dockerPrivateEndpoint", String.class);

    ConfigKey<String> remoteAddress = new ConfigKey<>("wdm.remoteAddress",
            String.class);

    ConfigKey<String> browserWatcherVersion = new ConfigKey<>(
            "wdm.browserWatcherVersion", String.class);

    private <T> T resolve(ConfigKey<T> configKey) {
        String name = configKey.getName();
        T tValue = configKey.getValue();
        Class<T> type = configKey.getType();

        return resolver(name, tValue, type);
    }

    private <T> T resolver(String name, T tValue, Class<T> type) {
        String strValue;
        strValue = System.getenv(name.toUpperCase(ROOT).replace(".", "_"));
        if (strValue == null) {
            strValue = System.getProperty(name);
        }
        if (strValue == null && tValue != null) {
            return tValue;
        }
        if (strValue == null) {
            strValue = getProperty(name);
        }
        return parse(type, strValue);
    }

    @SuppressWarnings("unchecked")
    private <T> T parse(Class<T> type, String strValue) {
        T output = null;
        if (type.equals(String.class)) {
            output = (T) strValue;
        } else if (type.equals(Integer.class)) {
            output = (T) Integer.valueOf(strValue);
        } else if (type.equals(Boolean.class)) {
            output = (T) Boolean.valueOf(strValue);
        } else if (type.equals(Path.class)) {
            output = (T) Paths.get(strValue);
        } else if (type.equals(URL.class)) {
            try {
                output = (T) new URL(strValue);
            } catch (Exception e) {
                throw new WebDriverManagerException(e);
            }
        } else {
            throw new WebDriverManagerException(
                    "Type " + type.getTypeName() + " cannot be parsed");
        }
        return output;
    }

    private String getProperty(String key) {
        String value = null;
        String propertiesValue = "/" + getProperties();
        String defaultProperties = "/webdrivermanager.properties";
        try {
            value = getPropertyFrom(propertiesValue, key);
            if (value == null) {
                value = getPropertyFrom(defaultProperties, key);
            }
        } finally {
            if (value == null) {
                value = "";
            }
        }
        return value;
    }

    private String getPropertyFrom(String properties, String key) {
        Properties props = new Properties();
        try (InputStream inputStream = Config.class
                .getResourceAsStream(properties)) {
            props.load(inputStream);
        } catch (IOException e) {
            log.trace("Property {} not found in {}", key, properties);
        }
        return props.getProperty(key);
    }

    public void reset() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType() == ConfigKey.class) {
                try {
                    ((ConfigKey<?>) field.get(this)).reset();
                } catch (Exception e) {
                    log.warn("Exception resetting {}", field);
                }
            }
        }
    }

    private String defaultOsName() {
        String osName = System.getProperty("os.name").toLowerCase(ROOT);
        if (IS_OS_WINDOWS) {
            osName = WIN.name();
        } else if (IS_OS_LINUX) {
            osName = LINUX.name();
        } else if (IS_OS_MAC) {
            osName = MAC.name();
        }
        return osName;
    }

    private String defaultArchitecture() {
        if (Architecture.ARM64.matchString(System.getProperty("os.arch"))) {
            return "ARM64";
        }
        return "X" + System.getProperty("sun.arch.data.model");
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public boolean isExecutable(File file) {
        return resolve(os).equalsIgnoreCase("win")
                ? file.getName().toLowerCase(ROOT).endsWith(".exe")
                : file.canExecute();
    }

    // Getters and setters

    public String getProperties() {
        return resolve(properties);
    }

    public Config setProperties(String properties) {
        this.properties.setValue(properties);
        return this;
    }

    private File getFileFromPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public File getCacheFolder() {
        return getFileFromPath(getCachePath());
    }

    public String getCachePath() {
        return resolvePath(resolve(cachePath));
    }

    private String resolvePath(String path) {
        if (path != null) {
            // Partial support for Bash tilde expansion:
            // http://www.gnu.org/software/bash/manual/html_node/Tilde-Expansion.html
            if (path.startsWith(HOME + '/')) {
                path = Paths
                        .get(System.getProperty("user.home"), path.substring(1))
                        .toString();
            } else if (path.equals(".")) {
                path = Paths.get("").toAbsolutePath().toString();
            }
        }
        return path;
    }

    public Config setCachePath(String value) {
        this.cachePath.setValue(value);
        return this;
    }

    public File getResolutionCachePath() {
        String resCachePath = resolvePath(resolve(resolutionCachePath));
        if (isNullOrEmpty(resCachePath)) {
            resCachePath = getCachePath();
        }
        return getFileFromPath(resCachePath);
    }

    public Config setResolutionCachePath(String value) {
        this.resolutionCachePath.setValue(value);
        return this;
    }

    public boolean isForceDownload() {
        return resolve(forceDownload);
    }

    public Config setForceDownload(boolean value) {
        this.forceDownload.setValue(value);
        return this;
    }

    public boolean isUseMirror() {
        return resolve(useMirror);
    }

    public Config setUseMirror(boolean value) {
        this.useMirror.setValue(value);
        return this;
    }

    public boolean isUseBetaVersions() {
        return resolve(useBetaVersions);
    }

    public Config setUseBetaVersions(boolean value) {
        this.useBetaVersions.setValue(value);
        return this;
    }

    public boolean isAvoidExport() {
        return resolve(avoidExport);
    }

    public Config setAvoidExport(boolean value) {
        this.avoidExport.setValue(value);
        return this;
    }

    public boolean isAvoidOutputTree() {
        return resolve(avoidOutputTree);
    }

    public Config setAvoidOutputTree(boolean value) {
        this.avoidOutputTree.setValue(value);
        return this;
    }

    public boolean isAvoidBrowserDetection() {
        return resolve(avoidBrowserDetection);
    }

    public Config setAvoidBrowserDetection(boolean value) {
        this.avoidBrowserDetection.setValue(value);
        return this;
    }

    public boolean isAvoidFallback() {
        return resolve(avoidFallback);
    }

    public Config setAvoidFallback(boolean value) {
        this.avoidFallback.setValue(value);
        return this;
    }

    public boolean isAvoidResolutionCache() {
        return resolve(avoidResolutionCache);
    }

    /**
     * @deprecated Replaced by {@link #isAvoidResolutionCache()}
     */
    @Deprecated
    public boolean isAvoidingResolutionCache() {
        return isAvoidResolutionCache();
    }

    public Config setAvoidResolutionCache(boolean value) {
        this.avoidResolutionCache.setValue(value);
        return this;
    }

    public boolean isAvoidReadReleaseFromRepository() {
        return resolve(avoidReadReleaseFromRepository);
    }

    public Config setAvoidReadReleaseFromRepository(boolean value) {
        this.avoidReadReleaseFromRepository.setValue(value);
        return this;
    }

    public boolean isAvoidTmpFolder() {
        return resolve(avoidTmpFolder);
    }

    public Config setAvoidTmpFolder(boolean value) {
        this.avoidTmpFolder.setValue(value);
        return this;
    }

    public int getTimeout() {
        return resolve(timeout);
    }

    public Config setTimeout(int value) {
        this.timeout.setValue(value);
        return this;
    }

    public boolean isVersionsPropertiesOnlineFirst() {
        return resolve(versionsPropertiesOnlineFirst);
    }

    /**
     * @deprecated Replaced by {@link #isVersionsPropertiesOnlineFirst()}
     */
    @Deprecated
    public boolean getVersionsPropertiesOnlineFirst() {
        return isVersionsPropertiesOnlineFirst();
    }

    public Config setVersionsPropertiesOnlineFirst(boolean value) {
        this.versionsPropertiesOnlineFirst.setValue(value);
        return this;
    }

    public boolean isCommandsPropertiesOnlineFirst() {
        return resolve(commandsPropertiesOnlineFirst);
    }

    /**
     * @deprecated Replaced by {@link #isCommandsPropertiesOnlineFirst()}
     */
    @Deprecated
    public boolean getCommandsPropertiesOnlineFirst() {
        return isCommandsPropertiesOnlineFirst();
    }

    public Config setCommandsPropertiesOnlineFirst(boolean value) {
        this.commandsPropertiesOnlineFirst.setValue(value);
        return this;
    }

    public URL getVersionsPropertiesUrl() {
        return resolve(versionsPropertiesUrl);
    }

    public Config setVersionsPropertiesUrl(URL value) {
        this.versionsPropertiesUrl.setValue(value);
        return this;
    }

    public URL getCommandsPropertiesUrl() {
        return resolve(commandsPropertiesUrl);
    }

    public Config setCommandsPropertiesUrl(URL value) {
        this.commandsPropertiesUrl.setValue(value);
        return this;
    }

    public boolean isClearResolutionCache() {
        return resolve(clearResolutionCache);
    }

    /**
     * @deprecated Replaced by {@link #isClearResolutionCache()}
     */
    @Deprecated
    public boolean getClearingResolutionCache() {
        return isClearResolutionCache();
    }

    public Config setClearResolutionCache(Boolean value) {
        this.clearResolutionCache.setValue(value);
        return this;
    }

    public boolean isClearDriverCache() {
        return resolve(clearDriverCache);
    }

    /**
     * @deprecated Replaced by {@link #isClearDriverCache()}
     */
    @Deprecated
    public boolean getClearingDriverCache() {
        return isClearDriverCache();
    }

    public Config setClearDriverCache(Boolean value) {
        this.clearDriverCache.setValue(value);
        return this;
    }

    public Architecture getArchitecture() {
        String architectureString = resolve(architecture);
        if ("32".equals(architectureString)) {
            return Architecture.X32;
        }
        if ("64".equals(architectureString)) {
            return Architecture.X64;
        }
        return Architecture.valueOf(architectureString.toUpperCase(ROOT));
    }

    public Config setArchitecture(Architecture value) {
        this.architecture.setValue(value.name());
        return this;
    }

    public String getOs() {
        return resolve(os);
    }

    public OperatingSystem getOperatingSystem() {
        return OperatingSystem.valueOf(getOs());
    }

    public Config setOs(String value) {
        this.os.setValue(value);
        return this;
    }

    public String getProxy() {
        return resolve(proxy);
    }

    public Config setProxy(String value) {
        this.proxy.setValue(value);
        return this;
    }

    public String getProxyUser() {
        return resolve(proxyUser);
    }

    public Config setProxyUser(String value) {
        this.proxyUser.setValue(value);
        return this;
    }

    public String getProxyPass() {
        return resolve(proxyPass);
    }

    public Config setProxyPass(String value) {
        this.proxyPass.setValue(value);
        return this;
    }

    public List<String> getIgnoreVersions() {
        String ignored = resolve(ignoreVersions);
        String[] out = {};
        if (!isNullOrEmpty(ignored)) {
            out = ignored.split(",");
        }
        return Arrays.asList(out);
    }

    public Config setIgnoreVersions(String... value) {
        this.ignoreVersions.setValue(join(",", value));
        return this;
    }

    public String getGitHubToken() {
        return resolve(gitHubToken);
    }

    public Config setGitHubToken(String value) {
        this.gitHubToken.setValue(value);
        return this;
    }

    public String getDefaultBrowser() {
        return resolve(defaultBrowser);
    }

    public Config setDefaultBrowser(String value) {
        this.defaultBrowser.setValue(value);
        return this;
    }

    public int getTtl() {
        return resolve(ttl);
    }

    public Config setTtl(int value) {
        this.ttl.setValue(value);
        return this;
    }

    public int getTtlForBrowsers() {
        return resolve(ttlForBrowsers);
    }

    public Config setTtlForBrowsers(int value) {
        this.ttlForBrowsers.setValue(value);
        return this;
    }

    public String getResolutionCache() {
        return resolve(resolutionCache);
    }

    public Config setResolutionCache(String value) {
        this.resolutionCache.setValue(value);
        return this;
    }

    public String getBrowserVersionDetectionRegex() {
        return resolve(browserVersionDetectionRegex);
    }

    public Config setBrowserVersionDetectionRegex(String value) {
        this.browserVersionDetectionRegex.setValue(value);
        return this;
    }

    public String getChromeDriverVersion() {
        return resolve(chromeDriverVersion);
    }

    public Config setChromeDriverVersion(String value) {
        this.chromeDriverVersion.setValue(value);
        return this;
    }

    public String getChromeVersion() {
        return resolve(chromeVersion);
    }

    public Config setChromeVersion(String value) {
        this.chromeVersion.setValue(value);
        return this;
    }

    public String getChromeDriverExport() {
        return resolve(chromeDriverExport);
    }

    public Config setChromeDriverExport(String value) {
        this.chromeDriverExport.setValue(value);
        return this;
    }

    public URL getChromeDriverUrl() {
        return resolve(chromeDriverUrl);
    }

    public Config setChromeDriverUrl(URL value) {
        this.chromeDriverUrl.setValue(value);
        return this;
    }

    public URL getChromeDriverMirrorUrl() {
        return resolve(chromeDriverMirrorUrl);
    }

    public Config setChromeDriverMirrorUrl(URL value) {
        this.chromeDriverMirrorUrl.setValue(value);
        return this;
    }

    public String getChromeDownloadUrlPattern() {
        return resolve(chromeDownloadUrlPattern);
    }

    public Config setChromeDownloadUrlPattern(String value) {
        this.chromeDownloadUrlPattern.setValue(value);
        return this;
    }

    public String getEdgeDriverVersion() {
        return resolve(edgeDriverVersion);
    }

    public Config setEdgeDriverVersion(String value) {
        this.edgeDriverVersion.setValue(value);
        return this;
    }

    public String getEdgeVersion() {
        return resolve(edgeVersion);
    }

    public Config setEdgeVersion(String value) {
        this.edgeVersion.setValue(value);
        return this;
    }

    public String getEdgeDriverExport() {
        return resolve(edgeDriverExport);
    }

    public Config setEdgeDriverExport(String value) {
        this.edgeDriverExport.setValue(value);
        return this;
    }

    public URL getEdgeDriverUrl() {
        return resolve(edgeDriverUrl);
    }

    public Config setEdgeDriverUrl(URL value) {
        this.edgeDriverUrl.setValue(value);
        return this;
    }

    public String getEdgeDownloadUrlPattern() {
        return resolve(edgeDownloadUrlPattern);
    }

    public Config setEdgeDownloadUrlPattern(String value) {
        this.edgeDownloadUrlPattern.setValue(value);
        return this;
    }

    public String getGeckoDriverVersion() {
        return resolve(geckoDriverVersion);
    }

    public Config setGeckoDriverVersion(String value) {
        this.geckoDriverVersion.setValue(value);
        return this;
    }

    public String getFirefoxVersion() {
        return resolve(firefoxVersion);
    }

    public Config setFirefoxVersion(String value) {
        this.firefoxVersion.setValue(value);
        return this;
    }

    public String getFirefoxDriverExport() {
        return resolve(firefoxDriverExport);
    }

    public Config setFirefoxDriverExport(String value) {
        this.firefoxDriverExport.setValue(value);
        return this;
    }

    public URL getFirefoxDriverUrl() {
        return resolve(firefoxDriverUrl);
    }

    public Config setFirefoxDriverUrl(URL value) {
        this.firefoxDriverUrl.setValue(value);
        return this;
    }

    public URL getFirefoxDriverMirrorUrl() {
        return resolve(firefoxDriverMirrorUrl);
    }

    public Config setFirefoxDriverMirrorUrl(URL value) {
        this.firefoxDriverMirrorUrl.setValue(value);
        return this;
    }

    public String getIExplorerDriverVersion() {
        return resolve(iExplorerDriverVersion);
    }

    public Config setIExplorerDriverVersion(String value) {
        this.iExplorerDriverVersion.setValue(value);
        return this;
    }

    public String getIExplorerDriverExport() {
        return resolve(iExplorerDriverExport);
    }

    public Config setIExplorerDriverExport(String value) {
        this.iExplorerDriverExport.setValue(value);
        return this;
    }

    /**
     * @deprecated Replaced by {@link #setIExplorerDriverExport(String)}
     */
    @Deprecated
    public Config setInternetExplorerDriverExport(String value) {
        return setIExplorerDriverExport(value);
    }

    public URL getIExplorerDriverUrl() {
        return resolve(iExplorerDriverUrl);
    }

    public Config setIExplorerDriverUrl(URL value) {
        this.iExplorerDriverUrl.setValue(value);
        return this;
    }

    public String getOperaDriverVersion() {
        return resolve(operaDriverVersion);
    }

    public Config setOperaDriverVersion(String value) {
        this.operaDriverVersion.setValue(value);
        return this;
    }

    public String getOperaVersion() {
        return resolve(operaVersion);
    }

    public Config setOperaVersion(String value) {
        this.operaVersion.setValue(value);
        return this;
    }

    public String getOperaDriverExport() {
        return resolve(operaDriverExport);
    }

    public Config setOperaDriverExport(String value) {
        this.operaDriverExport.setValue(value);
        return this;
    }

    public URL getOperaDriverUrl() {
        return resolve(operaDriverUrl);
    }

    public Config setOperaDriverUrl(URL value) {
        this.operaDriverUrl.setValue(value);
        return this;
    }

    public URL getOperaDriverMirrorUrl() {
        return resolve(operaDriverMirrorUrl);
    }

    public Config setOperaDriverMirrorUrl(URL value) {
        this.operaDriverMirrorUrl.setValue(value);
        return this;
    }

    public String getChromiumDriverVersion() {
        return resolve(chromiumDriverVersion);
    }

    public Config setChromiumDriverVersion(String value) {
        this.chromiumDriverVersion.setValue(value);
        return this;
    }

    public String getChromiumVersion() {
        return resolve(chromiumVersion);
    }

    public Config setChromiumVersion(String value) {
        this.chromiumVersion.setValue(value);
        return this;
    }

    public String getChromiumDriverSnapPath() {
        return resolve(chromiumDriverSnapPath);
    }

    public Config setChromiumDriverSnapPath(String value) {
        this.chromiumDriverSnapPath.setValue(value);
        return this;
    }

    public boolean isUseChromiumDriverSnap() {
        return resolve(useChromiumDriverSnap);
    }

    public Config setUseChromiumDriverSnap(boolean value) {
        this.useChromiumDriverSnap.setValue(value);
        return this;
    }

    public String getBrowserVersionDetectionCommand() {
        return resolve(browserVersionDetectionCommand);
    }

    public Config setBrowserVersionDetectionCommand(String value) {
        this.browserVersionDetectionCommand.setValue(value);
        return this;
    }

    public int getServerPort() {
        return resolve(serverPort);
    }

    public Config setServerPath(String value) {
        this.serverPath.setValue(value);
        return this;
    }

    public String getServerPath() {
        return resolve(serverPath);
    }

    public Config setServerPort(int value) {
        this.serverPort.setValue(value);
        return this;
    }

    public int getServerTimeoutSec() {
        return resolve(serverTimeoutSec);
    }

    public Config setServerTimeoutSec(int value) {
        this.serverTimeoutSec.setValue(value);
        return this;
    }

    public String getDockerDaemonUrl() {
        return resolve(dockerDaemonUrl);
    }

    public Config setDockerDaemonUrl(String value) {
        this.dockerDaemonUrl.setValue(value);
        return this;
    }

    public String getDockerHubUrl() {
        return resolve(dockerHubUrl);
    }

    public Config setDockerHubUrl(String value) {
        this.dockerHubUrl.setValue(value);
        return this;
    }

    public String getDockerNetwork() {
        return resolve(dockerNetwork);
    }

    public Config setDockerNetwork(String value) {
        this.dockerNetwork.setValue(value);
        return this;
    }

    public String getDockerTimezone() {
        return resolve(dockerTimezone);
    }

    public Config setDockerTimezone(String value) {
        this.dockerTimezone.setValue(value);
        return this;
    }

    public String getDockerLang() {
        return resolve(dockerLang);
    }

    public Config setDockerLang(String value) {
        this.dockerLang.setValue(value);
        return this;
    }

    public long getDockerMemSizeBytes(String memSize) {
        String size = memSize.toLowerCase(ROOT);
        long bytes = 0;
        try {
            bytes = Integer.parseInt(size.substring(0, size.length() - 1));
        } catch (Exception e) {
            log.warn("Exception parsing size to bytes", e);
        }
        if (size.endsWith("g")) {
            bytes *= 1024 * 1024 * 1024;
        } else if (size.endsWith("m")) {
            bytes *= 1024 * 1024;
        } else if (size.endsWith("k")) {
            bytes *= 1024;
        }
        log.trace("The memory size {} is equivalent to {} bytes)", memSize,
                bytes);
        return bytes;
    }

    public String getDockerShmSize() {
        return resolve(dockerShmSize);
    }

    public Config setDockerShmSize(String value) {
        this.dockerShmSize.setValue(value);
        return this;
    }

    public String getDockerTmpfsSize() {
        return resolve(dockerTmpfsSize);
    }

    public Config setDockerTmpfsSize(String value) {
        this.dockerTmpfsSize.setValue(value);
        return this;
    }

    public String getDockerTmpfsMount() {
        return resolve(dockerTmpfsMount);
    }

    public Config setDockerTmpfsMount(String value) {
        this.dockerTmpfsMount.setValue(value);
        return this;
    }

    public int getDockerStopTimeoutSec() {
        return resolve(dockerStopTimeoutSec);
    }

    public Config setDockerStopTimeoutSec(int value) {
        this.dockerStopTimeoutSec.setValue(value);
        return this;
    }

    public boolean isDockerEnabledVnc() {
        return resolve(dockerEnableVnc);
    }

    /**
     * @deprecated Replaced by {@link #isDockerEnabledVnc()}
     */
    @Deprecated
    public boolean isEnabledDockerVnc() {
        return isDockerEnabledVnc();
    }

    public Config setDockerEnabledVnc(boolean value) {
        this.dockerEnableVnc.setValue(value);
        return this;
    }

    /**
     * @deprecated Replaced by {@link #setDockerEnabledVnc(boolean)}
     */
    @Deprecated
    public Config setDockerEnableVnc(boolean value) {
        return setDockerEnabledVnc(value);
    }

    public boolean isDockerViewOnly() {
        return resolve(dockerViewOnly);
    }

    /**
     * @deprecated Replaced by {@link #isDockerViewOnly()}
     */
    @Deprecated
    public boolean getDockerViewOnly() {
        return isDockerViewOnly();
    }

    public Config setDockerViewOnly(boolean value) {
        this.dockerViewOnly.setValue(value);
        return this;
    }

    public boolean isDockerEnabledRecording() {
        return resolve(dockerEnableRecording);
    }

    /**
     * @deprecated Replaced by {@link #isDockerEnabledRecording()}
     */
    @Deprecated
    public boolean isEnabledDockerRecording() {
        return isDockerEnabledRecording();
    }

    public Config setDockerEnabledRecording(boolean value) {
        this.dockerEnableRecording.setValue(value);
        return this;
    }

    /**
     * @deprecated Replaced by {@link #setDockerEnabledRecording(boolean)}
     */
    @Deprecated
    public Config setDockerEnableRecording(boolean value) {
        return setDockerEnabledRecording(value);
    }

    public String getDockerScreenResolution() {
        return resolve(dockerScreenResolution);
    }

    public String getDockerVideoSize() {
        String screenResolution = getDockerScreenResolution().toLowerCase(ROOT);
        int count = StringUtils.countMatches(screenResolution,
                SCREEN_RESOLUTION_SEPARATOR);
        if (count == 2) {
            screenResolution = screenResolution.substring(0,
                    screenResolution.lastIndexOf(SCREEN_RESOLUTION_SEPARATOR));
        }
        log.trace("The screen resolution {} corresponds to a video size of {}",
                getDockerScreenResolution(), screenResolution);
        return screenResolution;
    }

    public Config setDockerScreenResolution(String value) {
        this.dockerScreenResolution.setValue(value);
        return this;
    }

    public String getDockerVncPassword() {
        return resolve(dockerVncPassword);
    }

    public Config setDockerVncPassword(String value) {
        this.dockerVncPassword.setValue(value);
        return this;
    }

    public int getDockerBrowserPort() {
        return resolve(dockerBrowserPort);
    }

    public Config setDockerBrowserPort(int value) {
        this.dockerBrowserPort.setValue(value);
        return this;
    }

    public int getDockerVncPort() {
        return resolve(dockerVncPort);
    }

    public Config setDockerVncPort(int value) {
        this.dockerVncPort.setValue(value);
        return this;
    }

    public int getDockerNoVncPort() {
        return resolve(dockerNoVncPort);
    }

    public Config setDockerNoVncPort(int value) {
        this.dockerNoVncPort.setValue(value);
        return this;
    }

    public int getDockerRecordingFrameRate() {
        return resolve(dockerRecordingFrameRate);
    }

    public Config setDockerRecordingFrameRate(int value) {
        this.dockerRecordingFrameRate.setValue(value);
        return this;
    }

    public Path getDockerRecordingOutput() {
        return resolve(dockerRecordingOutput);
    }

    public Config setDockerRecordingOutput(Path value) {
        this.dockerRecordingOutput.setValue(value);
        return this;
    }

    public String getDockerRecordingPrefix() {
        return resolve(dockerRecordingPrefix);
    }

    public Config setDockerRecordingPrefix(String value) {
        this.dockerRecordingPrefix.setValue(value);
        return this;
    }

    public String getDockerBrowserSelenoidImageFormat() {
        return resolve(dockerBrowserSelenoidImageFormat);
    }

    public Config setDockerBrowserSelenoidImageFormat(String value) {
        this.dockerBrowserSelenoidImageFormat.setValue(value);
        return this;
    }

    public String getDockerBrowserTwilioImageFormat() {
        return resolve(dockerBrowserTwilioImageFormat);
    }

    public Config setDockerBrowserTwilioImageFormat(String value) {
        this.dockerBrowserTwilioImageFormat.setValue(value);
        return this;
    }

    public String getDockerBrowserAerokubeImageFormat() {
        return resolve(dockerBrowserAerokubeImageFormat);
    }

    public Config setDockerBrowserAerokubeImageFormat(String value) {
        this.dockerBrowserAerokubeImageFormat.setValue(value);
        return this;
    }

    public String getDockerBrowserMobileImageFormat() {
        return resolve(dockerBrowserMobileImageFormat);
    }

    public Config setDockerBrowserMobileImageFormat(String value) {
        this.dockerBrowserMobileImageFormat.setValue(value);
        return this;
    }

    public String getDockerRecordingImage() {
        return resolve(dockerRecordingImage);
    }

    public Config setDockerRecordingImage(String value) {
        this.dockerRecordingImage.setValue(value);
        return this;
    }

    public String getDockerNoVncImage() {
        return resolve(dockerNoVncImage);
    }

    public Config setDockerNoVncImage(String value) {
        this.dockerNoVncImage.setValue(value);
        return this;
    }

    public String getDockerCustomImage() {
        return resolve(dockerCustomImage);
    }

    public Config setDockerCustomImage(String value) {
        this.dockerCustomImage.setValue(value);
        return this;
    }

    public String getBrowserWatcherVersion() {
        return resolve(browserWatcherVersion);
    }

    public Config setBrowserWatcherVersion(String value) {
        this.browserWatcherVersion.setValue(value);
        return this;
    }

    public String getDockerVolumes() {
        String volumes = resolve(dockerVolumes);
        if (volumes.contains(PWD + ":")) {
            String currentFolder = getFullPathNoEndSeparator(
                    new File("·").getAbsolutePath());
            volumes = volumes.replace(PWD + ":", currentFolder + ":");
        }
        if (volumes.contains(HOME)) {
            String homeFolder = System.getProperty("user.home");
            volumes = volumes.replace(HOME, homeFolder);
        }
        return volumes;
    }

    public Config setDockerVolumes(String... value) {
        this.dockerVolumes.setValue(join(":", value));
        return this;
    }

    public List<String> getDockerExtraHosts() {
        String extraHosts = resolve(dockerExtraHosts);
        String[] out = {};
        if (!isNullOrEmpty(extraHosts)) {
            out = extraHosts.split(",");
        }
        return Arrays.asList(out);
    }

    public Config setDockerExtraHosts(String... value) {
        this.dockerExtraHosts.setValue(join(",", value));
        return this;
    }

    public List<String> getDockerEnvVariables() {
        String envVariables = resolve(dockerEnvVariables);
        String[] out = {};
        if (!isNullOrEmpty(envVariables)) {
            out = envVariables.split(",");
        }
        return Arrays.asList(out);
    }

    public Config setDockerEnvVariables(String... value) {
        this.dockerEnvVariables.setValue(String.join(",", value));
        return this;
    }

    public String getDockerDefaultArgs() {
        return resolve(dockerDefaultArgs);
    }

    public Config setDockerDefaultArgs(String value) {
        this.dockerDefaultArgs.setValue(value);
        return this;
    }

    public boolean isDockerLocalFallback() {
        return resolve(dockerLocalFallback);
    }

    public Config setDockerLocalFallback(boolean value) {
        this.dockerLocalFallback.setValue(value);
        return this;
    }

    public String getDockerPrivateEndpoint() {
        return resolve(dockerPrivateEndpoint);
    }

    public Config setDockerPrivateEndpoint(String value) {
        this.dockerPrivateEndpoint.setValue(value);
        return this;
    }

    public String getRemoteAddress() {
        return resolve(remoteAddress);
    }

    public Config setRemoteAddress(String value) {
        this.remoteAddress.setValue(value);
        return this;
    }

}
