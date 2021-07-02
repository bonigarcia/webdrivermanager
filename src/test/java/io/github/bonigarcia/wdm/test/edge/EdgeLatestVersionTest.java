/*
 * (C) Copyright 2020 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.edge;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_16;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.online.HttpClient;
import io.github.bonigarcia.wdm.versions.VersionDetector;

/**
 * Test asserting latest version of Edge in repository.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 4.0.1
 */
class EdgeLatestVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    void edgeVersionTest() throws Exception {
        Config config = new Config();
        HttpClient httpClient = new HttpClient(config);
        VersionDetector versionDetector = new VersionDetector(config,
                httpClient);
        Optional<String> driverVersion = Optional.empty();
        URL driverUrl = new URL("https://msedgedriver.azureedge.net/");
        Charset versionCharset = UTF_16;
        String driverName = "msedgedriver";
        String versionLabel = "LATEST_STABLE";
        Optional<String> osLabel = Optional.empty();

        Optional<String> driverVersionFromRepository = versionDetector
                .getDriverVersionFromRepository(driverVersion, driverUrl,
                        versionCharset, driverName, versionLabel, versionLabel,
                        osLabel);
        assertThat(driverVersionFromRepository).isPresent();
        String edgeVersion = driverVersionFromRepository.get();
        log.debug("driverVersionFromRepository {}", edgeVersion);

        WebDriverManager edgedriver = WebDriverManager.edgedriver();
        List<String> driverVersions = edgedriver.getDriverVersions();
        log.debug("All driverUrls {}", driverVersions);

        if (!driverVersions.contains(edgeVersion)) {
            log.warn("{}", String.format(
                    "Stable version (%s) is not in the URL list", edgeVersion));
            edgedriver.win().forceDownload().avoidBrowserDetection().setup();
            assertThat(edgedriver.getDownloadedDriverVersion()).isNotNull();
        }
    }

}
