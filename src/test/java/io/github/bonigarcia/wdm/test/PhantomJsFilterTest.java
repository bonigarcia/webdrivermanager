/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
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

import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import io.github.bonigarcia.wdm.UrlFilter;
import io.github.bonigarcia.wdm.WdmHttpClient;

/**
 * Filter verifications for phantomjs.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */
public class PhantomJsFilterTest {

    final Logger log = getLogger(lookup().lookupClass());

    protected BrowserManager phatomJsManager;
    protected List<URL> driversUrls;
    protected final String phantomJsBinaryName = "phantomjs";

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        phatomJsManager = PhantomJsDriverManager.getInstance();
        Field field = BrowserManager.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(phatomJsManager, new WdmHttpClient.Builder().build());

        Method method = BrowserManager.class.getDeclaredMethod("getDrivers");
        method.setAccessible(true);
        driversUrls = (List<URL>) method.invoke(phatomJsManager);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilterPhantomJs() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method method = BrowserManager.class.getDeclaredMethod("getLatest",
                List.class, List.class);
        method.setAccessible(true);
        List<URL> latestUrls = (List<URL>) method.invoke(phatomJsManager,
                driversUrls, asList(phantomJsBinaryName));

        List<URL> filteredLatestUrls = new UrlFilter().filterByArch(latestUrls,
                X64);

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
        Method method = BrowserManager.class.getDeclaredMethod("getVersion",
                List.class, List.class, String.class);
        method.setAccessible(true);
        List<URL> specificVersionUrls = (List<URL>) method.invoke(
                phatomJsManager, driversUrls, asList(phantomJsBinaryName),
                specificVersion);

        List<URL> filteredVersionUrls = new UrlFilter()
                .filterByArch(specificVersionUrls, X64);

        log.info("Filtered URLS for {} version {}: {}", phantomJsBinaryName,
                specificVersion, filteredVersionUrls);

        assertThat(filteredVersionUrls, not(empty()));
    }

}
