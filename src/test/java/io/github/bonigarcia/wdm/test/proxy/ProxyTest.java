/*		
 * (C) Copyright 2021 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Proxy test using mockserver.
 * 
 * @author Boni Garcia
 * @since 5.0.0
 */

@ExtendWith(MockServerExtension.class)
class ProxyTest {

    @Test
    void testMockProx(MockServerClient client) throws MalformedURLException {
        WebDriverManager wdm = WebDriverManager.chromedriver()
                .proxy("localhost:" + client.getPort()).proxyUser("")
                .proxyPass("").driverRepositoryUrl(new URL(
                        "https://chromedriver.storage.googleapis.com/"));
        wdm.setup();
        File driver = new File(wdm.getDownloadedDriverPath());
        assertThat(driver).exists();
    }

}