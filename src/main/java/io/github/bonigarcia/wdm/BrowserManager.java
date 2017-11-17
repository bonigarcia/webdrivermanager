/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.Architecture.X32;
import static io.github.bonigarcia.wdm.Architecture.X64;
import static io.github.bonigarcia.wdm.Architecture.valueOf;
import static io.github.bonigarcia.wdm.DriverVersion.LATEST;
import static io.github.bonigarcia.wdm.DriverVersion.NOT_SPECIFIED;
import static io.github.bonigarcia.wdm.WdmConfig.getBoolean;
import static io.github.bonigarcia.wdm.WdmConfig.getInt;
import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static io.github.bonigarcia.wdm.WdmConfig.getUrl;
import static io.github.bonigarcia.wdm.WdmUtils.isNullOrEmpty;
import static java.io.File.separator;
import static java.lang.Integer.signum;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.sort;
import static java.util.Collections.reverse;
import static java.util.Collections.reverseOrder;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathFactory.newInstance;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

/**
 * Generic manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public abstract class BrowserManager {

    protected static final Logger log = LoggerFactory
            .getLogger(BrowserManager.class);

    public static final String TAOBAO_MIRROR = "npm.taobao.org";
    public static final String SEPARATOR = "/";
    public static final Architecture DEFAULT_ARCH = valueOf(
            "X" + getProperty("sun.arch.data.model"));
    public static final String MY_OS_NAME = getOsName();

    protected abstract List<URL> getDrivers() throws IOException;

    protected static BrowserManager instance;
    protected String versionToDownload;
    protected boolean mirrorLog = false;
    protected String version;
    protected Architecture architecture;
    protected boolean isForcingCache = false;
    protected boolean isForcingDownload = false;
    protected boolean useBetaVersions = getBoolean("wdm.useBetaVersions");
    protected URL driverUrl;
    protected String proxyValue;
    protected String binaryPath;
    protected List<String> listVersions;
    protected Downloader downloader;
    protected String proxyUser;
    protected String proxyPass;
    protected WdmHttpClient httpClient;
    protected String exportParameter;
    protected String driverVersionKey;
    protected String driverUrlKey;
    protected List<String> driverName;

    protected String getDriverVersion() {
        return version == null ? getString(getDriverVersionKey()) : version;
    }

    protected URL getDriverUrl() throws MalformedURLException {
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
                break;
            }
        }
        return target;
    }

    protected String getCurrentVersion(URL url, String driverName) {
        log.trace("Getting current version: url={}, driverName={}", url,
                driverName);
        return url.getFile().substring(url.getFile().indexOf(SEPARATOR) + 1,
                url.getFile().lastIndexOf(SEPARATOR));
    }

    protected void manage(Architecture arch, DriverVersion version) {
        manage(arch, version.name());
    }

    protected void manage(Architecture arch, String version) {
        httpClient = new WdmHttpClient.Builder().proxy(proxyValue)
                .proxyUser(proxyUser).proxyPass(proxyPass).build();
        try (WdmHttpClient wdmHttpClient = httpClient) {
            downloader = new Downloader(this, wdmHttpClient);
            if (isForcingDownload) {
                downloader.forceDownload();
            }

            boolean getLatest = version == null || version.isEmpty()
                    || version.equalsIgnoreCase(LATEST.name())
                    || version.equalsIgnoreCase(NOT_SPECIFIED.name());
            boolean cache = this.isForcingCache || getBoolean("wdm.forceCache")
                    || !isNetAvailable();

            Optional<String> driverInCache = handleCache(arch, version,
                    getLatest, cache);

            if (driverInCache.isPresent()) {
                versionToDownload = version;
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
                            + " for " + MY_OS_NAME + arch.toString()
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

    private void handleException(Exception e, Architecture arch,
            String version) {
        if (!isForcingCache) {
            isForcingCache = true;
            log.warn(
                    "There was an error managing {} {} ({}) ... trying again forcing to use cache",
                    getDriverName(), version, e.getMessage());
            manage(arch, version);
        } else {
            String errorMessage = "Exception managing driver for "
                    + getDriverName();
            log.error(errorMessage, e);

            throw new WebDriverManagerException(errorMessage, e);
        }
    }

    private void downloadCandidateUrls(List<URL> candidateUrls)
            throws IOException, InterruptedException {
        for (URL url : candidateUrls) {
            String export = candidateUrls.contains(url) ? getExportParameter()
                    : null;
            downloader.download(url, versionToDownload, export,
                    getDriverName());
        }
    }

    private List<URL> filterCandidateUrls(Architecture arch, String version,
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
            candidateUrls = filterByOs(candidateUrls);
            candidateUrls = filterByArch(candidateUrls, arch);

            // Extra round of filter phantomjs 2.5.0 in Linux
            if (IS_OS_LINUX && getDriverName().contains("phantomjs")) {
                candidateUrls = filterByDistro(candidateUrls, getDistroName(),
                        "2.5.0");
            }

            // Find out if driver version has been found or not
            continueSearchingVersion = candidateUrls.isEmpty() && getLatest;
            if (continueSearchingVersion) {
                log.trace("No valid binary found for {} {}", getDriverName(),
                        versionToDownload);
                urls = removeFromList(urls, versionToDownload);
                versionToDownload = null;
            }
        } while (continueSearchingVersion);
        return candidateUrls;
    }

    private Optional<String> handleCache(Architecture arch, String version,
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

    public boolean isExecutable(File file) {
        return IS_OS_WINDOWS ? file.getName().toLowerCase().endsWith(".exe")
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

    protected List<URL> filterByOs(List<URL> list) {
        log.trace("{} {} - URLs before filtering by OS: {}", getDriverName(),
                versionToDownload, list);
        List<URL> out = new ArrayList<>();

        for (URL url : list) {
            for (OperativeSystem os : OperativeSystem.values()) {
                if (((MY_OS_NAME.contains(os.name())
                        && url.getFile().toUpperCase().contains(os.name()))
                        || getDriverName().contains("IEDriverServer")
                        || (IS_OS_MAC
                                && url.getFile().toLowerCase().contains("osx")))
                        && !out.contains(url)) {
                    out.add(url);
                }
            }
        }

        log.trace("{} {} - URLs after filtering by OS ({}): {}",
                getDriverName(), versionToDownload, MY_OS_NAME, out);
        return out;
    }

    protected List<URL> filterByArch(List<URL> list, Architecture arch) {
        log.trace("{} {} - URLs before filtering by architecture: {}",
                getDriverName(), versionToDownload, list);
        List<URL> out = new ArrayList<>();

        if (out.size() > 1 && arch != null) {
            for (URL url : list) {
                // Exception: 32 bits (sometimes referred as x86 or i686)
                if (arch == X32 && ((url.getFile().contains("x86")
                        && !url.getFile().contains("64"))
                        || url.getFile().contains("i686"))) {
                    continue;
                }

                if (!url.getFile().contains(arch.toString())) {
                    out.remove(url);
                }
            }
        }

        log.trace("{} {} - URLs after filtering by architecture ({}): {}",
                getDriverName(), versionToDownload, arch, out);
        return out;
    }

    protected List<URL> filterByDistro(List<URL> list, String distro,
            String version) {
        log.trace("{} {} - URLs before filtering by distribution: {}",
                getDriverName(), versionToDownload, list);
        List<URL> out = new ArrayList<>(list);

        for (URL url : list) {
            if (url.getFile().contains(version)
                    && !url.getFile().contains(distro)) {
                out.remove(url);
            }
        }

        log.trace("{} {} - URLs after filtering by Linux distribution ({}): {}",
                getDriverName(), versionToDownload, distro, out);
        return out;
    }

    protected String getDistroName() throws IOException {
        String out = "";
        final String key = "UBUNTU_CODENAME";
        File dir = new File(separator + "etc");
        File[] fileList = new File[0];
        if (dir.exists()) {
            fileList = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith("-release");
                }
            });
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

    private void handleDriver(URL url, String driver, List<URL> out) {
        if (!useBetaVersions && url.getFile().toLowerCase().contains("beta")) {
            return;
        }
        if (url.getFile().contains(driver)) {
            log.trace("URL {} match with {}", url, driver);
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
        try {
            return getDriverUrl().getHost().equalsIgnoreCase(TAOBAO_MIRROR);
        } catch (MalformedURLException e) {
            throw new WebDriverManagerException(e);
        }
    }

    protected Integer versionCompare(String str1, String str2) {
        String[] vals1 = str1.replaceAll("v", "").split("\\.");
        String[] vals2 = str2.replaceAll("v", "").split("\\.");

        log.trace("Comparing {} to {}", str1, str2);

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
            int diff = Integer.valueOf(vals1[i])
                    .compareTo(Integer.valueOf(vals2[i]));
            int signum = signum(diff);
            log.trace("[1] Returning {}", signum);
            return signum;
        } else {
            int signum = signum(vals1.length - vals2.length);
            log.trace("[2] Returning {}", signum);
            return signum;
        }
    }

    /**
     * This method works also for http://npm.taobao.org/ and
     * https://bitbucket.org/ mirrors.
     *
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
        int timeout = (int) SECONDS.toMillis(getInt("wdm.timeout"));

        WdmHttpClient.Response response = httpClient
                .execute(new WdmHttpClient.Get(driverStr, timeout));
        try (InputStream in = response.getContent()) {
            org.jsoup.nodes.Document doc = Jsoup.parse(in, null, "");
            Iterator<org.jsoup.nodes.Element> iterator = doc.select("a")
                    .iterator();
            List<URL> urlList = new ArrayList<>();

            while (iterator.hasNext()) {
                String link = iterator.next().attr("href");
                if (link.contains("mirror") && link.endsWith(SEPARATOR)) {
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

        int retries = 1;
        int maxRetries = getInt("wdm.seekErrorRetries");
        do {
            try {
                WdmHttpClient.Response response = httpClient
                        .execute(new WdmHttpClient.Get(driverUrl));
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getContent()))) {
                    Document xml = loadXML(reader);
                    NodeList nodes = (NodeList) newInstance().newXPath()
                            .evaluate("//Contents/Key",
                                    xml.getDocumentElement(), NODESET);

                    for (int i = 0; i < nodes.getLength(); ++i) {
                        Element e = (Element) nodes.item(i);
                        urls.add(new URL(driverUrl
                                + e.getChildNodes().item(0).getNodeValue()));
                    }
                }
                break;
            } catch (Exception e) {
                log.warn("[{}/{}] Exception reading {} to seek {}: {} {}",
                        retries, maxRetries, driverUrl, getDriverName(),
                        e.getClass().getName(), e.getMessage(), e);
                retries++;
                if (retries > maxRetries) {
                    throw new WebDriverManagerException(e);
                }
            }
        } while (true);

        return urls;
    }

    protected Document loadXML(Reader reader)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(reader);
        return builder.parse(is);
    }

    protected static String getOsName() {
        String os = getProperty("os.name").toLowerCase();
        if (IS_OS_WINDOWS) {
            os = "WIN";
        } else if (IS_OS_LINUX) {
            os = "LINUX";
        } else if (IS_OS_MAC) {
            os = "MAC";
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
        WdmHttpClient.Get get = new WdmHttpClient.Get(driverUrl)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Connection", "keep-alive");

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

        return httpClient.execute(get).getContent();
    }

    public void setup() {
        String driverVersion = getDriverVersion();
        if (!driverVersion.equals("")) {
            manage(getDefaultArchitecture(),
                    isNullOrEmpty(driverVersion) ? NOT_SPECIFIED.name()
                            : driverVersion);
        }
    }

    private Architecture getDefaultArchitecture() {
        if (this.architecture == null) {
            String archStr = getString("wdm.architecture");
            if (archStr.equals("")) {
                this.architecture = DEFAULT_ARCH;
            } else {
                this.architecture = Architecture.valueOf("X" + archStr);
            }
        }
        return this.architecture;
    }

    public String getDownloadedVersion() {
        return versionToDownload;
    }

    public BrowserManager version(String version) {
        this.version = version;
        return this;
    }

    public BrowserManager architecture(Architecture architecture) {
        this.architecture = architecture;
        return this;
    }

    public BrowserManager arch32() {
        this.architecture = X32;
        return this;
    }

    public BrowserManager arch64() {
        this.architecture = X64;
        return this;
    }

    public BrowserManager forceCache() {
        this.isForcingCache = true;
        return this;
    }

    public BrowserManager forceDownload() {
        this.isForcingDownload = true;
        return this;
    }

    public BrowserManager driverRepositoryUrl(URL url) {
        this.driverUrl = url;
        return this;
    }

    public BrowserManager useTaobaoMirror() {
        String errorMessage = "Binaries for " + getDriverName()
                + " not available in taobao.org mirror (http://npm.taobao.org/mirrors/)";
        log.error(errorMessage);
        throw new WebDriverManagerException(errorMessage);
    }

    public BrowserManager useTaobaoMirror(String taobaoUrl) {
        try {
            taobaoUrl = getString(getString(taobaoUrl));
            driverUrl = new URL(taobaoUrl);
        } catch (MalformedURLException e) {
            String errorMessage = "Malformed URL " + taobaoUrl;
            log.error(errorMessage, e);
            throw new WebDriverManagerException(errorMessage, e);
        }
        return instance;
    }

    public BrowserManager proxy(String proxy) {
        this.proxyValue = proxy;
        return this;
    }

    public BrowserManager proxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
        return this;
    }

    public BrowserManager proxyPass(String proxyPass) {
        this.proxyPass = proxyPass;
        return this;
    }

    public String getBinaryPath() {
        return binaryPath;
    }

    public BrowserManager useBetaVersions() {
        this.useBetaVersions = true;
        return this;
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

}
