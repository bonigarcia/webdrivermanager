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

import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import io.github.bonigarcia.wdm.Shell;

/**
 * Shell utilities test.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.6.1
 */
@RunWith(Parameterized.class)
public class ShellTest {

    @Parameter(0)
    public String output;

    @Parameter(1)
    public String version;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { {
                "Chromium 73.0.3683.86 Built on Ubuntu , running on Ubuntu 16.04",
                "73" },
                { "Chromium 74.0.3729.169 Built on Ubuntu , running on Ubuntu 18.04",
                        "74" },
                { "Google Chrome 75.0.3770.80", "75" } });
    }

    @Test
    public void versionFromPosixOutputTest() {
        String versionFromPosixOutput = Shell.getVersionFromPosixOutput(output,
                CHROME.toString());
        assertEquals(version, versionFromPosixOutput);
    }

}
