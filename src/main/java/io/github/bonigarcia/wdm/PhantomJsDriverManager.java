/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
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
 * limitations under the License..
 *
 */
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.DriverManagerType.PHANTOMJS;
import static java.io.File.separator;
import static java.util.Optional.empty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Manager for PhantomJs.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsDriverManager extends WebDriverManager {

    @Override
    protected DriverManagerType getDriverManagerType() {
        return PHANTOMJS;
    }

    @Override
    protected String getDriverName() {
        return "phantomjs";
    }

    @Override
    protected String getDriverVersion() {
        return config().getPhantomjsDriverVersion();
    }

    @Override
    protected URL getDriverUrl() {
        return config().getPhantomjsDriverUrl();
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return Optional.of(config().getPhantomjsDriverMirrorUrl());
    }

    @Override
    protected Optional<String> getExportParameter() {
        return Optional.of(config().getPhantomjsDriverExport());
    }

    @Override
    protected void setDriverVersion(String version) {
        config().setPhantomjsDriverVersion(version);
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setPhantomjsDriverUrl(url);
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        URL driverUrl = getDriverUrl();
        log.info("Reading {} to seek {}", driverUrl, getDriverName());
        return getDriversFromMirror(driverUrl);
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName) {
        String file = url.getFile();
        file = url.getFile().substring(file.lastIndexOf(SLASH), file.length());
        int matchIndex = file.indexOf(driverName);
        String currentVersion = file
                .substring(matchIndex + driverName.length() + 1, file.length());
        int dashIndex = currentVersion.indexOf('-');

        if (dashIndex != -1) {
            String beta = currentVersion.substring(dashIndex + 1,
                    dashIndex + 1 + BETA.length());
            if (beta.equalsIgnoreCase(BETA)) {
                dashIndex = currentVersion.indexOf('-', dashIndex + 1);
            }
            currentVersion = dashIndex != -1
                    ? currentVersion.substring(0, dashIndex)
                    : "";
        } else {
            currentVersion = "";
        }

        return currentVersion;
    }

    @Override
    protected String preDownload(String target, String version) {
        int iSeparator = target.indexOf(version) - 1;
        int iDash = target.lastIndexOf(version) + version.length();
        int iPoint = target.lastIndexOf(".tar") != -1
                ? target.lastIndexOf(".tar")
                : target.lastIndexOf(".zip");
        target = target.substring(0, iSeparator + 1)
                + target.substring(iDash + 1, iPoint)
                + target.substring(iSeparator);
        target = target.replace("beta-", "");
        return target;
    }

    @Override
    protected File postDownload(File archive) {
        log.trace("PhantomJS package name: {}", archive);

        File extractFolder = archive.getParentFile()
                .listFiles(getFolderFilter())[0];
        log.trace("PhantomJS extract folder (to be deleted): {}",
                extractFolder);

        File binFolder = new File(
                extractFolder.getAbsoluteFile() + separator + "bin");
        // Exception for older version of PhantomJS
        int binaryIndex = 0;
        if (!binFolder.exists()) {
            binFolder = extractFolder;
            binaryIndex = 3;
        }

        log.trace("PhantomJS bin folder: {} (index {})", binFolder,
                binaryIndex);

        File phantomjs = binFolder.listFiles()[binaryIndex];
        log.trace("PhantomJS binary: {}", phantomjs);

        File target = new File(archive.getParentFile().getAbsolutePath(),
                phantomjs.getName());
        log.trace("PhantomJS target: {}", target);

        downloader.renameFile(phantomjs, target);
        downloader.deleteFolder(extractFolder);
        return target;
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        return empty();
    }

}
