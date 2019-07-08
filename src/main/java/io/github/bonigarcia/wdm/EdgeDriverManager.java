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

import static io.github.bonigarcia.wdm.Architecture.X64;
import static io.github.bonigarcia.wdm.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.OperatingSystem.MAC;
import static io.github.bonigarcia.wdm.Shell.getVersionFromPowerShellOutput;
import static io.github.bonigarcia.wdm.Shell.runAndWait;
import static java.util.Collections.sort;
import static java.util.Optional.empty;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.jsoup.Jsoup.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Manager for Microsoft Edge.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeDriverManager extends WebDriverManager {

    @Override
    protected DriverManagerType getDriverManagerType() {
        return EDGE;
    }

    @Override
    protected String getDriverName() {
        return "msedgedriver";
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
        listVersions = new ArrayList<>();
        List<URL> urlList = new ArrayList<>();

        URL driverUrl = getDriverUrl();
        log.debug("Reading {} to find out the latest version of Edge driver",
                driverUrl);

        try (InputStream in = httpClient
                .execute(httpClient.createHttpGet(driverUrl)).getEntity()
                .getContent()) {
            Document doc = parse(in, null, "");

            Elements downloadLinks = doc
                    .select("ul.driver-downloads li.driver-download > a");
            List<Optional<Element>> versionParagraph = downloadLinks.stream()
                  .map(link->link.parent().select("p.driver-download__meta"))
                  .map(paragraphs->paragraphs.size() == 1 ? Optional.of(paragraphs.get(0)) : Optional.<Element>empty())
                  .collect(Collectors.toList());

            log.trace("Download links:\n{}", downloadLinks);
            log.trace("Sibling version paragraphs:\n{}", versionParagraph);

            for (int i = 0; i < versionParagraph.size(); i++) {
                Element downloadLink = downloadLinks.get(i);
                Optional<Element> optionalParagraph = versionParagraph.get(i);
                if ( !optionalParagraph.isPresent() ) {
                    log.trace("No describing paragraph was found for download link {}.", downloadLink);
                    continue;
                }
                Element paragraph = optionalParagraph.get();
                String[] version = paragraph.text().split(" ");
                String v = version[1];
                listVersions.add(v);

                if (isChromiumBased(v)) {
                    // Edge driver version 75 and above
                    int childIndex = 0;
                    if (!paragraph.text().contains("76.0.174.0")) {
                        if (config().getOs().equals(MAC.name())) {
                            childIndex = 2;
                        } else if (config().getArchitecture() == X64) {
                            childIndex = 1;
                        }
                    }
                    urlList.add(
                            new URL(paragraph.child(childIndex).attr("href")));
                } else {
                    // Older versions
                    if (!v.equalsIgnoreCase("version")) {
                        urlList.add(new URL(
                                downloadLink.attr("href")));
                    }
                }
            }

            log.trace("Edge driver URL list {}", urlList);
            return urlList;
        }
    }

    @Override
    public List<String> getVersions() {
        httpClient = new HttpClient(config());
        try {
            getDrivers();
            sort(listVersions, new VersionComparator());
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
        return listFiles.iterator().next();
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        if (IS_OS_WINDOWS) {
            Optional<String> msedgeVersion = getDefaultBrowserVersion(
                    getProgramFilesEnv(),
                    "\\\\Microsoft\\\\Edge Dev\\\\Application\\\\msedge.exe",
                    "", "", "--version", getDriverManagerType().toString());
            String browserVersionOutput;
            if (msedgeVersion.isPresent()) {
                browserVersionOutput = msedgeVersion.get();
                log.debug("Edge Dev (based on Chromium) version {} found",
                        browserVersionOutput);
            } else {
                browserVersionOutput = runAndWait("powershell",
                        "get-appxpackage Microsoft.MicrosoftEdge");
            }
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional.of(
                        getVersionFromPowerShellOutput(browserVersionOutput));
            }
        }
        return empty();
    }

    private boolean isChromiumBased(String version) {
        long countDot = version.chars().filter(ch -> ch == '.').count();
        log.trace("Edge driver version {} ({} dots)", version, countDot);
        return countDot > 1;
    }

}
