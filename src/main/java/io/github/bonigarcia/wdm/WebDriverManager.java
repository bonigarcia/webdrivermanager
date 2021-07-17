/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.config.Architecture.ARM64;
import static io.github.bonigarcia.wdm.config.Architecture.X32;
import static io.github.bonigarcia.wdm.config.Architecture.X64;
import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROMIUM;
import static io.github.bonigarcia.wdm.config.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.config.DriverManagerType.IEXPLORER;
import static io.github.bonigarcia.wdm.config.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.config.DriverManagerType.SAFARI;
import static io.github.bonigarcia.wdm.config.OperatingSystem.LINUX;
import static io.github.bonigarcia.wdm.config.OperatingSystem.MAC;
import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.lang.System.getenv;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static java.util.Locale.ROOT;
import static java.util.Optional.empty;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathFactory.newInstance;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.jsoup.Jsoup;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import io.github.bonigarcia.wdm.cache.CacheHandler;
import io.github.bonigarcia.wdm.cache.ResolutionCache;
import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import io.github.bonigarcia.wdm.docker.DockerContainer;
import io.github.bonigarcia.wdm.docker.DockerService;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.ChromiumDriverManager;
import io.github.bonigarcia.wdm.managers.EdgeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import io.github.bonigarcia.wdm.managers.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.managers.OperaDriverManager;
import io.github.bonigarcia.wdm.managers.SafariDriverManager;
import io.github.bonigarcia.wdm.managers.VoidDriverManager;
import io.github.bonigarcia.wdm.online.Downloader;
import io.github.bonigarcia.wdm.online.GitHubApi;
import io.github.bonigarcia.wdm.online.HttpClient;
import io.github.bonigarcia.wdm.online.S3NamespaceContext;
import io.github.bonigarcia.wdm.online.UrlHandler;
import io.github.bonigarcia.wdm.versions.VersionComparator;
import io.github.bonigarcia.wdm.versions.VersionDetector;
import io.github.bonigarcia.wdm.webdriver.WebDriverBrowser;
import io.github.bonigarcia.wdm.webdriver.WebDriverCreator;

/**
 * Parent driver manager.
 *
 * @author Boni Garcia
 * @since 2.1.0
 */
public abstract class WebDriverManager {

    protected static final Logger log = getLogger(lookup().lookupClass());

    protected static final String SLASH = "/";
    protected static final String LATEST_RELEASE = "LATEST_RELEASE";
    protected static final NamespaceContext S3_NAMESPACE_CONTEXT = new S3NamespaceContext();
    protected static final String IN_DOCKER = "-in-docker";

    protected abstract List<URL> getDriverUrls() throws IOException;

    protected abstract String getDriverName();

    protected abstract String getDriverVersion();

    protected abstract void setDriverVersion(String driverVersion);

    protected abstract String getBrowserVersion();

    protected abstract void setBrowserVersion(String browserVersion);

    protected abstract void setDriverUrl(URL url);

    protected abstract URL getDriverUrl();

    protected abstract Optional<URL> getMirrorUrl();

    protected abstract Optional<String> getExportParameter();

    protected static Map<DriverManagerType, WebDriverManager> instanceMap = new EnumMap<>(
            DriverManagerType.class);

    public abstract DriverManagerType getDriverManagerType();

    protected Config config = new Config();
    protected HttpClient httpClient;
    protected Downloader downloader;
    protected ResolutionCache resolutionCache;
    protected CacheHandler cacheHandler;
    protected VersionDetector versionDetector;
    protected WebDriverCreator webDriverCreator;
    protected DockerService dockerService;

    protected boolean mirrorLog;
    protected boolean forcedArch;
    protected boolean forcedOs;
    protected int retryCount = 0;
    protected Capabilities capabilities;
    protected boolean shutdownHook = false;
    protected boolean dockerEnabled = false;
    protected boolean androidEnabled = false;
    protected List<WebDriverBrowser> webDriverList = new CopyOnWriteArrayList<>();

    protected String downloadedDriverVersion;
    protected String downloadedDriverPath;
    protected String noVncUrl;
    protected Path recordingPath;

    public static Config globalConfig() {
        Config global = new Config();
        global.setAvoidAutoReset(true);
        for (DriverManagerType type : DriverManagerType.values()) {
            WebDriverManager.getInstance(type).setConfig(global);
        }
        return global;
    }

    public Config config() {
        return config;
    }

    public static synchronized WebDriverManager chromedriver() {
        instanceMap.putIfAbsent(CHROME, new ChromeDriverManager());
        return instanceMap.get(CHROME);
    }

    public static synchronized WebDriverManager chromiumdriver() {
        instanceMap.putIfAbsent(CHROMIUM, new ChromiumDriverManager());
        return instanceMap.get(CHROMIUM);
    }

    public static synchronized WebDriverManager firefoxdriver() {
        instanceMap.putIfAbsent(FIREFOX, new FirefoxDriverManager());
        return instanceMap.get(FIREFOX);
    }

    public static synchronized WebDriverManager operadriver() {
        instanceMap.putIfAbsent(OPERA, new OperaDriverManager());
        return instanceMap.get(OPERA);
    }

