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
package io.github.bonigarcia.wdm.test;

import static io.github.bonigarcia.wdm.OperatingSystem.MAC;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.base.VersionTestParent;
import org.junit.Before;
import org.openqa.selenium.edge.EdgeDriver;

/**
 * Test asserting Edge driver versions on MacOSX.
 * This is a separate class because the Win versus MacOSX versions aren't the same
 * Please take a look at https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
 *
 * @author Elias Nogueira (elias.nogueira@gmail.com)
 * @since 3.8.2
 */
public class EdgeMacVersionTest extends VersionTestParent {

    @Before
    public void setup() {
        browserManager = WebDriverManager.getInstance(EdgeDriver.class);
        os = MAC;
        specificVersions = new String[] {
            "80.0.361.62", "80.0.361.66", "80.0.361.69",
            "81.0.416.28", "81.0.416.31", "81.0.416.34",
            "82.0.456.0", "82.0.457.0", "82.0.458.0"
        };
    }
}
