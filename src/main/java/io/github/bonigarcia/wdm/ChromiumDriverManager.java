/*
 * (C) Copyright 2020 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.DriverManagerType.CHROMIUM;

import java.io.File;
import java.util.Optional;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.8.0
 */
public class ChromiumDriverManager extends ChromeDriverManager {

    @Override
    protected DriverManagerType getDriverManagerType() {
        return CHROMIUM;
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        String[] programFilesEnvs = { "LOCALAPPDATA", getProgramFilesEnv(),
                getOtherProgramFilesEnv() };
        String[] winBrowserNames = {
                "\\\\Chromium\\\\Application\\\\chrome.exe" };
        return getDefaultBrowserVersion(programFilesEnvs, winBrowserNames,
                "chromium-browser",
                "/Applications/Chromium.app/Contents/MacOS/Chromium",
                "--version", getDriverManagerType().toString());
    }

    protected boolean snapDriverExists() {
        String chromiumDriverSnapPath = config().getChromiumDriverSnapPath();
        File snapChromiumDriverPath = new File(chromiumDriverSnapPath);
        boolean existsSnap = snapChromiumDriverPath.exists();
        if (existsSnap) {
            log.debug("Found {} snap", getDriverManagerType());
            exportDriver(chromiumDriverSnapPath);
        }
        return existsSnap;
    }

}
