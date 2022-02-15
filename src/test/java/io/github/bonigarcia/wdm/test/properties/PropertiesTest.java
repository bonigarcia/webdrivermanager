/*
 * (C) Copyright 2018 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.properties;

import static io.github.bonigarcia.wdm.config.Architecture.X32;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Config;

/**
 * Using different properties.
 *
 * @author Boni Garcia
 * @since 2.1.1
 */
class PropertiesTest {
    private static final String CUSTOM_VALUE = "custom_value";
    private static final String CUSTOM_URL = "https://custom_url";

    @Test
    void testCustomProperties() {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        wdm.config().setProperties("wdm-test.properties");
        wdm.setup();
        String driverPath = wdm.getDownloadedDriverPath();
        File driver = new File(driverPath);
        assertThat(driver).exists();

        wdm.config().isAvoidFallback();
    }

    @Test
    @DisplayName("Should have some properties with expected default values")
    void propertiesWithDefault() throws Exception {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        Config config = wdm.config();
        wdm.setup();

        assertThat(new File(config.getCachePath())).isEqualTo(
                new File(System.getProperty("user.home"), ".cache/selenium"));
        assertThat(config.getResolutionCachePath()).isEqualTo(
                new File(System.getProperty("user.home"), ".cache/selenium"));
        assertThat(config.isForceDownload()).isFalse();
        assertThat(config.isUseMirror()).isFalse();
        assertThat(config.isUseBetaVersions()).isFalse();
        assertThat(config.isAvoidExport()).isFalse();
        assertThat(config.isAvoidOutputTree()).isFalse();
        assertThat(config.isAvoidBrowserDetection()).isFalse();
        assertThat(config.isAvoidResolutionCache()).isFalse();
        assertThat(config.isAvoidFallback()).isFalse();
        assertThat(config.isAvoidReadReleaseFromRepository()).isFalse();
        assertThat(config.isAvoidTmpFolder()).isFalse();

        assertThat(config.isClearResolutionCache()).isFalse();
        assertThat(config.isClearDriverCache()).isFalse();

        assertThat(config.getTimeout()).isEqualTo(30);
        assertThat(config.getResolutionCache())
                .isEqualTo("resolution.properties");
        assertThat(config.getTtl()).isEqualTo(86400);
        assertThat(config.getTtlForBrowsers()).isEqualTo(3600);
        assertThat(config.getBrowserVersionDetectionRegex())
                .isEqualTo("[^\\d^\\.]");
        assertThat(config.getDefaultBrowser()).isEqualTo("chrome");

        assertThat(config.getServerPort()).isEqualTo(4444);
        assertThat(config.getServerPath()).isEqualTo("/");
        assertThat(config.getServerTimeoutSec()).isEqualTo(60);

        assertThat(config.getChromeDriverUrl()).isEqualTo(
                new URL("https://chromedriver.storage.googleapis.com/"));
        assertThat(config.getChromeDriverMirrorUrl()).isEqualTo(new URL(
                "https://registry.npmmirror.com/-/binary/chromedriver/"));
        assertThat(config.getChromeDriverExport())
                .isEqualTo("webdriver.chrome.driver");
        assertThat(config.getChromeDownloadUrlPattern()).isEqualTo(
                "https://chromedriver.storage.googleapis.com/%s/chromedriver_%s%s.zip");

        assertThat(config.getOperaDriverUrl()).isEqualTo(new URL(
                "https://api.github.com/repos/operasoftware/operachromiumdriver/releases"));
        assertThat(config.getOperaDriverMirrorUrl()).isEqualTo(
                new URL("https://registry.npmmirror.com/-/binary/operadriver/"));
        assertThat(config.getOperaDriverExport())
                .isEqualTo("webdriver.opera.driver");

        assertThat(config.getEdgeDriverUrl())
                .isEqualTo(new URL("https://msedgedriver.azureedge.net/"));
        assertThat(config.getEdgeDriverExport())
                .isEqualTo("webdriver.edge.driver");
        assertThat(config.getEdgeDownloadUrlPattern()).isEqualTo(
                "https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/%s/edgedriver_%s%s.zip");

        assertThat(config.getIExplorerDriverUrl()).isEqualTo(new URL(
                "https://api.github.com/repos/SeleniumHQ/selenium/releases"));
        assertThat(config.getIExplorerDriverExport())
                .isEqualTo("webdriver.ie.driver");

        assertThat(config.getChromiumDriverSnapPath())
                .isEqualTo("/snap/bin/chromium.chromedriver");
        assertThat(config.isUseChromiumDriverSnap()).isTrue();

        assertThat(config.isVersionsPropertiesOnlineFirst()).isTrue();
        assertThat(config.getVersionsPropertiesUrl()).isEqualTo(new URL(
                "https://raw.githubusercontent.com/bonigarcia/webdrivermanager/master/src/main/resources/versions.properties"));

        assertThat(config.isCommandsPropertiesOnlineFirst()).isTrue();
        assertThat(config.getCommandsPropertiesUrl()).isEqualTo(new URL(
                "https://raw.githubusercontent.com/bonigarcia/webdrivermanager/master/src/main/resources/commands.properties"));

        assertThat(config.getDockerHubUrl())
                .isEqualTo("https://hub.docker.com/");
        assertThat(config.getDockerNetwork()).isEqualTo("bridge");
        assertThat(config.getDockerTimezone()).isEqualTo("Etc/UTC");
        assertThat(config.getDockerLang()).isEqualTo("EN");
        assertThat(config.getDockerShmSize()).isEqualTo("256m");
        assertThat(config.getDockerTmpfsSize()).isEqualTo("128m");
        assertThat(config.getDockerTmpfsMount()).isEqualTo("/tmp");
        assertThat(config.getDockerStopTimeoutSec()).isEqualTo(5);
        assertThat(config.isDockerEnabledVnc()).isFalse();
        assertThat(config.isDockerViewOnly()).isFalse();
        assertThat(config.isDockerEnabledRecording()).isFalse();
        assertThat(config.getDockerScreenResolution())
                .isEqualTo("1280x1080x24");
        assertThat(config.getDockerVncPassword()).isEqualTo("selenoid");
        assertThat(config.getDockerBrowserPort()).isEqualTo(4444);
        assertThat(config.getDockerVncPort()).isEqualTo(5900);
        assertThat(config.getDockerNoVncPort()).isEqualTo(6080);
        assertThat(config.getDockerRecordingFrameRate()).isEqualTo(12);
        assertThat(config.getDockerRecordingOutput()).isEqualTo(Paths.get("."));
        assertThat(config.getDockerBrowserSelenoidImageFormat())
                .isEqualTo("selenoid/vnc:%s_%s");
        assertThat(config.getDockerBrowserTwilioImageFormat())
                .isEqualTo("twilio/selenoid:%s_%s");
        assertThat(config.getDockerBrowserAerokubeImageFormat())
                .isEqualTo("browsers/%s:%s");
        assertThat(config.getDockerBrowserMobileImageFormat())
                .isEqualTo("selenoid/%s:%s");
        assertThat(config.getDockerRecordingImage())
                .isEqualTo("selenoid/video-recorder:7.1");
        assertThat(config.getDockerNoVncImage())
                .isEqualTo("bonigarcia/novnc:1.1.0");
        assertThat(config.getDockerDefaultArgs()).isEqualTo("--disable-gpu");
        assertThat(config.isDockerLocalFallback()).isTrue();
    }