    public static synchronized WebDriverManager edgedriver() {
        instanceMap.putIfAbsent(EDGE, new EdgeDriverManager());
        return instanceMap.get(EDGE);
    }

    public static synchronized WebDriverManager iedriver() {
        instanceMap.putIfAbsent(IEXPLORER, new InternetExplorerDriverManager());
        return instanceMap.get(IEXPLORER);
    }

    public static synchronized WebDriverManager safaridriver() {
        instanceMap.putIfAbsent(SAFARI, new SafariDriverManager());
        return instanceMap.get(SAFARI);
    }

    protected static synchronized WebDriverManager voiddriver() {
        return new VoidDriverManager();
    }

    public static synchronized WebDriverManager getInstance(
            DriverManagerType driverManagerType) {
        return getInstance(driverManagerType.browserClass());
    }

    public static synchronized WebDriverManager getInstance(
            Class<?> webDriverClass) {
        return getInstance(webDriverClass.getName());
    }

    public static synchronized WebDriverManager getInstance(
            String webDriverClass) {
        switch (webDriverClass) {
        case "org.openqa.selenium.chrome.ChromeDriver":
            return chromedriver();
        case "org.openqa.selenium.chromium.ChromiumDriver":
            return chromiumdriver();
        case "org.openqa.selenium.firefox.FirefoxDriver":
            return firefoxdriver();
        case "org.openqa.selenium.opera.OperaDriver":
            return operadriver();
        case "org.openqa.selenium.ie.InternetExplorerDriver":
            return iedriver();
        case "org.openqa.selenium.edge.EdgeDriver":
            return edgedriver();
        default:
            return voiddriver();
        }
    }

    public static synchronized WebDriverManager getInstance() {
        WebDriverManager manager = voiddriver();
        String defaultBrowser = manager.config().getDefaultBrowser();
        try {
            if (defaultBrowser.contains(IN_DOCKER)) {
                defaultBrowser = defaultBrowser.substring(0,
                        defaultBrowser.indexOf(IN_DOCKER));
                manager = getInstance(DriverManagerType
                        .valueOf(defaultBrowser.toUpperCase(ROOT)));
                manager.dockerEnabled = true;

            } else {
                manager = getInstance(DriverManagerType
                        .valueOf(defaultBrowser.toUpperCase(ROOT)));
            }
            DriverManagerType managerType = DriverManagerType
                    .valueOf(defaultBrowser.toUpperCase(ROOT));
            return getInstance(managerType);

        } catch (Exception e) {
            log.error("Error trying to get manager for browser {}",
                    defaultBrowser, e);
        }
        return manager;
    }

    public synchronized void setup() {
        DriverManagerType driverManagerType = getDriverManagerType();
        initResolutionCache();
        cacheHandler = new CacheHandler(config);
        httpClient = new HttpClient(config());

        if (config().getClearingDriverCache()) {
            clearDriverCache();
        }
        if (config().getClearingResolutionCache()) {
            clearResolutionCache();
        }
        if (dockerEnabled || config().getRemoteAddress() != null) {
            return;
        }
        if (driverManagerType != null) {
            try {
                manage(getDriverVersion());
            } finally {
                reset();
            }
        }
    }

    public WebDriver create() {
        WebDriver driver = null;
        try {
            setup();
            driver = instantiateDriver();
        } finally {
            reset();
        }
        return driver;
    }

    public List<WebDriver> create(int numberOfBrowser) {
        List<WebDriver> browserList = new ArrayList<>();
        try {
            for (int i = 0; i < numberOfBrowser; i++) {
                if (i == 0) {
                    setup();
                }
                browserList.add(instantiateDriver());
            }
        } finally {
            reset();
        }
        return browserList;
    }

    public synchronized void quit() {
        for (WebDriverBrowser driverBrowser : webDriverList) {
            try {
                WebDriver driver = driverBrowser.getDriver();
                if (driver != null) {
                    driver.quit();
                }

                if (dockerService != null) {
                    List<DockerContainer> dockerContainerList = driverBrowser
                            .getDockerContainerList();
                    dockerContainerList.stream()
                            .forEach(dockerService::stopAndRemoveContainer);
                }
            } catch (Exception e) {
                log.warn("Exception closing {} ({})", driverBrowser.getDriver(),
                        e.getMessage());
            }
        }
        webDriverList.clear();
        noVncUrl = "";
        recordingPath = null;
    }

    public Optional<Path> getBrowserPath() {
        if (versionDetector == null) {
            httpClient = new HttpClient(config());
            versionDetector = new VersionDetector(config, httpClient);
        }
        return versionDetector.getBrowserPath(
                getDriverManagerType().getBrowserNameLowerCase());
    }

