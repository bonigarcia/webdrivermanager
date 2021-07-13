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
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;

/**
 * WebDriverManager server.
 *
 * @author Boni Garcia
 * @since 3.0.0
 */
public class WdmServer {

    final Logger log = getLogger(lookup().lookupClass());

    public WdmServer(int port) {
        Javalin app = Javalin.create().start(port);
        Handler handler = this::handleRequest;

        app.get("/chromedriver", handler);
        app.get("/firefoxdriver", handler);
        app.get("/edgedriver", handler);
        app.get("/iedriver", handler);
        app.get("/operadriver", handler);

        log.info("WebDriverManager server listening on port {}", port);
    }

    private void handleRequest(Context ctx) throws IOException {
        String requestMethod = ctx.method();
        String requestPath = ctx.path();
        log.info("Server request: {} {}", requestMethod, requestPath);

        Optional<WebDriverManager> driverManager = createDriverManager(
                requestPath);
        if (driverManager.isPresent()) {
            resolveDriver(ctx, driverManager.get());
        }
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
        default:
            log.warn("Unknown option {}", requestPath);
            out = Optional.empty();
        }
        return out;
    }

    private synchronized void resolveDriver(Context ctx,
            WebDriverManager driverManager) throws IOException {

        // Query string (for configuration parameters)
        Map<String, List<String>> queryParamMap = new TreeMap<>(
                ctx.queryParamMap());

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
        driverManager.config().setAvoidExport(true);
        driverManager.config().setAvoidBrowserDetection(true);
        driverManager.setup();
        File driver = new File(driverManager.getDownloadedDriverPath());
        String driverVersion = driverManager.getDownloadedDriverVersion();
        String driverName = driver.getName();
        String driverLength = String.valueOf(driver.length());

        // Response
        ctx.res.setHeader("Content-Disposition",
                "attachment; filename=\"" + driverName + "\"");
        ctx.result(openInputStream(driver));
        log.info("Server response: {} {} ({} bytes)", driverName, driverVersion,
                driverLength);

        // Clear configuration
        for (String key : queryParamMap.keySet()) {
            System.clearProperty("wdm." + key);
        }
    }

}
