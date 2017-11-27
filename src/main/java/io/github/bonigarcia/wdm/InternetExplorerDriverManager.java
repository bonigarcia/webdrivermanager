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

import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static io.github.bonigarcia.wdm.WdmConfig.getUrl;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Manager for Internet Explorer.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class InternetExplorerDriverManager extends BrowserManager {

    public static synchronized BrowserManager getInstance() {
        if (instance == null || !instance.getClass()
                .equals(InternetExplorerDriverManager.class)) {
            instance = new InternetExplorerDriverManager();
        }
        return instance;
    }

    public InternetExplorerDriverManager() {
        exportParameter = getString("wdm.internetExplorerExport");
        driverVersionKey = "wdm.internetExplorerVersion";
        driverUrlKey = "wdm.internetExplorerDriverUrl";
        driverName = asList("IEDriverServer");
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        return getDriversFromXml(getUrl("wdm.internetExplorerDriverUrl"));
    }

}
