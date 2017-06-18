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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class ChromeDriverManager extends BrowserManager {

    public static synchronized BrowserManager getInstance() {
        if (instance == null
                || !instance.getClass().equals(ChromeDriverManager.class)) {

            instance = new ChromeDriverManager();
        }
        return instance;
    }

    @Override
    protected List<URL> getDrivers() throws Exception {
        URL driverUrl = getDriverUrl();
        List<URL> urls;
        if (isUsingTaobaoMirror()) {
            urls = getDriversFromMirror(driverUrl);
        } else {
            urls = getDriversFromXml(getDriverUrl(), getDriverName());
        }
        return urls;
    }

    @Override
    protected String getExportParameter() {
        return WdmConfig.getString("wdm.chromeDriverExport");
    }

    @Override
    protected String getDriverVersionKey() {
        return "wdm.chromeDriverVersion";
    }

    @Override
    protected String getDriverUrlKey() {
        return "wdm.chromeDriverUrl";
    }

    @Override
    protected List<String> getDriverName() {
        return Arrays.asList("chromedriver");
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName)
            throws MalformedURLException {
        if (isUsingTaobaoMirror()) {
            int i = url.getFile().lastIndexOf(SEPARATOR);
            int j = url.getFile().substring(0, i).lastIndexOf(SEPARATOR) + 1;
            return url.getFile().substring(j, i);
        } else {
            return super.getCurrentVersion(url, driverName);
        }
    }

    @Override
    public BrowserManager useTaobaoMirror() {
        try {
            driverUrl = new URL(
                    WdmConfig.getString("wdm.chromeDriverTaobaoUrl"));
        } catch (MalformedURLException e) {
            log.error("Malformed URL", e);
            throw new RuntimeException(e);
        }
        return instance;
    }

}
