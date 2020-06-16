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

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
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
public class EdgeLatestVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    public void edgeVersionTest() throws Exception {
        Config config = new Config();
        HttpClient httpClient = new HttpClient(config);
        VersionDetector versionDetector = new VersionDetector(config,
                httpClient);
        Optional<String> driverVersion = Optional.empty();
        URL driverUrl = new URL("https://msedgedriver.azureedge.net/");
        Charset versionCharset = StandardCharsets.UTF_16;
        String driverName = "msedgedriver";
        String versionLabel = "LATEST_STABLE";

        Optional<String> driverVersionFromRepository = versionDetector
                .getDriverVersionFromRepository(driverVersion, driverUrl,
                        versionCharset, driverName, versionLabel, versionLabel);
        assertTrue(driverVersionFromRepository.isPresent());
        String edgeVersion = driverVersionFromRepository.get();
        log.debug("driverVersionFromRepository {}", edgeVersion);

        List<String> driverVersions = WebDriverManager.edgedriver()
                .getDriverVersions();
        log.debug("All driverUrls {}", driverVersions);
        assertTrue(format("Stable version (%s) is not in the URL list",
                edgeVersion), driverVersions.contains(edgeVersion));

    }

}
