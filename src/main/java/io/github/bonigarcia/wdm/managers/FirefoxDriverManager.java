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
 * limitations under the License.
 *
 */
package io.github.bonigarcia.wdm.managers;

import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static java.util.Optional.empty;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxOptions;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * Manager for Firefox.
 *
 * @author Boni Garcia
 * @since 1.5.0
 */
public class FirefoxDriverManager extends WebDriverManager {

    @Override
    public DriverManagerType getDriverManagerType() {
        return FIREFOX;
    }

    @Override
    protected String getDriverName() {
        return "geckodriver";
    }

    @Override
    protected String getDriverVersion() {
        return config().getGeckoDriverVersion();
    }

    @Override
    protected String getBrowserVersion() {
        return config().getFirefoxVersion();
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setGeckoDriverVersion(driverVersion);
    }

    @Override
    protected void setBrowserVersion(String browserVersion) {
        config().setFirefoxVersion(browserVersion);
    }

    @Override
    protected URL getDriverUrl() {
        return getDriverUrlCkeckingMirror(config().getFirefoxDriverUrl());
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return Optional.of(config().getFirefoxDriverMirrorUrl());
    }

    @Override
    protected Optional<String> getExportParameter() {
        return Optional.of(config().getFirefoxDriverExport());
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setFirefoxDriverUrl(url);
    }

    @Override
    protected List<URL> getDriverUrls() throws IOException {
        return getDriversFromGitHub();
    }

    @Override
    protected String getCurrentVersion(URL url) {
        String currentVersion = url.getFile().substring(
                url.getFile().indexOf('-') + 1, url.getFile().lastIndexOf('-'));
        if (currentVersion.startsWith("v")) {
            currentVersion = currentVersion.substring(1);
        }
        return currentVersion;
    }

    @Override
    protected Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion) {
        return empty();
    }

    @Override
    protected Capabilities getCapabilities() {
        return new FirefoxOptions();
    }

    public WebDriverManager exportParameter(String exportParameter) {
        config().setFirefoxDriverExport(exportParameter);
        return instanceMap.get(getDriverManagerType());
    }

}
