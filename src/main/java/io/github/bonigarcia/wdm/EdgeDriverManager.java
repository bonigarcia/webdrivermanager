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

import static io.github.bonigarcia.wdm.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.Shell.getVersionFromPowerShellOutput;
import static io.github.bonigarcia.wdm.Shell.runAndWait;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Optional.empty;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Manager for Microsoft Edge.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeDriverManager extends WebDriverManager {

    static final Logger log = getLogger(lookup().lookupClass());

    private static final String BASE_EDGE_URL = "https://msedgewebdriverstorage.blob.core.windows.net/";
    private static final String URL_EDGE_CHROMIUM_VERSIONS = BASE_EDGE_URL +
        "edgewebdriver?delimiter=%2F&restype=container&comp=list&timeout=60000";
    private static final String URL_EDGE_CHROMIUM_LINKS = BASE_EDGE_URL +
        "edgewebdriver?restype=container&comp=list";
    private static final String URL_EDGE_LEGACY = "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/";

    @Override
    protected DriverManagerType getDriverManagerType() {
        return EDGE;
    }

    @Override
    protected String getDriverName() {
        return "edgedriver";
    }

    @Override
    protected String getDriverVersion() {
        return config().getEdgeDriverVersion();
    }

    @Override
    protected URL getDriverUrl() {
        return config().getEdgeDriverUrl();
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return empty();
    }

    @Override
    protected Optional<String> getExportParameter() {
        return Optional.of(config().getEdgeDriverExport());
    }

    @Override
    protected void setDriverVersion(String version) {
        config().setEdgeDriverVersion(version);
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setEdgeDriverUrl(url);
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        List<URL> linksLegacy;
        List<URL> linkChromiumBased;

        linkChromiumBased = getChromiumBasedLinks();

        Map<String, URL> legacy = getLegacyVersions();
        linksLegacy = new ArrayList<>(legacy.values());

        List<URL> links = Stream.of(linkChromiumBased, linksLegacy)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        listVersions = new ArrayList<>(legacy.keySet());
        // normalize the versions removing /
        getChromiumBasedVersions().forEach(version -> listVersions.add(version.replace("/", StringUtils.EMPTY)));

        return links;
    }

    @Override
    public List<String> getVersions() {
        httpClient = new HttpClient(config());
        try {
            getDrivers();
            listVersions.sort(new VersionComparator());
            return listVersions;
        } catch (IOException e) {
            throw new WebDriverManagerException(e);
        }
    }

    @Override
    protected List<URL> checkLatest(List<URL> list, String driver) {
        log.trace("Checking the lastest version of {} with URL list {}", driver,
                list);
        List<URL> out = new ArrayList<>();
        versionToDownload = listVersions.iterator().next();
        out.add(list.iterator().next());
        log.info("Latest version of Edge driver is {}", versionToDownload);
        return out;
    }

    @Override
    protected String preDownload(String target, String version) {
        if (isChromiumBased(version)) {
            int iVersion = target.indexOf(version);
            if (iVersion != -1) {
                target = target.substring(0, iVersion)
                        + config().getArchitecture().name().toLowerCase()
                        + File.separator + target.substring(iVersion);
            }
        }
        log.trace("Pre-download in EdgeDriver -- target={}, version={}", target,
                version);
        return target;
    }

    @Override
    protected File postDownload(File archive) {
        Collection<File> listFiles = listFiles(new File(archive.getParent()),
                null, true);
        Iterator<File> iterator = listFiles.iterator();
        File file = null;
        while (iterator.hasNext()) {
            file = iterator.next();
            if (file.getName().contains(getDriverName())) {
                return file;
            }
        }
        return file;
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        String[] programFilesEnvs = { getProgramFilesEnv() };

        if (IS_OS_WINDOWS) {
            String[] msEdgePaths = {
                    "\\\\Microsoft\\\\Edge\\\\Application\\\\msedge.exe",
                    "\\\\Microsoft\\\\Edge Beta\\\\Application\\\\msedge.exe",
                    "\\\\Microsoft\\\\Edge Dev\\\\Application\\\\msedge.exe" };

            String browserVersionOutput = null;
            Optional<String> msedgeVersion = empty();
            for (String msEdgePath : msEdgePaths) {
                msedgeVersion = getDefaultBrowserVersion(programFilesEnvs,
                        msEdgePath, "", "", "--version",
                        getDriverManagerType().toString());
                if (msedgeVersion.isPresent()) {
                    browserVersionOutput = msedgeVersion.get();
                    log.debug("Edge (based on Chromium) version {} found",
                            browserVersionOutput);
                    break;
                }
            }

            if (!msedgeVersion.isPresent()) {
                browserVersionOutput = runAndWait("powershell",
                        "get-appxpackage Microsoft.MicrosoftEdge");
            }

            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional.of(
                        getVersionFromPowerShellOutput(browserVersionOutput));
            }
        }
        if (IS_OS_MAC_OSX) {
            String macBrowserName = "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge";

            return getDefaultBrowserVersion(programFilesEnvs, StringUtils.EMPTY, StringUtils.EMPTY, macBrowserName,
                "-version", getDriverManagerType().toString());
        }
        return empty();
    }

    @Override
    protected Optional<String> getLatestVersion() {
        Optional<String> latestVersion = Optional.empty();
        String latestVersionUrl = MessageFormat
            .format("{0}/{1}", "https://msedgedriver.azureedge.net/", "LATEST_STABLE");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(latestVersionUrl).openStream(),
            StandardCharsets.UTF_16))) {
            latestVersion = Optional.of(reader.readLine().trim());
        } catch (IOException e) {
            log.warn("Error getting the latest version: {}", e.getMessage());
        }
        return latestVersion;
    }

    private boolean isChromiumBased(String version) {
        long countDot = version.chars().filter(ch -> ch == '.').count();
        log.trace("Edge driver version {} ({} dots)", version, countDot);
        return countDot > 1;
    }

    private List<String> getChromiumBasedVersions() {
        String listVersions = "/EnumerationResults/Blobs/BlobPrefix/Name";
        NodeList nodeList = returnNodeListFromExpression(listVersions, URL_EDGE_CHROMIUM_VERSIONS);

        int bound = nodeList.getLength();

        return IntStream.range(0, bound).mapToObj(nodeList::item).map(Node::getTextContent)
            .collect(Collectors.toList());
    }

    private List<URL> getChromiumBasedLinks() throws IOException {
        List<URL> links = new ArrayList<>();

        // necessary to no download the wrong version
        String os = config.getOs().toLowerCase();
        String architecture = config.getArchitecture().toString();

        String listVersions = "/EnumerationResults/Blobs/Blob/Url";
        NodeList urlList = returnNodeListFromExpression(listVersions, URL_EDGE_CHROMIUM_LINKS);
        String chromiumUrls;
        for (int i = 0; i < urlList.getLength(); i++) {
            chromiumUrls = urlList.item(i).getTextContent();
            if (chromiumUrls.contains(os) && chromiumUrls.contains(architecture))
                links.add(new URL(chromiumUrls));
        }

        return links;
    }

    private NodeList returnNodeListFromExpression(String expression, String url) {
        NodeList nodeList = null;
        XPath xPath = XPathFactory.newInstance().newXPath();

        XPathExpression xPathExpression;
        try {
            xPathExpression = xPath.compile(expression);
            nodeList = (NodeList) xPathExpression
                .evaluate(returnURLDocument(url), XPathConstants.NODESET);
        } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
            log.warn("Error during xpath evaluation: {}", e.getMessage());
            log.error(e.getMessage());
        }

        return nodeList;
    }

    private org.w3c.dom.Document returnURLDocument(String url)
        throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        return builder.parse(url);
    }

    public Map<String, URL> getLegacyVersions() {
        org.jsoup.nodes.Document document;
        String cssSelectorLegacySection = "section[id='downloads'] > div[class*='layout'] > div:nth-of-type(2) "
            + "> ul > li[class='driver-download']";
        Map<String, URL> versions = new TreeMap<>();

        try {
            document = Jsoup.connect(URL_EDGE_LEGACY).get();
            Elements legacyDownloadElementList = document.select(cssSelectorLegacySection);

            String version;
            URL url;
            // starting with 1 because the first element is the description
            int bound = legacyDownloadElementList.size();
            for (int i = 1; i < bound; i++) {
                version = returnProperlyLegacyVersion(
                    legacyDownloadElementList.get(i).getElementsByClass("driver-download__meta").text());
                url = new URL(legacyDownloadElementList.get(i).getElementsByClass("subtitle").attr("href"));
                versions.put(version, url);
            }
        } catch (IOException e) {
            log.warn("Error getting the legacy versions: {}", e.getMessage());
        }

        return versions;
    }

    private String returnProperlyLegacyVersion(String text) {
        String regexp = "[0-9]{1}[.]\\d{5}";
        Matcher matcher = Pattern.compile(regexp).matcher(text);

        return !matcher.find() ? null : matcher.group(0);
    }

}
