/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
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
