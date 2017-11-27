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

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Generic manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.1
 */
public class WebDriverManager extends BrowserManager {

    public static synchronized BrowserManager getInstance(
            Class<?> webDriverClass) {
        Class<? extends BrowserManager> browserManagerClass = null;

        switch (webDriverClass.getName()) {
        case "org.openqa.selenium.chrome.ChromeDriver":
            browserManagerClass = ChromeDriverManager.class;
            break;
        case "org.openqa.selenium.opera.OperaDriver":
            browserManagerClass = OperaDriverManager.class;
            break;
        case "org.openqa.selenium.ie.InternetExplorerDriver":
            browserManagerClass = InternetExplorerDriverManager.class;
            break;
        case "org.openqa.selenium.edge.EdgeDriver":
            browserManagerClass = EdgeDriverManager.class;
            break;
        case "org.openqa.selenium.phantomjs.PhantomJSDriver":
            browserManagerClass = PhantomJsDriverManager.class;
            break;
        case "org.openqa.selenium.firefox.FirefoxDriver":
            browserManagerClass = FirefoxDriverManager.class;
            break;
        default:
            browserManagerClass = VoidDriverManager.class;
            break;
        }

        if (instance == null || instance.getClass() != browserManagerClass) {
            try {
                instance = browserManagerClass.newInstance();
            } catch (Exception e) {
                throw new WebDriverManagerException(e);
            }
        }

        return instance;
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        return instance.getDrivers();
    }

}
