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
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.Shell.getProgramFilePath;
import static io.github.bonigarcia.wdm.Shell.getVersionFromPosixOutput;
import static io.github.bonigarcia.wdm.Shell.getVersionFromWmicOutput;
import static io.github.bonigarcia.wdm.Shell.runAndWait;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class ChromeDriverManager extends WebDriverManager {

    protected ChromeDriverManager() {
        driverManagerType = CHROME;
        exportParameterKey = "wdm.chromeDriverExport";
        driverVersionKey = "wdm.chromeDriverVersion";
        driverUrlKey = "wdm.chromeDriverUrl";
        driverMirrorUrlKey = "wdm.chromeDriverMirrorUrl";
        driverName = asList("chromedriver");
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        URL driverUrl = config().getDriverUrl(driverUrlKey);
        List<URL> urls;
        if (isUsingTaobaoMirror()) {
            urls = getDriversFromMirror(driverUrl);
        } else {
            urls = getDriversFromXml(driverUrl);
        }
        return urls;
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName) {
        if (isUsingTaobaoMirror()) {
            int i = url.getFile().lastIndexOf(SLASH);
            int j = url.getFile().substring(0, i).lastIndexOf(SLASH) + 1;
            return url.getFile().substring(j, i);
        } else {
            return super.getCurrentVersion(url, driverName);
        }
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        if (IS_OS_WINDOWS) {
            String programFiles = getProgramFilePath();
            String browserVersionOutput = runAndWait("wmic", "datafile",
                    "where",
                    "name='" + programFiles
                            + "\\\\Google\\\\Chrome\\\\Application\\\\chrome.exe'",
                    "get", "Version", "/value");
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional
                        .of(getVersionFromWmicOutput(browserVersionOutput));
            }
        } else if (IS_OS_LINUX) {
            String browserVersionOutput = runAndWait("google-chrome",
                    "--version");
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional.of(getVersionFromPosixOutput(
                        browserVersionOutput, driverManagerType));
            }
        } else if (IS_OS_MAC) {
            String browserVersionOutput = runAndWait(
                    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                    "--version");
            if (!isNullOrEmpty(browserVersionOutput)) {
                return Optional.of(getVersionFromPosixOutput(
                        browserVersionOutput, driverManagerType));
            }
        }
        return empty();
    }

}
