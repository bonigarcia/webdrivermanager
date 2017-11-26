/*		
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)		
 *		
 * All rights reserved. This program and the accompanying materials		
 * are made available under the terms of the GNU Lesser General Public License		
 * (LGPL) version 2.1 which accompanies this distribution, and is available at		
 * http://www.gnu.org/licenses/lgpl-2.1.html		
 *		
 * This library is distributed in the hope that it will be useful,		
 * but WITHOUT ANY WARRANTY; without even the implied warranty of		
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU		
 * Lesser General Public License for more details.		
 *		
 */
package io.github.bonigarcia.wdm.test;

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
import org.mockserver.integration.ClientAndProxy;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.Downloader;

/**
 * Test for proxy with mock server.
 * 
 * @since 1.7.2
 */
public class MockProxyTest {

    final Logger log = getLogger(lookup().lookupClass());

    private ClientAndProxy proxy;
    private int proxyPort;

    @Before
    public void setup() throws IOException {
        File wdmCache = new File(new Downloader().getTargetPath());
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
        ChromeDriverManager.getInstance().proxy("localhost:" + proxyPort)
                .proxyUser("").proxyPass("")
                .driverRepositoryUrl(
                        new URL("https://chromedriver.storage.googleapis.com/"))
                .setup();
        File binary = new File(
                ChromeDriverManager.getInstance().getBinaryPath());
        assertTrue(binary.exists());
    }

}