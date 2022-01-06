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

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static io.github.bonigarcia.wdm.config.Architecture.X32;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Using different properties.
 *
 * @author Boni Garcia
 * @since 2.1.1
 */
class PropertiesTest {
    private static final String CUSTOM_VALUE = "custom_value";

    // TODO override default config ?
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

//        assertThat(config.getArchitecture()).isEqualTo(defaultArchitecture()); // Maybe set this method public
//        assertThat(config.getOs()).isEqualTo(defaultOsName()); // Maybe set this method public

        assertThat(config.getCachePath()).isEqualTo(System.getProperty("user.home") + "/.cache/selenium");
        assertThat(config.getResolutionCachePath()).isEqualTo(new File(System.getProperty("user.home") + "/.cache/selenium"));
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
        assertThat(config.getResolutionCache()).isEqualTo("resolution.properties");
        assertThat(config.getTtl()).isEqualTo(86400);
        assertThat(config.getTtlForBrowsers()).isEqualTo(3600);
        assertThat(config.getBrowserVersionDetectionRegex()).isEqualTo("[^\\d^\\.]");
        assertThat(config.getDefaultBrowser()).isEqualTo("chrome");

        assertThat(config.getServerPort()).isEqualTo(4444);
        assertThat(config.getServerPath()).isEqualTo("/");
        assertThat(config.getServerTimeoutSec()).isEqualTo(60);

        assertThat(config.getChromeDriverUrl()).isEqualTo(new URL("https://chromedriver.storage.googleapis.com/"));
        assertThat(config.getChromeDriverMirrorUrl()).isEqualTo(new URL("https://npm.taobao.org/mirrors/chromedriver/"));
        assertThat(config.getChromeDriverExport()).isEqualTo("webdriver.chrome.driver");
        assertThat(config.getChromeDownloadUrlPattern()).isEqualTo("https://chromedriver.storage.googleapis.com/%s/chromedriver_%s%s.zip");

//        config.getGeckoDriverVersion()
//        wdm.geckoDriverUrl=https://api.github.com/repos/mozilla/geckodriver/releases
//        wdm.geckoDriverMirrorUrl=https://npm.taobao.org/mirrors/geckodriver
//        wdm.geckoDriverExport=webdriver.gecko.driver

        assertThat(config.getOperaDriverUrl()).isEqualTo(new URL("https://api.github.com/repos/operasoftware/operachromiumdriver/releases"));
        assertThat(config.getOperaDriverMirrorUrl()).isEqualTo(new URL("https://npm.taobao.org/mirrors/operadriver"));
        assertThat(config.getOperaDriverExport()).isEqualTo("webdriver.opera.driver");

        assertThat(config.getEdgeDriverUrl()).isEqualTo(new URL("https://msedgedriver.azureedge.net/"));
        assertThat(config.getEdgeDriverExport()).isEqualTo("webdriver.edge.driver");
        assertThat(config.getEdgeDownloadUrlPattern()).isEqualTo("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/%s/edgedriver_%s%s.zip");

        assertThat(config.getIExplorerDriverUrl()).isEqualTo(new URL("https://api.github.com/repos/SeleniumHQ/selenium/releases"));
        assertThat(config.getIExplorerDriverExport()).isEqualTo("webdriver.ie.driver");

        assertThat(config.getChromiumDriverSnapPath()).isEqualTo("/snap/bin/chromium.chromedriver");
        assertThat(config.isUseChromiumDriverSnap()).isTrue();

        assertThat(config.isVersionsPropertiesOnlineFirst()).isTrue();
        assertThat(config.getVersionsPropertiesUrl()).isEqualTo(new URL("https://raw.githubusercontent.com/bonigarcia/webdrivermanager/master/src/main/resources/versions.properties"));

        assertThat(config.isCommandsPropertiesOnlineFirst()).isTrue();
        assertThat(config.getCommandsPropertiesUrl()).isEqualTo(new URL("https://raw.githubusercontent.com/bonigarcia/webdrivermanager/master/src/main/resources/commands.properties"));

