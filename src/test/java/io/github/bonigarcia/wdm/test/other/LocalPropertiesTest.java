/*
 * (C) Copyright 2021 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.other;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Tests with custom configuration for versions and commands properties.
 *
 * @author Boni Garcia
 * @since 4.4.1
 */
class LocalPropertiesTest {

    WebDriverManager wdm = WebDriverManager.chromedriver()
            .clearResolutionCache().avoidReadReleaseFromRepository();

    @Test
    void testCustomUrl() throws MalformedURLException {
        File dot = new File(".");
        URL versionsPropertiesUrl = new URL("file://" + dot.getAbsolutePath()
                + "/src/main/resources/versions.properties");
        URL commandsPropertiesUrl = new URL("file://" + dot.getAbsolutePath()
                + "/src/main/resources/commands.properties");
        wdm.versionsPropertiesUrl(versionsPropertiesUrl)
                .commandsPropertiesUrl(commandsPropertiesUrl).setup();

        assertDriverPath();
    }

    @Test
    void testLocalFirst() {
        wdm.useLocalCommandsPropertiesFirst().useLocalVersionsPropertiesFirst()
                .setup();

        assertDriverPath();
    }

    private void assertDriverPath() {
        String driverPath = wdm.getDownloadedDriverPath();
        File driver = new File(driverPath);
        assertThat(driver).exists();
    }

}
