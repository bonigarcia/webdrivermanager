/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.OperativeSystem.MAC;
import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Manager for Firefox.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.5.0
 */
public class FirefoxDriverManager extends BrowserManager {

    public static synchronized BrowserManager getInstance() {
        if (instance == null
                || !instance.getClass().equals(FirefoxDriverManager.class)) {
            instance = new FirefoxDriverManager();
        }
        return instance;
    }

    public FirefoxDriverManager() {
        exportParameter = getString("wdm.geckoDriverExport");
        driverVersionKey = "wdm.geckoDriverVersion";
        driverUrlKey = "wdm.geckoDriverUrl";
        driverName = asList("wires", "geckodriver");
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        return getDriversFromGitHub();
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName)
            throws MalformedURLException {
        String currentVersion = url.getFile().substring(
                url.getFile().indexOf("-") + 1, url.getFile().lastIndexOf("-"));
        if (currentVersion.startsWith("v")) {
            currentVersion = currentVersion.substring(1);
        }
        return currentVersion;
    }

    @Override
    protected String preDownload(String target, String version)
            throws IOException {
        int iSeparator = target.indexOf(version) - 1;
        int iDash = target.lastIndexOf(version) + version.length();
        int iPoint = target.lastIndexOf("tar.gz") != -1
                ? target.lastIndexOf(".tar.gz")
                : target.lastIndexOf(".gz") != -1 ? target.lastIndexOf(".gz")
                        : target.lastIndexOf(".zip");
        target = target.substring(0, iSeparator + 1)
                + target.substring(iDash + 1, iPoint).toLowerCase()
                + target.substring(iSeparator);
        return target;
    }

    @Override
    public BrowserManager useTaobaoMirror() {
        return useTaobaoMirror("wdm.geckoDriverTaobaoUrl");
    }

    @Override
    protected boolean shouldCheckArchitecture() {
        return !MY_OS_NAME.contains(MAC.name());
    }
}
