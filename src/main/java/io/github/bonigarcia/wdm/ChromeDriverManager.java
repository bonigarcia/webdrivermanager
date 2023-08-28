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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Manager for Chrome.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class ChromeDriverManager extends BrowserManager {
    final Logger log = getLogger(lookup().lookupClass());

    public static synchronized BrowserManager getInstance() {
        if (instance == null
                || !instance.getClass().equals(ChromeDriverManager.class)) {

            instance = new ChromeDriverManager();
        }
        return instance;
    }

    public ChromeDriverManager() {
        exportParameter = getString("wdm.chromeDriverExport");
        driverVersionKey = "wdm.chromeDriverVersion";
        String driverVersion = getDriverVersion();
        driverUrlKey =
                driverVersion.equals("LATEST") || Integer.parseInt(driverVersionKey.split("\\.")[0]) > 114
                        ? "wdm.chromeDriverUrl" : "wdm.chromeDriverUrl.legacy";
        driverName = asList("chromedriver");
    }

    @Override
    protected List<URL> getDrivers() throws IOException {
        URL driverUrl = getDriverUrl();
        List<URL> urls;
        if (isUsingTaobaoMirror()) {
            urls = getDriversFromMirror(driverUrl);
        } else if (isUsingNexus()) {
            urls = getDriversFromNexus(driverUrl);
        } else if (driverUrlKey.equals("wdm.chromeDriverUrl.legacy")) {
            urls = getDriversFromXml(driverUrl);
        } else {
            Function<JsonElement, List<URL>> parser = jsonElement -> {
                JsonArray versions = jsonElement.getAsJsonObject().getAsJsonArray("versions");
                List<URL> urlList = new ArrayList<>();
                List<JsonElement> drivers = versions.asList().stream()
                        .filter(version -> version.getAsJsonObject().getAsJsonObject("downloads")
                                .has("chromedriver"))
                        .collect(Collectors.toList());
                List<JsonArray> downloads = drivers.stream().map(driver
                        -> driver.getAsJsonObject().getAsJsonObject("downloads")
                        .getAsJsonArray("chromedriver")).collect(Collectors.toList());

                downloads.forEach(downloadList -> {
                    downloadList.forEach(download -> {
                        try {
                            urlList.add(new URL(download.getAsJsonObject().get("url").getAsString()));
                        } catch (MalformedURLException e) {
                            log.error(e.getMessage());
                        }
                    });
                });
                return urlList;
            };

            urls = getDriversFromJson(driverUrl, parser);
        }
        return urls;
    }

    @Override
    protected String getCurrentVersion(URL url, String driverName) {
        if (isUsingTaobaoMirror()) {
            int i = url.getFile().lastIndexOf(SLASH);
            int j = url.getFile().substring(0, i).lastIndexOf(SLASH) + 1;
            return url.getFile().substring(j, i);
        } else if (driverUrlKey.equals("wdm.chromeDriverUrl.legacy")) {
            return super.getCurrentVersion(url, driverName);
        } else {
            return url.getFile().split("/")[4];
        }
    }

    @Override
    public BrowserManager useTaobaoMirror() {
        return useTaobaoMirror("wdm.chromeDriverTaobaoUrl");
    }

}
