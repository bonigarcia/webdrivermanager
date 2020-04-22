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

import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.DriverManagerType.OPERA;
import static io.github.bonigarcia.wdm.DriverManagerType.PHANTOMJS;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.Downloader;
import io.github.bonigarcia.wdm.DriverManagerType;

/**
 * Target folder test.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.1
 */
@RunWith(Parameterized.class)
public class TargetTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Parameter(0)
    public String version;

    @Parameter(1)
    public String url;

    @Parameter(2)
    public String target;

    @Parameter(3)
    public DriverManagerType driverManagerType;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                // Chrome Linux
                { "2.21",
                        "http://chromedriver.storage.googleapis.com/2.21/chromedriver_linux64.zip",
                        "/chromedriver/linux64/2.21/chromedriver_linux64.zip",
                        CHROME },

                // Opera Linux
                { "0.2.2",
                        "https://github.com/operasoftware/operachromiumdriver/releases/download/v0.2.2/operadriver_linux64.zip",
                        "/operadriver/linux64/0.2.2/operadriver_linux64.zip",
                        OPERA },

                // PhantomJS Linux
                { "2.1.1",
                        "https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2",
                        "/phantomjs/linux-x86_64/2.1.1/phantomjs-2.1.1-linux-x86_64.tar.bz2",
                        PHANTOMJS },

                // PhantomJS Windows
                { "2.1.1",
                        "https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-windows.zip",
                        "/phantomjs/windows/2.1.1/phantomjs-2.1.1-windows.zip",
                        PHANTOMJS },

                // PhantomJS Mac OS X
                { "2.1.1",
                        "https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-macosx.zip",
                        "/phantomjs/macosx/2.1.1/phantomjs-2.1.1-macosx.zip",
                        PHANTOMJS },

                // Edge Windows
                { "75.0.137.0",
                        "https://az813057.vo.msecnd.net/webdriver/msedgedriver_x86/msedgedriver.exe",
                        "/msedgedriver/x64/75.0.137.0/msedgedriver.exe", EDGE },

                // Firefox Mac OS X
                { "0.6.2",
                        "https://github.com/jgraham/wires/releases/download/v0.6.2/wires-0.6.2-OSX.gz",
                        "/wires/osx/0.6.2/wires-0.6.2-OSX.gz", FIREFOX },
                { "0.3.0",
                        "https://github.com/jgraham/wires/releases/download/0.3.0/wires-0.3.0-osx.tar.gz",
                        "/wires/osx/0.3.0/wires-0.3.0-osx.tar.gz", FIREFOX },

                // Firefox Linux
                { "0.6.2",
                        "https://github.com/jgraham/wires/releases/download/v0.6.2/wires-0.6.2-linux64.gz",
                        "/wires/linux64/0.6.2/wires-0.6.2-linux64.gz",
                        FIREFOX },

                // Firefox Linux #2
                { "0.8.0",
                        "https://github.com/mozilla/geckodriver/releases/download/v0.8.0/geckodriver-0.8.0-linux64.gz",
                        "/geckodriver/linux64/0.8.0/geckodriver-0.8.0-linux64.gz",
                        FIREFOX } });

    }

    @Test
    public void testTarget() throws IOException {
        Downloader downloader = new Downloader(driverManagerType);
        String targetPath = downloader.getTargetPath();

        File result = downloader.getTarget(version, new URL(url));
        log.info("{}", result);
        log.info(targetPath + target);

        File fileReal = new File(targetPath + target);

        assertThat(result, equalTo(fileReal));
    }

}
