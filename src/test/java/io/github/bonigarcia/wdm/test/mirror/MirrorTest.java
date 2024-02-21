/*
 * (C) Copyright 2016 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.mirror;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

/**
 * Test for mirror repository.
 *
 * @author Boni Garcia
 * @since 1.6.1
 */

class MirrorTest {

    @Test
    void testMirrorChrome() throws Exception {
        WebDriverManager wdm = WebDriverManager.chromedriver()
                .browserVersion("100").avoidBrowserDetection()
                .clearResolutionCache().useMirror().forceDownload();
        wdm.config().setChromeDriverMirrorUrl(new URL(
                "https://registry.npmmirror.com/-/binary/chromedriver/"));
        wdm.setup();
        File driver = new File(wdm.getDownloadedDriverPath());
        assertThat(driver).exists();
    }

    @Test
    void testMirrorFirefox() throws Exception {
        WebDriverManager wdm = WebDriverManager.firefoxdriver().useMirror()
                .forceDownload();
        wdm.setup();
        File driver = new File(wdm.getDownloadedDriverPath());
        assertThat(driver).exists();
    }

    @Test
    void testMirrorOpera() throws Exception {
        WebDriverManager wdm = WebDriverManager.operadriver().useMirror()
                .browserVersion("97").forceDownload();
        wdm.setup();
        File driver = new File(wdm.getDownloadedDriverPath());
        assertThat(driver).exists();
    }

    @Test
    void testMirrorException() {
        WebDriverManager manager = WebDriverManager.edgedriver();
        assertThatThrownBy(manager::useMirror)
                .isInstanceOf(WebDriverManagerException.class);
    }

    @Disabled("Too slow")
    @Test
    void testMirrorDriverList() {
        List<String> driverVersions = WebDriverManager.chromedriver()
                .useMirror().getDriverVersions();
        assertThat(driverVersions).isNotEmpty();
    }

}
