/*
 * (C) Copyright 2018 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.WebDriverManager.config;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.prefs.Preferences.userNodeForPackage;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;

/**
 * Preferences class.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.0.0
 */
public class Preferences {

    final Logger log = getLogger(lookup().lookupClass());

    final static String TTL = "-ttl";

    java.util.prefs.Preferences preferences = userNodeForPackage(
            WebDriverManager.class);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String getVersionInPreferences(String key) {
        return preferences.get(key, null);
    }

    public long getExpirationTimeInPreferences(String key) {
        return preferences.getLong(getExpirationKey(key), 0);
    }

    public void putVersionInPreferencesIfEmpty(String key, String value) {
        if (getVersionInPreferences(key) == null) {
            preferences.put(key, value);
            long expirationTime = new Date().getTime()
                    + SECONDS.toMillis(config().getTtl());
            preferences.putLong(getExpirationKey(key), expirationTime);
            log.info(
                    "Storing version {} for {} in preferences (expiration time {})",
                    value, key, formatTime(expirationTime));
        }
    }

    public void clearVersionFromPreferences(String key) {
        preferences.remove(key);
        preferences.remove(getExpirationKey(key));
        log.debug("Removing preference {}", key);
    }

    public boolean isValid(String version, long expirationTime) {
        long now = new Date().getTime();
        boolean result = version != null && expirationTime != 0
                && expirationTime > now;
        log.trace("IsValid version={}? expirationDate={} now={} -- result={}",
                version, formatTime(expirationTime), formatTime(now), result);
        return result;
    }

    public String formatTime(long time) {
        return dateFormat.format(new Date(time));
    }

    private String getExpirationKey(String key) {
        return key + TTL;
    }

}
