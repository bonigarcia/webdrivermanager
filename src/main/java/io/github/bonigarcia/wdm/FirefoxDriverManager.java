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
package io.github.bonigarcia.wdm;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static io.github.bonigarcia.wdm.OperativeSystem.MAC;
import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static java.util.Arrays.asList;

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
        driverName = asList("wires", "geckodriver", "firefoxdriver");
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        URL driverUrl = getDriverUrl();
        if (isUsingNexus) {
            return getDriversFromNexus(driverUrl);
        } else {
            return getDriversFromGitHub();
        }
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName) {
        if (isUsingNexus){
            return super.getCurrentVersion(url, driverName);
        }
        String currentVersion = url.getFile().substring(
                url.getFile().indexOf('-') + 1, url.getFile().lastIndexOf('-'));
        if (currentVersion.startsWith("v")) {
            currentVersion = currentVersion.substring(1);
        }
        return currentVersion;
    }

    @Override
    protected String preDownload(String target, String version) {
        int iSeparator = target.indexOf(version) - 1;
        int iDash = target.lastIndexOf(version) + version.length();
        int iPoint = target.lastIndexOf(".zip");
        int iPointTazGz = target.lastIndexOf(".tar.gz");
        int iPointGz = target.lastIndexOf(".gz");

        if (iPointTazGz != -1) {
            iPoint = iPointTazGz;
        } else if (iPointGz != -1) {
            iPoint = iPointGz;
        }

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
        return !myOsName.contains(MAC.name());
    }
}
