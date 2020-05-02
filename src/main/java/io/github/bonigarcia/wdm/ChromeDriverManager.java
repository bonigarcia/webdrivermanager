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

import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class ChromeDriverManager extends WebDriverManager {

    @Override
    protected DriverManagerType getDriverManagerType() {
        return CHROME;
    }

    @Override
    protected String getDriverName() {
        return "chromedriver";
    }

    @Override
    protected String getDriverVersion() {
        return config().getChromeDriverVersion();
    }

    @Override
    protected URL getDriverUrl() {
        return getDriverUrlCkeckingMirror(config().getChromeDriverUrl());
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return Optional.of(config().getChromeDriverMirrorUrl());
    }

    @Override
    protected Optional<String> getExportParameter() {
        return Optional.of(config().getChromeDriverExport());
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setChromeDriverVersion(driverVersion);
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setChromeDriverUrl(url);
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        Optional<URL> mirrorUrl = getMirrorUrl();
        if (mirrorUrl.isPresent() && config().isUseMirror()) {
            return getDriversFromMirror(mirrorUrl.get());
        } else {
            return getDriversFromXml(getDriverUrl(), "//Contents/Key");
        }
    }

    @Override
    protected String getCurrentVersion(URL url) {
        if (config().isUseMirror()) {
            int i = url.getFile().lastIndexOf(SLASH);
            int j = url.getFile().substring(0, i).lastIndexOf(SLASH) + 1;
            return url.getFile().substring(j, i);
        } else {
            return super.getCurrentVersion(url);
        }
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        String[] programFilesEnvs = { getOtherProgramFilesEnv(), "LOCALAPPDATA",
                getProgramFilesEnv() };
        String[] winBrowserNames = {
                "\\\\Google\\\\Chrome\\\\Application\\\\chrome.exe" };
        Optional<String> browserVersion = getDefaultBrowserVersion(
                programFilesEnvs, winBrowserNames, "google-chrome",
                "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                "--version", getDriverManagerType().toString());

        if (IS_OS_WINDOWS && !browserVersion.isPresent()) {
            log.debug(
                    "Chrome version not discovered using wmic... trying reading the registry");
            browserVersion = getBrowserVersionFromWinRegistry(
                    "HKCU\\Software\\Google\\Chrome\\BLBeacon", "version");
        }

        return browserVersion;
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
        return StandardCharsets.UTF_8;
    }

}
