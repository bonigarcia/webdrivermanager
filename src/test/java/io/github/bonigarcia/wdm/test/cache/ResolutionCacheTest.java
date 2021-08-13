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
package io.github.bonigarcia.wdm.test.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test for resolution cache.
 *
 * @author Boni Garcia
 * @since 3.0.0
 */
class ResolutionCacheTest {

    @Test
    void testEmptyTtl() {
        WebDriverManager wdm = WebDriverManager.chromedriver().ttl(0)
                .ttlBrowsers(0);
        wdm.setup();
        File driver = new File(wdm.getDownloadedDriverPath());

        assertThat(driver).exists();
    }

}
