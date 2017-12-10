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

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;

/**
 * Configuration utility class.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class WdmConfig {

    static final Logger log = getLogger(lookup().lookupClass());

    private WdmConfig() {
        throw new IllegalStateException("Utility class");
    }

    public static String getString(String key) {
        String value = "";
        if (!key.equals("")) {
            value = System.getenv(key.toUpperCase().replace(".", "_"));
            if (value == null) {
                value = System.getProperty(key);
            }
            if (value == null) {
                value = getProperty(key);
            }
        }
        return value;
    }

    public static int getInt(String key) {
        return parseInt(getString(key));
    }

    public static boolean getBoolean(String key) {
        return parseBoolean(getString(key));
    }

    public static URL getUrl(String key) {
        try {
            return new URL(getString(key));
        } catch (MalformedURLException e) {
            throw new WebDriverManagerException(e);
        }
    }

    private static String getProperty(String key) {
        String value = null;
        Properties properties = new Properties();
        try {
            InputStream inputStream = WdmConfig.class
                    .getResourceAsStream(System.getProperty("wdm.properties",
                            "/webdrivermanager.properties"));
            properties.load(inputStream);
            value = properties.getProperty(key);
        } catch (Exception e) {
            throw new WebDriverManagerException(e);
        } finally {
            if (value == null) {
                log.trace("Property key {} not found, using default value",
                        key);
                value = "";
            }
        }
        return value;
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

}
