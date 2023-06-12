package io.github.bonigarcia.wdm.managers;

import static io.github.bonigarcia.wdm.config.Architecture.ARM64;
import static io.github.bonigarcia.wdm.config.Architecture.DEFAULT;
import static io.github.bonigarcia.wdm.config.Architecture.X32;
import static io.github.bonigarcia.wdm.config.Architecture.X64;
import static io.github.bonigarcia.wdm.config.OperatingSystem.LINUX;
import static io.github.bonigarcia.wdm.config.OperatingSystem.MAC;
import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.config.DummyConfig;

class ChromeDriverManagerTest {
    private static final String VERSION = "115.0.5790.24";
    private final ChromeDriverManager chromeDriverManager = new ChromeDriverManager();

    @Test
    void chromeVersionOnLinux() throws MalformedURLException {
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX,
                X32))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/linux64/chromedriver-linux64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX,
                X64))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/linux64/chromedriver-linux64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX,
                ARM64))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/linux64/chromedriver-linux64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX,
                DEFAULT))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/linux64/chromedriver-linux64.zip",
                        VERSION)));
    }

    @Test
    void chromeVersionOnMac() throws MalformedURLException {
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC,
                X32))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/mac-x64/chromedriver-mac-x64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC,
                X64))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/mac-x64/chromedriver-mac-x64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC,
                ARM64))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/mac-arm64/chromedriver-mac-arm64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC,
                DEFAULT))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/mac-x64/chromedriver-mac-x64.zip",
                        VERSION)));
    }

    @Test
    void chromeVersionOnWindows() throws MalformedURLException {
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN,
                X32))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/win32/chromedriver-win32.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN,
                X64))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/win64/chromedriver-win64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN,
                DEFAULT))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/win64/chromedriver-win64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN,
                ARM64))).hasValue(new URL(String.format(
                        "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/win64/chromedriver-win64.zip",
                        VERSION)));
    }
}
