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

import static io.github.bonigarcia.wdm.OperativeSystem.LINUX;
import static io.github.bonigarcia.wdm.OperativeSystem.MAC;
import static io.github.bonigarcia.wdm.OperativeSystem.WIN;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.Downloader;
import io.github.bonigarcia.wdm.OperativeSystem;

/**
 * Test for ignore versions.
 * 
 * @since 1.7.2
 */
@RunWith(Parameterized.class)
public class ForceOsTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Parameter
    public OperativeSystem operativeSystem;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { WIN }, { LINUX }, { MAC } });
    }

    @Before
    public void deleteDownloadedFiles() throws IOException {
        cleanDirectory(new File(new Downloader().getTargetPath()));
    }

    @Test
    public void testForceOs() {
        ChromeDriverManager.getInstance().forceOperativeSystem(operativeSystem)
                .setup();
        File binary = new File(
                ChromeDriverManager.getInstance().getBinaryPath());
        log.debug("OS {} - binary path {}", operativeSystem, binary);
        assertTrue(binary.exists());
    }

}