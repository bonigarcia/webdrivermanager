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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static java.io.File.separator;
import static java.util.Arrays.asList;

/**
 * Manager for Opera.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class OperaDriverManager extends BrowserManager {

    public static synchronized BrowserManager getInstance() {
        if (instance == null
                || !instance.getClass().equals(OperaDriverManager.class)) {
            instance = new OperaDriverManager();
        }
        return instance;
    }

    public OperaDriverManager() {
        exportParameter = getString("wdm.operaDriverExport");
        driverVersionKey = "wdm.operaDriverVersion";
        driverUrlKey = "wdm.operaDriverUrl";
        driverName = asList("operadriver");
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        if (isUsingNexus()) {
            return getDriversFromNexus(getDriverUrl());
        } else {
            return getDriversFromGitHub();
        }
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName) {
        if (isUsingNexus()) {
            return super.getCurrentVersion(url, driverName);
        }
        if (isUsingTaobaoMirror()) {
            int i = url.getFile().lastIndexOf(SLASH);
            int j = url.getFile().substring(0, i).lastIndexOf(SLASH) + 1;
            return url.getFile().substring(j, i);
        } else {
            return url.getFile().substring(
                    url.getFile().indexOf(SLASH + "v") + 2,
                    url.getFile().lastIndexOf(SLASH));
        }
    }

    @Override
    protected File postDownload(File archive) {
        log.trace("Post processing for Opera: {}", archive);

        File extractFolder = archive.getParentFile().listFiles()[0];
        if (!extractFolder.isFile()) {
            log.trace("Opera extract folder (to be deleted): {}",
                    extractFolder);
            File operadriver = extractFolder.listFiles()[0];
            log.trace("Operadriver binary: {}", operadriver);

            File target = new File(archive.getParentFile().getAbsolutePath()
                    + separator + operadriver.getName());
            log.trace("Operadriver target: {}", target);

            downloader.renameFile(operadriver, target);
            downloader.deleteFolder(extractFolder);
            return target;
        } else {
            return super.postDownload(archive);
        }
    }

    @Override
    public BrowserManager useTaobaoMirror() {
        return useTaobaoMirror("wdm.operaDriverTaobaoUrl");
    }

}
