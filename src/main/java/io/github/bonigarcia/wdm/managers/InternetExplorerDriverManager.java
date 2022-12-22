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

import static io.github.bonigarcia.wdm.config.DriverManagerType.IEXPLORER;
import static java.util.Optional.empty;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import io.github.bonigarcia.wdm.versions.UrlComparator;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.xml.namespace.NamespaceContext;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ie.InternetExplorerOptions;

/**
 * Manager for Internet Explorer.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
public class InternetExplorerDriverManager extends WebDriverManager {

    @Override
    public DriverManagerType getDriverManagerType() {
        return IEXPLORER;
    }

    protected String getDriverName() {
        return "IEDriverServer";
    }

    @Override
    protected String getDriverVersion() {
        return config().getIExplorerDriverVersion();
    }

    @Override
    protected String getBrowserVersion() {
        return "";
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setIExplorerDriverVersion(driverVersion);
    }

    @Override
    protected void setBrowserVersion(String browserVersion) {
        // Nothing required
    }

    @Override
    protected URL getDriverUrl() {
        return config().getIExplorerDriverUrl();
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return empty();
    }

    @Override
    protected Optional<String> getExportParameter() {
        return Optional.of(config().getIExplorerDriverExport());
    }

    @Override
    protected void setDriverUrl(URL url) {
        config().setIExplorerDriverUrl(url);
    }

    @Override
    protected List<URL> getDriverUrls(String driverVersion) throws IOException {
        List<URL> driverUrls = getDriversFromGitHub(driverVersion);
        driverUrls.sort(new UrlComparator());
        return driverUrls;
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
    protected NamespaceContext getNamespaceContext() {
        return S3_NAMESPACE_CONTEXT;
    }

    @Override
    public Optional<Path> getBrowserPath() {
        throw new WebDriverManagerException("The browser path of "
                + getDriverManagerType().getBrowserName()
                + " cannot be found since it is a legacy browser and not maintained in the commands database");
    }

    @Override
    protected Capabilities getCapabilities() {
        return new InternetExplorerOptions();
    }

    @Override
    protected String getCurrentVersion(URL url) {
        String currentVersion = super.getCurrentVersion(url);
        String versionRegex = config().getBrowserVersionDetectionRegex();
        return currentVersion.replaceAll(versionRegex, "");
    }

    @Override
    public WebDriverManager exportParameter(String exportParameter) {
        config().setIExplorerDriverExport(exportParameter);
        return this;
    }

}