        assertThat(config.getDockerHubUrl()).isEqualTo("https://hub.docker.com/");
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
        assertThat(config.getDockerScreenResolution()).isEqualTo("1280x1080x24");
        assertThat(config.getDockerVncPassword()).isEqualTo("selenoid");
        assertThat(config.getDockerBrowserPort()).isEqualTo(4444);
        assertThat(config.getDockerVncPort()).isEqualTo(5900);
        assertThat(config.getDockerNoVncPort()).isEqualTo(6080);
        assertThat(config.getDockerRecordingFrameRate()).isEqualTo(12);
        assertThat(config.getDockerRecordingOutput()).isEqualTo(Paths.get("."));
        assertThat(config.getDockerBrowserSelenoidImageFormat()).isEqualTo("selenoid/vnc:%s_%s");
        assertThat(config.getDockerBrowserTwilioImageFormat()).isEqualTo("twilio/selenoid:%s_%s");
        assertThat(config.getDockerBrowserAerokubeImageFormat()).isEqualTo("browsers/%s:%s");
        assertThat(config.getDockerBrowserMobileImageFormat()).isEqualTo("selenoid/%s:%s");
        assertThat(config.getDockerRecordingImage()).isEqualTo("selenoid/video-recorder:7.1");
        assertThat(config.getDockerNoVncImage()).isEqualTo("bonigarcia/novnc:1.1.0");
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
    }

    @Test
    @DisplayName("Should update config properties programmatically")
    void testPropertiesMutation() throws Exception {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        Config config = wdm.config();
        wdm.setup();

        config.setArchitecture(X32);
        assertThat(config.getArchitecture()).isEqualTo(X32);

        config.setOs("Linux for the best");
        assertThat(config.getOs()).isEqualTo("Linux for the best");

        config.setCachePath(CUSTOM_VALUE);
        assertThat(config.getCachePath()).isEqualTo(CUSTOM_VALUE);

        config.setResolutionCachePath(CUSTOM_VALUE);
        assertThat(config.getResolutionCachePath()).isEqualTo(new File(CUSTOM_VALUE));

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
        assertThat(config.getBrowserVersionDetectionRegex()).isEqualTo(CUSTOM_VALUE);

        config.setDefaultBrowser(CUSTOM_VALUE);
        assertThat(config.getDefaultBrowser()).isEqualTo(CUSTOM_VALUE);

        config.setServerPort(15);
        assertThat(config.getServerPort()).isEqualTo(15);

        config.setServerPath(CUSTOM_VALUE);
        assertThat(config.getServerPath()).isEqualTo(CUSTOM_VALUE);

        config.setServerTimeoutSec(15);
        assertThat(config.getServerTimeoutSec()).isEqualTo(15);

        assertThat(config.getChromeDriverUrl()).isEqualTo(new URL("https://chromedriver.storage.googleapis.com/"));
        assertThat(config.getChromeDriverMirrorUrl()).isEqualTo(new URL("https://npm.taobao.org/mirrors/chromedriver/"));
        assertThat(config.getChromeDriverExport()).isEqualTo("webdriver.chrome.driver");
        assertThat(config.getChromeDownloadUrlPattern()).isEqualTo("https://chromedriver.storage.googleapis.com/%s/chromedriver_%s%s.zip");

//        config.getGeckoDriverVersion()
//        wdm.geckoDriverUrl=https://api.github.com/repos/mozilla/geckodriver/releases
//        wdm.geckoDriverMirrorUrl=https://npm.taobao.org/mirrors/geckodriver
//        wdm.geckoDriverExport=webdriver.gecko.driver

        assertThat(config.getOperaDriverUrl()).isEqualTo(new URL("https://api.github.com/repos/operasoftware/operachromiumdriver/releases"));
        assertThat(config.getOperaDriverMirrorUrl()).isEqualTo(new URL("https://npm.taobao.org/mirrors/operadriver"));
        assertThat(config.getOperaDriverExport()).isEqualTo("webdriver.opera.driver");

        assertThat(config.getEdgeDriverUrl()).isEqualTo(new URL("https://msedgedriver.azureedge.net/"));
        assertThat(config.getEdgeDriverExport()).isEqualTo("webdriver.edge.driver");
        assertThat(config.getEdgeDownloadUrlPattern()).isEqualTo("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/%s/edgedriver_%s%s.zip");

        assertThat(config.getIExplorerDriverUrl()).isEqualTo(new URL("https://api.github.com/repos/SeleniumHQ/selenium/releases"));
        assertThat(config.getIExplorerDriverExport()).isEqualTo("webdriver.ie.driver");

        assertThat(config.getChromiumDriverSnapPath()).isEqualTo("/snap/bin/chromium.chromedriver");
        assertThat(config.isUseChromiumDriverSnap()).isTrue();

        assertThat(config.isVersionsPropertiesOnlineFirst()).isTrue();
        assertThat(config.getVersionsPropertiesUrl()).isEqualTo(new URL("https://raw.githubusercontent.com/bonigarcia/webdrivermanager/master/src/main/resources/versions.properties"));

        assertThat(config.isCommandsPropertiesOnlineFirst()).isTrue();
        assertThat(config.getCommandsPropertiesUrl()).isEqualTo(new URL("https://raw.githubusercontent.com/bonigarcia/webdrivermanager/master/src/main/resources/commands.properties"));

        assertThat(config.getDockerHubUrl()).isEqualTo("https://hub.docker.com/");
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
        assertThat(config.getDockerScreenResolution()).isEqualTo("1280x1080x24");
        assertThat(config.getDockerVncPassword()).isEqualTo("selenoid");
        assertThat(config.getDockerBrowserPort()).isEqualTo(4444);
        assertThat(config.getDockerVncPort()).isEqualTo(5900);
        assertThat(config.getDockerNoVncPort()).isEqualTo(6080);
        assertThat(config.getDockerRecordingFrameRate()).isEqualTo(12);
        assertThat(config.getDockerRecordingOutput()).isEqualTo(Paths.get("."));
        assertThat(config.getDockerBrowserSelenoidImageFormat()).isEqualTo("selenoid/vnc:%s_%s");
        assertThat(config.getDockerBrowserTwilioImageFormat()).isEqualTo("twilio/selenoid:%s_%s");
        assertThat(config.getDockerBrowserAerokubeImageFormat()).isEqualTo("browsers/%s:%s");
        assertThat(config.getDockerBrowserMobileImageFormat()).isEqualTo("selenoid/%s:%s");
        assertThat(config.getDockerRecordingImage()).isEqualTo("selenoid/video-recorder:7.1");
        assertThat(config.getDockerNoVncImage()).isEqualTo("bonigarcia/novnc:1.1.0");
        assertThat(config.getDockerDefaultArgs()).isEqualTo("--disable-gpu");
        assertThat(config.isDockerLocalFallback()).isTrue();


    }

}
