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
package io.github.bonigarcia.wdm.managers;

import static io.github.bonigarcia.wdm.config.DriverManagerType.IEXPLORER;
import static java.util.Optional.empty;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.NamespaceContext;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ie.InternetExplorerOptions;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

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

    @Override
    protected String getDriverName() {
        return "IEDriverServer";
    }

    @Override
    protected String getDriverVersion() {
        return config().getInternetExplorerDriverVersion();
    }

    @Override
    protected String getBrowserVersion() {
        return "";
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setInternetExplorerDriverVersion(driverVersion);
    }

    @Override
    protected void setBrowserVersion(String browserVersion) {
        // Nothing required
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
    protected void setDriverUrl(URL url) {
        config().setInternetExplorerDriverUrl(url);
    }

    @Override
    protected List<URL> getDriverUrls() throws IOException {
        return getDriversFromXml(getDriverUrl(), "//s3:Contents/s3:Key",
                getS3NamespaceContext());
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

    public WebDriverManager exportParameter(String exportParameter) {
        config().setInternetExplorerDriverExport(exportParameter);
        return instanceMap.get(getDriverManagerType());
    }

}
