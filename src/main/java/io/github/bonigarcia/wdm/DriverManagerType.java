/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
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

/**
 * Types for driver managers.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.1.0
 */
public enum DriverManagerType {

    CHROME, FIREFOX, OPERA, EDGE, PHANTOMJS, IEXPLORER,
    SELENIUM_SERVER_STANDALONE, CHROMIUM;

    @Override
    public String toString() {
        switch (this) {
        case CHROME:
            return "Google Chrome";
        case CHROMIUM:
            return "Chromium";
        case FIREFOX:
            return "Mozilla Firefox";
        case OPERA:
            return "Opera";
        case EDGE:
            return "Microsoft Edge";
        case PHANTOMJS:
            return "PhantomJS";
        case IEXPLORER:
            return "Internet Explorer";
        case SELENIUM_SERVER_STANDALONE:
            return "Selenium Server Standalone";
        default:
            throw new WebDriverManagerException(
                    "Invalid driver manager type: " + this.name());
        }
    }

}
