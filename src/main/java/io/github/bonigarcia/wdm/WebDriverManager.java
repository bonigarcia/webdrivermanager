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
import static io.github.bonigarcia.wdm.config.DriverManagerType.IEXPLORER;
import static io.github.bonigarcia.wdm.config.DriverManagerType.OPERA;
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
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
    protected static final String CLI_SERVER = "server";
    protected static final String CLI_RESOLVER = "resolveDriverFor=";
    protected static final String CLI_DOCKER = "runInDocker=";

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

    public abstract DriverManagerType getDriverManagerType();

    protected Config config;
    protected HttpClient httpClient;
    protected Downloader downloader;
    protected ResolutionCache resolutionCache;
    protected CacheHandler cacheHandler;
    protected VersionDetector versionDetector;
    protected WebDriverCreator webDriverCreator;
    protected DockerService dockerService;

    protected boolean mirrorLog;
    protected boolean forcedArch;
    protected int retryCount = 0;
    protected Capabilities capabilities;
    protected boolean shutdownHook = false;
    protected boolean dockerEnabled = false;
    protected boolean androidEnabled = false;
    protected List<WebDriverBrowser> webDriverList;

    protected String downloadedDriverVersion;
    protected String downloadedDriverPath;

    protected WebDriverManager() {
        config = new Config();
        cacheHandler = new CacheHandler(config);
        httpClient = new HttpClient(config);
        downloader = new Downloader(httpClient, config, this::postDownload);
        resolutionCache = new ResolutionCache(config);
        dockerService = new DockerService(config, httpClient, resolutionCache);
        versionDetector = new VersionDetector(config, httpClient);
        webDriverList = new CopyOnWriteArrayList<>();
        webDriverCreator = new WebDriverCreator(config);
    }

    public synchronized Config config() {
        return config;
    }

    public static synchronized WebDriverManager chromedriver() {
        return new ChromeDriverManager();
    }

    public static synchronized WebDriverManager chromiumdriver() {
        return new ChromiumDriverManager();
    }

    public static synchronized WebDriverManager firefoxdriver() {
        return new FirefoxDriverManager();
    }

    public static synchronized WebDriverManager operadriver() {
        return new OperaDriverManager();
    }

    public static synchronized WebDriverManager edgedriver() {
        return new EdgeDriverManager();
    }

    public static synchronized WebDriverManager iedriver() {
        return new InternetExplorerDriverManager();
    }

    public static synchronized WebDriverManager safaridriver() {
        return new SafariDriverManager();
    }

    protected static synchronized WebDriverManager voiddriver() {
        return new VoidDriverManager();
    }

    public static synchronized WebDriverManager getInstance(
            DriverManagerType driverManagerType) {
        // This condition is necessary for compatibility between Selenium 3 and
        // 4 (since in Selenium 4, the class
        // org.openqa.selenium.chromium.ChromiumDriver is not available)
        if (driverManagerType == CHROMIUM) {
            return chromiumdriver();
        }
        return getDriver(driverManagerType.browserClass());
    }

    public static synchronized WebDriverManager getInstance(
            String browserName) {
        DriverManagerType managerType;
        switch (browserName) {
        case "operablink":
            managerType = OPERA;
            break;
        case "MicrosoftEdge":
            managerType = EDGE;
            break;
        case "internet explorer":
            managerType = IEXPLORER;
            break;
        default:
            managerType = DriverManagerType
                    .valueOf(browserName.toUpperCase(ROOT));
            break;
        }
        return getInstance(managerType);
    }

    public static synchronized WebDriverManager getInstance(
            Class<?> webDriverClass) {
        return getDriver(webDriverClass.getName());
    }

    protected static synchronized WebDriverManager getDriver(
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
        case "org.openqa.selenium.safari.SafariDriver":
            return safaridriver();
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
            return manager;

        } catch (Exception e) {
            log.error("Error trying to get manager for browser {}",
                    defaultBrowser, e);
        }
        return manager;
    }

    public synchronized void setup() {
        if (config().getClearingDriverCache()) {
            clearDriverCache();
        }
        if (config().getClearingResolutionCache()) {
            clearResolutionCache();
        }
        if (dockerEnabled || !isNullOrEmpty(config().getRemoteAddress())) {
            return;
        }
        if (getDriverManagerType() != null) {
            manage(getDriverVersion());
        }
    }

    public synchronized WebDriver create() {
        setup();
        return instantiateDriver();
    }

    public synchronized List<WebDriver> create(int numberOfBrowser) {
        List<WebDriver> browserList = new ArrayList<>();
        for (int i = 0; i < numberOfBrowser; i++) {
            if (i == 0) {
                setup();
            }
            browserList.add(instantiateDriver());
        }
        return browserList;
    }

    public synchronized void quit() {
        webDriverList.stream().forEach(this::quit);
        webDriverList.clear();
    }

    public synchronized void quit(WebDriver driver) {
        Optional<WebDriverBrowser> webDriverBrowser = findWebDriverBrowser(
                driver);
        if (webDriverBrowser.isPresent()) {
            WebDriverBrowser driverBrowser = webDriverBrowser.get();
            quit(driverBrowser);
            webDriverList.remove(driverBrowser);
        }
    }

    protected synchronized void quit(WebDriverBrowser driverBrowser) {
        try {
            WebDriver driver = driverBrowser.getDriver();
            if (driver != null) {
                log.debug("Quitting {}", driver);
                driver.quit();
            }

            List<DockerContainer> dockerContainerList = driverBrowser
                    .getDockerContainerList();
            if (dockerContainerList != null) {
                dockerContainerList.stream()
                        .forEach(dockerService::stopAndRemoveContainer);
            }
        } catch (Exception e) {
            log.warn("Exception closing {} ({})", driverBrowser.getDriver(),
                    e.getMessage(), e);
        }
    }

    public Optional<Path> getBrowserPath() {
        return versionDetector.getBrowserPath(
                getDriverManagerType().getBrowserNameLowerCase());
    }

    public DockerService getDockerService() {
        return dockerService;
    }

    public WebDriverManager browserInDocker() {
        dockerEnabled = true;
        return this;
    }

    public WebDriverManager browserInDockerAndroid() {
        throw new WebDriverManagerException(
                getDriverManagerType().getBrowserName()
                        + " is not available in Docker Android");
    }

    public WebDriverManager dockerDaemonUrl(String daemonUrl) {
        config().setDockerDaemonUrl(daemonUrl);
        return this;
    }

    public WebDriverManager dockerNetwork(String network) {
        config().setDockerNetwork(network);
        return this;
    }

    public WebDriverManager dockerTimezone(String timezone) {
        config().setDockerTimezone(timezone);
        return this;
    }

    public WebDriverManager dockerLang(String lang) {
        config().setDockerLang(lang);
        return this;
    }

    public WebDriverManager dockerShmSize(String size) {
        config().setDockerShmSize(size);
        return this;
    }

    public WebDriverManager dockerTmpfsSize(String size) {
        config().setDockerTmpfsSize(size);
        return this;
    }

    public WebDriverManager dockerTmpfsMount(String mount) {
        config().setDockerTmpfsMount(mount);
        return this;
    }

    public WebDriverManager dockerVolumes(String[] volumes) {
        config().setDockerVolumes(String.join(",", volumes));
        return this;
    }

    public WebDriverManager dockerVolume(String volume) {
        config().setDockerVolumes(volume);
        return this;
    }

    public WebDriverManager dockerScreenResolution(String screenResolution) {
        config().setDockerScreenResolution(screenResolution);
        return this;
    }

    public WebDriverManager dockerRecordingFrameRate(int frameRate) {
        config().setDockerRecordingFrameRate(frameRate);
        return this;
    }

    public WebDriverManager enableVnc() {
        config().setDockerEnableVnc(true);
        return this;
    }

    public WebDriverManager enableRecording() {
        config().setDockerEnableRecording(true);
        return this;
    }

    public WebDriverManager recordingPrefix(String prefix) {
        config().setDockerRecordingPrefix(prefix);
        return this;
    }

    public WebDriverManager recordingOutput(String path) {
        return recordingOutput(Paths.get(path));
    }

    public WebDriverManager recordingOutput(Path path) {
        config().setDockerRecordingOutput(path);
        return this;
    }

    public WebDriverManager capabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    public WebDriverManager remoteAddress(String remoteAddress) {
        config().setRemoteAddress(remoteAddress);
        return this;
    }

    public WebDriverManager dockerImage(String dockerImage) {
        config().setDockerCustomImage(dockerImage);
        return this;
    }

    public WebDriverManager driverVersion(String driverVersion) {
        setDriverVersion(driverVersion);
        return this;
    }

    public WebDriverManager browserVersion(String browserVersion) {
        setBrowserVersion(browserVersion);
        return this;
    }

    public WebDriverManager architecture(Architecture architecture) {
        config().setArchitecture(architecture);
        forcedArch = true;
        return this;
    }

    public WebDriverManager arch32() {
        architecture(X32);
        return this;
    }

    public WebDriverManager arch64() {
        architecture(X64);
        return this;
    }

    public WebDriverManager arm64() {
        architecture(ARM64);
        return this;
    }

    public WebDriverManager win() {
        operatingSystem(WIN);
        return this;
    }

    public WebDriverManager linux() {
        operatingSystem(LINUX);
        return this;
    }

    public WebDriverManager mac() {
        operatingSystem(MAC);
        return this;
    }

    public WebDriverManager operatingSystem(OperatingSystem os) {
        config().setOs(os.name());
        return this;
    }

    public WebDriverManager forceDownload() {
        config().setForceDownload(true);
        return this;
    }

    public WebDriverManager driverRepositoryUrl(URL url) {
        setDriverUrl(url);
        return this;
    }

    public WebDriverManager useMirror() {
        Optional<URL> mirrorUrl = getMirrorUrl();
        if (!mirrorUrl.isPresent()) {
            throw new WebDriverManagerException("Mirror URL not available");
        }
        config().setUseMirror(true);
        return this;
    }

    public WebDriverManager proxy(String proxy) {
        config().setProxy(proxy);
        return this;
    }

    public WebDriverManager proxyUser(String proxyUser) {
        config().setProxyUser(proxyUser);
        return this;
    }

    public WebDriverManager proxyPass(String proxyPass) {
        config().setProxyPass(proxyPass);
        return this;
    }

    public WebDriverManager useBetaVersions() {
        config().setUseBetaVersions(true);
        return this;
    }

    public WebDriverManager ignoreDriverVersions(String... driverVersions) {
        config().setIgnoreVersions(driverVersions);
        return this;
    }

    public WebDriverManager gitHubToken(String gitHubToken) {
        config().setGitHubToken(gitHubToken);
        return this;
    }

    public WebDriverManager timeout(int timeout) {
        config().setTimeout(timeout);
        return this;
    }

    public WebDriverManager properties(String properties) {
        config().setProperties(properties);
        return this;
    }

    public WebDriverManager cachePath(String cachePath) {
        config().setCachePath(cachePath);
        return this;
    }

    public WebDriverManager resolutionCachePath(String resolutionCachePath) {
        config().setResolutionCachePath(resolutionCachePath);
        return this;
    }

    public WebDriverManager avoidExport() {
        config().setAvoidExport(true);
        return this;
    }

    public WebDriverManager avoidOutputTree() {
        config().setAvoidOutputTree(true);
        return this;
    }

    public WebDriverManager avoidBrowserDetection() {
        config().setAvoidBrowserDetection(true);
        return this;
    }

    public WebDriverManager avoidResolutionCache() {
        config().setAvoidResolutionCache(true);
        return this;
    }

    public WebDriverManager avoidFallback() {
        config().setAvoidFallback(true);
        return this;
    }

    public WebDriverManager avoidReadReleaseFromRepository() {
        config().setAvoidReadReleaseFromRepository(true);
        return this;
    }

    public WebDriverManager avoidTmpFolder() {
        config().setAvoidTmpFolder(true);
        return this;
    }

    public WebDriverManager avoidUseChromiumDriverSnap() {
        config().setUseChromiumDriverSnap(false);
        return this;
    }

    public WebDriverManager ttl(int seconds) {
        config().setTtl(seconds);
        return this;
    }

    public WebDriverManager ttlBrowsers(int seconds) {
        config().setTtlForBrowsers(seconds);
        return this;
    }

    public WebDriverManager browserVersionDetectionCommand(
            String browserVersionCommand) {
        config().setBrowserVersionDetectionCommand(browserVersionCommand);
        return this;
    }

    public WebDriverManager useLocalVersionsPropertiesFirst() {
        config().setVersionsPropertiesOnlineFirst(false);
        return this;
    }

    public WebDriverManager useLocalCommandsPropertiesFirst() {
        config().setCommandsPropertiesOnlineFirst(false);
        return this;
    }

    public WebDriverManager versionsPropertiesUrl(URL url) {
        config().setVersionsPropertiesUrl(url);
        return this;
    }

    public WebDriverManager commandsPropertiesUrl(URL url) {
        config().setCommandsPropertiesUrl(url);
        return this;
    }

    public WebDriverManager clearResolutionCache() {
        resolutionCache.clear();
        return this;
    }

    public WebDriverManager clearDriverCache() {
        String cachePath = config().getCachePath();
        try {
            log.debug("Clearing driver cache at {}", cachePath);
            cleanDirectory(new File(cachePath));
        } catch (Exception e) {
            log.warn("Exception deleting driver cache at {}", cachePath, e);
        }
        return this;
    }

    public WebDriverManager browserVersionDetectionRegex(String regex) {
        config().setBrowserVersionDetectionRegex(regex);
        return this;
    }

    public void reset() {
        config().reset();
        mirrorLog = false;
        forcedArch = false;
        retryCount = 0;
        shutdownHook = false;
        dockerEnabled = false;
        androidEnabled = false;
        capabilities = null;
    }

    // ------------

    public String getDownloadedDriverPath() {
        return downloadedDriverPath;
    }

    public String getDownloadedDriverVersion() {
        return downloadedDriverVersion;
    }

    public List<String> getDriverVersions() {
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

    public WebDriver getWebDriver() {
        List<WebDriver> driverList = getWebDriverList();
        return driverList.isEmpty() ? null : driverList.iterator().next();
    }

    public List<WebDriver> getWebDriverList() {
        List<WebDriver> webdriverList = new ArrayList<>();
        if (webDriverList.isEmpty()) {
            log.warn("WebDriver object(s) not available");
        } else {
            webdriverList = webDriverList.stream()
                    .map(WebDriverBrowser::getDriver)
                    .collect(Collectors.toList());
        }
        return webdriverList;
    }

    public String getDockerBrowserContainerId(WebDriver driver) {
        return (String) getPropertyFromWebDriverBrowser(driver,
                WebDriverBrowser::getBrowserContainerId);
    }

    public String getDockerBrowserContainerId() {
        return (String) getPropertyFromFirstWebDriverBrowser(
                WebDriverBrowser::getBrowserContainerId);
    }

    public URL getDockerSeleniumServerUrl(WebDriver driver) {
        return (URL) getPropertyFromWebDriverBrowser(driver,
                WebDriverBrowser::getSeleniumServerUrl);
    }

    public URL getDockerSeleniumServerUrl() {
        return (URL) getPropertyFromFirstWebDriverBrowser(
                WebDriverBrowser::getSeleniumServerUrl);
    }

    public URL getDockerNoVncUrl(WebDriver driver) {
        return (URL) getPropertyFromWebDriverBrowser(driver,
                WebDriverBrowser::getNoVncUrl);
    }

    public URL getDockerNoVncUrl() {
        return (URL) getPropertyFromFirstWebDriverBrowser(
                WebDriverBrowser::getNoVncUrl);
    }

    public Path getDockerRecordingPath(WebDriver driver) {
        return (Path) getPropertyFromWebDriverBrowser(driver,
                WebDriverBrowser::getRecordingPath);
    }

    public Path getDockerRecordingPath() {
        return (Path) getPropertyFromFirstWebDriverBrowser(
                WebDriverBrowser::getRecordingPath);
    }

    protected Object getPropertyFromWebDriverBrowser(WebDriver driver,
            Function<WebDriverBrowser, Object> function) {
        Object object = null;
        Optional<WebDriverBrowser> webDriverBrowser = findWebDriverBrowser(
                driver);
        if (webDriverBrowser.isPresent()) {
            object = function.apply(webDriverBrowser.get());
        }
        return object;
    }

    protected Optional<WebDriverBrowser> findWebDriverBrowser(
            WebDriver driver) {
        for (WebDriverBrowser webDriver : webDriverList) {
            if (webDriver.getIdentityHash() == webDriver
                    .calculateIdentityHash(driver)) {
                return Optional.of(webDriver);
            }
        }
        return empty();
    }

    protected Object getPropertyFromFirstWebDriverBrowser(
            Function<WebDriverBrowser, Object> function) {
        Object object = null;
        if (webDriverList.isEmpty()) {
            log.warn(
                    "Property not available since there is no browsers in Docker");
        } else {
            object = function.apply(webDriverList.get(0));
        }
        return object;
    }

    // ------------

    protected void manage(String driverVersion) {
        try (HttpClient wdmHttpClient = httpClient) {
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

                if (!versionDetector.isSnap()) {
                    storeInResolutionCache(preferenceKey, driverVersion,
                            optionalBrowserVersion.get());
                }
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

    protected URL getDriverUrlCkeckingMirror(URL url) {
        if (config().isUseMirror()) {
            Optional<URL> mirrorUrl = getMirrorUrl();
            if (mirrorUrl.isPresent()) {
                return mirrorUrl.get();
            }
        }
        return url;
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
        try {
            String remoteAddress = config().getRemoteAddress();
            if (dockerEnabled) {
                driver = createDockerWebDriver();
            } else if (!isNullOrEmpty(remoteAddress)) {
                Capabilities caps = Optional.ofNullable(capabilities)
                        .orElse(getCapabilities());
                driver = webDriverCreator.createRemoteWebDriver(remoteAddress,
                        caps);
                webDriverList.add(new WebDriverBrowser(driver));

            } else {
                driver = createLocalWebDriver();
            }

        } catch (Exception e) {
            log.error("There was an error creating WebDriver object for {}",
                    getDriverManagerType().getBrowserName(), e);
        }
        addShutdownHookIfRequired();

        return driver;
    }

    protected Capabilities getMergedCapabilities() {
        Capabilities caps = getCapabilities();
        if (capabilities != null) {
            caps = caps.merge(capabilities);
        }
        return caps;
    }

    protected void addShutdownHookIfRequired() {
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
        String browserName = getKeyForResolutionCache();
        if (androidEnabled) {
            browserName += "-mobile";
        }
        String browserVersion = getBrowserVersion();
        String browserCacheKey = browserName + "-container-";

        String dockerCustomImage = config().getDockerCustomImage();
        String browserImage;
        if (!isNullOrEmpty(dockerCustomImage)) {
            browserImage = dockerCustomImage;
            browserVersion = dockerService.getVersionFromImage(browserImage);
            browserCacheKey += "custom";

        } else {
            if (isUnknown(browserVersion) || dockerService
                    .isBrowserVersionLatesMinus(browserVersion)) {
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
            browserImage = dockerService.getDockerImage(browserName,
                    browserVersion, androidEnabled);
        }

        DockerContainer browserContainer = dockerService.startBrowserContainer(
                browserImage, browserCacheKey, browserVersion, androidEnabled);
        browserContainer.setBrowserName(browserName);
        String seleniumServerUrl = browserContainer.getContainerUrl();

        WebDriverBrowser driverBrowser = new WebDriverBrowser();
        driverBrowser.addDockerContainer(browserContainer);
        driverBrowser.setSeleniumServerUrl(seleniumServerUrl);
        log.trace("The Selenium Serverl URL is {}", seleniumServerUrl);
        driverBrowser.setBrowserContainerId(browserContainer.getContainerId());
        webDriverList.add(driverBrowser);

        WebDriver driver = webDriverCreator.createRemoteWebDriver(
                seleniumServerUrl, getMergedCapabilities());
        driverBrowser.setDriver(driver);
        String sessionId = webDriverCreator
                .getSessionId(driverBrowser.getDriver());
        browserContainer.setSessionId(sessionId);

        if (config.isEnabledDockerVnc()) {
            String noVncImage = config.getDockerNoVncImage();
            String noVncVersion = dockerService.getVersionFromImage(noVncImage);
            DockerContainer noVncContainer = dockerService.startNoVncContainer(
                    noVncImage, "novnc-container", noVncVersion,
                    browserContainer);
            driverBrowser.addDockerContainer(noVncContainer);
            String noVncUrl = noVncContainer.getContainerUrl();
            driverBrowser.setNoVncUrl(noVncUrl);

            log.info("Docker session noVNC URL: {}", noVncUrl);
        }

        if (config.isEnabledDockerRecording()) {
            String recorderImage = config.getDockerRecordingImage();
            String recorderVersion = dockerService
                    .getVersionFromImage(recorderImage);
            DockerContainer recorderContainer = dockerService
                    .startRecorderContainer(recorderImage, "recorder-container",
                            recorderVersion, browserContainer);
            driverBrowser.addDockerContainer(recorderContainer, 0);
            Path recordingPath = recorderContainer.getRecordingPath();
            driverBrowser.setRecordingPath(recordingPath);

            log.info("Starting recording {}", recordingPath);
        }

        return driverBrowser.getDriver();
    }

    protected WebDriver createLocalWebDriver() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        WebDriver driver = null;
        if (getDriverManagerType() != null) {
            Class<?> browserClass = Class
                    .forName(getDriverManagerType().browserClass());
            driver = webDriverCreator.createLocalWebDriver(browserClass,
                    capabilities);
            webDriverList.add(new WebDriverBrowser(driver));
        }
        return driver;
    }

    protected Capabilities getCapabilities() {
        return new MutableCapabilities();
    }

    protected void addDefaultArgumentsForDocker(Capabilities options)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException,
            SecurityException {
        if (dockerEnabled && !androidEnabled) {
            Method addArgumentsMethod = options.getClass()
                    .getMethod("addArguments", List.class);
            List<String> defaultArgs = Arrays
                    .asList(config.getDockerDefaultArgs().split(","));
            addArgumentsMethod.invoke(options, defaultArgs);
        }
    }

    protected static void logCliError(String browserForResolving,
            String browserForDocker, int port) {
        log.error("The valid arguments for WebDriverManager CLI are:");
        log.error("1. For resolving drivers locally:");
        log.error("\t{}browserName <driverVersion>", CLI_RESOLVER);
        log.error("(where browserName is: {})", browserForResolving);
        log.error("");
        log.error(
                "2. For running a browser in a Docker (and use it trough noVNC):");
        log.error("\t{}browserName <browserVersion>", CLI_DOCKER);
        log.error("(where browserName={})", browserForDocker);
        log.error("");
        log.error("3. For starting WebDriverManager Server:");
        log.error("\t{} <port>", CLI_SERVER);
        log.error("(where the default port is {})", port);
    }

    protected static void resolveLocal(String[] args, String validBrowsers,
            String browser) {
        log.info("Using WebDriverManager to resolve {}", browser);
        try {
            DriverManagerType driverManagerType = DriverManagerType
                    .valueOf(browser.toUpperCase(ROOT));
            WebDriverManager wdm = WebDriverManager
                    .getInstance(driverManagerType).avoidExport().cachePath(".")
                    .forceDownload().avoidResolutionCache();
            if (browser.equalsIgnoreCase("iexplorer")) {
                wdm.operatingSystem(WIN);
            }
            if (args.length > 1) {
                wdm.driverVersion(args[1]);
            }
            if (wdm.getDockerService().isRunningInsideDocker()) {
                wdm.avoidBrowserDetection();
            }
            wdm.avoidOutputTree().setup();
        } catch (Exception e) {
            log.error("Driver for {} not found (valid browsers {})", browser,
                    validBrowsers);
        }
    }

    protected static void runInDocker(String[] args, String validBrowsers,
            String browser) {
        log.info("Using WebDriverManager to run {} in Docker", browser);
        try {
            WebDriverManager wdm;
            if (browser.equalsIgnoreCase("chrome-mobile")) {
                wdm = WebDriverManager.chromedriver().browserInDockerAndroid();
            } else {
                DriverManagerType driverManagerType = DriverManagerType
                        .valueOf(browser.toUpperCase(ROOT));
                wdm = WebDriverManager.getInstance(driverManagerType)
                        .browserInDocker();
            }
            if (args.length > 1) {
                wdm.browserVersion(args[1]);
            }
            wdm.enableVnc().avoidResolutionCache().create();

            log.info("Press ENTER to exit");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            scanner.close();

            wdm.quit();

        } catch (Exception e) {
            log.error("Browser {} not available in Docker (valid browsers {})",
                    browser, validBrowsers);
        }
    }

    protected static void startServer(String[] args, int port) {
        if (args.length > 1 && isNumeric(args[1])) {
            port = parseInt(args[1]);
        }
        new WdmServer(port);
    }

    public static void main(String[] args) {
        String browserForResolving = "chrome|edge|firefox|opera|chromium|iexplorer";
        String browserForNoVnc = "chrome|edge|firefox|opera|safari|chrome-mobile";
        int port = new Config().getServerPort();
        if (args.length <= 0) {
            logCliError(browserForResolving, browserForNoVnc, port);
        } else {
            String arg = args[0].toLowerCase(ROOT);
            if (arg.equals(CLI_SERVER)) {
                startServer(args, port);
            } else if (arg.startsWith(CLI_RESOLVER.toLowerCase(ROOT))) {
                String browser = arg.substring(CLI_RESOLVER.length());
                resolveLocal(args, browserForResolving, browser);
            } else if (arg.startsWith(CLI_DOCKER.toLowerCase(ROOT))) {
                String browser = arg.substring(CLI_DOCKER.length());
                runInDocker(args, browserForNoVnc, browser);
            } else {
                logCliError(browserForResolving, browserForNoVnc, port);
            }
        }
    }

}
