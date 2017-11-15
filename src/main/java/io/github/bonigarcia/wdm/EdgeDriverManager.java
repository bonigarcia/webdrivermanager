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

import static io.github.bonigarcia.wdm.WdmConfig.getInt;
import static io.github.bonigarcia.wdm.WdmConfig.getString;
import static java.util.Arrays.asList;
import static org.jsoup.Jsoup.parse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
        driverName = asList("MicrosoftWebDriver");
    }

    @Override
    public List<URL> getDrivers() throws IOException {
        listVersions = new ArrayList<>();
        List<URL> urlList = new ArrayList<>();

        String edgeDriverUrl = getString("wdm.edgeDriverUrl");
        log.debug("Reading {} to find out the latest version of Edge driver",
                edgeDriverUrl);

        int timeout = (int) TimeUnit.SECONDS.toMillis(getInt("wdm.timeout"));

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
