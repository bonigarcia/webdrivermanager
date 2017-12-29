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
import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.DriverManagerType.IEXPLORER;
import static io.github.bonigarcia.wdm.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.DriverManagerType.PHANTOMJS;
import static io.github.bonigarcia.wdm.DriverManagerType.VOID;
import static io.github.bonigarcia.wdm.DriverVersion.LATEST;
import static io.github.bonigarcia.wdm.DriverVersion.NOT_SPECIFIED;
import static io.github.bonigarcia.wdm.OperativeSystem.LINUX;
import static io.github.bonigarcia.wdm.OperativeSystem.MAC;
import static io.github.bonigarcia.wdm.OperativeSystem.WIN;
import static io.github.bonigarcia.wdm.WdmConfig.getBoolean;
import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static io.github.bonigarcia.wdm.WdmConfig.getUrl;
import static io.github.bonigarcia.wdm.WdmConfig.isNullOrEmpty;
import static java.lang.Integer.signum;
import static java.lang.Integer.valueOf;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.sort;
import static java.util.Collections.reverse;
import static java.util.Collections.reverseOrder;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathFactory.newInstance;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    final Logger log = getLogger(lookup().lookupClass());

    public static final String SLASH = "/";

    static Map<DriverManagerType, WebDriverManager> instanceMap = new HashMap<>();

    DriverManagerType driverManagerType;

    protected abstract List<URL> getDrivers() throws IOException;

    protected static WebDriverManager instance;
    protected String myOsName = defaultOsName();
    protected boolean useBetaVersions = false;
    protected boolean mirrorLog = false;
    protected boolean isForcingCache = false;
    protected boolean isForcingDownload = false;
    protected List<String> listVersions;
    protected List<String> driverName;
    protected Architecture architecture;
    protected HttpClient httpClient;
    protected Downloader downloader;
    protected UrlFilter urlFilter;
    protected URL driverUrl;
    protected String versionToDownload;
    protected String downloadedVersion;
    protected String version;
    protected String proxyValue;
    protected String binaryPath;
    protected String proxyUser;
    protected String proxyPass;
    protected String exportParameter;
    protected String driverVersionKey;
    protected String driverUrlKey;
    protected String[] ignoredVersions;

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

    public static synchronized WebDriverManager getInstance(
            Class<?> webDriverClass) {
        switch (webDriverClass.getName()) {
        case "org.openqa.selenium.chrome.ChromeDriver":
            instance = chromedriver();
            break;
        case "org.openqa.selenium.firefox.FirefoxDriver":
            instance = firefoxdriver();
            break;
        case "org.openqa.selenium.opera.OperaDriver":
            instance = operadriver();
            break;
        case "org.openqa.selenium.ie.InternetExplorerDriver":
            instance = iedriver();
            break;
        case "org.openqa.selenium.edge.EdgeDriver":
            instance = edgedriver();
            break;

        case "org.openqa.selenium.phantomjs.PhantomJSDriver":
            instance = phantomjs();
            break;
        default:
            instance = voiddriver();
            break;
        }
        return instance;
    }

    public synchronized void setup() {
        if (driverManagerType != VOID) {
            String driverVersion = getDriverVersion();
            manage(getDefaultArchitecture(),
                    isNullOrEmpty(driverVersion) ? NOT_SPECIFIED.name()
                            : driverVersion);
            reset();
        }
    }

    public WebDriverManager version(String version) {
        this.version = version;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager architecture(Architecture architecture) {
        this.architecture = architecture;
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

    public WebDriverManager operativeSystem(OperativeSystem os) {
        this.myOsName = os.name();
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager forceCache() {
        this.isForcingCache = true;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager forceDownload() {
        this.isForcingDownload = true;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager driverRepositoryUrl(URL url) {
        this.driverUrl = url;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager useTaobaoMirror() {
        String errorMessage = "Binaries for " + getDriverName()
                + " not available in taobao.org mirror (http://npm.taobao.org/mirrors/)";
        log.error(errorMessage);
        throw new WebDriverManagerException(errorMessage);
    }

    public WebDriverManager useTaobaoMirror(String taobaoUrl) {
        driverUrl = getUrl(taobaoUrl);
        return instance;
    }

    public WebDriverManager proxy(String proxy) {
        this.proxyValue = proxy;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager proxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager proxyPass(String proxyPass) {
        this.proxyPass = proxyPass;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager useBetaVersions() {
        this.useBetaVersions = true;
        return instanceMap.get(driverManagerType);
    }

    public WebDriverManager ignoreVersions(String... versions) {
        this.ignoredVersions = versions;
        return instanceMap.get(driverManagerType);
    }

    public String getBinaryPath() {
        return instanceMap.get(driverManagerType).binaryPath;
    }

    public String getDownloadedVersion() {
        return instanceMap.get(driverManagerType).downloadedVersion;
    }

    // ------------

    protected static synchronized WebDriverManager voiddriver() {
        if (!instanceMap.containsKey(VOID)) {
            instanceMap.put(VOID, new VoidDriverManager());
        }
        return instanceMap.get(VOID);
    }

    protected String getDriverVersion() {
        return version == null ? getString(getDriverVersionKey()) : version;
    }

    protected URL getDriverUrl() {
        return driverUrl == null ? getUrl(getDriverUrlKey()) : driverUrl;
    }

    protected String preDownload(String target, String version) {
        log.trace("Pre-download. target={}, version={}", target, version);
        return target;
    }

    protected File postDownload(File archive) {
        File target = archive;
        File[] ls = archive.getParentFile().listFiles();
        for (File f : ls) {
            if (isExecutable(f)) {
                target = f;
                log.trace("Found binary in post-download: {}", target);
                break;
            }
        }
        return target;
    }

    protected String getCurrentVersion(URL url, String driverName) {
        return url.getFile().substring(url.getFile().indexOf(SLASH) + 1,
                url.getFile().lastIndexOf(SLASH));
    }

    protected void manage(Architecture arch, String version) {
        httpClient = new HttpClient(proxyValue, proxyUser, proxyPass);
        try (HttpClient wdmHttpClient = httpClient) {
            downloader = new Downloader(this, wdmHttpClient);
            urlFilter = new UrlFilter();
            if (isForcingDownload) {
                downloader.forceDownload();
            }
            updateValuesWithConfig();

            boolean getLatest = version == null || version.isEmpty()
                    || version.equalsIgnoreCase(LATEST.name())
                    || version.equalsIgnoreCase(NOT_SPECIFIED.name());
            boolean cache = this.isForcingCache || getBoolean("wdm.forceCache")
                    || !isNetAvailable();

            log.trace(">> Managing {} arch={} version={} getLatest={} cache={}",
                    getDriverName(), arch, version, getLatest, cache);

            Optional<String> driverInCache = handleCache(arch, version,
                    getLatest, cache);

            if (driverInCache.isPresent()) {
                versionToDownload = version;
                downloadedVersion = version;
                log.debug("Driver for {} {} found in cache {}", getDriverName(),
                        versionToDownload, driverInCache.get());
                exportDriver(getExportParameter(), driverInCache.get());

            } else {
                List<URL> candidateUrls = filterCandidateUrls(arch, version,
                        getLatest);

                if (candidateUrls.isEmpty()) {
                    String versionStr = getLatest ? "(latest version)"
                            : version;
                    String errorMessage = getDriverName() + " " + versionStr
                            + " for " + myOsName + arch.toString()
                            + " not found in " + getDriverUrl();
                    log.error(errorMessage);
                    throw new WebDriverManagerException(errorMessage);
                }

                downloadCandidateUrls(candidateUrls);
            }

        } catch (Exception e) {
            handleException(e, arch, version);
        }
    }

    protected void updateValuesWithConfig() {
        String wdmForceOs = getString("wdm.forceOs");
        if (!wdmForceOs.equals("")) {
            myOsName = wdmForceOs;
        }
        String wdmProxy = getString("wdm.proxy");
        if (!wdmProxy.equals("")) {
            proxyValue = wdmProxy;
        }
        String wdmProxyUser = getString("wdm.proxyUser");
        if (!wdmProxyUser.equals("")) {
            proxyUser = wdmProxyUser;
        }
        String wdmProxyPass = getString("wdm.proxyPass");
        if (!wdmProxyPass.equals("")) {
            proxyPass = wdmProxyPass;
        }
        if (getBoolean("wdm.useTaobaoMirror")) {
            useTaobaoMirror();
        }
    }

    protected void handleException(Exception e, Architecture arch,
            String version) {
        if (!isForcingCache) {
            isForcingCache = true;
            log.warn(
                    "There was an error managing {} {} ({}) ... trying again forcing to use cache",
                    getDriverName(), version, e.getMessage());
            manage(arch, version);
        } else {
            throw new WebDriverManagerException(e);
        }
    }

    protected void downloadCandidateUrls(List<URL> candidateUrls)
            throws IOException, InterruptedException {
        reverse(candidateUrls);
        URL url = candidateUrls.iterator().next();
        String export = candidateUrls.contains(url) ? getExportParameter()
                : null;
        downloader.download(url, versionToDownload, export, getDriverName());
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

            // Filter by architecture and OS
            candidateUrls = urlFilter.filterByOs(candidateUrls, myOsName);
            candidateUrls = urlFilter.filterByArch(candidateUrls, arch);

            // Extra round of filter phantomjs 2.5.0 in Linux
            if (myOsName.equalsIgnoreCase("linux")
                    && getDriverName().contains("phantomjs")) {
                candidateUrls = urlFilter.filterByDistro(candidateUrls,
                        "2.5.0");
            }

            // Filter by ignored version
            if (ignoredVersions != null) {
                candidateUrls = urlFilter.filterByIgnoredVersions(candidateUrls,
                        ignoredVersions);
            }

            // Find out if driver version has been found or not
            continueSearchingVersion = candidateUrls.isEmpty() && getLatest;
            if (continueSearchingVersion) {
                log.info(
                        "No binary found for {} {} ... seeking another version",
                        getDriverName(), versionToDownload);
                urls = removeFromList(urls, versionToDownload);
                versionToDownload = null;
            }
        } while (continueSearchingVersion);
        return candidateUrls;
    }

    protected Optional<String> handleCache(Architecture arch, String version,
            boolean getLatest, boolean cache) {
        Optional<String> driverInCache = empty();
        if (cache) {
            driverInCache = forceCache(downloader.getTargetPath());
        } else if (!getLatest) {
            versionToDownload = version;
            driverInCache = existsDriverInCache(downloader.getTargetPath(),
                    version, arch);
        }
        return driverInCache;
    }

    protected Optional<String> forceCache(String repository) {
        String driverInCache = null;
        for (String driver : getDriverName()) {
            log.trace("Checking if {} exists in cache {}", driver, repository);

            Collection<File> listFiles = listFiles(new File(repository), null,
                    true);
            Object[] array = listFiles.toArray();
            sort(array, reverseOrder());

            for (Object f : array) {
                driverInCache = f.toString();
                log.trace("Checking {}", driverInCache);
                if (driverInCache.contains(driver)
                        && isExecutable(new File(driverInCache))) {
                    log.info("Found {} in cache: {} ", driver, driverInCache);
                    return of(driverInCache);
                }
            }
        }
        return empty();
    }

    protected Optional<String> existsDriverInCache(String repository,
            String driverVersion, Architecture arch) {
        String driverInCache = null;
        for (String driver : getDriverName()) {
            log.trace("Checking if {} {} ({} bits) exists in cache {}", driver,
                    driverVersion, arch, repository);

            Collection<File> listFiles = listFiles(new File(repository), null,
                    true);
            Object[] array = listFiles.toArray();
            sort(array, reverseOrder());

            for (Object f : array) {
                driverInCache = f.toString();
                boolean checkArchitecture = !shouldCheckArchitecture()
                        || driverInCache.contains(arch.toString());
                log.trace("Checking {}", driverInCache);

                if (driverInCache.contains(driverVersion)
                        && driverInCache.contains(driver) && checkArchitecture
                        && isExecutable(new File(driverInCache))) {
                    log.debug("Found {} {} ({} bits) in cache: {}",
                            driverVersion, driver, arch, driverInCache);
                    return of(driverInCache);
                }
            }
        }
        return empty();
    }

    protected boolean isExecutable(File file) {
        return myOsName.equalsIgnoreCase("win")
                ? file.getName().toLowerCase().endsWith(".exe")
                : file.canExecute();
    }

    protected boolean shouldCheckArchitecture() {
        return true;
    }

    protected boolean isNetAvailable() {
        try {
            if (!httpClient.isValid(getDriverUrl())) {
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

    protected List<URL> getVersion(List<URL> list, List<String> match,
            String version) {
        List<URL> out = new ArrayList<>();
        if (getDriverName().contains("MicrosoftWebDriver")) {
            int i = listVersions.indexOf(version);
            if (i != -1) {
                out.add(list.get(i));
            }
        }

        for (String s : match) {
            reverse(list);
            for (URL url : list) {
                if (url.getFile().contains(s) && url.getFile().contains(version)
                        && !url.getFile().contains("-symbols")) {
                    out.add(url);
                }
            }
        }
        versionToDownload = version;
        log.debug("Using {} {}", match, version);
        return out;
    }

    protected List<URL> getLatest(List<URL> list, List<String> match) {
        log.trace("Checking the lastest version of {} with URL list {}", match,
                list);
        List<URL> out = new ArrayList<>();
        reverse(list);
        List<URL> copyOfList = new ArrayList<>(list);

        for (URL url : copyOfList) {
            for (String driver : match) {
                try {
                    handleDriver(url, driver, out);
                } catch (Exception e) {
                    log.trace("There was a problem with URL {} : {}",
                            url.toString(), e.getMessage());
                    list.remove(url);
                }
            }
        }
        if (versionToDownload.startsWith(".")) {
            versionToDownload = versionToDownload.substring(1);
        }
        log.info("Latest version of {} is {}", match, versionToDownload);
        return out;
    }

    protected void handleDriver(URL url, String driver, List<URL> out) {
        if (!useBetaVersions && !getBoolean("wdm.useBetaVersions")
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
        return getDriverUrl().getHost().equalsIgnoreCase("npm.taobao.org");
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
        if (!mirrorLog) {
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
        log.info("Reading {} to seek {}", driverUrl, getDriverName());
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

    protected String defaultOsName() {
        String os = getProperty("os.name").toLowerCase();
        if (IS_OS_WINDOWS) {
            os = WIN.name();
        } else if (IS_OS_LINUX) {
            os = LINUX.name();
        } else if (IS_OS_MAC) {
            os = MAC.name();
        }
        return os;
    }

    protected void exportDriver(String variableName, String variableValue) {
        log.info("Exporting {} as {}", variableName, variableValue);
        binaryPath = variableValue;
        System.setProperty(variableName, variableValue);
    }

    protected InputStream openGitHubConnection(URL driverUrl)
            throws IOException {
        HttpGet get = httpClient.createHttpGet(driverUrl);

        String gitHubTokenName = WdmConfig.getString("wdm.gitHubTokenName");
        gitHubTokenName = isNullOrEmpty(gitHubTokenName)
                ? getenv("WDM_GIT_HUB_TOKEN_NAME")
                : gitHubTokenName;

        String gitHubTokenSecret = WdmConfig.getString("wdm.gitHubTokenSecret");
        gitHubTokenSecret = isNullOrEmpty(gitHubTokenSecret)
                ? getenv("WDM_GIT_HUB_TOKEN_SECRET")
                : gitHubTokenSecret;

        if (!isNullOrEmpty(gitHubTokenName)
                && !isNullOrEmpty(gitHubTokenSecret)) {
            String userpass = gitHubTokenName + ":" + gitHubTokenSecret;
            String basicAuth = "Basic "
                    + new String(new Base64().encode(userpass.getBytes()));
            get.addHeader("Authorization", basicAuth);
        }

        return httpClient.execute(get).getEntity().getContent();
    }

    protected Architecture getDefaultArchitecture() {
        if (architecture == null) {
            String archStr = getString("wdm.architecture");
            if (archStr.equals("")) {
                archStr = getProperty("sun.arch.data.model");
            }
            architecture = Architecture.valueOf("X" + archStr);
        }
        return architecture;

    }

    protected List<URL> getDriversFromGitHub() throws IOException {
        List<URL> urls;
        if (isUsingTaobaoMirror()) {
            urls = getDriversFromMirror(getDriverUrl());

        } else {
            String driverVersion = versionToDownload;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            openGitHubConnection(getDriverUrl())))) {

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

    protected String getExportParameter() {
        return exportParameter;
    }

    protected String getDriverVersionKey() {
        return driverVersionKey;
    }

    protected String getDriverUrlKey() {
        return driverUrlKey;
    }

    protected List<String> getDriverName() {
        return driverName;
    }

    protected void reset() {
        myOsName = defaultOsName();
        useBetaVersions = false;
        mirrorLog = false;
        isForcingCache = false;
        isForcingDownload = false;
        listVersions = null;
        architecture = null;
        driverUrl = null;
        version = null;
        versionToDownload = null;
        proxyValue = null;
        proxyUser = null;
        proxyPass = null;
        ignoredVersions = null;
    }

}
