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
package io.github.bonigarcia.wdm.test.phantomjs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test with PhantomJS beta.
 *
 * @author Boni Garcia
 * @since 1.4.0
 */
class PhantomJsBetaTest {

    @BeforeAll
    static void setupClass() {
        WebDriverManager.phantomjs().useBetaVersions().setup();
    }

    @Test
    void testPhantomBeta() {
        String driverPath = WebDriverManager.phantomjs()
                .getDownloadedDriverPath();
        assertThat(driverPath).isNotNull();
    }
}
