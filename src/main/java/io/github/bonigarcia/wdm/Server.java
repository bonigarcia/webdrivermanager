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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;

import io.javalin.Context;
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

            Optional<WebDriverManager> driverManager = createDriverManager(
                    requestPath);
            if (driverManager.isPresent()) {
                resolveDriver(ctx, driverManager.get());
            }
        };

        app.get("/chromedriver", handler);
        app.get("/firefoxdriver", handler);
        app.get("/edgedriver", handler);
        app.get("/iedriver", handler);
        app.get("/operadriver", handler);
        app.get("/phantomjs", handler);

        log.info("WebDriverManager server listening on port {}", port);
    }

    private Optional<WebDriverManager> createDriverManager(String requestPath) {
        Optional<WebDriverManager> out;
        switch (requestPath.substring(1)) {
        case "chromedriver":
            out = Optional.of(chromedriver());
            break;
        case "firefoxdriver":
            out = Optional.of(firefoxdriver());
            break;
        case "edgedriver":
            out = Optional.of(edgedriver());
            break;
        case "iedriver":
            out = Optional.of(iedriver());
            break;
        case "operadriver":
            out = Optional.of(operadriver());
            break;
        case "phantomjs":
            out = Optional.of(phantomjs());
            break;
        default:
            log.warn("Unknown option {}", requestPath);
            out = Optional.empty();
        }
        return out;
    }

    private synchronized void resolveDriver(Context ctx,
            WebDriverManager driverManager) throws IOException {

        // Query string (for configuration parameters)
        Map<String, List<String>> queryParamMap = ctx.queryParamMap();
        if (!queryParamMap.isEmpty()) {
            log.info("Server query string for configuration {}", queryParamMap);
            for (Entry<String, List<String>> entry : queryParamMap.entrySet()) {
                String configKey = "wdm." + entry.getKey();
                String configValue = entry.getValue().get(0);
                log.trace("\t{} = {}", configKey, configValue);
                System.setProperty(configKey, configValue);
            }
        }

        // Resolve driver
        WebDriverManager.config().setAvoidExport(true);
        driverManager.setup();
        File binary = new File(driverManager.getBinaryPath());
        String binaryVersion = driverManager.getDownloadedVersion();
        String binaryName = binary.getName();
        String binaryLength = String.valueOf(binary.length());

        // Response
        ctx.res.setHeader("Content-Disposition",
                "attachment; filename=\"" + binaryName + "\"");
        ctx.result(openInputStream(binary));
        log.info("Server response: {} {} ({} bytes)", binaryName, binaryVersion,
                binaryLength);

        // Clear configuration
        for (String key : queryParamMap.keySet()) {
            System.clearProperty("wdm." + key);
        }
    }

}