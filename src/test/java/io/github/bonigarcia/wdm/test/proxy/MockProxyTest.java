/*		
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)		
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

import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockserver.integration.ClientAndProxy;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.etc.Config;
import io.github.bonigarcia.wdm.online.Downloader;

/**
 * Test for proxy with mock server.
 * 
 * @since 1.7.2
 */
@RunWith(MockitoJUnitRunner.class)
public class MockProxyTest {

    final Logger log = getLogger(lookup().lookupClass());

    @InjectMocks
    public Downloader downloader;

    @Spy
    public Config config = new Config();

    private ClientAndProxy proxy;
    private int proxyPort;

    @Before
    public void setup() throws IOException {
        File wdmCache = new File(downloader.getCachePath());
        log.debug("Cleaning local cache {}", wdmCache);
        cleanDirectory(wdmCache);

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            proxyPort = serverSocket.getLocalPort();
        }
        log.debug("Starting mock proxy on port {}", proxyPort);
        proxy = startClientAndProxy(proxyPort);
    }

    @After
    public void teardown() {
        log.debug("Stopping mock proxy on port {}", proxyPort);
        proxy.stop();
    }

    @Test
    public void testMockProx() throws MalformedURLException {
        chromedriver().proxy("localhost:" + proxyPort).proxyUser("")
                .proxyPass("")
                .driverRepositoryUrl(
                        new URL("https://chromedriver.storage.googleapis.com/"))
                .setup();
        File binary = new File(chromedriver().getBinaryPath());
        assertTrue(binary.exists());
    }

}