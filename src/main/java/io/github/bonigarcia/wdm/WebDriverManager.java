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

import static io.github.bonigarcia.wdm.Architecture.X32;
import static io.github.bonigarcia.wdm.Architecture.X64;
import static io.github.bonigarcia.wdm.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.DriverManagerType.IEXPLORER;
import static io.github.bonigarcia.wdm.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.DriverManagerType.PHANTOMJS;
import static io.github.bonigarcia.wdm.DriverManagerType.SELENIUM_SERVER_STANDALONE;
import static io.github.bonigarcia.wdm.OperatingSystem.WIN;
import static io.github.bonigarcia.wdm.Shell.getVersionFromPosixOutput;
import static io.github.bonigarcia.wdm.Shell.getVersionFromWmicOutput;
import static io.github.bonigarcia.wdm.Shell.runAndWait;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.signum;
import static java.lang.Integer.valueOf;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static java.util.Optional.empty;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathFactory.newInstance;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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

/**
 * Parent driver manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.0
 */
public abstract class WebDriverManager {

    static final Logger log = getLogger(lookup().lookupClass());

    protected static final String SLASH = "/";

    protected abstract List<URL> getDrivers() throws IOException;

    protected abstract Optional<String> getBrowserVersion();

    protected static Map<DriverManagerType, WebDriverManager> instanceMap = new EnumMap<>(
            DriverManagerType.class);
    protected static Config config;
    protected HttpClient httpClient;
    protected Downloader downloader;
    protected UrlFilter urlFilter;
    protected String versionToDownload;
    protected String downloadedVersion;
    protected String latestVersion;
    protected DriverManagerType driverManagerType;
    protected String binaryPath;
    protected boolean mirrorLog;
    protected List<String> listVersions;
    protected String driverName;
    protected String driverVersionKey;
    protected String driverUrlKey;
    protected String driverMirrorUrlKey;
    protected String exportParameterKey;
    protected boolean forcedArch;

    public static synchronized Config config() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    public static synchronized WebDriverManager chromedriver() {
        if (!instanceMap.containsKey(CHROME)) {
            instanceMap.put(CHROME, new ChromeDriverManager());
        }
        return instanceMap.get(CHROME);
    }

    public static synchronized WebDriverManager firefoxdriver() {
        if (!instanceMap.containsKey(FIREFOX)) {
            instanceMap.put(FIREFOX, new FirefoxDriverManager());
        }
        return instanceMap.get(FIREFOX);
    }

    public static synchronized WebDriverManager operadriver() {
        if (!instanceMap.containsKey(OPERA)) {
            instanceMap.put(OPERA, new OperaDriverManager());
        }
        return instanceMap.get(OPERA);
    }

    public static synchronized WebDriverManager edgedriver() {
        if (!instanceMap.containsKey(EDGE)) {
            instanceMap.put(EDGE, new EdgeDriverManager());
        }
        return instanceMap.get(EDGE);
    }

    public static synchronized WebDriverManager iedriver() {
        if (!instanceMap.containsKey(IEXPLORER)) {
            instanceMap.put(IEXPLORER, new InternetExplorerDriverManager());
        }
        return instanceMap.get(IEXPLORER);
    }

    public static synchronized WebDriverManager phantomjs() {
        if (!instanceMap.containsKey(PHANTOMJS)) {
            instanceMap.put(PHANTOMJS, new PhantomJsDriverManager());
        }
        return instanceMap.get(PHANTOMJS);
    }

    public static synchronized WebDriverManager seleniumServerStandalone() {
        if (!instanceMap.containsKey(SELENIUM_SERVER_STANDALONE)) {
            instanceMap.put(SELENIUM_SERVER_STANDALONE,
                    new SeleniumServerStandaloneManager());
        }
        return instanceMap.get(SELENIUM_SERVER_STANDALONE);
    }

    protected static synchronized WebDriverManager voiddriver() {
        return new WebDriverManager() {
            @Override
            protected List<URL> getDrivers() throws IOException {
                return emptyList();
            }

            @Override
            protected Optional<String> getBrowserVersion() {
                return empty();
            }
        };
    }

