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
package io.github.bonigarcia.wdm.test.chrome;

import org.junit.Before;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.test.base.VersionTestParent;

/**
 * Test asserting chromedriver versions.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.2.1
 */
public class ChromeVersionTest extends VersionTestParent {

    @Before
    public void setup() {
        browserManager = WebDriverManager.chromedriver();
        specificVersions = new String[] { "2.10", "2.33" };
    }

}