    @Test
    @DisplayName("Should have some properties without default values")
    void propertiesWithoutDefault() {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        Config config = wdm.config();
        wdm.setup();

        assertThat(config.getProxy()).isEmpty();
        assertThat(config.getProxyUser()).isEmpty();
        assertThat(config.getProxyPass()).isEmpty();

        assertThat(config.getRemoteAddress()).isEmpty();

        assertThat(config.getBrowserVersionDetectionCommand()).isEmpty();
        assertThat(config.getBrowserVersionDetectionCommand()).isEmpty();
        assertThat(config.getIgnoreVersions()).isEmpty();
        assertThat(config.getGitHubToken()).isEmpty();

        assertThat(config.getChromeDriverVersion()).isEmpty();
        assertThat(config.getChromeVersion()).isEmpty();

        assertThat(config.getEdgeDriverVersion()).isEmpty();
        assertThat(config.getEdgeVersion()).isEmpty();

        assertThat(config.getGeckoDriverVersion()).isEmpty();
        assertThat(config.getFirefoxVersion()).isEmpty();

        assertThat(config.getIExplorerDriverVersion()).isEmpty();

        assertThat(config.getOperaDriverVersion()).isEmpty();
        assertThat(config.getOperaVersion()).isEmpty();

        assertThat(config.getChromiumDriverVersion()).isEmpty();
        assertThat(config.getChromiumVersion()).isEmpty();

        assertThat(config.getDockerDaemonUrl()).isEmpty();
        assertThat(config.getDockerRecordingPrefix()).isEmpty();
        assertThat(config.getDockerCustomImage()).isEmpty();
        assertThat(config.getDockerVolumes()).isEmpty();
        assertThat(config.getDockerEnvVariables()).isEmpty();
        assertThat(config.getDockerPrivateEndpoint()).isEmpty();
        assertThat(config.getDockerExtraHosts()).isEmpty();
    }

