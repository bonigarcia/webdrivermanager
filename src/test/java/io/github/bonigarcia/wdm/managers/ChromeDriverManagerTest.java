package io.github.bonigarcia.wdm.managers;

import io.github.bonigarcia.wdm.config.DummyConfig;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static io.github.bonigarcia.wdm.config.Architecture.*;
import static io.github.bonigarcia.wdm.config.Architecture.ARM64;
import static io.github.bonigarcia.wdm.config.OperatingSystem.*;
import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;
import static org.assertj.core.api.Assertions.assertThat;

public class ChromeDriverManagerTest {
    private static final String VERSION = "108.0.5359.22";
    private final ChromeDriverManager chromeDriverManager = new ChromeDriverManager();
    @Test
    void chromeVersionOnLinux() throws MalformedURLException {
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX, X32)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_linux64.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX, X64)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_linux64.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX, ARM64)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_linux64.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX, DEFAULT)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_linux64.zip", VERSION)));
    }

    @Test
    void chromeVersionOnMac() throws MalformedURLException {
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC, X32)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_mac64.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC, X64)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_mac64.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC, ARM64)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_mac_arm64.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC, DEFAULT)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_mac64.zip", VERSION)));
    }

    @Test
    void chromeVersionOnWindows() throws MalformedURLException {
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN, X32)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_win32.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN, X64)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_win32.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN, DEFAULT)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_win32.zip", VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN, ARM64)))
                .hasValue(new URL(String.format("https://chromedriver.storage.googleapis.com/%s/chromedriver_win32.zip", VERSION)));
    }
}
