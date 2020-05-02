/*
 * (C) Copyright 2018 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.DriverManagerType.SELENIUM_SERVER_STANDALONE;
import static java.util.Optional.empty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Manager for selenium-server-standalone.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.0.1
 */
public class SeleniumServerStandaloneManager extends WebDriverManager {

    @Override
    protected DriverManagerType getDriverManagerType() {
        return SELENIUM_SERVER_STANDALONE;
    }

    @Override
    protected String getDriverName() {
        return "selenium-server-standalone";
    }

    @Override
    protected String getDriverVersion() {
        return config().getSeleniumServerStandaloneVersion();
    }

    @Override
    protected URL getDriverUrl() {
        return config().getSeleniumServerStandaloneUrl();
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return empty();
    }

    @Override
    protected Optional<String> getExportParameter() {
        return empty();
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setSeleniumServerStandaloneVersion(driverVersion);
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setSeleniumServerStandaloneUrl(url);
    }

    @Override
    protected File postDownload(File archive) {
        return archive;
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        return empty();
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        return getDriversFromXml(getDriverUrl(), "//Contents/Key");
    }

    @Override
    protected Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion) {
        return empty();
    }

}
