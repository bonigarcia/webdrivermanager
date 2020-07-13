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

import static io.github.bonigarcia.wdm.config.Architecture.X32;
import static io.github.bonigarcia.wdm.config.Architecture.X64;
import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROMIUM;
import static io.github.bonigarcia.wdm.config.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.config.DriverManagerType.IEXPLORER;
import static io.github.bonigarcia.wdm.config.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.config.DriverManagerType.PHANTOMJS;
import static io.github.bonigarcia.wdm.config.DriverManagerType.SELENIUM_SERVER_STANDALONE;
import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;
import static java.lang.Integer.parseInt;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.sort;
import static java.util.Optional.empty;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathFactory.newInstance;
import static org.apache.commons.io.FileUtils.deleteDirectory;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.jsoup.Jsoup;
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
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.ChromiumDriverManager;
import io.github.bonigarcia.wdm.managers.EdgeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import io.github.bonigarcia.wdm.managers.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.managers.OperaDriverManager;
import io.github.bonigarcia.wdm.managers.PhantomJsDriverManager;
import io.github.bonigarcia.wdm.managers.SeleniumServerStandaloneManager;
import io.github.bonigarcia.wdm.managers.VoidDriverManager;
import io.github.bonigarcia.wdm.online.BitBucketApi;
import io.github.bonigarcia.wdm.online.Downloader;
import io.github.bonigarcia.wdm.online.GitHubApi;
import io.github.bonigarcia.wdm.online.HttpClient;
import io.github.bonigarcia.wdm.online.S3NamespaceContext;
import io.github.bonigarcia.wdm.online.UrlHandler;
import io.github.bonigarcia.wdm.versions.VersionComparator;
import io.github.bonigarcia.wdm.versions.VersionDetector;

/**
 * Parent driver manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.0
 */
public abstract class WebDriverManager {

    protected static final Logger log = getLogger(lookup().lookupClass());

    protected static final String SLASH = "/";
    protected static final String LATEST_RELEASE = "LATEST_RELEASE";
    protected static final NamespaceContext S3_NAMESPACE_CONTEXT = new S3NamespaceContext();

    protected abstract List<URL> getDriverUrls() throws IOException;

    protected abstract Optional<String> getBrowserVersionFromTheShell();

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

    protected HttpClient httpClient;
    protected Downloader downloader;
    protected String downloadedDriverVersion;
    protected String binaryPath;
    protected boolean mirrorLog;
    protected boolean forcedArch;
    protected boolean forcedOs;
    protected int retryCount = 0;
    protected Config config = new Config();
    protected ResolutionCache resolutionCache = new ResolutionCache(config);
    protected CacheHandler cacheHandler = new CacheHandler(config);
    protected VersionDetector versionDetector;

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

    public static synchronized WebDriverManager phantomjs() {
        instanceMap.putIfAbsent(PHANTOMJS, new PhantomJsDriverManager());
        return instanceMap.get(PHANTOMJS);
    }

    public static synchronized WebDriverManager seleniumServerStandalone() {
        instanceMap.putIfAbsent(SELENIUM_SERVER_STANDALONE,
                new SeleniumServerStandaloneManager());
        return instanceMap.get(SELENIUM_SERVER_STANDALONE);
    }

    protected static synchronized WebDriverManager voiddriver() {
        return new VoidDriverManager();
    }

    public static synchronized WebDriverManager getInstance(
            DriverManagerType driverManagerType) {
        if (driverManagerType == null) {
            return voiddriver();
        }
        switch (driverManagerType) {
        case CHROME:
            return chromedriver();
        case CHROMIUM:
            return chromiumdriver();
        case FIREFOX:
            return firefoxdriver();
        case OPERA:
            return operadriver();
        case IEXPLORER:
            return iedriver();
        case EDGE:
            return edgedriver();
        case PHANTOMJS:
            return phantomjs();
        case SELENIUM_SERVER_STANDALONE:
            return seleniumServerStandalone();
        default:
            return voiddriver();
        }
    }

