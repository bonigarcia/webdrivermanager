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

import static io.github.bonigarcia.wdm.WebDriverManager.edgedriver;
import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;

/**
 * Test with Microsoft Edge forcing to extract MSI file.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.0
 */
public class EdgeMsiTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testMsiInWindows() {
        edgedriver().version("2.10586").setup();
        File binary = new File(edgedriver().getBinaryPath());
        log.debug("Edge driver {}", binary);
        assertTrue(binary.getName().endsWith(".exe"));
    }

}
