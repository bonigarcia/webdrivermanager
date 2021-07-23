/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.base;

import static io.github.bonigarcia.wdm.config.Architecture.DEFAULT;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.OperatingSystem;

/**
 * Parent class for version based tests.
 *
 * @author Boni Garcia
 * @since 1.4.1
 */
abstract public class VersionTestParent {

    protected Class<? extends WebDriver> driverClass;
    protected String[] specificVersions;
    protected OperatingSystem os;

    final Logger log = getLogger(lookup().lookupClass());

    @ParameterizedTest
    @EnumSource(names = { "DEFAULT", "X32", "X64" })
    void testLatestVersion(Architecture architecture) throws Exception {
        WebDriverManager wdm = WebDriverManager.getInstance(driverClass);

        String osLabel = "";
        if (os != null) {
            wdm.operatingSystem(os);
            osLabel = " os=" + os;
        }
        if (architecture != DEFAULT) {
            wdm.architecture(architecture);
        }

        log.debug("Test latest {} [arch={}{}]", wdm.getDriverManagerType(),
                architecture, osLabel);
        wdm.setup();

        assertThat(wdm.getDownloadedDriverVersion()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(names = { "DEFAULT", "X32", "X64" })
    void testSpecificVersions(Architecture architecture) throws Exception {
        for (String specificVersion : specificVersions) {
            WebDriverManager wdm = WebDriverManager.getInstance(driverClass);

            if (architecture != DEFAULT) {
                wdm.architecture(architecture);
            }
            String osLabel = "";
            if (os != null) {
                wdm.operatingSystem(os);
                osLabel = " os=" + os;
            }
            wdm.driverVersion(specificVersion);

            log.debug("Test {} version={} [arch={}{}]",
                    wdm.getDriverManagerType(), specificVersion, architecture,
                    osLabel);
            wdm.setup();

            assertThat(wdm.getDownloadedDriverVersion())
                    .isEqualTo(specificVersion);
            wdm.reset();
        }
    }

}
