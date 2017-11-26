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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.ChromeDriverManager;

/**
 * Test for ignore versions.
 * 
 * @since 1.7.2
 */
public class IgnoredVersionTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testIgnoreVersions() {
        String[] ignoredVersions = { "2.33", "2.32" };
        ChromeDriverManager.getInstance().ignoreVersions(ignoredVersions)
                .setup();
        File binary = new File(
                ChromeDriverManager.getInstance().getBinaryPath());
        log.debug("Using binary {} (ignoring {})", binary,
                Arrays.toString(ignoredVersions));

        for (String version : ignoredVersions) {
            assertThat(binary.getName(), not(containsString(version)));
        }
    }

}