/*
 * (C) Copyright 2020 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test.versions;

import static io.github.bonigarcia.wdm.etc.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.etc.DriverManagerType.PHANTOMJS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.etc.DriverManagerType;

/**
 * Current version test.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 4.0.0
 */
@RunWith(Parameterized.class)
public class CurrentVersionTest {

    @Parameter(0)
    public DriverManagerType driverManagerType;

    @Parameter(1)
    public String url;

    @Parameter(2)
    public String expectedVersion;

    @Parameters(name = "{index}: {0} {1} {2}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { CHROME,
                "https://chromedriver.storage.googleapis.com/81.0.4044.69/chromedriver_linux64.zip",
                "81.0.4044.69" },
                { EDGE, "https://msedgedriver.azureedge.net/81.0.416.64/edgedriver_win64.zip",
                        "81.0.416.64" },
                { OPERA, "https://github.com/operasoftware/operachromiumdriver/releases/download/v.81.0.4044.113/operadriver_win64.zip",
                        "81.0.4044.113" },
                { FIREFOX,
                        "https://github.com/mozilla/geckodriver/releases/download/v0.26.0/geckodriver-v0.26.0-win64.zip",
                        "0.26.0" },
                { PHANTOMJS,
                        "https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.5.0-beta-linux-ubuntu-trusty-x86_64.tar.gz",
                        "2.5.0-beta" } });

    }

    @Test
    public void testCurrentVersion() throws Exception {
        WebDriverManager browserManager = WebDriverManager
                .getInstance(driverManagerType);

        Method method = WebDriverManager.class
                .getDeclaredMethod("getCurrentVersion", URL.class);
        method.setAccessible(true);

        String currentVersion = (String) method.invoke(browserManager,
                new URL(url));

        assertThat(currentVersion, equalTo(expectedVersion));
    }

}
