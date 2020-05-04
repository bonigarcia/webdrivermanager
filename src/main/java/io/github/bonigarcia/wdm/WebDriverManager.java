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

import static io.github.bonigarcia.wdm.etc.Architecture.X32;
import static io.github.bonigarcia.wdm.etc.Architecture.X64;
import static io.github.bonigarcia.wdm.etc.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.CHROMIUM;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.IEXPLORER;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.PHANTOMJS;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.SELENIUM_SERVER_STANDALONE;
import static io.github.bonigarcia.wdm.etc.OperatingSystem.WIN;
import static io.github.bonigarcia.wdm.etc.Shell.getVersionFromPosixOutput;
import static io.github.bonigarcia.wdm.etc.Shell.getVersionFromWmicOutput;
import static io.github.bonigarcia.wdm.etc.Shell.runAndWait;
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
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
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
import java.util.Properties;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import io.github.bonigarcia.wdm.cache.CacheFilter;
import io.github.bonigarcia.wdm.cache.ResolutionCache;
import io.github.bonigarcia.wdm.etc.Architecture;
import io.github.bonigarcia.wdm.etc.Config;
import io.github.bonigarcia.wdm.etc.DriverManagerType;
import io.github.bonigarcia.wdm.etc.OperatingSystem;
import io.github.bonigarcia.wdm.etc.Shell;
import io.github.bonigarcia.wdm.etc.VersionComparator;
import io.github.bonigarcia.wdm.etc.WebDriverManagerException;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.ChromiumDriverManager;
import io.github.bonigarcia.wdm.managers.EdgeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import io.github.bonigarcia.wdm.managers.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.managers.OperaDriverManager;
import io.github.bonigarcia.wdm.managers.PhantomJsDriverManager;
import io.github.bonigarcia.wdm.managers.SeleniumServerStandaloneManager;
import io.github.bonigarcia.wdm.managers.VoidDriverManager;
import io.github.bonigarcia.wdm.online.Downloader;
import io.github.bonigarcia.wdm.online.GitHubApi;
import io.github.bonigarcia.wdm.online.HttpClient;
import io.github.bonigarcia.wdm.online.UrlHandler;

/**
 * Parent driver manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.0
 */
public abstract class WebDriverManager {

    protected static final Logger log = getLogger(lookup().lookupClass());

    protected static final String SLASH = "/";
    protected static final String ONLINE = "online";
    protected static final String LOCAL = "local";
    protected static final String LATEST_RELEASE = "LATEST_RELEASE";
    protected static final String REG_SZ = "REG_SZ";

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
    protected boolean isSnap;
    protected int retryCount = 0;
    protected Config config = new Config();
    protected ResolutionCache resolutionCache = new ResolutionCache(config);
    protected CacheFilter cacheFilter = new CacheFilter(config);
    protected Properties versionsProperties;

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

    public WebDriverManager avoidAutoVersion() {
        config().setAvoidAutoVersion(true);
        return instanceMap.get(getDriverManagerType());
    }

