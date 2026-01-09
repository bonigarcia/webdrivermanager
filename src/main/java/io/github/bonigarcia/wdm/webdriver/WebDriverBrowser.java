/*
 * (C) Copyright 2021 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.webdriver;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.docker.DockerContainer;

/**
 * WebDriver instance and associated Docker containers (if any(.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class WebDriverBrowser {

    static final Logger log = getLogger(lookup().lookupClass());

    WebDriver driver;
    String recordingName;
    String browserName;
    OperatingSystem os;
    List<DockerContainer> dockerContainerList;
    String browserContainerId;
    String noVncUrl;
    String vncUrl;
    String seleniumServerUrl;
    String recordingBase64;
    Path recordingPath;
    int identityHash;
    boolean isRecording;

    public WebDriverBrowser(String browserName, OperatingSystem os) {
        this.browserName = browserName;
        this.os = os;
        this.dockerContainerList = new ArrayList<>();
        this.isRecording = false;
    }

    public WebDriverBrowser(WebDriver driver, String browserName,
            OperatingSystem os) {
        super();
        this.browserName = browserName;
        this.os = os;
        setDriver(driver);
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
        this.identityHash = calculateIdentityHash(driver);
    }

    public List<DockerContainer> getDockerContainerList() {
        return dockerContainerList;
    }

    public void addDockerContainer(DockerContainer dockerContainer) {
        dockerContainerList.add(dockerContainer);
    }

    public void addDockerContainer(DockerContainer dockerContainer,
            int position) {
        dockerContainerList.add(position, dockerContainer);
    }

    public String getBrowserContainerId() {
        return browserContainerId;
    }

    public void setBrowserContainerId(String browserContainerId) {
        this.browserContainerId = browserContainerId;
    }

    public URL getNoVncUrl() {
        return getUrl(noVncUrl);
    }

    public void setNoVncUrl(String noVncUrl) {
        this.noVncUrl = noVncUrl;
    }

    public String getVncUrl() {
        return vncUrl;
    }

    public void setVncUrl(String vncUrl) {
        this.vncUrl = vncUrl;
    }

    public URL getSeleniumServerUrl() {
        return getUrl(seleniumServerUrl);
    }

    public void setSeleniumServerUrl(String seleniumServerUrl) {
        this.seleniumServerUrl = seleniumServerUrl;
    }

    protected URL getUrl(String urlStr) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            log.error("Exception creating URL", e);
        }
        return url;
    }

    public Path getRecordingPath() {
        if (recordingBase64 != null && recordingPath == null) {
            try {
                byte[] videoBytes = Base64.getDecoder().decode(recordingBase64);
                Path path = Paths.get(recordingName);
                Files.write(path, videoBytes);
                setRecordingPath(path);
            } catch (Exception e) {
                log.warn("Exception stopping recording", e);
            }
        }
        return recordingPath;
    }

    public void setRecordingPath(Path recordingPath) {
        this.recordingPath = recordingPath;
    }

    public int getIdentityHash() {
        return identityHash;
    }

    public int calculateIdentityHash(Object object) {
        return System.identityHashCode(object);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> readLogs() {
        return (List<Map<String, Object>>) readJavaScriptVariable(
                "console._bwLogs");
    }

    public Object readJavaScriptVariable(String jsVariable) {
        return executeJavaScript("return " + jsVariable + ";");
    }

    public Object executeJavaScript(String jsCommand) {
        return ((JavascriptExecutor) driver).executeScript(jsCommand);
    }

    public void startRecording() {
        startRecording(Recording.getRecordingNameForBrowserWatcher(browserName,
                ((RemoteWebDriver) driver).getSessionId().toString()));
    }

    public void startRecording(String recordingName) {
        isRecording = true;
        setRecordingName(recordingName);
        ((JavascriptExecutor) driver).executeScript(
                "window.postMessage({ type: \"startRecording\", name: \""
                        + recordingName + "\" }, \"*\");");
    }

    public void stopRecording() {
        if (isRecording) {
            String script = "var callback = arguments[0];"
                    + "function handler(event) {"
                    + "    if (event.data.type === \"stopRecordingResponse\") {"
                    + "        window.removeEventListener(\"message\", handler);"
                    + "        callback(event.data.result);" + "    }" + "}"
                    + "window.addEventListener(\"message\", handler);"
                    + "window.postMessage({ type: \"stopRecordingBase64\" }, \"*\");";

            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> result = (java.util.Map<String, Object>) ((JavascriptExecutor) driver)
                    .executeAsyncScript(script);

            setRecordingName((String) result.get("name"));
            setRecordingBase64((String) result.get("base64"));
        }
        isRecording = false;
    }

    public String getRecordingName() {
        return recordingName;
    }

    public void setRecordingName(String recordingName) {
        this.recordingName = recordingName;
    }

    public String getRecordingBase64() {
        String recording = path2Base64(recordingPath);
        if (recording != null) {
            setRecordingBase64(recording);
        }
        return recordingBase64;
    }

    public void setRecordingBase64(String recordingBase64) {
        this.recordingBase64 = recordingBase64;
    }

    public static String path2Base64(Path filePath) {
        try {
            if (filePath != null) {
                byte[] fileBytes = Files.readAllBytes(filePath);
                return Base64.getEncoder().encodeToString(fileBytes);
            }
        } catch (Exception e) {
            log.warn("Exception converting {} to Base64", filePath, e);
        }
        return null;
    }

}
