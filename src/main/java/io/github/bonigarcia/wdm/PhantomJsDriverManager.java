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
 * limitations under the License..
 *
 */
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static java.io.File.separator;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Manager for PhantomJs.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsDriverManager extends BrowserManager {

    public static synchronized BrowserManager getInstance() {
        if (instance == null
                || !instance.getClass().equals(PhantomJsDriverManager.class)) {
            instance = new PhantomJsDriverManager();
        }
        return instance;
    }

    public PhantomJsDriverManager() {
        exportParameter = getString("wdm.phantomjsDriverExport");
        driverVersionKey = "wdm.phantomjsDriverVersion";
        driverUrlKey = "wdm.phantomjsDriverUrl";
        driverName = asList("phantomjs");
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        return getDriversFromMirror(getDriverUrl());
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName) {
        String file = url.getFile();
        file = url.getFile().substring(file.lastIndexOf(SLASH), file.length());
        int matchIndex = file.indexOf(driverName);
        String currentVersion = file
                .substring(matchIndex + driverName.length() + 1, file.length());
        int dashIndex = currentVersion.indexOf('-');
        currentVersion = currentVersion.substring(0, dashIndex);
        return currentVersion;
    }

    @Override
    protected String preDownload(String target, String version) {
        int iSeparator = target.indexOf(version) - 1;
        int iDash = target.lastIndexOf(version) + version.length();
        int iPoint = target.lastIndexOf(".tar") != -1
                ? target.lastIndexOf(".tar")
                : target.lastIndexOf(".zip");
        target = target.substring(0, iSeparator + 1)
                + target.substring(iDash + 1, iPoint)
                + target.substring(iSeparator);
        target = target.replace("beta-", "");
        return target;
    }

    @Override
    protected File postDownload(File archive) {
        log.trace("PhatomJS package name: {}", archive);

        File extractFolder = archive.getParentFile().listFiles()[0];
        log.trace("PhatomJS extract folder (to be deleted): {}", extractFolder);

        File binFolder = new File(
                extractFolder.getAbsoluteFile() + separator + "bin");
        // Exception for older version of PhantomJS
        int binaryIndex = 0;
        if (!binFolder.exists()) {
            binFolder = extractFolder;
            binaryIndex = 3;
        }

        log.trace("PhatomJS bin folder: {} (index {})", binFolder, binaryIndex);

        File phantomjs = binFolder.listFiles()[binaryIndex];
        log.trace("PhatomJS binary: {}", phantomjs);

        File target = new File(archive.getParentFile().getAbsolutePath()
                + separator + phantomjs.getName());
        log.trace("PhatomJS target: {}", target);

        downloader.renameFile(phantomjs, target);
        downloader.deleteFolder(extractFolder);
        return target;
    }

    @Override
    public BrowserManager useTaobaoMirror() {
        return useTaobaoMirror("wdm.phantomjsDriverTaobaoUrl");
    }

    @Override
    protected boolean shouldCheckArchitecture() {
        return false;
    }
}
