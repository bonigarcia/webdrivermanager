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

import static io.github.bonigarcia.wdm.config.DriverManagerType.OPERA;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

/**
 * Manager for Opera.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
public class OperaDriverManager extends WebDriverManager {

    @Override
    public DriverManagerType getDriverManagerType() {
        return OPERA;
    }

    @Override
    protected String getDriverName() {
        return "operadriver";
    }

    @Override
    protected String getDriverVersion() {
        return config().getOperaDriverVersion();
    }

    @Override
    protected String getBrowserVersion() {
        return config().getOperaVersion();
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setOperaDriverVersion(driverVersion);
    }

    @Override
    protected void setBrowserVersion(String browserVersion) {
        config().setOperaVersion(browserVersion);
    }

    @Override
    protected URL getDriverUrl() {
        return getDriverUrlCkeckingMirror(config().getOperaDriverUrl());
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return Optional.of(config().getOperaDriverMirrorUrl());
    }

    @Override
    protected Optional<String> getExportParameter() {
        return Optional.of(config().getOperaDriverExport());
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setOperaDriverUrl(url);
    }

    @Override
    protected String getCurrentVersion(URL url) {
        String currentVersion;
        if (config().isUseMirror()) {
            int i = url.getFile().lastIndexOf(SLASH);
            int j = url.getFile().substring(0, i).lastIndexOf(SLASH) + 1;
            currentVersion = url.getFile().substring(j, i);
            return currentVersion;
        } else {
            currentVersion = url.getFile().substring(
                    url.getFile().indexOf(SLASH + "v") + 2,
                    url.getFile().lastIndexOf(SLASH));
        }
        if (currentVersion.startsWith(".")) {
            currentVersion = currentVersion.substring(1);
        }
        return currentVersion;
    }

    @Override
    protected List<URL> getDriverUrls(String driverVersion) throws IOException {
        if (isUseMirror()) {
            String versionPath = driverVersion;
            if (!driverVersion.isEmpty()) {
                versionPath = driverVersion.startsWith("0") ? "v" + versionPath
                        : "v." + versionPath;
            }
            return getDriversFromMirror(getMirrorUrl().get(), versionPath);
        } else {
            return getDriversFromGitHub(driverVersion);
        }
    }

    @Override
    protected List<File> postDownload(File archive) {
        log.trace("Post processing for Opera: {}", archive);

        File extractFolder = archive.getParentFile()
                .listFiles(getFolderFilter())[0];
        if (!extractFolder.isFile()) {
            File target;
            try {
                log.trace("Opera extract folder (to be deleted): {}",
                        extractFolder);
                File[] listFiles = extractFolder.listFiles();
                int i = 0;
                File operadriver;
                boolean isOperaDriver;
                do {
                    if (i >= listFiles.length) {
                        throw new WebDriverManagerException(
                                "Driver for Opera not found in zip file");
                    }
                    operadriver = listFiles[i];
                    isOperaDriver = operadriver.getName()
                            .contains(getDriverName());

                    i++;
                    log.trace("{} is valid: {}", operadriver, isOperaDriver);
                } while (!isOperaDriver);
                log.info("Operadriver: {}", operadriver);

                target = new File(archive.getParentFile().getAbsolutePath(),
                        operadriver.getName());
                log.trace("Operadriver target: {}", target);

                downloader.renameFile(operadriver, target);
            } finally {
                downloader.deleteFolder(extractFolder);
            }
            return singletonList(target);
        } else {
            return super.postDownload(archive);
        }
    }

    @Override
    protected Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion) {
        return empty();
    }

    @Override
    protected Capabilities getCapabilities() {
        ChromeOptions options = new ChromeOptions();
        if (!isUsingDocker()) {
            Optional<Path> browserPath = getBrowserPath();
            if (browserPath.isPresent()) {
                options.setBinary(browserPath.get().toFile());
            }
        }
        return options;
    }

    @Override
    public WebDriverManager exportParameter(String exportParameter) {
        config().setOperaDriverExport(exportParameter);
        return this;
    }

}
