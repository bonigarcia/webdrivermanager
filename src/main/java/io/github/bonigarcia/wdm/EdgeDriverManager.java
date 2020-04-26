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

import static io.github.bonigarcia.wdm.DriverManagerType.EDGE;
import static java.util.Optional.empty;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Manager for Microsoft Edge.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeDriverManager extends WebDriverManager {

    protected static final String LATEST_STABLE = "LATEST_STABLE";

    @Override
    protected DriverManagerType getDriverManagerType() {
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
        return getDriversFromXml(
                new URL(getDriverUrl() + "?restype=container&comp=list"),
                "//Blob/Name");
    }

    @Override
    protected String preDownload(String target, String version) {
        int iVersion = target.indexOf(version);
        if (iVersion != -1) {
            target = target.substring(0, iVersion)
                    + config().getArchitecture().name().toLowerCase()
                    + File.separator + target.substring(iVersion);
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
        String[] winBrowserNames = {
                "\\\\Microsoft\\\\Edge\\\\Application\\\\msedge.exe",
                "\\\\Microsoft\\\\Edge Beta\\\\Application\\\\msedge.exe",
                "\\\\Microsoft\\\\Edge Dev\\\\Application\\\\msedge.exe" };
        String macBrowserName = "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge";
        String version = IS_OS_MAC_OSX ? "-version" : "--version";

        return getDefaultBrowserVersion(programFilesEnvs, winBrowserNames, "",
                macBrowserName, version, getDriverManagerType().toString());
    }

    @Override
    protected Optional<String> getLatestDriverVersionFromRepository() {
        return getDriverVersionFromRepository(empty());
    }

    @Override
    protected Charset getVersionCharset() {
        return StandardCharsets.UTF_16;
    }

    @Override
    protected String getLatestVersionLabel() {
        return LATEST_STABLE;
    }

}
