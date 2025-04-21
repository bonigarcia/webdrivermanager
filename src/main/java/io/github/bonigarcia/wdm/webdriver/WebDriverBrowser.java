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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;

import io.github.bonigarcia.wdm.config.OperatingSystem;
import io.github.bonigarcia.wdm.docker.DockerContainer;

/**
 * WebDriver instance and associated Docker containers (if any(.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class WebDriverBrowser {

    final Logger log = getLogger(lookup().lookupClass());

    static final String DEFAULT_DOWNLOADS_FOLDER = "Downloads";
    static final String LINUX_ENV_DOWNLOAD_DIR = "XDG_DOWNLOAD_DIR";
    static final String USER_HOME_PROPERTY = "user.home";
    static final int RECORDING_TIMEOUT_SEC = 5;
    static final int POLL_TIME_MSEC = 500;

    WebDriver driver;
    String recordingName;
    String browserName;
    OperatingSystem os;
    List<DockerContainer> dockerContainerList;
    String browserContainerId;
    String noVncUrl;
    String vncUrl;
    String seleniumServerUrl;
    Path recordingPath;
    int identityHash;

    public WebDriverBrowser(String browserName, OperatingSystem os) {
        this.browserName = browserName;
        this.os = os;
        this.dockerContainerList = new ArrayList<>();
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
        startRecording(Recording.getRecordingName(browserName,
                ((RemoteWebDriver) driver).getSessionId().toString()));
    }

    public void startRecording(String recordingName) {
        setRecordingName(recordingName);
        ((JavascriptExecutor) driver).executeScript(
                "window.postMessage({ type: \"startRecording\", name: \""
                        + recordingName + "\" }, \"*\");");
    }

    public void stopRecording() {
        ((JavascriptExecutor) driver).executeScript(
                "window.postMessage({ type: \"stopRecording\" }, \"*\");");
        waitForRecording();
    }

    public Path getDownloadsFolderPath() {
        Path downloadsPath;
        if (os.isWin()) {
            downloadsPath = Paths.get(Shell32Util
                    .getKnownFolderPath(KnownFolders.FOLDERID_Downloads));
        } else if (os.isMac()) {
            downloadsPath = Paths.get(System.getProperty(USER_HOME_PROPERTY),
                    DEFAULT_DOWNLOADS_FOLDER);
        } else {
            String xdgDownloadDir = System.getenv(LINUX_ENV_DOWNLOAD_DIR);
            if (xdgDownloadDir != null && !xdgDownloadDir.isEmpty()) {
                downloadsPath = Paths.get(xdgDownloadDir);
            } else {
                downloadsPath = Paths.get(
                        System.getProperty(USER_HOME_PROPERTY),
                        DEFAULT_DOWNLOADS_FOLDER);
            }
        }
        return downloadsPath;
    }

    public boolean waitForRecording() {
        Path downloadsPath = getDownloadsFolderPath();
        Path filePath = Paths.get(downloadsPath.toString(), getRecordingName());
        Instant start = Instant.now();
        Duration timeout = Duration.ofSeconds(RECORDING_TIMEOUT_SEC);
        log.trace("Waiting for recording at {}", filePath);

        while (true) {
            if (Files.exists(filePath)) {
                log.debug("Recording found at {}", filePath);
                setRecordingPath(filePath);
                return true;
            }
            if (Duration.between(start, Instant.now()).compareTo(timeout) > 0) {
                log.warn(
                        "Timeout of {} seconds reached, recording {} not found",
                        RECORDING_TIMEOUT_SEC, filePath);
                return false;
            }
            try {
                Thread.sleep(POLL_TIME_MSEC);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public String getRecordingName() {
        return recordingName + Recording.EXTENSION_RECORDING_EXT;
    }

    public void setRecordingName(String recordingName) {
        this.recordingName = recordingName;
    }

}
