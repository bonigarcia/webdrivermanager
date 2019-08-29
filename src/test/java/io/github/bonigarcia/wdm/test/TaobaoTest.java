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
package io.github.bonigarcia.wdm.test;

import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.WebDriverManagerException;

/**
 * Test for taobao.org mirror.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.6.1
 */
public class TaobaoTest {

    @Test
    public void testTaobao() throws Exception {
        chromedriver().config().setAvoidAutoVersion(true)
                .setChromeDriverMirrorUrl(
                        new URL("http://npm.taobao.org/mirrors/chromedriver/"));
        chromedriver().useMirror().forceDownload().setup();

        File binary = new File(chromedriver().getBinaryPath());
        assertTrue(binary.exists());
    }

    @Ignore("Flaky test due to cnpmjs.org")
    @Test
    public void testOtherMirrorUrl() throws Exception {
        chromedriver().config().setAvoidAutoVersion(true)
                .setChromeDriverMirrorUrl(
                        new URL("https://cnpmjs.org/mirrors/chromedriver/"));
        chromedriver().useMirror().forceDownload().setup();

        File binary = new File(chromedriver().getBinaryPath());
        assertTrue(binary.exists());
    }

    @Test(expected = WebDriverManagerException.class)
    public void testTaobaoException() {
        WebDriverManager.edgedriver().useMirror().setup();
        File binary = new File(WebDriverManager.edgedriver().getBinaryPath());
        assertTrue(binary.exists());
    }

}