    @Test
    @DisplayName("Should update config properties programmatically")
    void testPropertiesMutation() throws Exception {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        Config config = wdm.config();
        wdm.setup();

        // Default properties
        config.setArchitecture(X32);
        assertThat(config.getArchitecture()).isEqualTo(X32);

        config.setOs("Linux for the best");
        assertThat(config.getOs()).isEqualTo("Linux for the best");

        config.setCachePath(CUSTOM_VALUE);
        assertThat(config.getCachePath()).isEqualTo(CUSTOM_VALUE);

        config.setResolutionCachePath(CUSTOM_VALUE);
        assertThat(config.getResolutionCachePath())
                .isEqualTo(new File(CUSTOM_VALUE));

        config.setForceDownload(true);
        assertThat(config.isForceDownload()).isTrue();

        config.setUseMirror(true);
        assertThat(config.isUseMirror()).isTrue();

        config.setUseBetaVersions(true);
        assertThat(config.isUseBetaVersions()).isTrue();

        config.setAvoidExport(true);
        assertThat(config.isAvoidExport()).isTrue();

        config.setAvoidOutputTree(true);
        assertThat(config.isAvoidOutputTree()).isTrue();

        config.setAvoidBrowserDetection(true);
        assertThat(config.isAvoidBrowserDetection()).isTrue();

        config.setAvoidResolutionCache(true);
        assertThat(config.isAvoidResolutionCache()).isTrue();

        config.setAvoidFallback(true);
        assertThat(config.isAvoidFallback()).isTrue();

        config.setAvoidReadReleaseFromRepository(true);
        assertThat(config.isAvoidReadReleaseFromRepository()).isTrue();

        config.setAvoidTmpFolder(true);
        assertThat(config.isAvoidTmpFolder()).isTrue();

        config.setClearResolutionCache(true);
        assertThat(config.isClearResolutionCache()).isTrue();

        config.setClearDriverCache(true);
        assertThat(config.isClearDriverCache()).isTrue();

        config.setTimeout(15);
        assertThat(config.getTimeout()).isEqualTo(15);

        config.setResolutionCache(CUSTOM_VALUE);
        assertThat(config.getResolutionCache()).isEqualTo(CUSTOM_VALUE);

        config.setTtl(15);
        assertThat(config.getTtl()).isEqualTo(15);

        config.setTtlForBrowsers(15);
        assertThat(config.getTtlForBrowsers()).isEqualTo(15);

        config.setBrowserVersionDetectionRegex(CUSTOM_VALUE);
        assertThat(config.getBrowserVersionDetectionRegex())
                .isEqualTo(CUSTOM_VALUE);

        config.setDefaultBrowser(CUSTOM_VALUE);
        assertThat(config.getDefaultBrowser()).isEqualTo(CUSTOM_VALUE);

        config.setServerPort(15);
        assertThat(config.getServerPort()).isEqualTo(15);

        config.setServerPath(CUSTOM_VALUE);
        assertThat(config.getServerPath()).isEqualTo(CUSTOM_VALUE);

        config.setServerTimeoutSec(15);
        assertThat(config.getServerTimeoutSec()).isEqualTo(15);

        config.setChromeDriverUrl(new URL(CUSTOM_URL));
        assertThat(config.getChromeDriverUrl()).isEqualTo(new URL(CUSTOM_URL));

        config.setChromeDriverMirrorUrl(new URL(CUSTOM_URL));
        assertThat(config.getChromeDriverMirrorUrl())
                .isEqualTo(new URL(CUSTOM_URL));

        config.setChromeDriverExport(CUSTOM_VALUE);
        assertThat(config.getChromeDriverExport()).isEqualTo(CUSTOM_VALUE);

        config.setChromeDownloadUrlPattern(CUSTOM_VALUE);
        assertThat(config.getChromeDownloadUrlPattern())
                .isEqualTo(CUSTOM_VALUE);

        config.setOperaDriverUrl(new URL(CUSTOM_URL));
        assertThat(config.getOperaDriverUrl()).isEqualTo(new URL(CUSTOM_URL));

        config.setOperaDriverMirrorUrl(new URL(CUSTOM_URL));
        assertThat(config.getOperaDriverMirrorUrl())
                .isEqualTo(new URL(CUSTOM_URL));

        config.setOperaDriverExport(CUSTOM_VALUE);
        assertThat(config.getOperaDriverExport()).isEqualTo(CUSTOM_VALUE);

        config.setEdgeDriverUrl(new URL(CUSTOM_URL));
        assertThat(config.getEdgeDriverUrl()).isEqualTo(new URL(CUSTOM_URL));

        config.setEdgeDriverExport(CUSTOM_VALUE);
        assertThat(config.getEdgeDriverExport()).isEqualTo(CUSTOM_VALUE);

        config.setEdgeDownloadUrlPattern(CUSTOM_VALUE);
        assertThat(config.getEdgeDownloadUrlPattern()).isEqualTo(CUSTOM_VALUE);

        config.setIExplorerDriverUrl(new URL(CUSTOM_URL));
        assertThat(config.getIExplorerDriverUrl())
                .isEqualTo(new URL(CUSTOM_URL));

        config.setIExplorerDriverExport(CUSTOM_VALUE);
        assertThat(config.getIExplorerDriverExport()).isEqualTo(CUSTOM_VALUE);

        config.setChromiumDriverSnapPath(CUSTOM_VALUE);
        assertThat(config.getChromiumDriverSnapPath()).isEqualTo(CUSTOM_VALUE);

        config.setUseChromiumDriverSnap(false);
        assertThat(config.isUseChromiumDriverSnap()).isFalse();

        config.setVersionsPropertiesOnlineFirst(false);
        assertThat(config.isVersionsPropertiesOnlineFirst()).isFalse();

        config.setVersionsPropertiesUrl(new URL(CUSTOM_URL));
        assertThat(config.getVersionsPropertiesUrl())
                .isEqualTo(new URL(CUSTOM_URL));

        config.setCommandsPropertiesOnlineFirst(false);
        assertThat(config.isCommandsPropertiesOnlineFirst()).isFalse();

        config.setCommandsPropertiesUrl(new URL(CUSTOM_URL));
        assertThat(config.getCommandsPropertiesUrl())
                .isEqualTo(new URL(CUSTOM_URL));

        config.setDockerHubUrl(CUSTOM_VALUE);
        assertThat(config.getDockerHubUrl()).isEqualTo(CUSTOM_VALUE);

        config.setDockerNetwork(CUSTOM_VALUE);
        assertThat(config.getDockerNetwork()).isEqualTo(CUSTOM_VALUE);

        config.setDockerTimezone(CUSTOM_VALUE);
        assertThat(config.getDockerTimezone()).isEqualTo(CUSTOM_VALUE);

        config.setDockerLang(CUSTOM_VALUE);
        assertThat(config.getDockerLang()).isEqualTo(CUSTOM_VALUE);

        config.setDockerShmSize(CUSTOM_VALUE);
        assertThat(config.getDockerShmSize()).isEqualTo(CUSTOM_VALUE);

        config.setDockerTmpfsSize(CUSTOM_VALUE);
        assertThat(config.getDockerTmpfsSize()).isEqualTo(CUSTOM_VALUE);

        config.setDockerTmpfsMount(CUSTOM_VALUE);
        assertThat(config.getDockerTmpfsMount()).isEqualTo(CUSTOM_VALUE);

        config.setDockerStopTimeoutSec(15);
        assertThat(config.getDockerStopTimeoutSec()).isEqualTo(15);

        config.setDockerEnabledVnc(true);
        assertThat(config.isDockerEnabledVnc()).isTrue();

        config.setDockerViewOnly(true);
        assertThat(config.isDockerViewOnly()).isTrue();

        config.setDockerEnabledRecording(true);
        assertThat(config.isDockerEnabledRecording()).isTrue();

        config.setDockerScreenResolution(CUSTOM_VALUE);
        assertThat(config.getDockerScreenResolution()).isEqualTo(CUSTOM_VALUE);

        config.setDockerVncPassword(CUSTOM_VALUE);
        assertThat(config.getDockerVncPassword()).isEqualTo(CUSTOM_VALUE);

        config.setDockerBrowserPort(15);
        assertThat(config.getDockerBrowserPort()).isEqualTo(15);

        config.setDockerVncPort(15);
        assertThat(config.getDockerVncPort()).isEqualTo(15);

        config.setDockerNoVncPort(15);
        assertThat(config.getDockerNoVncPort()).isEqualTo(15);

        config.setDockerRecordingFrameRate(15);
        assertThat(config.getDockerRecordingFrameRate()).isEqualTo(15);

        config.setDockerRecordingOutput(Paths.get(CUSTOM_VALUE));
        assertThat(config.getDockerRecordingOutput())
                .isEqualTo(Paths.get(CUSTOM_VALUE));

        config.setDockerBrowserSelenoidImageFormat(CUSTOM_VALUE);
        assertThat(config.getDockerBrowserSelenoidImageFormat())
                .isEqualTo(CUSTOM_VALUE);

        config.setDockerBrowserTwilioImageFormat(CUSTOM_VALUE);
        assertThat(config.getDockerBrowserTwilioImageFormat())
                .isEqualTo(CUSTOM_VALUE);

        config.setDockerBrowserAerokubeImageFormat(CUSTOM_VALUE);
        assertThat(config.getDockerBrowserAerokubeImageFormat())
                .isEqualTo(CUSTOM_VALUE);

        config.setDockerBrowserMobileImageFormat(CUSTOM_VALUE);
        assertThat(config.getDockerBrowserMobileImageFormat())
                .isEqualTo(CUSTOM_VALUE);

        config.setDockerRecordingImage(CUSTOM_VALUE);
        assertThat(config.getDockerRecordingImage()).isEqualTo(CUSTOM_VALUE);

        config.setDockerNoVncImage(CUSTOM_VALUE);
        assertThat(config.getDockerNoVncImage()).isEqualTo(CUSTOM_VALUE);

        config.setDockerDefaultArgs(CUSTOM_VALUE);
        assertThat(config.getDockerDefaultArgs()).isEqualTo(CUSTOM_VALUE);

        config.setDockerLocalFallback(false);
        assertThat(config.isDockerLocalFallback()).isFalse();

        // Empty properties
        config.setProxy(CUSTOM_VALUE);
        assertThat(config.getProxy()).isEqualTo(CUSTOM_VALUE);

        config.setProxyUser(CUSTOM_VALUE);
        assertThat(config.getProxyUser()).isEqualTo(CUSTOM_VALUE);

        config.setProxyPass(CUSTOM_VALUE);
        assertThat(config.getProxyPass()).isEqualTo(CUSTOM_VALUE);

        config.setRemoteAddress(CUSTOM_VALUE);
        assertThat(config.getRemoteAddress()).isEqualTo(CUSTOM_VALUE);

        config.setBrowserVersionDetectionCommand(CUSTOM_VALUE);
        assertThat(config.getBrowserVersionDetectionCommand())
                .isEqualTo(CUSTOM_VALUE);

        config.setBrowserVersionDetectionCommand(CUSTOM_VALUE);
        assertThat(config.getBrowserVersionDetectionCommand())
                .isEqualTo(CUSTOM_VALUE);

        config.setIgnoreVersions(CUSTOM_VALUE);
        assertThat(config.getIgnoreVersions()).containsExactly(CUSTOM_VALUE);

        config.setGitHubToken(CUSTOM_VALUE);
        assertThat(config.getGitHubToken()).isEqualTo(CUSTOM_VALUE);

        config.setChromeDriverVersion(CUSTOM_VALUE);
        assertThat(config.getChromeDriverVersion()).isEqualTo(CUSTOM_VALUE);

        config.setChromeVersion(CUSTOM_VALUE);
        assertThat(config.getChromeVersion()).isEqualTo(CUSTOM_VALUE);

        config.setEdgeDriverVersion(CUSTOM_VALUE);
        assertThat(config.getEdgeDriverVersion()).isEqualTo(CUSTOM_VALUE);

        config.setEdgeVersion(CUSTOM_VALUE);
        assertThat(config.getEdgeVersion()).isEqualTo(CUSTOM_VALUE);

        config.setGeckoDriverVersion(CUSTOM_VALUE);
        assertThat(config.getGeckoDriverVersion()).isEqualTo(CUSTOM_VALUE);

        config.setFirefoxVersion(CUSTOM_VALUE);
        assertThat(config.getFirefoxVersion()).isEqualTo(CUSTOM_VALUE);

        config.setIExplorerDriverVersion(CUSTOM_VALUE);
        assertThat(config.getIExplorerDriverVersion()).isEqualTo(CUSTOM_VALUE);

        config.setOperaDriverVersion(CUSTOM_VALUE);
        assertThat(config.getOperaDriverVersion()).isEqualTo(CUSTOM_VALUE);

        config.setOperaVersion(CUSTOM_VALUE);
        assertThat(config.getOperaVersion()).isEqualTo(CUSTOM_VALUE);

        config.setChromiumDriverVersion(CUSTOM_VALUE);
        assertThat(config.getChromiumDriverVersion()).isEqualTo(CUSTOM_VALUE);

        config.setChromiumVersion(CUSTOM_VALUE);
        assertThat(config.getChromiumVersion()).isEqualTo(CUSTOM_VALUE);

        config.setDockerDaemonUrl(CUSTOM_VALUE);
        assertThat(config.getDockerDaemonUrl()).isEqualTo(CUSTOM_VALUE);

        config.setDockerRecordingPrefix(CUSTOM_VALUE);
        assertThat(config.getDockerRecordingPrefix()).isEqualTo(CUSTOM_VALUE);

        config.setDockerCustomImage(CUSTOM_VALUE);
        assertThat(config.getDockerCustomImage()).isEqualTo(CUSTOM_VALUE);

        config.setDockerVolumes(CUSTOM_VALUE);
        assertThat(config.getDockerVolumes()).isEqualTo(CUSTOM_VALUE);

        config.setDockerEnvVariables(CUSTOM_VALUE);
        assertThat(config.getDockerEnvVariables())
                .containsExactly(CUSTOM_VALUE);

        config.setDockerPrivateEndpoint(CUSTOM_VALUE);
        assertThat(config.getDockerPrivateEndpoint()).isEqualTo(CUSTOM_VALUE);

        config.setDockerPrivateEndpoint("host1:192.168.48.82,host2:192.168.48.16");
        assertThat(config.getDockerPrivateEndpoint()).isEqualTo("host1:192.168.48.82,host2:192.168.48.16");
    }
}