    public WebDriverManager browserInDocker() {
        this.dockerEnabled = true;
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager browserInDockerAndroid() {
        throw new WebDriverManagerException(
                getDriverManagerType().getBrowserName()
                        + " is not available in Docker Android");
    }

    public WebDriverManager enableVnc() {
        config().setDockerEnableVnc(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager enableRecording() {
        config().setDockerEnableRecording(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager recordingOutput(String path) {
        return recordingOutput(Paths.get(path));
    }

    public WebDriverManager recordingOutput(Path path) {
        config().setDockerRecordingOutput(path);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager withCapabilities(Capabilities options) {
        this.capabilities = options;
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager withRemoteAddress(String remoteAddress) {
        try {
            return withRemoteAddress(new URL(remoteAddress));
        } catch (Exception e) {
            log.error(
                    "Exception trying to create manager using remote address {}",
                    remoteAddress, e);
        }
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager withRemoteAddress(URL remoteAddress) {
        config().setRemoteAddress(remoteAddress);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager driverVersion(String driverVersion) {
        setDriverVersion(driverVersion);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager browserVersion(String browserVersion) {
        setBrowserVersion(browserVersion);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager architecture(Architecture architecture) {
        config().setArchitecture(architecture);
        forcedArch = true;
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager arch32() {
        architecture(X32);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager arch64() {
        architecture(X64);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager arm64() {
        architecture(ARM64);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager win() {
        operatingSystem(WIN);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager linux() {
        operatingSystem(LINUX);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager mac() {
        operatingSystem(MAC);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager operatingSystem(OperatingSystem os) {
        config().setOs(os.name());
        forcedOs = true;
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager forceDownload() {
        config().setForceDownload(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager driverRepositoryUrl(URL url) {
        setDriverUrl(url);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager useMirror() {
        Optional<URL> mirrorUrl = getMirrorUrl();
        if (!mirrorUrl.isPresent()) {
            throw new WebDriverManagerException("Mirror URL not available");
        }
        config().setUseMirror(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager proxy(String proxy) {
        config().setProxy(proxy);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager proxyUser(String proxyUser) {
        config().setProxyUser(proxyUser);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager proxyPass(String proxyPass) {
        config().setProxyPass(proxyPass);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager useBetaVersions() {
        config().setUseBetaVersions(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager ignoreDriverVersions(String... driverVersions) {
        config().setIgnoreVersions(driverVersions);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager gitHubToken(String gitHubToken) {
        config().setGitHubToken(gitHubToken);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager timeout(int timeout) {
        config().setTimeout(timeout);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager properties(String properties) {
        config().setProperties(properties);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager cachePath(String cachePath) {
        config().setCachePath(cachePath);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager resolutionCachePath(String resolutionCachePath) {
        config().setResolutionCachePath(resolutionCachePath);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidExport() {
        config().setAvoidExport(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidOutputTree() {
        config().setAvoidOutputTree(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidBrowserDetection() {
        config().setAvoidBrowserDetection(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidResolutionCache() {
        config().setAvoidResolutionCache(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidFallback() {
        config().setAvoidFallback(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidReadReleaseFromRepository() {
        config().setAvoidReadReleaseFromRepository(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidTmpFolder() {
        config().setAvoidTmpFolder(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidUseChromiumDriverSnap() {
        config().setUseChromiumDriverSnap(false);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager ttl(int seconds) {
        config().setTtl(seconds);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager ttlBrowsers(int seconds) {
        config().setTtlForBrowsers(seconds);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager browserVersionDetectionCommand(
            String browserVersionCommand) {
        config().setBrowserVersionDetectionCommand(browserVersionCommand);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager useLocalVersionsPropertiesFirst() {
        config().setVersionsPropertiesOnlineFirst(false);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager useLocalCommandsPropertiesFirst() {
        config().setCommandsPropertiesOnlineFirst(false);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager versionsPropertiesUrl(URL url) {
        config().setVersionsPropertiesUrl(url);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager commandsPropertiesUrl(URL url) {
        config().setCommandsPropertiesUrl(url);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager clearResolutionCache() {
        initResolutionCache();
        instanceMap.get(getDriverManagerType()).resolutionCache.clear();
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager clearDriverCache() {
        String cachePath = config().getCachePath();
        try {
            log.debug("Clearing driver cache at {}", cachePath);
            cleanDirectory(new File(cachePath));
        } catch (Exception e) {
            log.warn("Exception deleting driver cache at {}", cachePath, e);
        }
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager browserVersionDetectionRegex(String regex) {
        config().setBrowserVersionDetectionRegex(regex);
        return instanceMap.get(getDriverManagerType());
    }

    // ------------

    public String getDownloadedDriverPath() {
        return instanceMap.get(getDriverManagerType()).downloadedDriverPath;
    }

    public String getDownloadedDriverVersion() {
        return instanceMap.get(getDriverManagerType()).downloadedDriverVersion;
    }

    public List<String> getDriverVersions() {
        httpClient = new HttpClient(config());
        try {
            List<URL> driverUrls = getDriverUrls();
            List<String> driverVersionList = new ArrayList<>();
            for (URL url : driverUrls) {
                String driverVersion = getCurrentVersion(url);
                if (driverVersion.isEmpty()
                        || driverVersion.equalsIgnoreCase("icons")) {
                    continue;
                }
                if (!driverVersionList.contains(driverVersion)) {
                    driverVersionList.add(driverVersion);
                }
            }
            log.trace("Driver version list before sorting {}",
                    driverVersionList);
            sort(driverVersionList, new VersionComparator());
            return driverVersionList;
        } catch (IOException e) {
            throw new WebDriverManagerException(e);
        }
    }

    public URL getDockerNoVncUrl() {
        try {
            return new URL(noVncUrl);
        } catch (MalformedURLException e) {
            log.error("URL for Docker session not available", e);
            return null;
        }
    }

    public Path getDockerRecordingPath() {
        return recordingPath;
    }

    // ------------

    protected void manage(String driverVersion) {
        try (HttpClient wdmHttpClient = httpClient) {
            versionDetector = new VersionDetector(config, httpClient);
            downloader = new Downloader(httpClient, config(),
                    this::postDownload);

            if (isUnknown(driverVersion)) {
                driverVersion = resolveDriverVersion(driverVersion);
            }
            if (versionDetector.isSnap() && config.isUseChromiumDriverSnap()) {
                String chromiumDriverSnapPath = config()
                        .getChromiumDriverSnapPath();
                File snapChromiumDriverPath = new File(chromiumDriverSnapPath);
                boolean existsSnap = snapChromiumDriverPath.exists();
                if (existsSnap) {
                    log.debug("Found {} snap", getDriverManagerType());
                    exportDriver(chromiumDriverSnapPath);
                }
                return;
            }

            Optional<String> driverInCache = empty();
            if (!isUnknown(driverVersion)) {
                driverInCache = cacheHandler.getDriverFromCache(driverVersion,
                        getDriverName(), getDriverManagerType(),
                        config().getArchitecture(), config().getOs());
            }

            String exportValue;
            if (driverInCache.isPresent() && !config().isForceDownload()) {
                log.debug("Driver {} {} found in cache", getDriverName(),
                        getDriverVersionLabel(driverVersion));
                exportValue = driverInCache.get();
                downloadedDriverVersion = driverVersion;
            } else {
                exportValue = download(driverVersion);
            }

            exportDriver(exportValue);

        } catch (Exception e) {
            handleException(e, driverVersion);
        }
    }

    protected String resolveDriverVersion(String driverVersion) {
        String preferenceKey = getKeyForResolutionCache();
        Optional<String> optionalBrowserVersion = Optional
                .ofNullable(getBrowserVersion())
                .filter(StringUtils::isNotEmpty);

        if (!optionalBrowserVersion.isPresent()) {
            optionalBrowserVersion = getValueFromResolutionCache(preferenceKey);
        }

        if (!optionalBrowserVersion.isPresent()) {
            optionalBrowserVersion = detectBrowserVersion();
        }
        if (optionalBrowserVersion.isPresent()) {
            preferenceKey = getKeyForResolutionCache()
                    + optionalBrowserVersion.get();
            Optional<String> optionalDriverVersion = getValueFromResolutionCache(
                    preferenceKey);

            if (!optionalDriverVersion.isPresent()) {
                optionalDriverVersion = getDriverVersionFromRepository(
                        optionalBrowserVersion);
            }
            if (!optionalDriverVersion.isPresent()) {
                optionalDriverVersion = versionDetector
                        .getDriverVersionFromProperties(preferenceKey);
            }
            if (optionalDriverVersion.isPresent()) {
                driverVersion = optionalDriverVersion.get();
                log.info("Using {} {} (resolved driver for {} {})",
                        getDriverName(), driverVersion,
                        getDriverManagerType().getBrowserName(),
                        optionalBrowserVersion.get());

                if (config.getIgnoreVersions().contains(driverVersion)) {
                    String formerBrowserVersion = valueOf(
                            parseInt(optionalBrowserVersion.get()) - 1);
                    log.info(
                            "The driver {} {} is configured to be ignored ... trying again resolving driver for former version of {} (i.e. {})",
                            getDriverName(), driverVersion,
                            getDriverManagerType(), formerBrowserVersion);
                    setBrowserVersion(formerBrowserVersion);
                    return resolveDriverVersion("");
                }

                storeInResolutionCache(preferenceKey, driverVersion,
                        optionalBrowserVersion.get());
            }
        }

        if (isUnknown(driverVersion)) {
            String browserVersionStr = optionalBrowserVersion.isPresent()
                    ? " " + optionalBrowserVersion.get()
                    : "";
            log.debug(
                    "The driver version for {}{} is unknown ... trying with latest",
                    getDriverManagerType(), browserVersionStr);

            Optional<String> latestDriverVersionFromRepository = getLatestDriverVersionFromRepository();
            if (latestDriverVersionFromRepository.isPresent()) {
                driverVersion = latestDriverVersionFromRepository.get();
            }
        }
        return driverVersion;
    }

    protected void initResolutionCache() {
        resolutionCache = new ResolutionCache(config);
    }

    protected String download(String driverVersion) throws IOException {
        if (driverVersion.startsWith(".")) {
            driverVersion = driverVersion.substring(1);
        }
        UrlHandler urlHandler = createUrlHandler(driverVersion);
        URL url = urlHandler.getCandidateUrl();
        downloadedDriverVersion = urlHandler.getDriverVersion();
        return downloader.download(url, downloadedDriverVersion,
                getDriverName(), getDriverManagerType());
    }

    protected void exportDriver(String variableValue) {
        downloadedDriverPath = variableValue;
        Optional<String> exportParameter = getExportParameter();
        if (!config.isAvoidExport() && exportParameter.isPresent()) {
            String variableName = exportParameter.get();
            log.info("Exporting {} as {}", variableName, variableValue);
            System.setProperty(variableName, variableValue);
        } else {
            log.info("Driver location: {}", variableValue);
        }
    }

    protected void storeInResolutionCache(String preferenceKey,
            String resolvedDriverVersion, String resolvedBrowserVersion) {
        if (useResolutionCache()) {
            resolutionCache.putValueInResolutionCacheIfEmpty(
                    getKeyForResolutionCache(), resolvedBrowserVersion,
                    config().getTtlForBrowsers());
            resolutionCache.putValueInResolutionCacheIfEmpty(preferenceKey,
                    resolvedDriverVersion, config().getTtl());
        }
    }

    protected Optional<String> getValueFromResolutionCache(
            String preferenceKey) {
        Optional<String> optionalBrowserVersion = empty();
        if (useResolutionCacheWithKey(preferenceKey)) {
            optionalBrowserVersion = Optional.of(
                    resolutionCache.getValueFromResolutionCache(preferenceKey));
        }
        return optionalBrowserVersion;
    }

    protected List<File> postDownload(File archive) {
        File parentFolder = archive.getParentFile();
        File[] ls = parentFolder.listFiles();
        for (File f : ls) {
            if (getDriverName().contains(removeExtension(f.getName()))) {
                log.trace("Found driver in post-download: {}", f);
                return singletonList(f);
            }
        }
        throw new WebDriverManagerException("Driver " + getDriverName()
                + " not found (using temporal folder " + parentFolder + ")");
    }

    protected Optional<String> getBrowserVersionFromTheShell() {
        return versionDetector.getBrowserVersionFromTheShell(
                getDriverManagerType().getBrowserNameLowerCase());
    }

    protected Optional<String> detectBrowserVersion() {
        if (config().isAvoidBrowserDetection()) {
            return empty();
        }

        String driverManagerTypeLowerCase = getDriverManagerType()
                .getNameLowerCase();
        Optional<String> optionalBrowserVersion;

        if (useResolutionCacheWithKey(driverManagerTypeLowerCase)) {
            optionalBrowserVersion = Optional.of(resolutionCache
                    .getValueFromResolutionCache(driverManagerTypeLowerCase));

            log.trace("Detected {} version {}", getDriverManagerType(),
                    optionalBrowserVersion);
        } else {
            optionalBrowserVersion = getBrowserVersionFromTheShell();
        }
        return optionalBrowserVersion;
    }

    protected boolean useResolutionCacheWithKey(String key) {
        return useResolutionCache()
                && resolutionCache.checkKeyInResolutionCache(key);
    }

    protected boolean useResolutionCache() {
        return !config().isAvoidingResolutionCache();
    }

    protected boolean isUnknown(String driverVersion) {
        return isNullOrEmpty(driverVersion)
                || driverVersion.equalsIgnoreCase("latest");
    }

    protected String getCurrentVersion(URL url) {
        String currentVersion = "";
        String pattern = "/([^/]*?)/[^/]*?" + getShortDriverName();
        Matcher matcher = compile(pattern, CASE_INSENSITIVE)
                .matcher(url.getFile());
        boolean find = matcher.find();
        if (find) {
            currentVersion = matcher.group(1);
        } else {
            log.trace("Version not found in URL {}", url);
        }
        return currentVersion;
    }

    protected void handleException(Exception e, String driverVersion) {
        String driverVersionStr = getDriverVersionLabel(driverVersion);
        String errorMessage = String.format(
                "There was an error managing %s %s (%s)", getDriverName(),
                driverVersionStr, e.getMessage());
        if (retryCount == 0 && !config().isAvoidFallback()) {
            retryCount++;
            if (getDriverManagerType() == EDGE
                    || getDriverManagerType() == CHROME) {
                config().setAvoidReadReleaseFromRepository(true);
                clearResolutionCache();
                log.warn(
                        "{} ... trying again avoiding reading release from repository",
                        errorMessage);
                manage("");
            } else {
                retryCount++;
                fallback(e, errorMessage);
            }

        } else if (retryCount == 1 && !config().isAvoidFallback()) {
            fallback(e, errorMessage);
        } else {
            log.error("{}", errorMessage, e);
            throw new WebDriverManagerException(e);
        }
    }

    protected void fallback(Exception e, String errorMessage) {
        String driverVersion;
        config().setAvoidBrowserDetection(true);
        driverVersion = "";
        setBrowserVersion("");
        retryCount++;
        log.warn("{} ... trying again using latest driver stored in cache",
                errorMessage);
        if (log.isTraceEnabled()) {
            log.trace("Error trace: ", e);
        }

        manage(driverVersion);
    }

    protected UrlHandler createUrlHandler(String driverVersion)
            throws IOException {
        List<URL> candidateUrls = getDriverUrls();
        String shortDriverName = getShortDriverName();
        UrlHandler urlHandler = new UrlHandler(config(), candidateUrls,
                driverVersion, shortDriverName, this::buildUrl);
        log.trace("All driver URLs: {}", candidateUrls);

        boolean getLatest = isUnknown(driverVersion);
        boolean continueSearchingVersion;

        do {
            // Filter by driver name
            urlHandler.filterByDriverName(shortDriverName);

            // Filter for latest or concrete driver version
            if (getLatest) {
                urlHandler.filterByLatestVersion(this::getCurrentVersion);
            } else {
                urlHandler.filterByVersion(driverVersion);
            }

            if (urlHandler.getDriverVersion() == null) {
                break;
            }

            log.debug("Driver to be downloaded {} {}", shortDriverName,
                    urlHandler.getDriverVersion());
            log.trace("Driver URLs after filtering for version: {}",
                    urlHandler.getCandidateUrls());
            String os = config().getOs();
            Architecture architecture = config().getArchitecture();

            // Filter by OS
            if (architecture != ARM64 || getDriverManagerType() != EDGE) {
                urlHandler.filterByOs(getDriverName(), os);
            }

            // Rest of filters
            urlHandler.filterByArch(architecture, forcedArch);
            urlHandler.filterByIgnoredVersions(config().getIgnoreVersions());
            urlHandler.filterByBeta(config().isUseBetaVersions());

            continueSearchingVersion = urlHandler.hasNoCandidateUrl()
                    && getLatest;

            if (continueSearchingVersion) {
                log.info(
                        "No proper driver found for {} {} ... seeking another version",
                        getDriverName(), getDriverVersionLabel(driverVersion));
                urlHandler.resetList(candidateUrls);
                candidateUrls = urlHandler.getCandidateUrls();
            }
        } while (continueSearchingVersion);
        return urlHandler;
    }

    /**
     * This method works also for http://npm.taobao.org/ and
     * https://bitbucket.org/ mirrors.
     */
    protected List<URL> getDriversFromMirror(URL driverUrl) throws IOException {
        if (!mirrorLog) {
            log.debug("Crawling driver list from mirror {}", driverUrl);
            mirrorLog = true;
        } else {
            log.trace("[Recursive call] Crawling driver list from mirror {}",
                    driverUrl);
        }

        String driverStr = driverUrl.toString();
        String driverOrigin = String.format("%s://%s", driverUrl.getProtocol(),
                driverUrl.getAuthority());

        try (CloseableHttpResponse response = httpClient
                .execute(httpClient.createHttpGet(driverUrl))) {
            InputStream in = response.getEntity().getContent();
            org.jsoup.nodes.Document doc = Jsoup.parse(in, null, driverStr);
            Iterator<org.jsoup.nodes.Element> iterator = doc.select("a")
                    .iterator();
            List<URL> urlList = new ArrayList<>();

            while (iterator.hasNext()) {
                String link = iterator.next().attr("abs:href");
                if (link.startsWith(driverStr) && link.endsWith(SLASH)) {
                    urlList.addAll(getDriversFromMirror(new URL(link)));
                } else if (link.startsWith(driverOrigin)
                        && !link.contains("icons")
                        && (link.toLowerCase(ROOT).endsWith(".bz2")
                                || link.toLowerCase(ROOT).endsWith(".zip")
                                || link.toLowerCase(ROOT).endsWith(".gz"))) {
                    urlList.add(new URL(link));
                }
            }
            return urlList;
        }
    }

    protected NamespaceContext getNamespaceContext() {
        return null;
    }

    protected Optional<NamespaceContext> getS3NamespaceContext() {
        return Optional.of(S3_NAMESPACE_CONTEXT);
    }

    protected List<URL> getDriversFromXml(URL driverUrl, String xpath,
            Optional<NamespaceContext> namespaceContext) throws IOException {
        logSeekRepo(driverUrl);
        List<URL> urls = new ArrayList<>();
        try {
            try (CloseableHttpResponse response = httpClient
                    .execute(httpClient.createHttpGet(driverUrl))) {
                Document xml = loadXML(response.getEntity().getContent());
                XPath xPath = newInstance().newXPath();
                if (namespaceContext.isPresent()) {
                    xPath.setNamespaceContext(namespaceContext.get());
                }
                NodeList nodes = (NodeList) xPath.evaluate(xpath,
                        xml.getDocumentElement(), NODESET);
                for (int i = 0; i < nodes.getLength(); ++i) {
                    Element e = (Element) nodes.item(i);
                    urls.add(new URL(driverUrl.toURI().resolve(".")
                            + e.getChildNodes().item(0).getNodeValue()));
                }
            }
        } catch (Exception e) {
            throw new WebDriverManagerException(e);
        }
        return urls;
    }

    protected void logSeekRepo(URL driverUrl) {
        log.info("Reading {} to seek {}", driverUrl, getDriverName());
    }

    protected Document loadXML(InputStream inputStream)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(
                new ByteArrayInputStream(IOUtils.toByteArray(inputStream))));
    }

    protected InputStream openGitHubConnection(URL driverUrl)
            throws IOException {
        HttpGet get = httpClient.createHttpGet(driverUrl);

        String gitHubToken = config().getGitHubToken();
        if (isNullOrEmpty(gitHubToken)) {
            gitHubToken = getenv("GITHUB_TOKEN");
        }
        if (!isNullOrEmpty(gitHubToken)) {
            get.addHeader("Authorization", "token " + gitHubToken);
        }

        return httpClient.execute(get).getEntity().getContent();
    }

    protected List<URL> getDriversFromGitHub() throws IOException {
        List<URL> urls;
        URL driverUrl = getDriverUrl();
        logSeekRepo(driverUrl);

        Optional<URL> mirrorUrl = getMirrorUrl();
        if (mirrorUrl.isPresent() && config.isUseMirror()) {
            urls = getDriversFromMirror(mirrorUrl.get());

        } else {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(openGitHubConnection(driverUrl)))) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                GitHubApi[] releaseArray = gson.fromJson(reader,
                        GitHubApi[].class);

                urls = new ArrayList<>();
                for (GitHubApi release : releaseArray) {
                    if (release != null) {
                        List<LinkedTreeMap<String, Object>> assets = release
                                .getAssets();
                        for (LinkedTreeMap<String, Object> asset : assets) {
                            urls.add(new URL(asset.get("browser_download_url")
                                    .toString()));
                        }
                    }
                }
            }
        }
        return urls;
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    protected FilenameFilter getFolderFilter() {
        return (dir, name) -> dir.isDirectory()
                && name.toLowerCase(ROOT).contains(getDriverName());
    }

    protected Charset getVersionCharset() {
        return defaultCharset();
    }

    protected String getLatestVersionLabel() {
        return LATEST_RELEASE;
    }

    protected Optional<String> getOsLabel() {
        return empty();
    }

    protected Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion) {
        return config().isAvoidReadReleaseFromRepository() ? empty()
                : versionDetector.getDriverVersionFromRepository(driverVersion,
                        getDriverUrl(), getVersionCharset(), getDriverName(),
                        getLatestVersionLabel(), LATEST_RELEASE, getOsLabel());
    }

    protected void reset() {
        if (!config().isAvoidAutoReset()) {
            config().reset();
            mirrorLog = false;
            forcedArch = false;
            forcedOs = false;
            retryCount = 0;
            shutdownHook = false;
            dockerEnabled = false;
            androidEnabled = false;
            capabilities = null;
        }
    }

    protected URL getDriverUrlCkeckingMirror(URL url) {
        if (config().isUseMirror()) {
            Optional<URL> mirrorUrl = getMirrorUrl();
            if (mirrorUrl.isPresent()) {
                return mirrorUrl.get();
            }
        }
        return url;
    }

    protected static void resolveLocal(String validBrowsers, String arg) {
        log.info("Using WebDriverManager to resolve {}", arg);
        try {
            DriverManagerType driverManagerType = DriverManagerType
                    .valueOf(arg.toUpperCase(ROOT));
            WebDriverManager wdm = WebDriverManager
                    .getInstance(driverManagerType).avoidExport().cachePath(".")
                    .forceDownload().avoidResolutionCache();
            if (arg.equalsIgnoreCase("iexplorer")) {
                wdm.operatingSystem(WIN);
            }
            wdm.avoidOutputTree().setup();
        } catch (Exception e) {
            log.error("Driver for {} not found (valid browsers {})", arg,
                    validBrowsers);
        }
    }

    protected static void startServer(String[] args) {
        int port = new Config().getServerPort();
        if (args.length > 1 && isNumeric(args[1])) {
            port = parseInt(args[1]);
        }
        new WdmServer(port);
    }

    protected static void logCliError(String validBrowsers) {
        log.error("There are 3 options to run WebDriverManager CLI");
        log.error("1. WebDriverManager used to resolve drivers locally:");
        log.error("\tWebDriverManager browserName");
        log.error("\t(where browserName={})", validBrowsers);

        log.error("2. WebDriverManager as a server:");
        log.error("\tWebDriverManager server <port>");
        log.error("\t(where default port is 4041)");

        log.error(
                "3. To clear resolution cache (i.e. previously resolved driver versions):");
        log.error("\tWebDriverManager clear-resolution-cache");
    }

    protected void setConfig(Config config) {
        this.config = config;
    }

    protected Optional<String> getLatestDriverVersionFromRepository() {
        return empty();
    }

    protected String getShortDriverName() {
        return getDriverName();
    }

    protected String getKeyForResolutionCache() {
        return getDriverManagerType().getNameLowerCase();
    }

    protected String getDriverVersionLabel(String driverVersion) {
        return isUnknown(driverVersion) ? "(latest version)" : driverVersion;
    }

    protected Optional<URL> buildUrl(String driverVersion) {
        return empty();
    }

    protected WebDriver instantiateDriver() {
        WebDriver driver = null;
        if (webDriverCreator == null) {
            webDriverCreator = new WebDriverCreator(config);
        }
        try {
            String remoteAddress = config().getRemoteAddress().toString();
            if (remoteAddress != null) {
                Capabilities caps = Optional.ofNullable(capabilities)
                        .orElse(getCapabilities());
                driver = webDriverCreator.createRemoteWebDriver(remoteAddress,
                        caps);

            } else if (dockerEnabled) {
                driver = createDockerWebDriver();
            } else {
                driver = createLocalWebDriver();
            }

        } catch (Exception e) {
            log.error("There was an error creating WebDriver object for {}",
                    getDriverManagerType().getBrowserName(), e);
        }
        addShutdownHook();

        return driver;
    }

    protected void addShutdownHook() {
        if (!shutdownHook) {
            Runtime.getRuntime()
                    .addShutdownHook(new Thread("wdm-shutdown-hook") {
                        @Override
                        public void run() {
                            try {
                                quit();
                            } catch (Exception e) {
                                log.warn("Exception in wdm-shutdown-hook ({})",
                                        e.getMessage());
                            }
                        }
                    });
            shutdownHook = true;
        }
    }

    protected WebDriver createDockerWebDriver() {
        if (dockerService == null) {
            dockerService = new DockerService(config, httpClient,
                    resolutionCache);
        }

        String browserName = getKeyForResolutionCache();
        if (androidEnabled) {
            browserName += "-mobile";
        }
        String browserVersion = getBrowserVersion();
        String browserCacheKey = browserName + "-container-";

        if (isUnknown(browserVersion)
                || dockerService.isBrowserVersionLatesMinus(browserVersion)) {
            browserCacheKey += isNullOrEmpty(browserVersion) ? "latest"
                    : browserVersion;
            browserVersion = dockerService.getImageVersionFromDockerHub(
                    getDriverManagerType(), browserCacheKey, browserName,
                    browserVersion, androidEnabled);
        } else {
            if (!dockerService.isBrowserVersionWildCard(browserVersion)
                    && !browserVersion.contains(".")) {
                browserVersion += ".0";
            }
            browserCacheKey += browserVersion;
        }

        String browserImage = dockerService.getDockerImage(browserName,
                browserVersion, androidEnabled);
        DockerContainer browserContainer = dockerService.startBrowserContainer(
                browserImage, browserCacheKey, browserVersion, androidEnabled);
        browserContainer.setBrowserName(browserName);
        String containerUrl = browserContainer.getContainerUrl();

        WebDriverBrowser driverBrowser = new WebDriverBrowser();
        driverBrowser.addDockerContainer(browserContainer);
        webDriverList.add(driverBrowser);

        WebDriver driver = webDriverCreator.createRemoteWebDriver(containerUrl,
                getCapabilities());
        driverBrowser.setDriver(driver);
        String sessionId = webDriverCreator
                .getSessionId(driverBrowser.getDriver());
        browserContainer.setSessionId(sessionId);

        if (config.isEnabledDockerVnc()) {
            String noVncImage = config.getDockerNoVncImage();
            String noVncVersion = noVncImage
                    .substring(noVncImage.indexOf(":") + 1);
            DockerContainer noVncContainer = dockerService.startNoVncContainer(
                    noVncImage, "novnc-container", noVncVersion,
                    browserContainer);
            driverBrowser.addDockerContainer(noVncContainer);
            noVncUrl = noVncContainer.getContainerUrl();

            log.info("Docker session noVNC URL: {}", noVncUrl);
        }

        if (config.isEnabledDockerRecording()) {
            String recorderImage = config.getDockerRecordingImage();
            String recorderVersion = recorderImage
                    .substring(recorderImage.indexOf(":") + 1);
            DockerContainer recorderContainer = dockerService
                    .startRecorderContainer(recorderImage, "recorder-container",
                            recorderVersion, browserContainer);
            driverBrowser.addDockerContainer(recorderContainer, 0);
            recordingPath = recorderContainer.getRecordingPath();

            log.info("Starting recording {}", recordingPath);
        }

        return driverBrowser.getDriver();
    }

    protected WebDriver createLocalWebDriver() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Class<?> browserClass = Class
                .forName(getDriverManagerType().browserClass());
        WebDriver driver = webDriverCreator.createLocalWebDriver(browserClass,
                capabilities);
        webDriverList.add(new WebDriverBrowser(driver));

        return driver;
    }

    protected Capabilities getCapabilities() {
        return new MutableCapabilities();
    }

    public static void main(String[] args) {
        String validBrowsers = "chrome|chromium|firefox|opera|edge|iexplorer";
        if (args.length <= 0) {
            logCliError(validBrowsers);
        } else {
            String arg = args[0];
            if (arg.equalsIgnoreCase("server")) {
                startServer(args);
            } else if (arg.equalsIgnoreCase("clear-resolution-cache")) {
                new ResolutionCache(new Config()).clear();
            } else {
                resolveLocal(validBrowsers, arg);
            }
        }
    }

}
