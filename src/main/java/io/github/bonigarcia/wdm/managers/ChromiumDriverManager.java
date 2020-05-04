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
package io.github.bonigarcia.wdm.managers;

import static io.github.bonigarcia.wdm.etc.DriverManagerType.CHROMIUM;

import java.util.Optional;

import io.github.bonigarcia.wdm.etc.DriverManagerType;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.8.0
 */
public class ChromiumDriverManager extends ChromeDriverManager {

    @Override
    public DriverManagerType getDriverManagerType() {
        return CHROMIUM;
    }

    @Override
    protected String getDriverVersion() {
        return config().getChromiumDriverVersion();
    }

    @Override
    protected String getBrowserVersion() {
        return config().getChromiumVersion();
    }

    @Override
    protected void setDriverVersion(String driverVersion) {
        config().setChromiumDriverVersion(driverVersion);
    }

    @Override
    protected void setBrowserVersion(String browserVersion) {
        config().setChromiumVersion(browserVersion);
    }

    @Override
    protected Optional<String> getBrowserVersionFromTheShell() {
        String[] programFilesEnvs = { "LOCALAPPDATA", getOtherProgramFilesEnv(),
                getProgramFilesEnv() };
        String[] winBrowserNames = {
                "\\\\Chromium\\\\Application\\\\chrome.exe" };
        return versionDetector.getDefaultBrowserVersion(programFilesEnvs,
                winBrowserNames, "chromium-browser",
                "/Applications/Chromium.app/Contents/MacOS/Chromium",
                "--version", getDriverManagerType().toString());
    }

}
