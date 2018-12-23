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

import java.io.IOException;
import java.net.URL;
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
        return config().getChromeDriverUrl();
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
    protected void setDriverVersion(String version) {
        config().setChromeDriverVersion(version);
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setChromeDriverUrl(url);
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        URL driverUrl = getDriverUrl();
        List<URL> urls;
        if (isUsingTaobaoMirror()) {
            urls = getDriversFromMirror(driverUrl);
        } else {
            urls = getDriversFromXml(driverUrl);
        }
        return urls;
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName) {
        if (isUsingTaobaoMirror()) {
            int i = url.getFile().lastIndexOf(SLASH);
            int j = url.getFile().substring(0, i).lastIndexOf(SLASH) + 1;
            return url.getFile().substring(j, i);
        } else {
            return super.getCurrentVersion(url, driverName);
        }
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        return getDefaultBrowserVersion(getProgramFilesEnv(),
                "\\\\Google\\\\Chrome\\\\Application\\\\chrome.exe",
                "google-chrome",
                "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                "--version", getDriverManagerType().toString());
    }

}