    public WebDriverManager avoidResolutionCache() {
        config().setAvoidResolutionCache(true);
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
            downloader = new Downloader(httpClient, config(), this::preDownload,
                    this::postDownload);

            if (isUnknown(driverVersion)) {
                driverVersion = resolveDriverVersion(driverVersion);
            }

            Optional<String> driverInCache = empty();
            if (!isUnknown(driverVersion)) {
                driverInCache = cacheFilter.getDriverFromCache(driverVersion,
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
                    optionalBrowserVersion);
        }
        if (!optionalBrowserVersion.isPresent()) {
            optionalBrowserVersion = detectBrowserVersion();
        }
        if (optionalBrowserVersion.isPresent()) {
            preferenceKey = getKeyForResolutionCache()
                    + optionalBrowserVersion.get();
            driverVersion = resolutionCache
                    .getValueFromResolutionCache(preferenceKey);

            Optional<String> optionalDriverVersion = empty();
            if (isUnknown(driverVersion)) {
                optionalDriverVersion = getDriverVersionFromRepository(
                        optionalBrowserVersion);
            }
            if (isUnknown(driverVersion)) {
                optionalDriverVersion = getDriverVersionFromProperties(
                        preferenceKey);
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

    private void noCandidateUrlFound(String driverVersion) {
        Architecture arch = config().getArchitecture();
        String os = config().getOs();
        String errorMessage = String.format("%s %s for %s %s not found in %s",
                getDriverName(), getDriverVersionLabel(driverVersion), os,
                arch.toString(), getDriverUrl());
        log.error(errorMessage);
        throw new WebDriverManagerException(errorMessage);
    }

    protected void exportDriver(String variableValue) {
        binaryPath = variableValue;
        Optional<String> exportParameter = getExportParameter();
        if (!config.isAvoidExport() && exportParameter.isPresent()) {
            String variableName = exportParameter.get();
            log.info("Exporting {} as {}", variableName, variableValue);
            System.setProperty(variableName, variableValue);
        } else {
            log.info("Resulting binary {}", variableValue);
        }
    }

    protected void storeInResolutionCache(String preferenceKey,
            String resolvedDriverVersion, String resolvedBrowserVersion) {
        if (useResolutionCache()) {
            resolutionCache.putValueInResolutionCacheIfEmpty(
                    getKeyForResolutionCache(), resolvedBrowserVersion);
            resolutionCache.putValueInResolutionCacheIfEmpty(preferenceKey,
                    resolvedDriverVersion);
        }
    }

    protected Optional<String> getValueFromResolutionCache(String preferenceKey,
            Optional<String> optionalBrowserVersion) {
        if (useResolutionCache()
                && resolutionCache.checkKeyInResolutionCache(preferenceKey)) {
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
        if (config().isAvoidAutoVersion()) {
            return empty();
        }

        String driverManagerTypeLowerCase = getDriverManagerType().name()
                .toLowerCase();
        Optional<String> optionalBrowserVersion;
        if (useResolutionCache() && resolutionCache
                .checkKeyInResolutionCache(driverManagerTypeLowerCase)) {
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

    protected Optional<String> getDriverVersionFromProperties(String key) {
        // Chromium values are the same than Chrome
        if (key.contains("chromium")) {
            key = key.replace("chromium", "chrome");
        }

        boolean online = config().getVersionsPropertiesOnlineFirst();
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

    protected Properties getVersionFromProperties(boolean online) {
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

    protected InputStream getVersionsInputStream(boolean online)
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

    protected InputStream getLocalVersionsInputStream() {
        InputStream inputStream;
        inputStream = Config.class.getResourceAsStream("/versions.properties");
        return inputStream;
    }

    protected InputStream getOnlineVersionsInputStream() throws IOException {
        return httpClient
                .execute(httpClient
                        .createHttpGet(config().getVersionsPropertiesUrl()))
                .getEntity().getContent();
    }

    protected void handleException(Exception e, String driverVersion) {
        String driverVersionStr = getDriverVersionLabel(driverVersion);
        String errorMessage = String.format(
                "There was an error managing %s %s (%s)", getDriverName(),
                driverVersionStr, e.getMessage());
        if (retryCount == 0) {
            config().setAvoidAutoVersion(true);
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
        UrlHandler urlHandler = new UrlHandler(candidateUrls, driverVersion,
                config().isUseBetaVersions());
        log.trace("All driver URLs: {}", candidateUrls);

        boolean getLatest = isUnknown(driverVersion);
        boolean continueSearchingVersion;
        String shortDriverName = getShortDriverName();

        do {
            // Filter by driver name
            urlHandler.filterByDriverName(shortDriverName);

            // Filter for latest or concrete driver version
            if (getLatest) {
                urlHandler.filterByLatestVersion(this::getCurrentVersion);
            } else {
                urlHandler.filterByVersion(driverVersion);
            }

            log.info("Latest version of {} is {}", shortDriverName,
                    urlHandler.getDriverVersion());
            log.trace("Driver URLs after filtering for version: {}",
                    urlHandler.getCandidateUrls());
            if (urlHandler.hasNoCandidateUrl()) {
                noCandidateUrlFound(driverVersion);
            }

            // Rest of filters
            urlHandler.filterByOs(getDriverName(), config().getOs());
            urlHandler.filterByArch(config().getArchitecture(), forcedArch);
            urlHandler.filterByDistro(config().getOs(), getDriverName());
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

    protected List<URL> getDriversFromXml(URL driverUrl, String xpath)
            throws IOException {
        log.info("Reading {} to seek {}", driverUrl, getDriverName());
        List<URL> urls = new ArrayList<>();
        try {
            try (CloseableHttpResponse response = httpClient
                    .execute(httpClient.createHttpGet(driverUrl))) {
                Document xml = loadXML(response.getEntity().getContent());
                NodeList nodes = (NodeList) newInstance().newXPath()
                        .evaluate(xpath, xml.getDocumentElement(), NODESET);

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

    protected Document loadXML(InputStream inputStream)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
        log.info("Reading {} to seek {}", driverUrl, getDriverName());

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
                && name.toLowerCase().contains(getDriverName());
    }

    protected Optional<String> getDefaultBrowserVersion(
            String[] programFilesEnvs, String[] winBrowserNames,
            String linuxBrowserName, String macBrowserName, String versionFlag,
            String browserNameInOutput) {

        String browserBinaryPath = config().getBinaryPath();
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

    protected String getPosixBrowserPath(String linuxBrowserName,
            String macBrowserName, String browserBinaryPath) {
        if (!isNullOrEmpty(browserBinaryPath)) {
            return browserBinaryPath;
        } else {
            return IS_OS_LINUX ? linuxBrowserName : macBrowserName;
        }
    }

    protected String getBrowserVersionInWindows(String programFilesEnv,
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

    protected Optional<String> getBrowserVersionFromWinRegistry(String key,
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

    protected Charset getVersionCharset() {
        return defaultCharset();
    }

    protected String getLatestVersionLabel() {
        return LATEST_RELEASE;
    }

    protected void reset() {
        config().reset();
        mirrorLog = false;
        forcedArch = false;
        forcedOs = false;
        retryCount = 0;
        isSnap = false;
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
        new Server(port);
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

    protected Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion) {
        String url = driverVersion.isPresent()
                ? getDriverUrl() + LATEST_RELEASE + "_" + driverVersion.get()
                : getDriverUrl() + getLatestVersionLabel();
        Optional<String> result = Optional.empty();
        try (InputStream response = httpClient
                .execute(httpClient.createHttpGet(new URL(url))).getEntity()
                .getContent()) {
            result = Optional.of(IOUtils.toString(response, getVersionCharset())
                    .replaceAll("\r\n", ""));
        } catch (Exception e) {
            log.warn("Exception reading {} to get latest version of {} ({})",
                    url, getDriverName(), e.getMessage());
        }
        if (result.isPresent()) {
            log.debug("Latest version of {} according to {} is {}",
                    getDriverName(), url, result.get());
        }
        return result;
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
