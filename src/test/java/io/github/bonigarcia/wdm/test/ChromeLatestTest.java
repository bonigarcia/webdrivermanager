/*
 * (C) Copyright 2019 Boni Garcia (http://bonigarcia.github.io/)
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

import static java.lang.invoke.MethodHandles.lookup;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test latest version of chromedriver.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.6.0
 */
public class ChromeLatestTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testLatestAndBetaChromedriver() {
        WebDriverManager.chromedriver().avoidPreferences().avoidAutoVersion()
                .setup();
        String chromedriverStable = WebDriverManager.chromedriver()
                .getDownloadedVersion();
        log.debug("Chromedriver LATEST version: {}", chromedriverStable);

        WebDriverManager.chromedriver().avoidPreferences().avoidAutoVersion()
                .useBetaVersions().setup();
        String chromedriverBeta = WebDriverManager.chromedriver()
                .getDownloadedVersion();
        log.debug("Chromedriver BETA version: {}", chromedriverBeta);

        assertThat(chromedriverStable, not(equalTo(chromedriverBeta)));
    }

}
