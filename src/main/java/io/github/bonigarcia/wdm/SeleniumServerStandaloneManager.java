/*
 * (C) Copyright 2018 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.DriverManagerType.SELENIUM_SERVER_STANDALONE;
import static java.util.Optional.empty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Manager for selenium-server-standalone.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.0.1
 */
public class SeleniumServerStandaloneManager extends WebDriverManager {

    protected SeleniumServerStandaloneManager() {
        driverManagerType = SELENIUM_SERVER_STANDALONE;
        driverVersionKey = "wdm.seleniumServerStandaloneVersion";
        driverUrlKey = "wdm.seleniumServerStandaloneUrl";
        driverName = "selenium-server-standalone";
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        URL driverUrl = config().getDriverUrl(driverUrlKey);
        return getDriversFromXml(driverUrl);
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        return empty();
    }

    @Override
    protected File postDownload(File archive) {
        return archive;
    }

}
