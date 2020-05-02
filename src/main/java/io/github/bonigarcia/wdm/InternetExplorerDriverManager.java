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

import static io.github.bonigarcia.wdm.DriverManagerType.IEXPLORER;
import static java.util.Optional.empty;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Manager for Internet Explorer.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class InternetExplorerDriverManager extends WebDriverManager {

    @Override
    protected DriverManagerType getDriverManagerType() {
        return IEXPLORER;
    }

    @Override
    protected String getDriverName() {
        return "IEDriverServer";
    }

    @Override
    protected String getDriverVersion() {
        return config().getInternetExplorerDriverVersion();
    }

    @Override
    protected URL getDriverUrl() {
        return config().getInternetExplorerDriverUrl();
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return empty();
    }

    @Override
    protected Optional<String> getExportParameter() {
        return Optional.of(config().getInternetExplorerDriverExport());
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setInternetExplorerDriverVersion(driverVersion);
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setInternetExplorerDriverUrl(url);
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        return getDriversFromXml(getDriverUrl(), "//Contents/Key");
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        return empty();
    }

    @Override
    protected Optional<String> getDriverVersionFromRepository(
            Optional<String> driverVersion) {
        return empty();
    }

}
