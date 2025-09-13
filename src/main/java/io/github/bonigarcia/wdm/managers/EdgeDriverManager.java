/*
 * (C) Copyright 2015 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.managers;

import static io.github.bonigarcia.wdm.config.Architecture.ARM64;
import static io.github.bonigarcia.wdm.config.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.config.OperatingSystem.MAC;
import static java.util.Locale.ROOT;
import static java.util.Optional.empty;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathFactory.newInstance;
import static org.apache.commons.io.FileUtils.listFiles;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.edge.EdgeOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import io.github.bonigarcia.wdm.webdriver.OptionsWithArguments;

/**
 * Manager for Microsoft Edge.
 *
 * @author Boni Garcia
 * @since 1.3.0
 */
public class EdgeDriverManager extends WebDriverManager {

    protected static final String LATEST_STABLE = "LATEST_STABLE";
    protected static final String STORAGE_QUERY = "?restype=container&comp=list";

    @Override
    public DriverManagerType getDriverManagerType() {
        return EDGE;
    }

    @Override
    protected String getDriverName() {
        return "msedgedriver";
    }

    @Override
    protected String getShortDriverName() {
        return "edgedriver";
    }

    @Override
    protected String getDriverVersion() {
        return config().getEdgeDriverVersion();
    }

    @Override
    protected String getBrowserVersion() {
        return config().getEdgeVersion();
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setEdgeDriverVersion(driverVersion);
    }

    @Override
    protected void setBrowserVersion(String browserVersion) {
        config().setEdgeVersion(browserVersion);
    }

    @Override
    protected String getBrowserBinary() {
        return config().getEdgeBinary();
    }

    @Override
    protected void setBrowserBinary(String browserBinary) {
        config().setEdgeBinary(browserBinary);
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
    protected void setDriverUrl(URL url) {
        config().setEdgeDriverUrl(url);
    }

    @Override
    protected List<URL> getDriverUrls(String driverVersion) throws IOException {
        return getDriversFromXml(new URL(getDriverUrl() + STORAGE_QUERY),
                "//Blob/Name", empty());
    }

    @Override
    protected List<File> postDownload(File archive) {
        Collection<File> listFiles = listFiles(new File(archive.getParent()),
                null, true);
        Iterator<File> iterator = listFiles.iterator();
        File file = null;

        List<File> files = new ArrayList<>();
        while (iterator.hasNext()) {
            file = iterator.next();
            String fileName = file.getName();
            if (fileName.contains(getDriverName())) {
                log.trace(
                        "Adding {} at the begining of the resulting file list",
                        fileName);
                files.add(0, file);
            } else if (fileName.toLowerCase(ROOT).endsWith(".dylib")) {
                log.trace("Adding {} to the resulting file list", fileName);
                files.add(file);
            }
        }

        return files;
    }

    @Override
    protected Optional<String> getLatestDriverVersionFromRepository() {
        if (config().isUseBetaVersions()) {
            return empty();
        } else {
            return getDriverVersionFromRepository(empty());
        }
    }

    @Override
    protected Charset getVersionCharset() {
        return StandardCharsets.UTF_16;
    }

    @Override
    protected String getLatestVersionLabel() {
        return LATEST_STABLE;
    }

    @Override
    protected Optional<String> getOsLabel() {
        String label = "_";
        switch (config().getOperatingSystem()) {
        case WIN:
            label += "WINDOWS";
            break;
        case MAC:
            label += "MACOS";
            break;
        default:
            label += config().getOs();
            break;
        }
        return Optional.of(label);
    }

    @Override
    protected Optional<URL> buildUrl(String driverVersion) {
        return buildUrl(driverVersion, config());
    }

    Optional<URL> buildUrl(String driverVersion, Config config) {
        Optional<URL> optionalUrl = empty();
        if (!config.isUseMirror()) {
            String downloadUrlPattern = config.getEdgeDownloadUrlPattern();
            OperatingSystem os = config.getOperatingSystem();
            Architecture arch = config.getArchitecture();
            String archLabel = os.isWin() ? arch.toString() : "64";
            if (arch == ARM64) {
                archLabel = "64";
            }
            String osName = arch != ARM64 ? os.getName() : "arm";
            String builtUrl = os == MAC && arch == ARM64
                    ? String.format(downloadUrlPattern, driverVersion, "mac",
                            "64_m1")
                    : String.format(downloadUrlPattern, driverVersion, osName,
                            archLabel);

            log.debug("Using URL built from repository pattern: {}", builtUrl);
            try {
                optionalUrl = Optional.of(new URL(builtUrl));
            } catch (MalformedURLException e) {
                log.warn("Error building URL from pattern {} {}", builtUrl,
                        e.getMessage());
            }
        }
        return optionalUrl;
    }

    @Override
    protected Capabilities getCapabilities() {
        Capabilities options = new EdgeOptions();
        try {
            addDefaultArgumentsForDocker(options);
        } catch (Exception e) {
            log.error(
                    "Exception adding default arguments for Docker, retyring with custom class");
            options = new OptionsWithArguments("MicrosoftEdge",
                    "ms:edgeOptions");
            try {
                addDefaultArgumentsForDocker(options);
            } catch (Exception e1) {
                log.error("Exception getting default capabilities", e);
            }
        }
        return options;
    }

    @Override
    public WebDriverManager exportParameter(String exportParameter) {
        config().setEdgeDriverExport(exportParameter);
        return this;
    }

    protected List<URL> getDriversFromXml(URL driverUrl, String xpath,
            Optional<NamespaceContext> namespaceContext) throws IOException {
        logSeekRepo(driverUrl);
        List<URL> urls = new ArrayList<>();
        try {
            try (ClassicHttpResponse response = getHttpClient()
                    .execute(getHttpClient().createHttpGet(driverUrl))) {
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

                NodeList nextMarkerNodes = (NodeList) xPath.evaluate(
                        "/EnumerationResults/NextMarker",
                        xml.getDocumentElement(), NODESET);
                if (nextMarkerNodes.getLength() > 0) {
                    String containerName = String.format("%s://%s/",
                            driverUrl.getProtocol(), driverUrl.getAuthority());
                    Element e = (Element) nextMarkerNodes.item(0);
                    if (e.hasChildNodes()) {
                        String marker = e.getFirstChild().getNodeValue();
                        if (StringUtils.isNotEmpty(marker)) {
                            urls.addAll(getDriversFromXml(
                                    new URIBuilder(
                                            containerName + STORAGE_QUERY)
                                                    .setParameter("marker",
                                                            marker)
                                                    .build().toURL(),
                                    xpath, namespaceContext));
                        }
                    }
                }

            }
        } catch (Exception e) {
            throw new WebDriverManagerException(e);
        }
        return urls;
    }

}
