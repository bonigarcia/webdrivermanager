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

import io.github.bonigarcia.wdm.config.DummyConfig;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static io.github.bonigarcia.wdm.config.Architecture.ARM64;
import static io.github.bonigarcia.wdm.config.Architecture.DEFAULT;
import static io.github.bonigarcia.wdm.config.Architecture.X32;
import static io.github.bonigarcia.wdm.config.Architecture.X64;
import static io.github.bonigarcia.wdm.config.OperatingSystem.LINUX;
import static io.github.bonigarcia.wdm.config.OperatingSystem.MAC;
import static io.github.bonigarcia.wdm.config.OperatingSystem.WIN;
import static org.assertj.core.api.Assertions.assertThat;

class EdgeDriverManagerTest {
    private final EdgeDriverManager edgeDriverManager = new EdgeDriverManager();
    private final String version = "106.0.1370.47";

    @Test
    void edgeVersionOnLinux() throws MalformedURLException {
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(LINUX, X32))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_linux64.zip"));
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(LINUX, X64))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_linux64.zip"));
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(LINUX, ARM64))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_arm64.zip"));
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(LINUX, DEFAULT))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_linux64.zip"));
    }

    @Test
    void edgeVersionOnMac() throws MalformedURLException {
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(MAC, X32))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_mac64.zip"));
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(MAC, X64))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_mac64.zip"));
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(MAC, ARM64))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_mac64_m1.zip"));
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(MAC, DEFAULT))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_mac64.zip"));
    }

    @Test
    void edgeVersionOnWindows() throws MalformedURLException {
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(WIN, X32))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_win32.zip"));
        assertThat(edgeDriverManager.buildUrl(version,
                new DummyConfig(WIN, X64))).hasValue(new URL(
                        "https://msedgedriver.microsoft.com/106.0.1370.47/edgedriver_win64.zip"));
    }
}