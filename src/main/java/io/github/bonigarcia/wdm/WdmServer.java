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
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.WebDriverManager.chromedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.edgedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver;
import static io.github.bonigarcia.wdm.WebDriverManager.iedriver;
import static io.github.bonigarcia.wdm.WebDriverManager.operadriver;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.hc.client5.http.config.RequestConfig.custom;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;

import com.google.gson.Gson;

import io.github.bonigarcia.wdm.config.Config;
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

    private static final String SESSION = "/session";
    private static final String GET = "GET";
    private static final String DELETE = "DELETE";
    private static final String POST = "POST";
    private static final String SESSIONID = "\"sessionId\":";

    static final Logger log = getLogger(lookup().lookupClass());

    private Map<String, URL> sessionMap;
    private Map<String, WebDriverManager> wdmMap;
    private Config config;
    private String path;

    public WdmServer(int port) {
        sessionMap = new ConcurrentHashMap<>();
        wdmMap = new ConcurrentHashMap<>();
        config = new Config();

        String serverPath = config.getServerPath();
        path = serverPath.endsWith("/")
                ? serverPath.substring(0, serverPath.length() - 1)
                : serverPath;

        Javalin app = Javalin.create().start(port);
        Handler handler = this::handleRequest;

        // Resolve drivers
        app.get(path + "/chromedriver", handler);
        app.get(path + "/firefoxdriver", handler);
        app.get(path + "/edgedriver", handler);
        app.get(path + "/iedriver", handler);
        app.get(path + "/operadriver", handler);

        // Selenium Server
        app.post(path + SESSION, handler);
        app.post(path + SESSION + "/*", handler);
        app.get(path + SESSION + "/*", handler);
        app.delete(path + SESSION + "/*", handler);

        String localHostAddress = getLocalHostAddress();
        log.info("WebDriverManager Server listening on http://{}:{}{}",
                localHostAddress, port, path);
    }

    private String getLocalHostAddress() {
        String localHostAddress;
        try {
            localHostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localHostAddress = InetAddress.getLoopbackAddress()
                    .getHostAddress();
        }
        return localHostAddress;
    }

    private void handleRequest(Context ctx) throws IOException {
        String requestMethod = ctx.method();
        String requestPath = ctx.path();
        log.info("Request: {} {}", requestMethod, requestPath);

        Optional<WebDriverManager> driverManager = createDriverManager(
                requestPath);
        if (driverManager.isPresent()) {
            // Resolve drivers
            resolveDriver(ctx, driverManager.get());
        } else {
            // Selenium Server
            seleniumServer(ctx);
        }
    }

    private void seleniumServer(Context ctx) throws IOException {
        String requestMethod = ctx.method();
        String requestPath = ctx.path().replace(path, "");
        String requestBody = ctx.body();
        log.trace("Body: {} ", requestBody);
        Session session = new Gson().fromJson(requestBody, Session.class);
        URL seleniumServerUrl;

        // POST /session
        boolean isSessionCreate = session != null
                && session.getDesiredCapabilities() != null;
        WebDriverManager wdm = null;
        if (isSessionCreate) {
            String browserName = session.getDesiredCapabilities()
                    .getBrowserName();
            String version = session.getDesiredCapabilities().getVersion();
            wdm = WebDriverManager.getInstance(browserName).browserInDocker()
                    .browserVersion(version);
            wdm.create();
            seleniumServerUrl = wdm.getDockerSeleniumServerUrl();
        } else {
            String sessionIdFromPath = getSessionIdFromPath(requestPath);
            seleniumServerUrl = sessionMap.get(sessionIdFromPath);
        }

        // exchange request-response
        String response = exchange(
                seleniumServerUrl.toString().replaceAll("/\\z", "")
                        + requestPath,
                requestMethod, requestBody, config.getServerTimeoutSec());
        log.info("Response: {}", response);
        ctx.contentType("application/json");
        ctx.result(response);

        if (isSessionCreate) {
            String sessionId = getSessionIdFromResponse(response);
            sessionMap.put(sessionId, seleniumServerUrl);
            wdmMap.put(sessionId, wdm);
        }

        // DELETE /session/sessionId
        if (requestMethod.equalsIgnoreCase(DELETE)
                && requestPath.startsWith(SESSION + "/")) {
            String sessionIdFromPath = getSessionIdFromPath(requestPath);
            wdmMap.get(sessionIdFromPath).quit();
            wdmMap.remove(sessionIdFromPath);
            sessionMap.remove(sessionIdFromPath);
        }
    }

    private String getSessionIdFromResponse(String response) {
        response = response.substring(
                response.indexOf(SESSIONID) + SESSIONID.length() + 1);
        response = response.substring(0, response.indexOf("\""));
        return response;
    }

    private String getSessionIdFromPath(String path) {
        path = path.substring(path.indexOf("/") + 1);
        path = path.substring(path.indexOf("/") + 1);
        int i = path.indexOf("/");
        if (i != -1) {
            path = path.substring(0, i);
        }
        return path;
    }

    private Optional<WebDriverManager> createDriverManager(String requestPath) {
        Optional<WebDriverManager> out;
        switch (requestPath.replace(path, "").substring(1)) {
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

    public String exchange(String url, String method, String json,
            int timeoutSec) throws IOException {
        String responseContent = null;
        try (CloseableHttpClient closeableHttpClient = HttpClientBuilder
                .create().build()) {

            HttpUriRequestBase request = null;
            switch (method) {
            case GET:
                request = new HttpGet(url);
                break;
            case DELETE:
                request = new HttpDelete(url);
                break;
            default:
            case POST:
                request = new HttpPost(url);
                HttpEntity body = new StringEntity(json);
                request.setEntity(body);
                request.setHeader("Content-Type", "application/json");
                break;
            }

            RequestConfig requestConfig = custom()
                    .setConnectTimeout(timeoutSec, TimeUnit.SECONDS).build();
            request.setConfig(requestConfig);

            try (CloseableHttpResponse response = closeableHttpClient
                    .execute(request)) {
                responseContent = IOUtils
                        .toString(response.getEntity().getContent(), UTF_8);
            }
        }

        return responseContent;
    }

    static class Session {
        DesiredCapabilities desiredCapabilities;

        public DesiredCapabilities getDesiredCapabilities() {
            return desiredCapabilities;
        }
    }

    static class DesiredCapabilities {
        String browserName;
        String version;
        String platform;

        public String getBrowserName() {
            return browserName;
        }

        public String getVersion() {
            return version;
        }

        public String getPlatform() {
            return platform;
        }
    }

}
