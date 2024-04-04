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

import static io.github.bonigarcia.wdm.config.Architecture.ARM64;
import static io.github.bonigarcia.wdm.config.Architecture.X32;
import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static java.util.Locale.ROOT;
import static java.util.Optional.empty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.namespace.NamespaceContext;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.online.LastGoodVersions;
import io.github.bonigarcia.wdm.online.Parser;
import io.github.bonigarcia.wdm.versions.VersionDetector;
import io.github.bonigarcia.wdm.webdriver.OptionsWithArguments;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
public class ChromeDriverManager extends WebDriverManager {

    private static final String CHROMEDRIVER_DOWNLOAD_OLD_PATTERN = "https://chromedriver.storage.googleapis.com/%s/chromedriver_%s%s.zip";

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
    protected List<URL> getDriverUrls(String driverVersion) throws IOException {
        if (isUseMirror()) {
            return getDriversFromMirror(getMirrorUrl().get(), driverVersion);
        } else {
            String cftUrl = config.getChromeLastGoodVersionsUrl();
            LastGoodVersions versions = Parser.parseJson(getHttpClient(),
                    cftUrl, LastGoodVersions.class);
            return versions.channels.stable.downloads.chromedriver.stream()
                    .map(platformUrl -> {
                        try {
                            return new URL(platformUrl.url);
                        } catch (MalformedURLException e) {
                            throw new WebDriverException(
                                    "Incorrect CfT URL " + platformUrl.url);
                        }
                    }).collect(Collectors.toList());
        }
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

    @Override
    protected NamespaceContext getNamespaceContext() {
        return S3_NAMESPACE_CONTEXT;
    }

    @Override
    protected Optional<URL> buildUrl(String driverVersion) {
        return buildUrl(driverVersion, config());
    }

    Optional<URL> buildUrl(String driverVersion, Config config) {
        Optional<URL> optionalUrl = empty();
        if (!config.isUseMirror() && !isNullOrEmpty(driverVersion)) {
            String downloadUrlPattern = config.getChromeDownloadUrlPattern();
            OperatingSystem os = config.getOperatingSystem();
            Architecture arch = config.getArchitecture();
            String archLabel = os.isLinux() ? "64"
                    : arch.toString().toLowerCase(ROOT);
            if (os.isWin() && !X32.equals(arch)) {
                archLabel = "64";
            }
            if (os.isMac() && !ARM64.equals(arch)) {
                archLabel = "x64";
            }
            String separator = os.isMac() ? "-" : "";
            String label = os.getName() + separator + archLabel;

            String builtUrl = String.format(downloadUrlPattern, driverVersion,
                    label, label);
            if (!VersionDetector.isCfT(driverVersion)) {
                archLabel = os.isWin() ? "32" : "64";
                builtUrl = String.format(CHROMEDRIVER_DOWNLOAD_OLD_PATTERN,
                        driverVersion, os.getName(), archLabel);
            }
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
            options = new OptionsWithArguments("chrome", "goog:chromeOptions");
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

    @Override
    public WebDriverManager exportParameter(String exportParameter) {
        config().setChromeDriverExport(exportParameter);
        return this;
    }

}
