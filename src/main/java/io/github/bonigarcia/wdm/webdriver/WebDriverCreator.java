package io.github.bonigarcia.wdm.webdriver;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

/**
 * Builder for WebDriver objects.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class WebDriverCreator {

    final Logger log = getLogger(lookup().lookupClass());

    static final int POLL_TIME_SEC = 1;

    Config config;

    public WebDriverCreator(Config config) {
        this.config = config;
    }

    public synchronized WebDriver createLocalWebDriver(Class<?> browserClass,
                                                       Capabilities capabilities)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        WebDriver driver;
        if (capabilities != null) {
            driver = (WebDriver) browserClass
                    .getDeclaredConstructor(capabilities.getClass())
                    .newInstance(capabilities);
        } else {
            driver = (WebDriver) browserClass.getDeclaredConstructor()
                    .newInstance();
        }
        return driver;
    }

    public WebDriver createRemoteWebDriver(String remoteUrl,
                                           Capabilities capabilities) {
        WebDriver webdriver = null;
        int waitTimeoutSec = config.getTimeout();
        long timeoutMs = System.currentTimeMillis()
                + TimeUnit.SECONDS.toMillis(waitTimeoutSec);

        String browserName = capabilities.getBrowserName();
        log.debug("Creating WebDriver object for {} at {} with {}", browserName,
                remoteUrl, capabilities);
        WebDriverFactory factory = new WebDriverFactory(config);
        do {
            try {
                URL url = new URL(remoteUrl);
                HttpURLConnection huc = (HttpURLConnection) url
                        .openConnection();
                huc.connect();
                int responseCode = huc.getResponseCode();
                log.trace("Requesting {} (the response code is {})", remoteUrl,
                        responseCode);

                webdriver = factory.createWebDriver(url, capabilities);
            } catch (Exception e1) {
                try {
                    log.trace("{} creating WebDriver object ({})",
                            e1.getClass().getSimpleName(), e1.getMessage());
                    if (System.currentTimeMillis() > timeoutMs) {
                        throw new WebDriverManagerException(
                                "Timeout of " + waitTimeoutSec
                                        + " seconds creating WebDriver object",
                                e1);
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(POLL_TIME_SEC));
                } catch (InterruptedException e2) {
                    log.warn("Interrupted exception creating WebDriver object",
                            e2);
                    Thread.currentThread().interrupt();
                }
            }

        } while (webdriver == null);

        return webdriver;
    }

    public String getSessionId(WebDriver webDriver) {
        String sessionId = ((RemoteWebDriver) webDriver).getSessionId()
                .toString();
        log.debug("The sessionId is {}", sessionId);
        return sessionId;
    }
}
