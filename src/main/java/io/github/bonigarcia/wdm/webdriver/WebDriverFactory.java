package io.github.bonigarcia.wdm.webdriver;

import java.net.URL;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;


import io.github.bonigarcia.wdm.config.Config;


public class WebDriverFactory {


    private final Config config;


    public WebDriverFactory(Config config) {
        this.config = config;
    }


    public WebDriver createWebDriver(URL url, Capabilities capabilities) {
        if (config.getEnableTracing()) {
            return new RemoteWebDriver(url, capabilities);
        } else {
            return new RemoteWebDriver(url, capabilities, false);
        }
    }
}
