/*
 * (C) Copyright 2023 Boni Garcia (https://bonigarcia.github.io/)
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
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/linux64/chromedriver-linux64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX,
                X64))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/linux64/chromedriver-linux64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX,
                ARM64))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/linux64/chromedriver-linux64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(LINUX,
                DEFAULT))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/linux64/chromedriver-linux64.zip",
                        VERSION)));
    }

    @Test
    void chromeVersionOnMac() throws MalformedURLException {
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC,
                X32))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/mac-x64/chromedriver-mac-x64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC,
                X64))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/mac-x64/chromedriver-mac-x64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC,
                ARM64))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/mac-arm64/chromedriver-mac-arm64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(MAC,
                DEFAULT))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/mac-x64/chromedriver-mac-x64.zip",
                        VERSION)));
    }

    @Test
    void chromeVersionOnWindows() throws MalformedURLException {
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN,
                X32))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/win32/chromedriver-win32.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN,
                X64))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/win64/chromedriver-win64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN,
                DEFAULT))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/win64/chromedriver-win64.zip",
                        VERSION)));
        assertThat(chromeDriverManager.buildUrl(VERSION, new DummyConfig(WIN,
                ARM64))).hasValue(new URL(String.format(
                        "https://storage.googleapis.com/chrome-for-testing-public/%s/win64/chromedriver-win64.zip",
                        VERSION)));
    }
}
