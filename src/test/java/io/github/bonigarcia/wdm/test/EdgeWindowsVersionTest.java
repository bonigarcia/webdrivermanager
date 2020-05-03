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

import static io.github.bonigarcia.wdm.etc.OperatingSystem.WIN;

import org.junit.Before;
import org.openqa.selenium.edge.EdgeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.base.VersionTestParent;

/**
 * Test asserting Edge driver versions on Windows.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeWindowsVersionTest extends VersionTestParent {

    @Before
    public void setup() {
        browserManager = WebDriverManager.getInstance(EdgeDriver.class);
        os = WIN;
        specificVersions = new String[] { "75.0.139.20", "76.0.183.0",
                "77.0.237.0", "78.0.277.0", "79.0.313.0", "80.0.361.111",
                "81.0.409.0", "82.0.459.1" };
    }

}
