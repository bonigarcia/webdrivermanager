/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.test;

import static io.github.bonigarcia.wdm.Architecture.X64;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.HttpClient;
import io.github.bonigarcia.wdm.UrlFilter;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Filter verifications for phantomjs.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsFilterTest {

    final Logger log = getLogger(lookup().lookupClass());

    protected WebDriverManager phatomJsManager;
    protected List<URL> driversUrls;
    protected final String phantomJsBinaryName = "phantomjs";

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        phatomJsManager = WebDriverManager.phantomjs();
        Field field = WebDriverManager.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(phatomJsManager, new HttpClient());

        Method method = WebDriverManager.class.getDeclaredMethod("getDrivers");
        method.setAccessible(true);
        driversUrls = (List<URL>) method.invoke(phatomJsManager);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilterPhantomJs() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method method = WebDriverManager.class.getDeclaredMethod("getLatest",
                List.class, List.class);
        method.setAccessible(true);
        List<URL> latestUrls = (List<URL>) method.invoke(phatomJsManager,
                driversUrls, asList(phantomJsBinaryName));

        List<URL> filteredLatestUrls = new UrlFilter().filterByArch(latestUrls,
                X64, false);

        log.info("Filtered URLS for LATEST version {} : {}",
                phantomJsBinaryName, filteredLatestUrls);

        assertThat(filteredLatestUrls, not(empty()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilterVersionPhantomJs() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        String specificVersion = "1.9.6";
        Method method = WebDriverManager.class.getDeclaredMethod("getVersion",
                List.class, List.class, String.class);
        method.setAccessible(true);
        List<URL> specificVersionUrls = (List<URL>) method.invoke(
                phatomJsManager, driversUrls, asList(phantomJsBinaryName),
                specificVersion);

        List<URL> filteredVersionUrls = new UrlFilter()
                .filterByArch(specificVersionUrls, X64, false);

        log.info("Filtered URLS for {} version {}: {}", phantomJsBinaryName,
                specificVersion, filteredVersionUrls);

        assertThat(filteredVersionUrls, not(empty()));
    }

}
