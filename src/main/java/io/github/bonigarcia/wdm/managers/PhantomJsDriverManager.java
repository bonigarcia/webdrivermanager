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
package io.github.bonigarcia.wdm.managers;

import static io.github.bonigarcia.wdm.config.DriverManagerType.PHANTOMJS;
import static io.github.bonigarcia.wdm.online.UrlHandler.BETA;
import static java.io.File.separator;
import static java.util.Optional.empty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * Manager for PhantomJs.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsDriverManager extends WebDriverManager {

    @Override
    public DriverManagerType getDriverManagerType() {
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
        return getDriverUrlCkeckingMirror(config().getPhantomjsDriverUrl());
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
    protected void setDriverVersion(String driverVersion) {
        config().setPhantomjsDriverVersion(driverVersion);
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setPhantomjsDriverUrl(url);
    }

    @Override
    protected List<URL> getDriverUrls() throws IOException {
        return getDriversFromBitBucket();
    }

    @Override
    protected String getCurrentVersion(URL url) {
        String driverName = getDriverName();
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
    protected File postDownload(File archive) {
        log.trace("PhantomJS package name: {}", archive);

        File extractFolder = archive.getParentFile()
                .listFiles(getFolderFilter())[0];
        log.trace("PhantomJS extract folder (to be deleted): {}",
                extractFolder);

        File binFolder = new File(
                extractFolder.getAbsoluteFile() + separator + "bin");
        // Exception for older versions of PhantomJS
        int driverIndex = 0;
        if (!binFolder.exists()) {
            binFolder = extractFolder;
            driverIndex = 3;
        }

        log.trace("PhantomJS bin folder: {} (index {})", binFolder,
                driverIndex);

        File phantomjs = binFolder.listFiles()[driverIndex];
        log.trace("PhantomJS driver: {}", phantomjs);

        File target = new File(archive.getParentFile().getAbsolutePath(),
                phantomjs.getName());
        log.trace("PhantomJS target: {}", target);

        downloader.renameFile(phantomjs, target);
        downloader.deleteFolder(extractFolder);
        return target;
    }

    @Override
    protected Optional<String> getBrowserVersionFromTheShell() {
        return empty();
    }

    @Override
    protected Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion) {
        return empty();
    }

    @Override
    protected String getBrowserVersion() {
        return "";
    }

    @Override
    protected void setBrowserVersion(String browserVersion) {
        // Nothing required
    }

}
