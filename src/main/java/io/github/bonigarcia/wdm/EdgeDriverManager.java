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

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static io.github.bonigarcia.wdm.WdmConfig.getInt;
import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jsoup.Jsoup.parse;

/**
 * Manager for Microsoft Edge.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.0
 */
public class EdgeDriverManager extends BrowserManager {

    public static synchronized BrowserManager getInstance() {
        if (instance == null
                || !instance.getClass().equals(EdgeDriverManager.class)) {
            instance = new EdgeDriverManager();
        }
        return instance;
    }

    public EdgeDriverManager() {
        exportParameter = getString("wdm.edgeExport");
        driverVersionKey = "wdm.edgeVersion";
        driverUrlKey = "wdm.edgeDriverUrl";
        driverName = asList("MicrosoftWebDriver", "edgedriver");
    }

    @Override
    public List<URL> getDrivers() throws IOException {
        if (isUsingNexus()) {
            return getDriversFromNexus(getDriverUrl());
        } else {
            listVersions = new ArrayList<>();
            List<URL> urlList = new ArrayList<>();

            String edgeDriverUrl = getString("wdm.edgeDriverUrl");
            log.debug("Reading {} to find out the latest version of Edge driver",
                    edgeDriverUrl);

            int timeout = (int) SECONDS.toMillis(getInt("wdm.timeout"));

            try (InputStream in = httpClient
                    .execute(new WdmHttpClient.Get(edgeDriverUrl, timeout))
                    .getContent()) {
                Document doc = parse(in, null, "");

                Elements downloadLink = doc
                        .select("ul.driver-downloads li.driver-download > a");
                Elements versionParagraph = doc.select(
                        "ul.driver-downloads li.driver-download p.driver-download__meta");

                for (int i = 0; i < downloadLink.size(); i++) {
                    String[] version = versionParagraph.get(i).text().split(" ");
                    listVersions.add(version[1]);
                    urlList.add(new URL(downloadLink.get(i).attr("href")));
                }

                return urlList;
            }
        }
    }

    @Override
    protected List<URL> getLatest(List<URL> list, List<String> match) {
        log.trace("Checking the lastest version of {} with URL list {}", match,
                list);
        List<URL> out = new ArrayList<>();
        versionToDownload = listVersions.iterator().next();
        out.add(list.iterator().next());
        log.info("Latest version of MicrosoftWebDriver is {}",
                versionToDownload);
        return out;
    }

}
