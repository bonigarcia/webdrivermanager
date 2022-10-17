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
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(LINUX, X32)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_linux64.zip"));
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(LINUX, X64)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_linux64.zip"));
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(LINUX, ARM64)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_arm64.zip"));
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(LINUX, DEFAULT)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_linux64.zip"));
  }
  
  @Test
  void edgeVersionOnMac() throws MalformedURLException {
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(MAC, X32)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_mac64.zip"));
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(MAC, X64)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_mac64.zip"));
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(MAC, ARM64)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_mac64_m1.zip"));
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(MAC, DEFAULT)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_mac64.zip"));
  }

  @Test
  void edgeVersionOnWindows() throws MalformedURLException {
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(WIN, X32)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_win32.zip"));
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(WIN, X64)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_win64.zip"));
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(WIN, DEFAULT)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_winDEFAULT.zip")); // TODO seems to be incorrect
    assertThat(edgeDriverManager.buildUrl(version, new DummyConfig(WIN, ARM64)))
      .hasValue(new URL("https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/106.0.1370.47/edgedriver_armARM64.zip")); // TODO seems to be incorrect
  }
}