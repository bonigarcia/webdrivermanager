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
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
        return getDriversFromGitHub();
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName)
            throws MalformedURLException {
        if (isUsingTaobaoMirror()) {
            int i = url.getFile().lastIndexOf(SEPARATOR);
            int j = url.getFile().substring(0, i).lastIndexOf(SEPARATOR) + 1;
            return url.getFile().substring(j, i);
        } else {
            return url.getFile().substring(
                    url.getFile().indexOf(SEPARATOR + "v") + 2,
                    url.getFile().lastIndexOf(SEPARATOR));
        }
    }

    @Override
    protected File postDownload(File archive) throws IOException {
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
            deleteDirectory(extractFolder);
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
