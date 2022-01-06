/*
 * (C) Copyright 2018 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.cache;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

/**
 * Resolution cache.
 *
 * @author Boni Garcia
 * @since 3.0.0
 */
public class ResolutionCache {

    final Logger log = getLogger(lookup().lookupClass());

    static final String TTL = "-ttl";
    static final String RESOLUTION_CACHE_INFO = "WebDriverManager Resolution Cache";

    Properties props = new Properties() {
        private static final long serialVersionUID = 3734950329657085291L;

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<Object>(super.keySet()));
        }
    };

    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy z");
    Config config;
    File resolutionCacheFile;

    public ResolutionCache(Config config) {
        this.config = config;

        if (!config.isAvoidResolutionCache()) {
            File resolutionCachePath = config.getResolutionCachePath();
            this.resolutionCacheFile = new File(resolutionCachePath,
                    config.getResolutionCache());
            try {
                if (!resolutionCacheFile.exists()) {
                    boolean createNewFile = resolutionCacheFile.createNewFile();
                    if (createNewFile) {
                        log.debug("Created new resolution cache file at {}",
                                resolutionCacheFile);
                    }
                }
                try (InputStream fis = new FileInputStream(
                        resolutionCacheFile)) {
                    props.load(fis);
                }
            } catch (Exception e) {
                throw new WebDriverManagerException(
                        "Exception reading resolution cache as a properties file",
                        e);
            }
        }
    }

    public String getValueFromResolutionCache(String key) {
        return props.getProperty(key, null);
    }

    private Date getExpirationDateFromResolutionCache(String key) {
        Date result = new Date(0);
        try {
            result = dateFormat.parse(props.getProperty(getExpirationKey(key)));
            return result;
        } catch (Exception e) {
            log.warn("Exception parsing date ({}) from resolution cache {}",
                    key, e.getMessage());
        }
        return result;
    }

    public void putValueInResolutionCacheIfEmpty(String key, String value,
            int ttl) {
        if (ttl > 0 && getValueFromResolutionCache(key) == null) {
            props.put(key, value);

            long now = new Date().getTime();
            Date expirationDate = new Date(now + SECONDS.toMillis(ttl));
            String expirationDateStr = formatDate(expirationDate);
            props.put(getExpirationKey(key), expirationDateStr);
            if (log.isDebugEnabled()) {
                log.debug("Storing resolution {}={} in cache (valid until {})",
                        key, value, expirationDateStr);
            }
            storeProperties();
        }
    }

    private synchronized void storeProperties() {
        try (OutputStream fos = new FileOutputStream(resolutionCacheFile)) {
            props.store(fos, RESOLUTION_CACHE_INFO);
        } catch (Exception e) {
            log.warn(
                    "Exception writing resolution cache as a properties file {}",
                    e.getClass().getName());
        }
    }

    private void clearFromResolutionCache(String key) {
        props.remove(key);
        props.remove(getExpirationKey(key));
        storeProperties();
    }

    public void clear() {
        log.info("Clearing WebDriverManager resolution cache");
        props.clear();
        storeProperties();
    }

    private boolean checkValidity(String key, String value,
            Date expirationDate) {
        long now = new Date().getTime();
        long expirationTime = expirationDate != null ? expirationDate.getTime()
                : 0;
        boolean isValid = value != null && expirationTime != 0
                && expirationTime > now;
        if (!isValid) {
            log.debug("Removing resolution {}={} from cache (expired on {})",
                    key, value, expirationDate);
            clearFromResolutionCache(key);
        }
        return isValid;
    }

    private String formatDate(Date date) {
        return date != null ? dateFormat.format(date) : "";
    }

    private String getExpirationKey(String key) {
        return key + TTL;
    }

    public boolean checkKeyInResolutionCache(String key) {
        return checkKeyInResolutionCache(key, true);
    }

    public boolean checkKeyInResolutionCache(String key, boolean showLog) {
        String valueFromResolutionCache = getValueFromResolutionCache(key);
        boolean valueInResolutionCache = valueFromResolutionCache != null
                && !valueFromResolutionCache.isEmpty();
        if (valueInResolutionCache) {
            Date expirationDate = getExpirationDateFromResolutionCache(key);
            valueInResolutionCache &= checkValidity(key,
                    valueFromResolutionCache, expirationDate);
            if (valueInResolutionCache) {
                String strDate = formatDate(expirationDate);
                if (showLog) {
                    log.debug("Resolution {}={} in cache (valid until {})", key,
                            valueFromResolutionCache, strDate);
                }
            }
        }
        return valueInResolutionCache;
    }

}
