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
package io.github.bonigarcia.wdm.test.opera;

import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.opera.OperaDriver;

import io.github.bonigarcia.wdm.test.base.VersionTestParent;

/**
 * Test asserting operadriver versions.
 *
 * @author Boni Garcia
 * @since 1.2.2
 */
class OperaVersionTest extends VersionTestParent {

    @BeforeEach
    void setup() {
        driverClass = OperaDriver.class;
        specificVersions = new String[] { "0.2.2", "2.32" };
    }

}
