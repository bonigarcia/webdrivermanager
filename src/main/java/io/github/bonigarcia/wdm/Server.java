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

import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.edgedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver;
import static io.github.bonigarcia.wdm.WebDriverManager.iedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.operadriver;
import static io.github.bonigarcia.wdm.WebDriverManager.phantomjs;
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.slf4j.Logger;

import io.javalin.Handler;
import io.javalin.Javalin;

/**
 * WebDriverManager server.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.0.0
 */
public class Server {

    final Logger log = getLogger(lookup().lookupClass());

    public Server(int port) {
        Javalin app = Javalin.create().start(port);
        Handler handler = ctx -> {

            String requestMethod = ctx.method();
            String requestPath = ctx.path();
            log.info("Server request: {} {}", requestMethod, requestPath);

            WebDriverManager driverManager = null;
            switch (requestPath.substring(1)) {
            case "chromedriver":
                driverManager = chromedriver();
                break;
            case "firefoxdriver":
                driverManager = firefoxdriver();
                break;
            case "edgedriver":
                driverManager = edgedriver();
                break;
            case "iedriver":
                driverManager = iedriver();
                break;
            case "operadriver":
                driverManager = operadriver();
                break;
            case "phantomjs":
                driverManager = phantomjs();
                break;
            }

            driverManager.setup();
            File binary = new File(driverManager.getBinaryPath());
            String binaryVersion = driverManager.getDownloadedVersion();
            String binaryName = binary.getName();
            String binaryLength = String.valueOf(binary.length());

            ctx.res.setHeader("Content-Disposition",
                    "attachment; filename=\"" + binaryName + "\"");
            ctx.res.setHeader("Content-Length", binaryLength);
            ctx.res.setHeader("Content-Type", "application/octet-stream");

            ctx.result(openInputStream(binary));

            log.info("Server response: {} {} ({} bytes)", binaryName,
                    binaryVersion, binaryLength);
        };

        app.get("/chromedriver", handler);
        app.get("/firefoxdriver", handler);
        app.get("/edgedriver", handler);
        app.get("/iedriver", handler);
        app.get("/operadriver", handler);
        app.get("/phantomjs", handler);

        log.info("WebDriverManager server listening on port {}", port);
    }

}