/*
 * (C) Copyright 2017 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.config;

import static java.util.Locale.ROOT;

/**
 * Types for driver managers.
 *
 * @author Boni Garcia
 * @since 2.1.0
 */
public enum DriverManagerType {

    CHROME("org.openqa.selenium.chrome.ChromeDriver"),
    FIREFOX("org.openqa.selenium.firefox.FirefoxDriver"),
    OPERA("org.openqa.selenium.opera.OperaDriver"),
    EDGE("org.openqa.selenium.edge.EdgeDriver"),
    IEXPLORER("org.openqa.selenium.ie.InternetExplorerDriver"),
    CHROMIUM("org.openqa.selenium.chrome.ChromeDriver"),
    SAFARI("org.openqa.selenium.safari.SafariDriver");

    String browserClass;

    DriverManagerType(String browserClass) {
        this.browserClass = browserClass;
    }

    public String browserClass() {
        return browserClass;
    }

    public String getNameLowerCase() {
        return name().toLowerCase(ROOT);
    }

    public String getBrowserName() {
        switch (this) {
        case CHROME:
            return "Chrome";
        case CHROMIUM:
            return "Chromium";
        case FIREFOX:
            return "Firefox";
        case OPERA:
            return "Opera";
        case EDGE:
            return "Edge";
        case IEXPLORER:
            return "Internet Explorer";
        case SAFARI:
            return "Safari";
        default:
            throw new WebDriverManagerException(
                    "Invalid driver manager type: " + this.name());
        }
    }

    public String getBrowserNameLowerCase() {
        return getBrowserName().toLowerCase(ROOT);
    }

    public static DriverManagerType valueOfDisplayName(String displayName) {
        int iComma = displayName.indexOf(",");
        if (iComma != -1) {
            displayName = displayName.substring(0, iComma);
        }
        return DriverManagerType.valueOf(displayName
                .substring(displayName.indexOf("=") + 1).toUpperCase());
    }

}
