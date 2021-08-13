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

import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static java.util.Optional.empty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.NamespaceContext;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.BrowserType;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.webdriver.OptionsWithArguments;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
public class ChromeDriverManager extends WebDriverManager {

    @Override
    public DriverManagerType getDriverManagerType() {
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
    protected String getBrowserVersion() {
        return config().getChromeVersion();
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setChromeDriverVersion(driverVersion);
    }

    @Override
    protected void setBrowserVersion(String browserVersion) {
        config().setChromeVersion(browserVersion);
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
    protected void setDriverUrl(URL url) {
        config().setChromeDriverUrl(url);
    }

    @Override
    protected List<URL> getDriverUrls() throws IOException {
        Optional<URL> mirrorUrl = getMirrorUrl();
        if (mirrorUrl.isPresent() && config().isUseMirror()) {
            return getDriversFromMirror(mirrorUrl.get());
        } else {
            return getDriversFromXml(getDriverUrl(), "//s3:Contents/s3:Key",
                    getS3NamespaceContext());
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
    protected Optional<String> getLatestDriverVersionFromRepository() {
        if (config().isUseBetaVersions()
                || config().isAvoidReadReleaseFromRepository()) {
            return empty();
        } else {
            return getDriverVersionFromRepository(empty());
        }
    }

    @Override
    protected Charset getVersionCharset() {
        return StandardCharsets.UTF_8;
    }

    @Override
    protected NamespaceContext getNamespaceContext() {
        return S3_NAMESPACE_CONTEXT;
    }

    @Override
    protected Optional<URL> buildUrl(String driverVersion) {
        Optional<URL> optionalUrl = empty();
        if (!config().isUseMirror()) {
            String downloadUrlPattern = config().getChromeDownloadUrlPattern();
            OperatingSystem os = config().getOperatingSystem();
            String arch = os.isWin() ? "32" : "64";
            String builtUrl = String.format(downloadUrlPattern, driverVersion,
                    os.getName(), arch);
            log.debug("Using URL built from repository pattern: {}", builtUrl);
            try {
                optionalUrl = Optional.of(new URL(builtUrl));
            } catch (MalformedURLException e) {
                log.warn("Error building URL from pattern {} {}", builtUrl,
                        e.getMessage());
            }
        }
        return optionalUrl;
    }

    @Override
    protected Capabilities getCapabilities() {
        Capabilities options = new ChromeOptions();
        try {
            addDefaultArgumentsForDocker(options);
        } catch (Exception e) {
            log.error(
                    "Exception adding default arguments for Docker, retyring with custom class");
            options = new OptionsWithArguments(BrowserType.CHROME,
                    "goog:chromeOptions");
            try {
                addDefaultArgumentsForDocker(options);
            } catch (Exception e1) {
                log.error("Exception getting default capabilities", e);
            }
        }
        return options;
    }

    @Override
    public WebDriverManager browserInDockerAndroid() {
        this.dockerEnabled = true;
        this.androidEnabled = true;
        return this;
    }

    public WebDriverManager exportParameter(String exportParameter) {
        config().setChromeDriverExport(exportParameter);
        return this;
    }

}
