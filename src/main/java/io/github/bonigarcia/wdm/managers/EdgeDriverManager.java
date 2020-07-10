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
package io.github.bonigarcia.wdm.managers;

import static io.github.bonigarcia.wdm.config.DriverManagerType.EDGE;
import static java.util.Optional.empty;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * Manager for Microsoft Edge.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeDriverManager extends WebDriverManager {

    protected static final String LATEST_STABLE = "LATEST_STABLE";

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
    protected List<URL> getDriverUrls() throws IOException {
        return getDriversFromXml(
                new URL(getDriverUrl() + "?restype=container&comp=list"),
                "//Blob/Name", empty());
    }

    @Override
    protected String preDownload(String target, String driverVersion) {
        int iVersion = target.indexOf(driverVersion);
        if (iVersion != -1) {
            target = target.substring(0, iVersion)
                    + config().getArchitecture().name().toLowerCase()
                    + File.separator + target.substring(iVersion);
        }
        log.trace("Pre-download in EdgeDriver -- target={}, driverVersion={}",
                target, driverVersion);
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
    protected Optional<String> getBrowserVersionFromTheShell() {
        String[] programFilesEnvs = { getOtherProgramFilesEnv(),
                getProgramFilesEnv() };
        String[] winBrowserNames = {
                "\\\\Microsoft\\\\Edge\\\\Application\\\\msedge.exe",
                "\\\\Microsoft\\\\Edge Beta\\\\Application\\\\msedge.exe",
                "\\\\Microsoft\\\\Edge Dev\\\\Application\\\\msedge.exe" };
        String macBrowserName = "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge";
        String versionFlag = IS_OS_MAC_OSX ? "-version" : "--version";

        return versionDetector.getDefaultBrowserVersion(programFilesEnvs,
                winBrowserNames, "", macBrowserName, versionFlag,
                getDriverManagerType().toString());
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
    protected Optional<URL> buildUrl(String driverVersion) {
        Optional<URL> optionalUrl = empty();
        if (!config().isUseMirror()) {
            String downloadUrlPattern = config().getEdgeDownloadUrlPattern();
            String os = config().getOs().toLowerCase();
            String arch = os.contains("win")
                    ? arch = config().getArchitecture().toString()
                    : "64";
            String builtUrl = String.format(downloadUrlPattern, driverVersion,
                    os, arch);
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

}
