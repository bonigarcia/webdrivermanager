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

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Configuration utility class.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class WdmConfig {

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

    public static URL getUrl(String key) throws MalformedURLException {
        return new URL(getString(key));
    }

    private static String getProperty(String key) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = WdmConfig.class
                    .getResourceAsStream(System.getProperty("wdm.properties",
                            "/webdrivermanager.properties"));
            properties.load(inputStream);
        } catch (Exception e) {
            throw new WebDriverManagerException(e);
        }
        return properties.getProperty(key);
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

}