    public static synchronized WebDriverManager getInstance(
            DriverManagerType driverManagerType) {
        if (driverManagerType == null) {
            return voiddriver();
        }
        switch (driverManagerType) {
        case CHROME:
            return chromedriver();
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
        if (driverManagerType != null) {
            try {
                manage(config().getArchitecture(),
                        config().getDriverVersion(driverVersionKey));
            } finally {
                reset();
            }
        }
    }

    public WebDriverManager version(String version) {
        config().setDriverVersion(version);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager architecture(Architecture architecture) {
        config().setArchitecture(architecture);
        forcedArch = true;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager arch32() {
        architecture(X32);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager arch64() {
        architecture(X64);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager operatingSystem(OperatingSystem os) {
        config().setOs(os.name());
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager forceCache() {
        config().setForceCache(true);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager forceDownload() {
        config().setOverride(true);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager driverRepositoryUrl(URL url) {
        config().setDriverUrl(url);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager useMirror() {
        config().setUseMirror(true);
        if (config.getUseMirror(driverMirrorUrlKey)) {
            config().setDriverUrl(config().getDriverUrl(driverMirrorUrlKey));
        }
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager proxy(String proxy) {
        config().setProxy(proxy);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager proxyUser(String proxyUser) {
        config().setProxyUser(proxyUser);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager proxyPass(String proxyPass) {
        config().setProxyPass(proxyPass);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager useBetaVersions() {
        config().setUseBetaVersions(true);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager ignoreVersions(String... versions) {
        config().setIgnoreVersions(versions);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager gitHubTokenName(String gitHubTokenName) {
        config().setGitHubTokenName(gitHubTokenName);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager gitHubTokenSecret(String gitHubTokenSecret) {
        config().setGitHubTokenSecret(gitHubTokenSecret);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager timeout(int timeout) {
        config().setTimeout(timeout);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager properties(String properties) {
        config().setProperties(properties);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager targetPath(String targetPath) {
        config().setTargetPath(targetPath);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager avoidExport() {
        config().setAvoidExport(true);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager avoidOutputTree() {
        config().setAvoidOutputTree(true);
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager avoidAutoVersion() {
        config().setAvoidAutoVersion(true);
        return instanceMap.get(driverManagerType);
    }

    // ------------

    public String getBinaryPath() {
        return instanceMap.get(driverManagerType).binaryPath;
    }

    public String getDownloadedVersion() {
        return instanceMap.get(driverManagerType).downloadedVersion;
    }

    public List<String> getVersions() {
        httpClient = new HttpClient(config().getTimeout());
        try {
            List<URL> drivers = getDrivers();
            List<String> versions = new ArrayList<>();
            for (URL url : drivers) {
                String version = getCurrentVersion(url, driverName);
                if (version.isEmpty() || version.equalsIgnoreCase("icons")) {
                    continue;
                }
                if (version.startsWith(".")) {
                    version = version.substring(1);
                }
                if (!versions.contains(version)) {
                    versions.add(version);
                }
            }
            log.trace("Version list before sorting {}", versions);
            sort(versions, new VersionComparator());
            return versions;
        } catch (IOException e) {
            throw new WebDriverManagerException(e);
        }
    }

    // ------------

    protected String preDownload(String target, String version) {
        log.trace("Pre-download. target={}, version={}", target, version);
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
        throw new WebDriverManagerException("Driver " + driverName
                + " not found (using temporal folder " + parentFolder + ")");
    }

    protected String getCurrentVersion(URL url, String driverName) {
        String currentVersion = "";
        try {
            currentVersion = url.getFile().substring(
                    url.getFile().indexOf(SLASH) + 1,
                    url.getFile().lastIndexOf(SLASH));
        } catch (StringIndexOutOfBoundsException e) {
            log.trace("Exception getting version of URL {} ({})", url,
                    e.getMessage());
        }

        return currentVersion;
    }

    protected void manage(Architecture arch, String version) {
        httpClient = new HttpClient(config().getTimeout());
        try (HttpClient wdmHttpClient = httpClient) {
            downloader = new Downloader(driverManagerType);
            urlFilter = new UrlFilter();

            boolean getLatest = isNullOrEmpty(version)
                    || version.equalsIgnoreCase("latest");
            boolean cache = config().isForceCache() || !isNetAvailable();
            if (getLatest && !config().isAvoidAutoVersion()) {
                version = getVersionForInstalledBrowser(driverManagerType);
                getLatest = version.isEmpty();
            }

            String os = config().getOs();
            log.trace("Managing {} arch={} version={} getLatest={} cache={}",
                    driverName, arch, version, getLatest, cache);

            if (getLatest && latestVersion != null) {
                log.debug("Latest version of {} is {} (recently resolved)",
                        driverName, latestVersion);
                version = latestVersion;
                cache = true;
            }

            Optional<String> driverInCache = handleCache(arch, version, os,
                    getLatest, cache);

            String versionStr = getLatest ? "(latest version)" : version;
            if (driverInCache.isPresent() && !config().isOverride()) {
                versionToDownload = version;
                downloadedVersion = version;
                log.debug("Driver {} {} found in cache", driverName,
                        versionStr);
                exportDriver(driverInCache.get());
            } else {
                List<URL> candidateUrls = filterCandidateUrls(arch, version,
                        getLatest);

                if (candidateUrls.isEmpty()) {
                    String errorMessage = driverName + " " + versionStr
                            + " for " + os + arch.toString() + " not found in "
                            + config().getDriverUrl(driverUrlKey);
                    log.error(errorMessage);
                    throw new WebDriverManagerException(errorMessage);
                }

                downloadCandidateUrls(candidateUrls);
            }

        } catch (Exception e) {
            handleException(e, arch, version);
        }
    }

    private String getVersionForInstalledBrowser(DriverManagerType driverManagerType) {
        return getBrowserVersion().map(browserVersion ->
            getDriverVersionForBrowser(driverManagerType, browserVersion).map(version -> {
                log.info(
                    "Using {} {} (since {} {} is installed in your machine)",
                    driverName, version, driverManagerType, browserVersion);
                return version;
            }).orElseGet(() -> {
                log.warn(
                    "The driver version for {} {} is unknown ... trying with latest",
                    driverManagerType, browserVersion);
                return "";
            })
        ).orElse("");
    }

    private Optional<String> getDriverVersionForBrowser(DriverManagerType driverManagerType, String browserVersion) {
        String key = driverManagerType.name().toLowerCase() + browserVersion;
        String value = getVersionsDescription().getProperty(key);
        return value == null ? empty() : Optional.of(value);
    }

    private Properties getVersionsDescription() {
        try (InputStream inputStream = Config.class.getResourceAsStream("/versions.properties")) {
            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find file /versions.properties in classpath", e);
        }
    }

    protected void handleException(Exception e, Architecture arch,
            String version) {
        String errorMessage = String.format(
                "There was an error managing %s %s (%s)", driverName, version,
                e.getMessage());
        if (!config().isForceCache()) {
            config().setForceCache(true);
            log.warn("{} ... trying again forcing to use cache", errorMessage,
                    e);
            manage(arch, version);
        } else {
            log.error("{}", errorMessage, e);
            throw new WebDriverManagerException(e);
        }
    }

    protected void downloadCandidateUrls(List<URL> candidateUrls)
            throws IOException, InterruptedException {
        URL url = candidateUrls.iterator().next();

        String exportValue = downloader.download(url, versionToDownload,
                getDriverName());
        exportDriver(exportValue);
        downloadedVersion = versionToDownload;
    }

    protected List<URL> filterCandidateUrls(Architecture arch, String version,
            boolean getLatest) throws IOException {
        List<URL> urls = getDrivers();
        List<URL> candidateUrls;
        log.trace("All URLs: {}", urls);

        boolean continueSearchingVersion;
        do {
            // Get the latest or concrete version
            candidateUrls = getLatest ? getLatest(urls, getDriverName())
                    : getVersion(urls, getDriverName(), version);
            log.trace("Candidate URLs: {}", candidateUrls);
            if (versionToDownload == null
                    || this.getClass().equals(EdgeDriverManager.class)) {
                break;
            }

            // Filter by OS
            if (!getDriverName().equalsIgnoreCase("IEDriverServer")
                    && !getDriverName()
                            .equalsIgnoreCase("selenium-server-standalone")) {
                candidateUrls = urlFilter.filterByOs(candidateUrls,
                        config().getOs());
            }

            // Filter by architecture
            candidateUrls = urlFilter.filterByArch(candidateUrls, arch,
                    forcedArch);

            // Filter by distro
            candidateUrls = filterByDistro(candidateUrls);

            // Filter by ignored versions
            candidateUrls = filterByIgnoredVersions(candidateUrls);

            // Find out if driver version has been found or not
            continueSearchingVersion = candidateUrls.isEmpty() && getLatest;
            if (continueSearchingVersion) {
                log.info(
                        "No binary found for {} {} ... seeking another version",
                        driverName, versionToDownload);
                urls = removeFromList(urls, versionToDownload);
                versionToDownload = null;
            }
        } while (continueSearchingVersion);
        return candidateUrls;
    }

    protected List<URL> filterByIgnoredVersions(List<URL> candidateUrls) {
        if (config().getIgnoreVersions() != null && !candidateUrls.isEmpty()) {
            candidateUrls = urlFilter.filterByIgnoredVersions(candidateUrls,
                    config().getIgnoreVersions());
        }
        return candidateUrls;
    }

    protected List<URL> filterByDistro(List<URL> candidateUrls)
            throws IOException {
        // Filter phantomjs 2.5.0 in Linux
        if (config().getOs().equalsIgnoreCase("linux")
                && getDriverName().contains("phantomjs")) {
            candidateUrls = urlFilter.filterByDistro(candidateUrls, "2.5.0");
        }
        return candidateUrls;
    }

    protected Optional<String> handleCache(Architecture arch, String version,
            String os, boolean getLatest, boolean cache) {
        Optional<String> driverInCache = empty();
        if (cache || !getLatest) {
            driverInCache = getDriverFromCache(version, arch, os);
        }
        if (!version.isEmpty()) {
            versionToDownload = version;
        }
        return driverInCache;
    }

    protected Optional<String> getDriverFromCache(String driverVersion,
            Architecture arch, String os) {
        log.trace("Checking if {} exists in cache", driverName);
        List<File> filesInCache = getFilesInCache();
        if (!filesInCache.isEmpty()) {
            // Filter by name
            filesInCache = filterCacheBy(filesInCache, driverName);

            // Filter by version
            filesInCache = filterCacheBy(filesInCache, driverVersion);

            // Filter by OS
            if (!driverName.equals("MicrosoftWebDriver")) {
                filesInCache = filterCacheBy(filesInCache, os.toLowerCase());
            }

            if (filesInCache.size() == 1) {
                return Optional.of(filesInCache.get(0).toString());
            }

            // Filter by arch
            filesInCache = filterCacheBy(filesInCache, arch.name());

            if (!filesInCache.isEmpty()) {
                return Optional.of(filesInCache.get(0).toString());
            }
        }

        log.debug("{} not found in cache", driverName);
        return empty();
    }

    protected List<File> filterCacheBy(List<File> input, String key) {
        List<File> output = new ArrayList<>(input);
        if (!key.isEmpty() && !input.isEmpty()) {
            for (File f : input) {
                if (!f.toString().contains(key)) {
                    output.remove(f);
                }
            }
        }
        log.trace("Filter cache by {} -- input list {} -- output list {} ", key,
                input, output);
        return output;
    }

    protected List<File> getFilesInCache() {
        return (List<File>) listFiles(new File(downloader.getTargetPath()),
                null, true);
    }

    protected boolean isNetAvailable() {
        try {
            if (!httpClient.isValid(config().getDriverUrl(driverUrlKey))) {
                log.warn("Page not available. Forcing the use of cache");
                return false;
            }
        } catch (IOException e) {
            log.warn("Network not available. Forcing the use of cache");
            return false;
        }
        return true;
    }

    protected List<URL> removeFromList(List<URL> list, String version) {
        List<URL> out = new ArrayList<>(list);
        for (URL url : list) {
            if (url.getFile().contains(version)) {
                out.remove(url);
            }
        }
        return out;
    }

    protected List<URL> getVersion(List<URL> list, String driver,
            String version) {
        List<URL> out = new ArrayList<>();
        if (getDriverName().contains("MicrosoftWebDriver")) {
            int i = listVersions.indexOf(version);
            if (i != -1) {
                out.add(list.get(i));
            }
        }

        for (URL url : list) {
            if (url.getFile().contains(driver)
                    && url.getFile().contains(version)
                    && !url.getFile().contains("-symbols")) {
                out.add(url);
            }
        }
        versionToDownload = version;
        log.debug("Using {} {}", driver, version);
        return out;
    }

    protected List<URL> getLatest(List<URL> list, String driver) {
        log.trace("Checking the lastest version of {} with URL list {}", driver,
                list);
        List<URL> out = new ArrayList<>();
        List<URL> copyOfList = new ArrayList<>(list);

        for (URL url : copyOfList) {
            try {
                handleDriver(url, driver, out);
            } catch (Exception e) {
                log.trace("There was a problem with URL {} : {}", url,
                        e.getMessage());
                list.remove(url);
            }
        }
        if (versionToDownload != null && versionToDownload.startsWith(".")) {
            versionToDownload = versionToDownload.substring(1);
        }
        latestVersion = versionToDownload;
        log.info("Latest version of {} is {}", driver, versionToDownload);
        return out;
    }

    protected void handleDriver(URL url, String driver, List<URL> out) {
        if (!config().isUseBetaVersions()
                && url.getFile().toLowerCase().contains("beta")) {
            return;
        }
        if (url.getFile().contains(driver)) {
            String currentVersion = getCurrentVersion(url, driver);

            if (currentVersion.equalsIgnoreCase(driver)) {
                return;
            }
            if (versionToDownload == null) {
                versionToDownload = currentVersion;
            }
            if (versionCompare(currentVersion, versionToDownload) > 0) {
                versionToDownload = currentVersion;
                out.clear();
            }
            if (url.getFile().contains(versionToDownload)) {
                out.add(url);
            }
        }
    }

    protected boolean isUsingTaobaoMirror() {
        return config().getDriverUrl(driverUrlKey).getHost()
                .equalsIgnoreCase("npm.taobao.org");
    }

    protected Integer versionCompare(String str1, String str2) {
        String[] vals1 = str1.replaceAll("v", "").split("\\.");
        String[] vals2 = str2.replaceAll("v", "").split("\\.");

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

    /**
     * This method works also for http://npm.taobao.org/ and
     * https://bitbucket.org/ mirrors.
     */
    protected List<URL> getDriversFromMirror(URL driverUrl) throws IOException {
        if (mirrorLog) {
            log.info("Crawling driver list from mirror {}", driverUrl);
            mirrorLog = true;
        } else {
            log.trace("[Recursive call] Crawling driver list from mirror {}",
                    driverUrl);
        }

        String driverStr = driverUrl.toString();
        String driverUrlContent = driverUrl.getPath();

        HttpResponse response = httpClient
                .execute(httpClient.createHttpGet(driverUrl));
        try (InputStream in = response.getEntity().getContent()) {
            org.jsoup.nodes.Document doc = Jsoup.parse(in, null, "");
            Iterator<org.jsoup.nodes.Element> iterator = doc.select("a")
                    .iterator();
            List<URL> urlList = new ArrayList<>();

            while (iterator.hasNext()) {
                String link = iterator.next().attr("href");
                if (link.contains("mirror") && link.endsWith(SLASH)) {
                    urlList.addAll(getDriversFromMirror(new URL(
                            driverStr + link.replace(driverUrlContent, ""))));
                } else if (link.startsWith(driverUrlContent)
                        && !link.contains("icons")) {
                    urlList.add(new URL(
                            driverStr + link.replace(driverUrlContent, "")));
                }
            }
            return urlList;
        }
    }

    protected List<URL> getDriversFromXml(URL driverUrl) throws IOException {
        log.info("Reading {} to seek {}", driverUrl, driverName);
        List<URL> urls = new ArrayList<>();
        HttpResponse response = httpClient
                .execute(httpClient.createHttpGet(driverUrl));
        try {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()))) {
                Document xml = loadXML(reader);
                NodeList nodes = (NodeList) newInstance().newXPath().evaluate(
                        "//Contents/Key", xml.getDocumentElement(), NODESET);

                for (int i = 0; i < nodes.getLength(); ++i) {
                    Element e = (Element) nodes.item(i);
                    urls.add(new URL(driverUrl
                            + e.getChildNodes().item(0).getNodeValue()));
                }
            }
        } catch (Exception e) {
            throw new WebDriverManagerException(e);
        }
        return urls;
    }

    protected Document loadXML(Reader reader)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(reader);
        return builder.parse(is);
    }

    protected void exportDriver(String variableValue) {
        binaryPath = variableValue;
        if (!config.isAvoidExport() && exportParameterKey != null) {
            String variableName = config().getDriverExport(exportParameterKey);
            log.info("Exporting {} as {}", variableName, variableValue);
            System.setProperty(variableName, variableValue);
        } else {
            log.info("Resulting binary {}", variableValue);
        }
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
        URL driverUrl = config().getDriverUrl(driverUrlKey);
        log.info("Reading {} to seek {}", driverUrl, driverName);

        if (isUsingTaobaoMirror()) {
            urls = getDriversFromMirror(driverUrl);

        } else {
            String driverVersion = versionToDownload;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(openGitHubConnection(driverUrl)))) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                GitHubApi[] releaseArray = gson.fromJson(reader,
                        GitHubApi[].class);

                if (driverVersion != null) {
                    releaseArray = new GitHubApi[] {
                            getVersion(releaseArray, driverVersion) };
                }

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

    protected GitHubApi getVersion(GitHubApi[] releaseArray, String version) {
        GitHubApi out = null;
        for (GitHubApi release : releaseArray) {
            log.trace("Get version {} of {}", version, release);
            if ((release.getName() != null
                    && release.getName().contains(version))
                    || (release.getTagName() != null
                            && release.getTagName().contains(version))) {
                out = release;
                break;
            }
        }
        return out;
    }

    protected String getDriverName() {
        return driverName;
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    protected FilenameFilter getFolderFilter() {
        return (dir, name) -> dir.isDirectory()
                && name.toLowerCase().contains(driverName);
    }

    protected Optional<String> getDefaultBrowserVersion(String programFilesEnv,
            String winBrowserName, String linuxBrowserName,
            String macBrowserName, String versionFlag,
            String browserNameInOutput) {
        if (IS_OS_WINDOWS) {
            String programFiles = System.getenv(programFilesEnv)
                    .replaceAll("\\\\", "\\\\\\\\");
            String browserVersionOutput = runAndWait("wmic", "datafile",
                    "where", "name='" + programFiles + winBrowserName + "'",
                    "get", "Version", "/value");
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional
                        .of(getVersionFromWmicOutput(browserVersionOutput));
            }
        } else if (IS_OS_LINUX || IS_OS_MAC) {
            String browserName = IS_OS_LINUX ? linuxBrowserName
                    : macBrowserName;
            String browserVersionOutput = runAndWait(browserName, versionFlag);
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional.of(getVersionFromPosixOutput(
                        browserVersionOutput, browserNameInOutput));
            }
        }
        return empty();
    }

    protected void reset() {
        config().reset();
        mirrorLog = false;
        listVersions = null;
        versionToDownload = null;
        forcedArch = false;
    }

    protected String getProgramFilesEnv() {
        return System.getProperty("os.arch").contains("64")
                ? "PROGRAMFILES(X86)"
                : "PROGRAMFILES";
    }

    public static void main(String[] args) {
        String validBrowsers = "chrome|firefox|opera|edge|phantomjs|iexplorer";
        if (args.length <= 0) {
            logCliError(validBrowsers);
        } else {
            String arg = args[0];
            if (arg.equalsIgnoreCase("server")) {
                startServer(args);
            } else {
                resolveLocal(validBrowsers, arg);
            }
        }
    }

    private static void resolveLocal(String validBrowsers, String arg) {
        log.info("Using WebDriverManager to resolve {}", arg);
        try {
            DriverManagerType driverManagerType = DriverManagerType
                    .valueOf(arg.toUpperCase());
            WebDriverManager wdm = WebDriverManager
                    .getInstance(driverManagerType).avoidExport()
                    .targetPath(".").forceDownload();
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

    private static void startServer(String[] args) {
        int port = config().getServerPort();
        if (args.length > 1 && isNumeric(args[1])) {
            port = parseInt(args[1]);
        }
        new Server(port);
    }

    private static void logCliError(String validBrowsers) {
        log.error("There are 2 options to run WebDriverManager CLI");
        log.error(
                "1. WebDriverManager used to resolve binary drivers locally:");
        log.error("\tWebDriverManager browserName");
        log.error("\t(where browserName={})", validBrowsers);

        log.error("2. WebDriverManager as a server:");
        log.error("\tWebDriverManager server <port>");
        log.error("\t(where default port is 4041)");
    }

}