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

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.bonigarcia.wdm.config.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Using different properties.
 *
 * @author Boni Garcia
 * @since 2.1.1
 */
class PropertiesTest {

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
    void defaultProperties() throws Exception {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        Config config = wdm.config();
        wdm.setup();

        String home = System.getProperty("user.home");
        assertThat(config.getCachePath()).isEqualTo(home + "/.cache/selenium");
        assertThat(config.isForceDownload()).isFalse();
        assertThat(config.isUseMirror()).isFalse();
        assertThat(config.isUseBetaVersions()).isFalse();
        assertThat(config.isAvoidExport()).isFalse();
        assertThat(config.isAvoidOutputTree()).isFalse();
        assertThat(config.isAvoidBrowserDetection()).isFalse();
        assertThat(config.isAvoidingResolutionCache()).isFalse();
        assertThat(config.isAvoidFallback()).isFalse();
        assertThat(config.isAvoidReadReleaseFromRepository()).isFalse();
        assertThat(config.isAvoidTmpFolder()).isFalse();
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
    void properties() throws Exception {

//        wdm.proxy=proxy
//        wdm.proxyUser=proxyUser
//        wdm.proxyPass=proxyPass


    }

    @Test
    @DisplayName("Should update config properties programmatically")
    void testPropertiesMutation() {
        WebDriverManager wdm = WebDriverManager.chromedriver();
        Config config = wdm.config();
        wdm.setup();

        config.setTtl(60);
        assertThat(config.getTtl()).isEqualTo(60);





    }

}
