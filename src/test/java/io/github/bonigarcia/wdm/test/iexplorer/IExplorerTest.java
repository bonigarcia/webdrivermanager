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
package io.github.bonigarcia.wdm.test.iexplorer;

import static io.github.bonigarcia.wdm.WebDriverManager.iedriver;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

/**
 * Test with Internet Explorer browser.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
class IExplorerTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    void testIExplorerLatest() {
        iedriver().win().setup();
        assertIEDriver();
    }

    @Test
    void testIExplorerVersion() {
        iedriver().win().driverVersion("3.11").setup();
        assertIEDriver();
    }

    private void assertIEDriver() {
        File driver = new File(iedriver().getDownloadedDriverPath());
        log.debug("Path for IEDriverServer {}", driver);
        assertThat(driver).exists();
    }

}
