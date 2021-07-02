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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.OperatingSystem;

/**
 * Parent class for version based tests.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.1
 */
abstract public class VersionTestParent {

    protected WebDriverManager browserManager;
    protected String[] specificVersions;
    protected OperatingSystem os;

    final Logger log = getLogger(lookup().lookupClass());

    @ParameterizedTest
    @EnumSource(names = { "DEFAULT", "X32", "X64" })
    void testLatestVersion(Architecture architecture) throws Exception {
        String osLabel = "";
        if (os != null) {
            browserManager.operatingSystem(os);
            osLabel = " os=" + os;
        }
        log.debug("Test latest {} [arch={}{}]",
                browserManager.getDriverManagerType(), architecture, osLabel);

        switch (architecture) {
        case X32:
            browserManager.arch32().setup();
            break;
        case X64:
            browserManager.arch64().setup();
            break;
        default:
            browserManager.setup();
        }

        assertThat(browserManager.getDownloadedDriverVersion(), notNullValue());
    }

    @ParameterizedTest
    @EnumSource(names = { "DEFAULT", "X32", "X64" })
    void testSpecificVersions(Architecture architecture)
            throws Exception {
        for (String specificVersion : specificVersions) {
            if (architecture != DEFAULT) {
                browserManager.architecture(architecture);
            }
            String osLabel = "";
            if (os != null) {
                browserManager.operatingSystem(os);
                osLabel = " os=" + os;
            }
            log.debug("Test {} version={} [arch={}{}]",
                    browserManager.getDriverManagerType(), specificVersion,
                    architecture, osLabel);

            browserManager.driverVersion(specificVersion).setup();

            assertThat(browserManager.getDownloadedDriverVersion(),
                    equalTo(specificVersion));
        }
    }

}