    public static synchronized WebDriverManager getInstance(
            Class<?> webDriverClass) {
        switch (webDriverClass.getName()) {
        case "org.openqa.selenium.chrome.ChromeDriver":
            return chromedriver();
        case "org.openqa.selenium.firefox.FirefoxDriver":
            return firefoxdriver();
        case "org.openqa.selenium.opera.OperaDriver":
            return operadriver();
        case "org.openqa.selenium.ie.InternetExplorerDriver":
            return iedriver();
        case "org.openqa.selenium.edge.EdgeDriver":
            return edgedriver();
        case "org.openqa.selenium.phantomjs.PhantomJSDriver":
            return phantomjs();
        default:
            return voiddriver();
        }
    }

    public synchronized void setup() {
        DriverManagerType driverManagerType = getDriverManagerType();
        if (driverManagerType != null) {
            try {
                // WebDriverManager survey link (to be removed in next version)
                log.info(
                        "Please answer the following questionnaire based on your experience with WebDriverManager. Thanks a lot!");
                log.info("====> http://tiny.cc/wdm-survey <====");

                if (config().getClearingResolutionCache()) {
                    clearResolutionCache();
                }
                String driverVersion = getDriverVersion();
                manage(driverVersion);
            } finally {
                if (!config().isAvoidAutoReset()) {
                    reset();
                }
            }
        }
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

    public WebDriverManager gitHubTokenSecret(String gitHubTokenSecret) {
        config().setGitHubTokenSecret(gitHubTokenSecret);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager gitHubTokenName(String gitHubTokenName) {
        config().setGitHubTokenName(gitHubTokenName);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager localRepositoryUser(String localRepositoryUser) {
        config().setLocalRepositoryUser(localRepositoryUser);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager localRepositoryPassword(
            String localRepositoryPassword) {
        config().setLocalRepositoryPassword(localRepositoryPassword);
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

    public WebDriverManager ttl(int seconds) {
        config().setTtl(seconds);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager browserPath(String browserPath) {
        config().setBinaryPath(browserPath);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager useLocalVersionsPropertiesFirst() {
        config().setVersionsPropertiesOnlineFirst(false);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager clearResolutionCache() {
        instanceMap.get(getDriverManagerType()).resolutionCache.clear();
        return instanceMap.get(getDriverManagerType());
    }

    // ------------

    public String getBinaryPath() {
        return instanceMap.get(getDriverManagerType()).binaryPath;
    }

    public String getDownloadedVersion() {
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

    public void clearCache() {
        String cachePath = config().getCachePath();
        try {
            log.debug("Clearing cache at {}", cachePath);
            deleteDirectory(new File(cachePath));
        } catch (Exception e) {
            log.warn("Exception deleting cache at {}", cachePath, e);
        }
    }

    // ------------

    protected void manage(String driverVersion) {
        httpClient = new HttpClient(config());
        try (HttpClient wdmHttpClient = httpClient) {
            versionDetector = new VersionDetector(config, httpClient);
            downloader = new Downloader(httpClient, config(), this::preDownload,
                    this::postDownload);

            if (isUnknown(driverVersion)) {
                driverVersion = resolveDriverVersion(driverVersion);
            }
            if (versionDetector.isSnap()) {
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
            optionalBrowserVersion = getValueFromResolutionCache(preferenceKey,
                    optionalBrowserVersion, config().getTtlForBrowsers());
        }
        if (!optionalBrowserVersion.isPresent()) {
            optionalBrowserVersion = detectBrowserVersion();
        }
        if (optionalBrowserVersion.isPresent()) {
            preferenceKey = getKeyForResolutionCache()
                    + optionalBrowserVersion.get();
            if (useResolutionCache()
                    && resolutionCache.checkKeyInResolutionCache(preferenceKey,
                            config().getTtl())) {
                driverVersion = resolutionCache
                        .getValueFromResolutionCache(preferenceKey);
            }

            Optional<String> optionalDriverVersion = empty();
            if (isUnknown(driverVersion)) {
                optionalDriverVersion = getDriverVersionFromRepository(
                        optionalBrowserVersion);
            }
            if (isUnknown(driverVersion)) {
                optionalDriverVersion = versionDetector
                        .getDriverVersionFromProperties(preferenceKey);
            }
            if (optionalDriverVersion.isPresent()) {
                driverVersion = optionalDriverVersion.get();
            }
            if (isUnknown(driverVersion)) {
                log.debug(
                        "The driver version for {} {} is unknown ... trying with latest",
                        getDriverManagerType(), optionalBrowserVersion.get());
            } else if (!isUnknown(driverVersion)) {
                log.info(
                        "Using {} {} (since {} {} is installed in your machine)",
                        getDriverName(), driverVersion, getDriverManagerType(),
                        optionalBrowserVersion.get());
                storeInResolutionCache(preferenceKey, driverVersion,
                        optionalBrowserVersion.get());
            }
        }

        if (isUnknown(driverVersion)) { // if still unknown, try with latest
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
                getDriverName());
    }

    protected void noCandidateUrlFound(UrlHandler urlHandler,
            String driverVersion) {
        Optional<URL> buildUrl = buildUrl(driverVersion);
        if (buildUrl.isPresent()) {
            urlHandler.getCandidateUrls().add(buildUrl.get());
        } else {
            Architecture arch = config().getArchitecture();
            String os = config().getOs();
            String errorMessage = String.format(
                    "%s %s for %s %s not found in %s", getDriverName(),
                    getDriverVersionLabel(driverVersion), os, arch.toString(),
                    getDriverUrl());
            log.error(errorMessage);
            throw new WebDriverManagerException(errorMessage);
        }
    }

    protected void exportDriver(String variableValue) {
        binaryPath = variableValue;
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

    protected Optional<String> getValueFromResolutionCache(String preferenceKey,
            Optional<String> optionalBrowserVersion, int ttl) {
        if (useResolutionCache() && resolutionCache
                .checkKeyInResolutionCache(preferenceKey, ttl)) {
            optionalBrowserVersion = Optional.of(
                    resolutionCache.getValueFromResolutionCache(preferenceKey));
        }
        return optionalBrowserVersion;
    }

    protected String preDownload(String target, String driverVersion) {
        log.trace("Pre-download. target={}, driverVersion={}", target,
                driverVersion);
        return target;
    }

    protected File postDownload(File archive) {
        File parentFolder = archive.getParentFile();
        File[] ls = parentFolder.listFiles();
        for (File f : ls) {
            if (getDriverName().contains(removeExtension(f.getName()))) {
                log.trace("Found binary in post-download: {}", f);
                return f;
            }
        }
        throw new WebDriverManagerException("Driver " + getDriverName()
                + " not found (using temporal folder " + parentFolder + ")");
    }

    protected Optional<String> detectBrowserVersion() {
        if (config().isAvoidBrowserDetection()) {
            return empty();
        }

        String driverManagerTypeLowerCase = getDriverManagerType().name()
                .toLowerCase();
        Optional<String> optionalBrowserVersion;

        if (useResolutionCache() && resolutionCache.checkKeyInResolutionCache(
                driverManagerTypeLowerCase, config().getTtlForBrowsers())) {
            optionalBrowserVersion = Optional.of(resolutionCache
                    .getValueFromResolutionCache(driverManagerTypeLowerCase));

            log.trace("Detected {} version {}", getDriverManagerType(),
                    optionalBrowserVersion);
        } else {
            optionalBrowserVersion = getBrowserVersionFromTheShell();
        }
        return optionalBrowserVersion;
    }

    protected boolean useResolutionCache() {
        return !config().isAvoidingResolutionCache()
                && !config().isForceDownload() && !forcedArch && !forcedOs;
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
        } else {
            log.error("{}", errorMessage, e);
            throw new WebDriverManagerException(e);
        }
    }

    protected UrlHandler createUrlHandler(String driverVersion)
            throws IOException {
        List<URL> candidateUrls = getDriverUrls();
        String shortDriverName = getShortDriverName();
        UrlHandler urlHandler = new UrlHandler(candidateUrls, driverVersion,
                shortDriverName, config().isUseBetaVersions());
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

            log.debug("Driver to be downloaded {} {}", shortDriverName,
                    urlHandler.getDriverVersion());
            log.trace("Driver URLs after filtering for version: {}",
                    urlHandler.getCandidateUrls());
            String os = config().getOs();
            Architecture architecture = config().getArchitecture();

            if (urlHandler.hasNoCandidateUrl()) {
                noCandidateUrlFound(urlHandler, driverVersion);
            }

            // Rest of filters

            urlHandler.filterByOs(getDriverName(), os);
            urlHandler.filterByArch(architecture, forcedArch);
            urlHandler.filterByDistro(os, getDriverName());
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
                        && (link.toLowerCase().endsWith(".bz2")
                                || link.toLowerCase().endsWith(".zip")
                                || link.toLowerCase().endsWith(".gz"))) {
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

    private void logSeekRepo(URL driverUrl) {
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

        String gitHubTokenName = config().getGitHubTokenName();
        String gitHubTokenSecret = config().getGitHubTokenSecret();
        if (!isNullOrEmpty(gitHubTokenName)
                && !isNullOrEmpty(gitHubTokenSecret)) {
            String userpass = gitHubTokenName + ":" + gitHubTokenSecret;
            String basicAuth = "Basic "
                    + new String(new Base64().encode(userpass.getBytes()));
            get.addHeader("Authorization", basicAuth);
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

    protected List<URL> getDriversFromBitBucket() throws IOException {
        List<URL> urls;
        URL driverUrl = getDriverUrl();
        logSeekRepo(driverUrl);

        Optional<URL> mirrorUrl = getMirrorUrl();
        if (mirrorUrl.isPresent() && config.isUseMirror()) {
            urls = getDriversFromMirror(mirrorUrl.get());

        } else {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(httpClient
                            .execute(httpClient.createHttpGet(driverUrl))
                            .getEntity().getContent()))) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                BitBucketApi bitBucketInfo = gson.fromJson(reader,
                        BitBucketApi.class);

                urls = bitBucketInfo.getUrls();
            }
        }
        return urls;
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    protected FilenameFilter getFolderFilter() {
        return (dir, name) -> dir.isDirectory()
                && name.toLowerCase().contains(getDriverName());
    }

    protected Charset getVersionCharset() {
        return defaultCharset();
    }

    protected String getLatestVersionLabel() {
        return LATEST_RELEASE;
    }

    protected Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion) {
        return versionDetector.getDriverVersionFromRepository(driverVersion,
                getDriverUrl(), getVersionCharset(), getDriverName(),
                getLatestVersionLabel(), LATEST_RELEASE);
    }

    protected void reset() {
        config().reset();
        mirrorLog = false;
        forcedArch = false;
        forcedOs = false;
        retryCount = 0;
    }

    protected String getProgramFilesEnv() {
        return System.getProperty("os.arch").contains("64") ? "PROGRAMFILES"
                : "PROGRAMFILES(X86)";
    }

    protected String getOtherProgramFilesEnv() {
        return System.getProperty("os.arch").contains("64")
                ? "PROGRAMFILES(X86)"
                : "PROGRAMFILES";
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
                    .valueOf(arg.toUpperCase());
            WebDriverManager wdm = WebDriverManager
                    .getInstance(driverManagerType).avoidExport().cachePath(".")
                    .forceDownload();
            if (arg.equalsIgnoreCase("edge")
                    || arg.equalsIgnoreCase("iexplorer")) {
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
        log.error(
                "1. WebDriverManager used to resolve binary drivers locally:");
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
        return getDriverManagerType().name().toLowerCase();
    }

    protected String getDriverVersionLabel(String driverVersion) {
        return isUnknown(driverVersion) ? "(latest version)" : driverVersion;
    }

    protected Optional<URL> buildUrl(String driverVersion) {
        return empty();
    }

    public static void main(String[] args) {
        String validBrowsers = "chrome|firefox|opera|edge|phantomjs|iexplorer|selenium_server_standalone";
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
