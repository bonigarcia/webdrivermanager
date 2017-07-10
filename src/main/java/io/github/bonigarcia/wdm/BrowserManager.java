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

import static io.github.bonigarcia.wdm.WdmUtils.isNullOrEmpty;
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
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

    public static final Architecture DEFAULT_ARCH = Architecture
            .valueOf("x" + System.getProperty("sun.arch.data.model"));
    public static final String MY_OS_NAME = getOsName();

    protected abstract List<URL> getDrivers() throws Exception;

    protected abstract String getExportParameter();

    protected abstract String getDriverVersionKey();

    protected abstract List<String> getDriverName();

    protected abstract String getDriverUrlKey();

    protected static BrowserManager instance;

    protected String versionToDownload;

    protected boolean mirrorLog = false;

    protected String version;

    protected Architecture architecture;

    protected boolean forceCache = false;

    protected boolean forceDownload = false;

    protected boolean useBetaVersions = WdmConfig
            .getBoolean("wdm.useBetaVersions");

    protected URL driverUrl;

    protected String proxy;

    protected String binaryPath;

    protected List<String> listVersions;

    protected boolean triedWithCache = false;

    /**
     * @since 1.6.2
     */
    protected String proxyUser;

    /**
     * @since 1.6.2
     */
    protected String proxyPass;

    /**
     * @since 1.6.2
     */
    protected WdmHttpClient httpClient;

    protected String getDriverVersion() {
        return version == null ? WdmConfig.getString(getDriverVersionKey())
                : version;
    }

    protected URL getDriverUrl() throws MalformedURLException {
        return driverUrl == null ? WdmConfig.getUrl(getDriverUrlKey())
                : driverUrl;
    }

    protected String preDownload(String target, String version)
            throws IOException {
        return target;
    }

    protected File postDownload(File archive) throws IOException {
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

    protected String getCurrentVersion(URL url, String driverName)
            throws MalformedURLException {
        return url.getFile().substring(url.getFile().indexOf(SEPARATOR) + 1,
                url.getFile().lastIndexOf(SEPARATOR));
    }

    protected void manage(Architecture arch, DriverVersion version) {
        manage(arch, version.name());
    }

    protected void manage(Architecture arch, String version) {

        this.httpClient = new WdmHttpClient.Builder().proxy(proxy)
                .proxyUser(proxyUser).proxyPass(proxyPass).build();

        try (WdmHttpClient httpClient = this.httpClient) {

            Downloader downloader = new Downloader(this, httpClient);
            if (forceDownload) {
                downloader.forceDownload();
            }

            boolean getLatest = version == null || version.isEmpty()
                    || version.equalsIgnoreCase(DriverVersion.LATEST.name())
                    || version.equalsIgnoreCase(
                            DriverVersion.NOT_SPECIFIED.name());

            boolean forceCache = this.forceCache
                    || WdmConfig.getBoolean("wdm.forceCache")
                    || !isNetAvailable();

            String driverInCache = null;
            if (forceCache) {
                driverInCache = forceCache(downloader.getTargetPath());
            } else if (!getLatest) {
                versionToDownload = version;
                driverInCache = existsDriverInCache(downloader.getTargetPath(),
                        version, arch);
            }

            if (driverInCache != null) {
                versionToDownload = version;
                log.debug("Driver for {} {} found in cache {}", getDriverName(),
                        versionToDownload, driverInCache);
                exportDriver(getExportParameter(), driverInCache);

            } else {
                // Get the complete list of URLs
                List<URL> urls = getDrivers();
                if (!urls.isEmpty()) {
                    List<URL> candidateUrls;
                    boolean continueSearchingVersion;

                    do {
                        // Get the latest or concrete version or Edge (the only
                        // version that can be downloaded is the latest)
                        if (getLatest) {
                            candidateUrls = getLatest(urls, getDriverName());
                        } else {
                            candidateUrls = getVersion(urls, getDriverName(),
                                    version);
                        }
                        if (versionToDownload == null) {
                            break;
                        }

                        log.trace("All URLs: {}", urls);
                        log.trace("Candidate URLs: {}", candidateUrls);

                        if (this.getClass().equals(EdgeDriverManager.class)) {
                            // Microsoft Edge binaries are different
                            continueSearchingVersion = false;

                        } else {
                            // Filter by architecture and OS
                            candidateUrls = filter(candidateUrls, arch);

                            // Exception for phantomjs 2.5.0 in Linux
                            if (IS_OS_LINUX
                                    && getDriverName().contains("phantomjs")) {
                                candidateUrls = filterByDistro(candidateUrls,
                                        getDistroName(), "2.5.0");
                            }

                            // Find out if driver version has been found or not
                            continueSearchingVersion = candidateUrls.isEmpty()
                                    && getLatest;
                            if (continueSearchingVersion) {
                                log.info("No valid binary found for {} {}",
                                        getDriverName(), versionToDownload);
                                urls = removeFromList(urls, versionToDownload);
                                versionToDownload = null;
                            }
                        }

                    } while (continueSearchingVersion);

                    if (candidateUrls.isEmpty()) {
                        String versionStr = getLatest ? "(latest version)"
                                : version;
                        String errMessage = getDriverName() + " " + versionStr
                                + " for " + MY_OS_NAME + arch.toString()
                                + " not found in " + getDriverUrl();
                        log.error(errMessage);
                        throw new RuntimeException(errMessage);
                    }

                    for (URL url : candidateUrls) {
                        String export = candidateUrls.contains(url)
                                ? getExportParameter() : null;
                        downloader.download(url, versionToDownload, export,
                                getDriverName());
                    }
                }
            }

        } catch (Exception e) {
            if (!forceCache && !triedWithCache) {
                forceCache = true;
                triedWithCache = true;
                log.warn(
                        "There was an error managing {} {} ({}) ... trying again forcing to use cache",
                        getDriverName(), version, e.getMessage());
                manage(arch, version);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    protected String forceCache(String repository) throws IOException {
        String driverInCache = null;
        for (String driverName : getDriverName()) {
            log.trace("Checking if {} exists in cache {}", driverName,
                    repository);

            Collection<File> listFiles = FileUtils
                    .listFiles(new File(repository), null, true);
            Object[] array = listFiles.toArray();
            Arrays.sort(array, Collections.reverseOrder());

            for (Object f : array) {
                driverInCache = f.toString();
                log.trace("Checking {}", driverInCache);
                if (driverInCache.contains(driverName)
                        && isExecutable(new File(driverInCache))) {
                    log.info("Found {} in cache: {} ", driverName,
                            driverInCache);
                    break;
                } else {
                    driverInCache = null;
                }
            }

            if (driverInCache == null) {
                log.trace("{} do not exist in cache {}", driverName,
                        repository);
            } else {
                break;
            }
        }
        return driverInCache;
    }

    protected String existsDriverInCache(String repository,
            String driverVersion, Architecture arch) throws IOException {
        String driverInCache = null;
        for (String driverName : getDriverName()) {
            log.trace("Checking if {} {} ({} bits) exists in cache {}",
                    driverName, driverVersion, arch, repository);

            Collection<File> listFiles = FileUtils
                    .listFiles(new File(repository), null, true);
            Object[] array = listFiles.toArray();
            Arrays.sort(array, Collections.reverseOrder());

            for (Object f : array) {
                driverInCache = f.toString();

                // Exception for phantomjs
                boolean architecture = !shouldCheckArchitecture(driverName)
                        || driverInCache.contains(arch.toString());
                log.trace("Checking {}", driverInCache);

                if (driverInCache.contains(driverVersion)
                        && driverInCache.contains(driverName) && architecture) {
                    if (!isExecutable(new File(driverInCache))) {
                        continue;
                    }
                    log.debug("Found {} {} ({} bits) in cache: {}",
                            driverVersion, driverName, arch, driverInCache);
                    break;
                } else {
                    driverInCache = null;
                }
            }

            if (driverInCache == null) {
                log.trace("{} {} ({} bits) do not exist in cache {}",
                        driverVersion, driverName, arch, repository);
            } else {
                break;
            }
        }
        return driverInCache;
    }

    public boolean isExecutable(File file) {
        return IS_OS_WINDOWS ? file.getName().toLowerCase().endsWith(".exe")
                : file.canExecute();
    }

    /**
     * @since 1.6.2
     */
    protected boolean shouldCheckArchitecture(String driverName) {
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

    protected List<URL> filter(List<URL> list, Architecture arch) {
        log.trace("{} {} - URLs before filtering: {}", getDriverName(),
                versionToDownload, list);

        List<URL> out = new ArrayList<URL>();

        // Round #1 : Filter by OS
        for (URL url : list) {
            for (OperativeSystem os : OperativeSystem.values()) {
                if (((MY_OS_NAME.contains(os.name())
                        && url.getFile().toLowerCase().contains(os.name()))
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

        // Round #2 : Filter by architecture (32/64 bits)
        if (out.size() > 1 && arch != null) {
            for (URL url : list) {
                // Exception: 32 bits (sometimes referred as x86 or i686)
                if (arch == Architecture.x32 && ((url.getFile().contains("x86")
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
            String version) throws IOException {
        log.trace("{} {} - URLs before filtering by distro: {}",
                getDriverName(), versionToDownload, list);

        List<URL> out = new ArrayList<URL>(list);
        // Round #3 : Filter by distribution (for Linux)
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
        File dir = new File("/etc/");
        File fileList[] = new File[0];
        if (dir.exists()) {
            fileList = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith("-release");
                }
            });
        }
        File fileVersion = new File("/proc/version");
        if (fileVersion.exists()) {
            fileList = Arrays.copyOf(fileList, fileList.length + 1);
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
        List<URL> out = new ArrayList<URL>(list);
        for (URL url : list) {
            if (url.getFile().contains(version)) {
                out.remove(url);
            }
        }
        return out;
    }

    protected List<URL> getVersion(List<URL> list, List<String> match,
            String version) {
        List<URL> out = new ArrayList<URL>();
        if (getDriverName().contains("MicrosoftWebDriver")) {
            int i = listVersions.indexOf(version);
            if (i != -1) {
                out.add(list.get(i));
            }
        }

        for (String s : match) {
            Collections.reverse(list);
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
        log.trace("Checking the lastest version of {}", match);
        log.trace("Input URL list {}", list);
        List<URL> out = new ArrayList<URL>();

        // Edge
        if (getDriverName().contains("MicrosoftWebDriver")) {
            versionToDownload = listVersions.iterator().next();
            out.add(list.iterator().next());
            log.info("Latest version of MicrosoftWebDriver is {}",
                    versionToDownload);
            return out;
        }

        Collections.reverse(list);

        List<URL> copyOfList = new ArrayList<>(list);
        for (URL url : copyOfList) {
            for (String driverName : match) {
                try {
                    // Beta versions
                    if (!useBetaVersions
                            && url.getFile().toLowerCase().contains("beta")) {
                        continue;
                    }
                    if (url.getFile().contains(driverName)) {
                        log.trace("URL {} match with {}", url, driverName);
                        String currentVersion = getCurrentVersion(url,
                                driverName);

                        if (currentVersion.equalsIgnoreCase(driverName)) {
                            continue;
                        }

                        if (versionToDownload == null) {
                            versionToDownload = currentVersion;
                        }

                        if (versionCompare(currentVersion,
                                versionToDownload) > 0) {
                            versionToDownload = currentVersion;
                            out.clear();
                        }
                        if (url.getFile().contains(versionToDownload)) {
                            out.add(url);
                        }
                    }

                } catch (Exception e) {
                    log.trace("There was a problem with URL {} : {}",
                            url.toString(), e.getMessage());
                    list.remove(url);
                    continue;
                }
            }
        }
        if (versionToDownload.startsWith(".")) {
            versionToDownload = versionToDownload.substring(1);
        }
        log.info("Latest version of {} is {}", match, versionToDownload);
        return out;
    }

    protected boolean isUsingTaobaoMirror() throws MalformedURLException {
        return getDriverUrl().getHost().equalsIgnoreCase(TAOBAO_MIRROR);
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

        log.trace("Version 1 {}", Arrays.toString(vals1));
        log.trace("Version 2 {}", Arrays.toString(vals2));

        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i])
                    .compareTo(Integer.valueOf(vals2[i]));
            int signum = Integer.signum(diff);
            log.trace("[1] Returning {}", signum);
            return signum;
        } else {
            int signum = Integer.signum(vals1.length - vals2.length);
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
        int timeout = (int) TimeUnit.SECONDS
                .toMillis(WdmConfig.getInt("wdm.timeout"));

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

    protected List<URL> getDriversFromXml(URL driverUrl,
            List<String> driverBinary) throws Exception {
        log.info("Reading {} to seek {}", driverUrl, getDriverName());

        List<URL> urls = new ArrayList<URL>();

        int retries = 1;
        int maxRetries = WdmConfig.getInt("wdm.seekErrorRetries");
        do {
            try {
                WdmHttpClient.Response response = httpClient
                        .execute(new WdmHttpClient.Get(driverUrl));
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getContent()))) {
                    Document xml = loadXML(reader);

                    XPath xPath = XPathFactory.newInstance().newXPath();
                    NodeList nodes = (NodeList) xPath.evaluate("//Contents/Key",
                            xml.getDocumentElement(), XPathConstants.NODESET);

                    for (int i = 0; i < nodes.getLength(); ++i) {
                        Element e = (Element) nodes.item(i);
                        String version = e.getChildNodes().item(0)
                                .getNodeValue();
                        urls.add(new URL(driverUrl + version));
                    }
                }
                break;
            } catch (Throwable e) {
                log.warn("[{}/{}] Exception reading {} to seek {}: {} {}",
                        retries, maxRetries, driverUrl, getDriverName(),
                        e.getClass().getName(), e.getMessage(), e);
                retries++;
                if (retries > maxRetries) {
                    throw e;
                }
            }
        } while (true);

        return urls;
    }

    protected Document loadXML(Reader reader) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(reader);
        return builder.parse(is);
    }

    protected static String getOsName() {
        String os = System.getProperty("os.name").toLowerCase();

        if (SystemUtils.IS_OS_WINDOWS) {
            os = "win";
        } else if (SystemUtils.IS_OS_LINUX) {
            os = "linux";
        } else if (SystemUtils.IS_OS_MAC) {
            os = "mac";
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
                ? System.getenv("WDM_GIT_HUB_TOKEN_NAME") : gitHubTokenName;

        String gitHubTokenSecret = WdmConfig.getString("wdm.gitHubTokenSecret");
        gitHubTokenSecret = isNullOrEmpty(gitHubTokenSecret)
                ? System.getenv("WDM_GIT_HUB_TOKEN_SECRET") : gitHubTokenSecret;

        if (!isNullOrEmpty(gitHubTokenName)
                && !isNullOrEmpty(gitHubTokenSecret)) {
            String userpass = gitHubTokenName + ":" + gitHubTokenSecret;
            String basicAuth = "Basic "
                    + new String(new Base64().encode(userpass.getBytes()));
            get.addHeader("Authorization", basicAuth);
        }

        return httpClient.execute(get).getContent();
    }

    /**
     * @deprecated Since 1.6.2. This method remain to keep a backward
     *             compatibility(gh-118).
     */
    @Deprecated
    protected Proxy createProxy() {
        return httpClient.createProxy(proxy);
    }

    // *************************************
    // Public methods
    // *************************************

    public void setup() {
        Architecture architecture = this.architecture == null ? DEFAULT_ARCH
                : this.architecture;
        String driverVersion = getDriverVersion();
        if (!driverVersion.equals("")) {
            String version = isNullOrEmpty(driverVersion)
                    ? DriverVersion.NOT_SPECIFIED.name() : driverVersion;
            setup(architecture, version);
        }
    }

    /**
     *
     * @deprecated use {@link #version(String)} instead.
     */
    @Deprecated
    public void setup(String version) {
        Architecture architecture = this.architecture == null ? DEFAULT_ARCH
                : this.architecture;
        setup(architecture, version);
    }

    /**
     *
     * @deprecated use {@link #architecture(Architecture)} instead.
     */
    @Deprecated
    public void setup(Architecture architecture) {
        String driverVersion = getDriverVersion();
        String version = isNullOrEmpty(driverVersion)
                ? DriverVersion.NOT_SPECIFIED.name() : driverVersion;
        setup(architecture, version);
    }

    /**
     *
     * @deprecated use {@link #version(String)} and
     *             {@link #architecture(Architecture)} instead.
     */
    @Deprecated
    public void setup(Architecture architecture, String version) {
        // Honor property if available (even when version is present)
        String driverVersion = getDriverVersion();
        if (!driverVersion.equalsIgnoreCase(DriverVersion.LATEST.name())
                || version.equals(DriverVersion.NOT_SPECIFIED.name())) {
            version = driverVersion;
        }

        instance.manage(architecture, version);
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
        this.architecture = Architecture.x32;
        return this;
    }

    public BrowserManager arch64() {
        this.architecture = Architecture.x64;
        return this;
    }

    public BrowserManager forceCache() {
        this.forceCache = true;
        return this;
    }

    public BrowserManager forceDownload() {
        this.forceDownload = true;
        return this;
    }

    public BrowserManager driverRepositoryUrl(URL url) {
        this.driverUrl = url;
        return this;
    }

    public BrowserManager useTaobaoMirror() {
        throw new RuntimeException("Binaries for " + getDriverName()
                + " not available in taobao.org mirror"
                + " (http://npm.taobao.org/mirrors/)");
    }

    public BrowserManager proxy(String proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * @since 1.6.2
     */
    public BrowserManager proxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
        return this;
    }

    /**
     * @since 1.6.2
     */
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

}
