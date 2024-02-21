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
package io.github.bonigarcia.wdm.test.firefox;

import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.bonigarcia.wdm.test.base.VersionTestParent;

/**
 * Test asserting Firefox versions.
 *
 * @author Boni Garcia
 * @since 1.5.0
 */
class FirefoxVersionTest extends VersionTestParent {

    @BeforeEach
    void setup() {
        driverClass = FirefoxDriver.class;
        specificVersions = new String[] { "0.20.0", "0.29.0" };
    }

}
