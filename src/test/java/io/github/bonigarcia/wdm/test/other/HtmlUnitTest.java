/*
 * (C) Copyright 2018 Boni Garcia (http://bonigarcia.github.io/)
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.test.base.BrowserTestParent;

/**
 * Test with HtmlUnit browser (which uses void driver manager).
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
class HtmlUnitTest extends BrowserTestParent {

    private static Class<? extends WebDriver> webDriverClass;

    @BeforeAll
    static void setupClass() {
        webDriverClass = HtmlUnitDriver.class;
        WebDriverManager.getInstance(webDriverClass).setup();
    }

    @BeforeEach
    void htmlUnitTest()
            throws InstantiationException, IllegalAccessException {
        driver = webDriverClass.newInstance();
    }

}
