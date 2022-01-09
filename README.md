[![Maven Central](https://img.shields.io/maven-central/v/io.github.bonigarcia/webdrivermanager.svg)](https://search.maven.org/search?q=g:io.github.bonigarcia%20a:webdrivermanager)
[![Build Status](https://github.com/bonigarcia/webdrivermanager/workflows/build/badge.svg)](https://github.com/bonigarcia/webdrivermanager/actions)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=io.github.bonigarcia:webdrivermanager&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=io.github.bonigarcia%3Awebdrivermanager)
[![codecov](https://codecov.io/gh/bonigarcia/webdrivermanager/branch/master/graph/badge.svg)](https://codecov.io/gh/bonigarcia/webdrivermanager)
[![badge-jdk](https://img.shields.io/badge/jdk-8-green.svg)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![License badge](https://img.shields.io/badge/license-Apache2-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Backers on Open Collective](https://opencollective.com/webdrivermanager/backers/badge.svg)](#backers)
[![Sponsors on Open Collective](https://opencollective.com/webdrivermanager/sponsors/badge.svg)](#sponsors)
[![Support badge](https://img.shields.io/badge/stackoverflow-webdrivermanager_java-green.svg?logo=stackoverflow)](https://stackoverflow.com/questions/tagged/webdrivermanager-java)
[![Twitter Follow](https://img.shields.io/twitter/follow/boni_gg.svg?style=social)](https://twitter.com/boni_gg)

# [![][Logo]][WebDriverManager]
[WebDriverManager] is an open-source Java library that carries out the management (i.e., download, setup, and maintenance) of the drivers required by [Selenium WebDriver] (e.g., chromedriver, geckodriver, msedgedriver, etc.) in a fully automated manner. In addition, WebDriverManager provides other relevant features, such as the capability to discover browsers installed in the local system, building WebDriver objects (such as `ChromeDriver`, `FirefoxDriver`, `EdgeDriver`, etc.), and running browsers in Docker containers seamlessly.

## Documentation
As of version 5, the documentation of WebDriverManager has moved [here][WebDriverManager]. This site contains all the features, examples, configuration, and advanced capabilities of WebDriverManager.

## Driver Management
The primary use of WebDriverManager is the automation of driver management. For using this feature, you need to select a given manager in the WebDriverManager API (e.g., `chromedriver()` for Chrome) and invoke the method `setup()`. The following example shows the skeleton of a test case using [JUnit 5], [Selenium WebDriver], and [WebDriverManager].

```java
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

class ChromeTest {

    WebDriver driver;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        driver = new ChromeDriver();
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void test() {
        // Your test logic here
    }

}
```

For further information about the driver resolution algorithm implemented by WebDriverManager and configuration capabilities, read the [documentation][WebDriverManager].

## Browsers in Docker
Another relevant new feature available in WebDriverManager 5 is the ability to create browsers in [Docker] containers out of the box. The requirement to use this feature is to have installed a [Docker Engine] in the machine running the tests. To use it, we need to invoke the method `browserInDocker()` in conjunction with `create()` of a given manager. This way, WebDriverManager pulls the image from [Docker Hub], starts the container, and instantiates the WebDriver object to use it. The following test shows a simple example using Chrome in Docker. This example also enables the recording of the browser session and remote access using [noVNC]:

```java
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

class DockerChromeVncTest {

    WebDriver driver;

    WebDriverManager wdm = WebDriverManager.chromedriver().browserInDocker()
            .enableVnc().enableRecording();

    @BeforeEach
    void setupTest() {
        driver = wdm.create();
    }

    @AfterEach
    void teardown() {
        wdm.quit();
    }

    @Test
    void test() {
        // Your test logic here
    }

}
```

## Support
WebDriverManager is part of [OpenCollective], an online funding platform for open and transparent communities. You can support the project by contributing as a backer (i.e., a personal [donation] or [recurring contribution]) or as a [sponsor] (i.e., a recurring contribution by a company).

### Backers
<a href="https://opencollective.com/webdrivermanager" target="_blank"><img src="https://opencollective.com/webdrivermanager/backers.svg?width=890"></a>

### Sponsors
<a href="https://opencollective.com/webdrivermanager" target="_blank"><img src="https://opencollective.com/webdrivermanager/sponsor.svg?width=890"></a>

## About
WebDriverManager (Copyright &copy; 2015-2022) is a project created and maintained by [Boni Garcia] and licensed under the terms of the [Apache 2.0 License].

[Logo]: https://bonigarcia.github.io/img/wdm.png
[WebDriverManager]: https://bonigarcia.dev/webdrivermanager/
[Selenium WebDriver]: https://www.selenium.dev/documentation/webdriver/
[JUnit 5]: https://junit.org/junit5/
[Docker]: https://www.docker.com/
[Docker Engine]: https://docs.docker.com/engine/
[Docker Hub]: https://hub.docker.com/
[noVNC]: https://novnc.com/
[OpenCollective]: https://opencollective.com/webdrivermanager
[donation]: https://opencollective.com/webdrivermanager/donate
[recurring contribution]: https://opencollective.com/webdrivermanager/contribute/backer-8132/checkout
[sponsor]: https://opencollective.com/webdrivermanager/contribute/sponsor-8133/checkout
[Boni Garcia]: https://bonigarcia.dev/
[Apache 2.0 License]: https://www.apache.org/licenses/LICENSE-2.0
