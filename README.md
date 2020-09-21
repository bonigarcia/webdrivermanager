[![Maven Central](https://img.shields.io/maven-central/v/io.github.bonigarcia/webdrivermanager.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aio.github.bonigarcia%20a%3Awebdrivermanager)
[![Build Status](https://travis-ci.org/bonigarcia/webdrivermanager.svg?branch=master)](https://travis-ci.org/bonigarcia/webdrivermanager)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=io.github.bonigarcia:webdrivermanager&metric=alert_status)](https://sonarcloud.io/dashboard/index/io.github.bonigarcia:webdrivermanager)
[![codecov](https://codecov.io/gh/bonigarcia/webdrivermanager/branch/master/graph/badge.svg)](https://codecov.io/gh/bonigarcia/webdrivermanager)
[![badge-jdk](https://img.shields.io/badge/jdk-8-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![License badge](https://img.shields.io/badge/license-Apache2-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Backers on Open Collective](https://opencollective.com/webdrivermanager/backers/badge.svg)](#backers)
[![Sponsors on Open Collective](https://opencollective.com/webdrivermanager/sponsors/badge.svg)](#sponsors)
[![Support badge](https://img.shields.io/badge/stackoverflow-webdrivermanager_java-green.svg)](http://stackoverflow.com/questions/tagged/webdrivermanager-java)
[![Twitter Follow](https://img.shields.io/twitter/follow/boni_gg.svg?style=social)](https://twitter.com/boni_gg)

# [![][Logo]][GitHub Repository]

WebDriverManager is a library which allows to automate the management of the drivers (e.g. *chromedriver*, *geckodriver*, etc.) required by [Selenium WebDriver].

## Table of contents

1. [Motivation](#motivation)
2. [WebDriverManager as Java dependency](#webdrivermanager-as-java-dependency)
   1. [Basic usage](#basic-usage)
   2. [Examples](#examples)
   3. [Resolution cache](#resolution-cache)
   4. [WebDriverManager API](#webdrivermanager-api)
   5. [Configuration](#configuration)
3. [WebDriverManager CLI](#webdrivermanager-cli)
4. [WebDriverManager Server](#webdrivermanager-server)
5. [WebDriverManager Agent](#webdrivermanager-agent)
6. [WebDriverManager Docker Container](#webdrivermanager-docker-container)
7. [Known issues](#known-issues)
8. [Help](#help)
9. [Backers](#backers)
10. [Sponsors](#sponsors)
11. [About](#about)

## Motivation

If you use [Selenium WebDriver], you probably know that to use some browsers such as **Chrome**, **Firefox**, **Edge**, **Opera**, **PhantomJS**, or **Internet Explorer**, first you need to download the so-called *driver*, i.e. a binary file which allows WebDriver to handle these browsers. In Java, the path to this driver should be set as JVM properties, as follows:

```java
System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");
System.setProperty("webdriver.gecko.driver", "/path/to/geckodriver");
System.setProperty("webdriver.edge.driver", "/path/to/msedgedriver.exe");
System.setProperty("webdriver.opera.driver", "/path/to/operadriver");
System.setProperty("phantomjs.binary.path", "/path/to/phantomjs");
System.setProperty("webdriver.ie.driver", "C:/path/to/IEDriverServer.exe");
```

This is quite annoying since it forces you to link directly this driver into your source code. In addition, you have to check manually when new versions of the drivers are released. WebDriverManager comes to the rescue, performing in an automated way this job for you. WebDriverManager can be used in different ways:

1. [WebDriverManager as Java dependency](#webdrivermanager-as-java-dependency) (typically from test cases).
2. [WebDriverManager as Command Line Interface (CLI) tool](#webdrivermanager-cli) (from the shell).
3. [WebDriverManager as Server](#webdrivermanager-server) (using a REST-like API).
4. [WebDriverManager as Agent](#webdrivermanager-agent) (using Java instrumentation).
5. [WebDriverManager as Container](#webdrivermanager-docker-container) (using Docker).

WebDriverManager is open-source, released under the terms of [Apache 2.0 License].

## WebDriverManager as Java dependency

### Basic usage

To use WebDriverManager from tests in a Maven project, you need to add the following dependency in your `pom.xml` (Java 8 or upper required), typically using the `test` scope:

```xml
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>4.2.1</version>
    <scope>test</scope>
</dependency>
```

... or in Gradle project:

```
dependencies {
    testCompile("io.github.bonigarcia:webdrivermanager:4.2.1")
}
```

Once we have included this dependency, you can let WebDriverManager to do the driver management for you. Take a look at this JUnit 4 example which uses Chrome with Selenium WebDriver (to use WebDriverManager in conjunction with **JUnit 5**, the extension [Selenium-Jupiter] is highly recommended):

```java
public class ChromeTest {

    private WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void setupTest() {
        driver = new ChromeDriver();
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test() {
        // Your test code here
    }

}
```

Notice that simply adding ``WebDriverManager.chromedriver().setup();`` WebDriverManager does magic for you:

1. It checks the version of the browser installed in your machine (e.g. Chrome, Firefox).
2. It matches the version of the driver (e.g. *chromedriver*, *geckodriver*). If unknown, it uses the latest version of the driver.
3. It downloads the driver if it is not present on the WebDriverManager cache (``~/.cache/selenium`` by default).
4. It exports the proper WebDriver Java environment variables required by Selenium (not done when using WebDriverManager from the CLI or as a Server).

WebDriverManager resolves the drivers for the browsers **Chrome**, **Firefox**, **Edge**, **Opera**, **PhantomJS**, **Internet Explorer**, and **Chromium**. For that, it provides several *drivers managers* for these browsers. These *drivers managers* can be used as follows:

```java
WebDriverManager.chromedriver().setup();
WebDriverManager.firefoxdriver().setup();
WebDriverManager.edgedriver().setup();
WebDriverManager.operadriver().setup();
WebDriverManager.phantomjs().setup();
WebDriverManager.iedriver().setup();
WebDriverManager.chromiumdriver().setup();
```

Moreover, WebDriverManager provides a generic *driver manager*. This manager which can be parameterized using Selenium driver classes (e.g. `org.openqa.selenium.chrome.ChromeDriver`, `org.openqa.selenium.firefox.FirefoxDriver`, etc), as follows: 

```java
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

// ...

Class<? extends WebDriver> driverClass = ChromeDriver.class;
WebDriverManager.getInstance(driverClass).setup();
WebDriver driver = driverClass.newInstance();
```

This generic *driver manager* can be also parameterized using the enumeration `DriverManagerType`. For instance as follows:

```java
import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

// ...

WebDriverManager.getInstance(CHROME).setup();
WebDriver driver = new ChromeDriver();
```

You can also use the `DriverManagerType` and get the complete driver class name. It might help you to create a browser instance without explicitly define the browser class.

```java
import static io.github.bonigarcia.wdm.DriverManagerType.CHROME;

import org.openqa.selenium.WebDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

// ...

DriverManagerType chrome = DriverManagerType.CHROME;
WebDriverManager.getInstance(chrome).setup();
Class<?> chromeClass =  Class.forName(chrome.browserClass());
driver = (WebDriver) chromeClass.newInstance();
```


### Examples

Check out the repository [WebDriverManager Examples] which contains different JUnit 4 test examples using WebDriverManager.


### Resolution cache

The relationship between browser version and driver version is managed in a internal database called **resolution cache**. As of WebDriverManager 4.0.0, this database is stored in a Java properties file called ``resolution.properties`` located by default in the cache folder (``~/.cache/selenium``).  The validity of this relationship (browser version and driver version) is limited by a *time-to-live* (ttl) value. There are two kinds of TTLs. First, a TTL for driver versions, with a default value of 86400 seconds (i.e. 1 day). Second, a TTL for browser versions, with a default value of 3600 seconds (i.e. 1 hour).

To resolve the driver version for a given browser, first WebDriverManager try to find out the version of that browser. This mechanism depends on the browser (Chrome, Firefox, etc) and the platform (Linux, Windows, Mac). For instance, for Chrome in Linux, the command ``google-chrome --version`` is executed in the shell.

Then, WebDriverManager tries to determine which is the proper driver version for the detected browser version. To that aim, first the values of ``LATEST_RELEASE`` labels within the driver repositories are read. Besides, WebDriverManager maintains another properties file called [versions.properties] which maps the proper driver and browsers versions. To use always the latest relationships between browser and driver, the online version of [versions.properties] (master branch) is used. As alternative, the local version of this file (distributed within WebDriverManager) is used.


### WebDriverManager API

WebDriverManager exposes its API by means of the **builder pattern**. This means that given a *WebDriverManger* instance, their capabilities can be tuned using different methods. The following table summarizes the WebDriverManager API, together with the equivalent configuration key:

| Method                                | Description                                                                                                                                                                                                                                                                                                                                                  | Equivalent configuration key                                                                                                                                                                                         |
|---------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ``driverVersion(String)``             | By default, WebDriverManager tries to download the proper version of a given driver. A concrete version can be specified using this method.                                                                                                                                                                                                                  | ``wdm.chromeDriverVersion``, ``wdm.operaDriverVersion``, ``wdm.internetExplorerDriverVersion``, ``wdm.edgeDriverVersion``, ``wdm.phantomjsDriverVersion``, ``wdm.geckoDriverVersion``, ``wdm.chromiumDriverVersion`` |
| ``browserVersion(String)``            | Alternatively to the driver version, the major version of the browser can be specified using this method.                                                                                                                                                                                                                                                    | ``wdm.chromeVersion``, ``wdm.operaVersion``,  ``wdm.edgeVersion``, ``wdm.firefoxVersion``, ``wdm.chromiumVersion``                                                                                                   |
| ``cachePath(String)``                 | Folder in which drivers are stored (WedDriverManager *cache*).                                                                                                                                                                                                                                                                                               | ``wdm.cachePath``                                                                                                                                                                                                    |
| ``resolutionCachePath(String)``       | Folder in which the resolution cache file is stored (by default, in the same folder than the driver cache).                                                                                                                                                                                                                                                  | ``wdm.resolutionCachePath``                                                                                                                                                                                          |
| ``forceDownload()``                   | By default, WebDriverManager finds out the latest version of the driver, and then it uses the cached version if exists. This option forces to download again the driver even if it has been previously cached.                                                                                                                                               | ``wdm.forceDownload=true``                                                                                                                                                                                           |
| ``useBetaVersions()``                 | By default, WebDriverManager skip beta versions. With this method, WebDriverManager will download also beta versions.                                                                                                                                                                                                                                        | ``wdm.useBetaVersions=true``                                                                                                                                                                                         |
| ``architecture(Architecture)``        | By default, WebDriverManager would try to use the proper driver for the platform running the test case (i.e. 32-bit or 64-bit). This behavior can be changed by forcing a given architecture: 32-bits (``Architecture.x32``) or 64-bits (``Architecture.x64``);                                                                                              | ``wdm.architecture``                                                                                                                                                                                                 |
| ``arch32()``                          | Force to use the 32-bit version of a given driver.                                                                                                                                                                                                                                                                                                           | ``wdm.architecture=32``                                                                                                                                                                                              |
| ``arch64()``                          | Force to use the 64-bit version of a given driver.                                                                                                                                                                                                                                                                                                           | ``wdm.architecture=64``                                                                                                                                                                                              |
| ``operatingSystem(OperatingSystem)``  | By default, WebDriverManager downloads the driver for the same operating systems than the machine running the test. This can be changed using this method (accepted values: ``WIN``, ``LINUX``, ``MAC``).                                                                                                                                                    | ``wdm.os=WIN``, ``wdm.os=LINUX``, ``wdm.os=MAC``                                                                                                                                                                     |
| ``win()``                             | Force to use driver for Windows.                                                                                                                                                                                                                                                                                                                             | ``wdm.os=WIN``                                                                                                                                                                                                       |
| ``linux()``                           | Force to use driver for Linux.                                                                                                                                                                                                                                                                                                                               | ``wdm.os=LINUX``                                                                                                                                                                                                     |
| ``mac()``                             | Force to use driver for Mac OS.                                                                                                                                                                                                                                                                                                                              | ``wdm.os=MAC``                                                                                                                                                                                                       |
| ``driverRepositoryUrl(URL)``          | This method allows to change the repository URL in which the drivers are hosted (see next section for default values).                                                                                                                                                                                                                                       | ``wdm.chromeDriverUrl``, ``wdm.operaDriverUrl``, ``wdm.internetExplorerDriverUrl``, ``wdm.edgeDriverUrl``, ``wdm.phantomjsDriverUrl``, ``wdm.geckoDriverUrl``                                                        |
| ``useMirror()``                       | The [npm.taobao.org] site is a mirror which hosts different software assets. Among them, it hosts *chromedriver*, *geckodriver*,  *operadriver*, and *phantomjs* driver. Therefore, this method can be used for Chrome, Firefox, Opera, and PhantomJS to force to use the taobao.org mirror.                                                                 | ``wdm.useMirror=true``                                                                                                                                                                                               |
| ``proxy(String)``                     | Use a HTTP proxy for the Internet connection using the following notation: ``my.http.proxy:1234`` or ``username:password@my.http.proxy:1234``. This can be also configured using the environment variable environment variable ``HTTPS_PROXY``.                                                                                                              | ``wdm.proxy``                                                                                                                                                                                                        |
| ``proxyUser(String)``                 | Specify a username for HTTP proxy. This can be also configured using the environment variable environment variable ``HTTPS_PROXY_USER``.                                                                                                                                                                                                                     | ``wdm.proxyUser``                                                                                                                                                                                                    |
| ``proxyPass(String)``                 | Specify a password for HTTP proxy. This can be also configured using the environment variable environment variable ``HTTPS_PROXY_PASS``.                                                                                                                                                                                                                     | ``wdm.proxyPass``                                                                                                                                                                                                    |
| ``ignoreVersions(String...)``         | Ignore some versions to be downloaded.                                                                                                                                                                                                                                                                                                                       | ``wdm.ignoreVersions``                                                                                                                                                                                               |
| ``gitHubTokenName(String)``           | Token name for authenticated requests (see [Known issues](#known-issues)).                                                                                                                                                                                                                                                                                   | ``wdm.gitHubTokenName``                                                                                                                                                                                              |
| ``gitHubTokenSecret(String)``         | Secret for authenticated requests (see [Known issues](#known-issues)).                                                                                                                                                                                                                                                                                       | ``wdm.gitHubTokenSecret``                                                                                                                                                                                            |
| ``localRepositoryUser(String)``       | Specify a username for local repository.                                                                                                                                                                                                                                                                                                                     | ``wdm.proxyUser``                                                                                                                                                                                                    |
| ``localRepositoryPassword(String)``   | Specify a password for local repository.                                                                                                                                                                                                                                                                                                                     | ``wdm.proxyPass``                                                                                                                                                                                                    |
| ``timeout(int)``                      | Timeout (in seconds) to connect and download drivers from online repositories                                                                                                                                                                                                                                                                                | ``wdm.timeout``                                                                                                                                                                                                      |
| ``properties(String)``                | Properties file for configuration values (by default ``webdrivermanager.properties``).                                                                                                                                                                                                                                                                       | ``wdm.properties``                                                                                                                                                                                                   |
| ``avoidExport()``                     | Avoid exporting JVM properties with the path of drivers (i.e. ``webdriver.chrome.driver``, ``webdriver.gecko.driver``, etc). Only recommended for interactive mode.                                                                                                                                                                                          | ``wdm.avoidExport``                                                                                                                                                                                                  |
| ``avoidOutputTree()``                 | Avoid create tree structure for downloaded drivers (e.g. ``webdriver/chromedriver/linux64/2.37/`` for ``chromedriver``). Used by default in interactive mode.                                                                                                                                                                                                | ``wdm.avoidOutputTree``                                                                                                                                                                                              |
| ``avoidFallback()``                   | If some problem is detected while resolving a driver, a fallback mechanism is used by default (i.e. use the latest version from cache as driver). To method should be used to deactivate this fallback mechanism.                                                                                                                                            | ``wdm.avoidFallback``                                                                                                                                                                                                |
| ``avoidBrowserDetection()``           | Avoid checking the version of the installed browser (e.g. Chrome, Firefox) to find out the proper version of the required driver (e.g. *chromedriver*, *geckodriver*). Only recommended for WebDriverManager as Java dependency mode.                                                                                                                        | ``wdm.avoidBrowserDetection``                                                                                                                                                                                        |
| ``avoidReadReleaseFromRepository()``  | Avoid checking the repository (e.g. [chromedriver-latest], [msedgedriver-latest]) to find out the version of the required driver.                                                                                                                                                                                                                            | ``wdm.avoidReadReleaseFromRepository``                                                                                                                                                                               |
| ``browserPath()``                     | As of WebDriverManager 3.0.0, to find the versions of browsers, a command is executed on the shell (e.g. ``google-chrome --version`` in Linux). The full path of the browser including the binary name (e.g. ``/usr/bin/google-chrome-beta``) can be configured using this configuration key                                                                 | ``wdm.browserPath``                                                                                                                                                                                                  |
| ``ttl(int)``                          | As of WebDriverManager 3.1.0, resolved versions of drivers are stored in the resolution cache. These values has a expiration time based on this Time To Live (TTL) value, measured in **seconds**. By default this value is 86400 (i.e. 1 day).                                                                                                              | ``wdm.ttl``                                                                                                                                                                                                          |
| ``ttlBrowsers(int)``                  | As of WebDriverManager 4.1.0, resolved versions of browsers are stored in the resolution cache. These values has a expiration time based on this Time To Live (TTL) value, measured in **seconds**. By default this value is 3600 (i.e. 1 hour).                                                                                                             | ``wdm.ttlForBrowsers``                                                                                                                                                                                               |
| ``setVersionsPropertiesUrl(URL)``     | URL of the online ``version.properties`` file, used if the relationship between browser and driver is unknown in the local version of that file. By default this value targets to the master branch of GitHub.                                                                                                                                               | ``wdm.versionsPropertiesUrl``                                                                                                                                                                                        |
| ``useLocalVersionsPropertiesFirst()`` | As of WebDriverManager 3.7.1, the online ``version.properties`` file is read to check the latest relationship browser-driver. If the local ``version.properties`` want to be used instead, this method should be invoked                                                                                                                                     | ``wdm.versionsPropertiesOnlineFirst``                                                                                                                                                                                |
| ``clearResolutionCache()``            | This methods allows to remove the resolution cache (browser and driver versions previously resolved).                                                                                                                                                                                                                                                        | ``wdm.clearResolutionCache``                                                                                                                                                                                         |
| ``clearDriverCache()``                | This methods allows to remove the driver cache (driver previously resolved). By default, the resolution cache is located in the same folder that the driver, and therefore, it will be also cleared when calling this method.                                                                                                                                | ``wdm.clearDriverCache``                                                                                                                                                                                             |

The following table contains some examples:

| Example                                                                            | Description                                                                  |
|------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| ``WebDriverManager.chromedriver().driverVersion("81.0.4044.138").setup();``        | Force to use version 81.0.4044.138 of *chromedriver*                         |
| ``WebDriverManager.firefoxdriver().browserVersion("75").setup();``                 | Force to use proper version of *geckodriver* for Firefox 75                  |
| ``WebDriverManager.operadriver().proxy("server:port").setup();``                   | Using proxy *server:port* to resolve the proper version of *operadriver*     |
| ``WebDriverManager.edgedriver().operatingSystem(OperatingSystem.MAC).setup();``    | Force to download *msedgedriver* for Mac OS                                  |


Additional methods are exposed by WebDriverManager, namely:

* ``getDriverVersions()``: This method allows to find out the list of available driver versions.
* ``getDriverManagerType()``: This methods allows to get the driver manager type (as ``enum``) of a given manager.
* ``getDownloadedDriverPath()``: This method allows to find out the path of the latest resolved driver.
* ``getDownloadedDriverVersion()``: This method allows to find out the version of the latest resolved driver.
 

### Configuration

Configuration parameters for WebDriverManager are set in the ``webdrivermanager.properties`` file:

```properties
wdm.cachePath=~/.cache/selenium
wdm.forceDownload=false
wdm.useMirror=false
wdm.useBetaVersions=false
wdm.avoidExport=false
wdm.avoidOutputTree=false
wdm.avoidBrowserDetection=false
wdm.avoidAutoReset=false
wdm.avoidResolutionCache=false
wdm.avoidFallback=false
wdm.avoidReadReleaseFromRepository=false
wdm.timeout=30
wdm.serverPort=4041
wdm.resolutionCache=resolution.properties
wdm.ttl=86400
wdm.ttlForBrowsers=3600

wdm.chromeDriverUrl=https://chromedriver.storage.googleapis.com/
wdm.chromeDriverMirrorUrl=http://npm.taobao.org/mirrors/chromedriver/
wdm.chromeDriverExport=webdriver.chrome.driver
wdm.chromeDownloadUrlPattern=https://chromedriver.storage.googleapis.com/%s/chromedriver_%s%s.zip

wdm.geckoDriverUrl=https://api.github.com/repos/mozilla/geckodriver/releases
wdm.geckoDriverMirrorUrl=http://npm.taobao.org/mirrors/geckodriver
wdm.geckoDriverExport=webdriver.gecko.driver

wdm.operaDriverUrl=https://api.github.com/repos/operasoftware/operachromiumdriver/releases
wdm.operaDriverMirrorUrl=http://npm.taobao.org/mirrors/operadriver
wdm.operaDriverExport=webdriver.opera.driver

wdm.edgeDriverUrl=https://msedgedriver.azureedge.net/
wdm.edgeDriverExport=webdriver.edge.driver
wdm.edgeDownloadUrlPattern=https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/%s/edgedriver_%s%s.zip

wdm.internetExplorerDriverUrl=https://selenium-release.storage.googleapis.com/
wdm.internetExplorerDriverExport=webdriver.ie.driver

wdm.phantomjsDriverUrl=https://bitbucket.org/api/2.0/repositories/ariya/phantomjs/downloads
wdm.phantomjsDriverMirrorUrl=http://npm.taobao.org/mirrors/phantomjs
wdm.phantomjsDriverExport=phantomjs.binary.path

wdm.seleniumServerStandaloneUrl=https://selenium-release.storage.googleapis.com/

wdm.chromiumDriverSnapPath=/snap/bin/chromium.chromedriver

wdm.versionsPropertiesOnlineFirst=true
wdm.versionsPropertiesUrl=https://raw.githubusercontent.com/bonigarcia/webdrivermanager/master/src/main/resources/versions.properties
```

For instance, the variable ``wdm.cachePath`` is the default folder in which the drivers are stored. By default the path of the Maven local repository is used. This property can be overwritten by Java system properties, for example:

```java
System.setProperty("wdm.cachePath", "/my/custom/path/to/drivers");
```

... or by command line, for example:

```properties
-Dwdm.cachePath=/my/custom/path/to/drivers
```

By default, WebDriverManager tries to download the proper version of the driver for the installed browsers. Nevertheless, concrete driver versions can be forced by changing the value of the variables ``wdm.chromeDriverVersion``, ``wdm.operaDriverVersion``,  ``wdm.internetExplorerDriverVersion``, or  ``wdm.edgeDriverVersion`` to a concrete version. For instance:

```properties
-Dwdm.chromeDriverVersion=81.0.4044.138
-Dwdm.geckoDriverVersion=0.26.0
-Dwdm.edgeDriverVersion=81.0.410.0
-Dwdm.operaDriverVersion=81.0.4044.113
```

The value of these properties can be also overridden by means of *environmental variables*. The name of these variables result from putting the name in uppercase and replacing the symbol `.` by `_`. For example, the property ``wdm.cachePath`` can be overridden by the environment variable ``WDM_CACHEPATH``.

Moreover, configuration values can be customized from Java code. The configuration object is unique per driver manager (i.e. ``WebDriverManager.chromedriver()``,  ``WebDriverManager.firefoxdriver()``, etc.) and is accessed using the method ``config()``. The configuration parameters can be reset to default values using the method ``reset()``. For example:

```java
WebDriverManager.chromedriver().config().setProperties("/path/to/my-wdm.properties");
WebDriverManager.operadriver().config().setForceDownload(true);
WebDriverManager.chromedriver().config().reset();
```

Finally, if a single configuration object needs to be specified for all the driver managers, it can be done using the static method ``globalConfig()``. This global configuration is reset as usual using the ``reset()`` method. For example:

```java
WebDriverManager.globalConfig().setCachePath("/path/to/my-wdm-cache");
WebDriverManager.globalConfig().reset();
```

## WebDriverManager CLI

As of version 2.2.0, WebDriverManager can used interactively from the Command Line Interface (CLI), i.e. the shell, to resolve and download drivers for the supported browsers. There are two ways of using this feature:

* Directly from the source code, using Maven. The command to be used is ``mvn exec:java -Dexec.args="browserName"``. For instance:

```
> mvn exec:java -Dexec.args="chrome"
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building WebDriverManager 4.2.1
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ webdrivermanager ---
[INFO] Using WebDriverManager to resolve chrome
[DEBUG] Running command on the shell: [google-chrome, --version]
[DEBUG] Result: Google Chrome 81.0.4044.138
[INFO] Using chromedriver 81.0.4044.138 (resolved driver for Chrome 81)
[INFO] Reading https://chromedriver.storage.googleapis.com/ to seek chromedriver
[DEBUG] Driver to be downloaded chromedriver 81.0.4044.138
[INFO] Downloading https://chromedriver.storage.googleapis.com/81.0.4044.138/chromedriver_linux64.zip
[INFO] Extracting driver from compressed file chromedriver_linux64.zip
[INFO] Driver location: /home/boni/dev/webdrivermanager/chromedriver
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.595 s
[INFO] Finished at: 2020-05-12T16:11:13+02:00
[INFO] ------------------------------------------------------------------------
```

* Using WebDriverManager as a *fat-jar* (i.e. WebDriverManager with all its dependencies in a single executable JAR file). This JAR file can downloaded from [here](https://github.com/bonigarcia/webdrivermanager/releases/download/webdrivermanager-4.2.1/webdrivermanager-4.2.1-fat.jar) and also it can be created using the command ``mvn compile assembly:single`` from the source code. Once you get the *fat-jar*, you simply need to use the command ``java -jar webdrivermanager-4.2.0-fat.jar browserName``, for instance:

```
> java -jar webdrivermanager-4.2.1-fat.jar chrome
[INFO] Using WebDriverManager to resolve chrome
[DEBUG] Running command on the shell: [google-chrome, --version]
[DEBUG] Result: Google Chrome 81.0.4044.138
[INFO] Using chromedriver 81.0.4044.138 (resolved driver for Chrome 81)
[INFO] Reading https://chromedriver.storage.googleapis.com/ to seek chromedriver
[DEBUG] Driver to be downloaded chromedriver 81.0.4044.138
[INFO] Downloading https://chromedriver.storage.googleapis.com/81.0.4044.138/chromedriver_linux64.zip
[INFO] Extracting driver from compressed file chromedriver_linux64.zip
[INFO] Driver location: /home/boni/Downloads/chromedriver

```

## WebDriverManager Server

As of version 3.0.0, WebDriverManager can used as a server. To start this mode, the shell is used. Once again, two options are allowed:

* Directly from the source code and Maven. The command to be used is ``mvn exec:java -Dexec.args="server <port>"``. If the second argument is not specified, the default port will be used (4041):

```
$ mvn exec:java -Dexec.args="server"
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building WebDriverManager 4.2.1
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ webdrivermanager ---
[INFO] WebDriverManager server listening on port 4041
```

* Using WebDriverManager as a [fat-jar]. For instance:

```
> java -jar webdrivermanager-4.2.1-fat.jar server
[INFO] WebDriverManager server listening on port 4041
```

When the WebDriverManager is up and running, a REST-like API using HTTP request can be done to resolve drivers (*chromedriver*, *geckodriver*, etc.). For instance, supposing that WebDriverManager is running the local host and in the default port:

* http://localhost:4041/chromedriver : To download the latest version of *chromedriver*
* http://localhost:4041/firefoxdriver : To download the latest version of *geckodriver*
* http://localhost:4041/operadriver : To download the latest version of *operadriver*
* http://localhost:4041/phantomjs : To download the latest version of *phantomjs* driver
* http://localhost:4041/edgedriver : To download the latest version of *msedgedriver*
* http://localhost:4041/iedriver : To download the latest version of *IEDriverServer*

These requests use HTTP GET and can be done using a normal browser. The driver is automatically downloaded by the browser since it is sent as an attachment in the HTTP response.

In addition, configuration parameters can be specified in the URL using query arguments. The name of these arguments are identical to the parameters in the ``webdrivermanager.properties`` file but skipping the prefix ``wdm.`` (see [configuration](#webdrivermanager-api) section). For instance: 

| Example                                                                   | Description                                                       |
|---------------------------------------------------------------------------|-------------------------------------------------------------------|
| http://localhost:4041/chromedriver?chromeVersion=83                       | Downloads the proper driver (*chromedriver*) for Chrome 83        |
| http://localhost:4041/firefoxdriver?geckoDriverVersion=0.26.0             | Downloads the version 0.26.0 of *geckodriver*                     |
| http://localhost:4041/operadriver?os=WIN&forceDownload=true               | Force not to use the cache version of *operadriver* for Windows   |

Finally, requests to WebDriverManager server can be done interactively using tools such as [curl], as follows:

```
curl -O -J http://localhost:4041/chromedriver
curl -O -J "http://localhost:4041/chromedriver?chromeVersion=83"
```


## WebDriverManager Agent

As of version 4.0.0, WebDriverManager can be used as Java Agent. To configure that, we need to specify the the path of the WebDriverManager [fat-jar] using the JVM flag ``-javaagent:/path/to/webdrivermanager.jar``. Alternatively, it can be done using Maven (see a complete project example [here](https://github.com/bonigarcia/wdm-agent-example)).

Using this approach, Selenium WebDriver tests can drop the driver setup for *chromedriver*, *geckodriver*, etc. For example:


```java
public class ChromeTest {

    private WebDriver driver;


    @Before
    public void setupTest() {
        driver = new ChromeDriver();
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test() {
        // Test logic
    }

}
```

```java
public class FirefoxTest {

    private WebDriver driver;

    @Before
    public void setupTest() {
        driver = new FirefoxDriver();
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test() {
        // Test logic
    }

}
```

The WebDriverManager Agent will check the objects being created in the JVM. Just before Selenium WebDriver objects are instantiated in the tests (``org.openqa.selenium.chrome.ChromeDriver``, ``org.openqa.selenium.firefox.FirefoxDriver``, ``org.openqa.selenium.opera.OperaDriver``, ``org.openqa.selenium.edge.EdgeDriver``, ``org.openqa.selenium.phantomjs.PhantomJSDriver``, or ``org.openqa.selenium.ie.InternetExplorerDriver``), the proper WebDriverManager setup call is executed to manage the required driver (*chromedriver*, *geckodriver*, *msedgedriver*, etc).

```
$ mvn test
...
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.github.bonigarcia.wdm.agent.test.ChromeTest
2020-05-12 15:38:49 [main] DEBUG io.github.bonigarcia.wdm.WdmAgent.transform(86) -- WebDriverManager Agent is going to resolve the driver for Chrome
2020-05-12 15:38:49 [main] DEBUG i.g.b.wdm.cache.ResolutionCache.checkKeyInResolutionCache(183) -- Resolution chrome=81 in cache (valid until 15:23:29 13/05/2020 CEST)
2020-05-12 15:38:49 [main] INFO  i.g.bonigarcia.wdm.WebDriverManager.resolveDriverVersion(571) -- Using chromedriver 81.0.4044.138 (since Chrome 81 is installed in your machine)
2020-05-12 15:38:49 [main] DEBUG i.g.bonigarcia.wdm.WebDriverManager.manage(520) -- Driver chromedriver 81.0.4044.138 found in cache
2020-05-12 15:38:49 [main] INFO  i.g.bonigarcia.wdm.WebDriverManager.exportDriver(615) -- Exporting webdriver.chrome.driver as /home/boni/.cache/selenium/webdriver/chromedriver/linux64/81.0.4044.138/chromedriver
Starting ChromeDriver 81.0.4044.138 (8c6c7ba89cc9453625af54f11fd83179e23450fa-refs/branch-heads/4044@{#999}) on port 31286
2020-05-12 15:38:50 [main] DEBUG i.g.b.wdm.agent.test.ChromeTest.test(54) -- The title of https://bonigarcia.github.io/selenium-jupiter/ is Selenium-Jupiter: JUnit 5 extension for Selenium
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.814 s - in io.github.bonigarcia.wdm.agent.test.ChromeTest
[INFO] Running io.github.bonigarcia.wdm.agent.test.FirefoxTest
2020-05-12 15:46:04 [main] DEBUG io.github.bonigarcia.wdm.WdmAgent.transform(86) -- WebDriverManager Agent is going to resolve the driver for Firefox
2020-05-12 15:46:04 [main] DEBUG i.g.b.wdm.cache.ResolutionCache.checkKeyInResolutionCache(183) -- Resolution firefox=76 in cache (valid until 15:45:41 13/05/2020 CEST)
2020-05-12 15:46:04 [main] INFO  i.g.bonigarcia.wdm.WebDriverManager.resolveDriverVersion(571) -- Using geckodriver 0.26.0 (since Firefox 76 is installed in your machine)
2020-05-12 15:46:04 [main] DEBUG i.g.bonigarcia.wdm.WebDriverManager.manage(520) -- Driver geckodriver 0.26.0 found in cache
2020-05-12 15:46:04 [main] INFO  i.g.bonigarcia.wdm.WebDriverManager.exportDriver(615) -- Exporting webdriver.gecko.driver as /home/boni/.cache/selenium/webdriver/geckodriver/linux64/0.26.0/geckodriver
2020-05-12 15:46:07 [main] DEBUG i.g.b.wdm.agent.test.FirefoxTest.test(54) -- The title of https://bonigarcia.github.io/selenium-jupiter/ is Selenium-Jupiter: JUnit 5 extension for Selenium
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.163 s - in io.github.bonigarcia.wdm.agent.test.FirefoxTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  10.017 s
[INFO] Finished at: 2020-05-12T15:46:10+02:00
[INFO] ------------------------------------------------------------------------
```

## WebDriverManager Docker container

As of version 4.0.0, WebDriverManager can be used as a [Docker container] to execute the Server and CLI modes. To execute WebDriverManager Server in Docker, you simply need to run the following command:

```
docker run -p 4041:4041 bonigarcia/webdrivermanager:4.2.1
```

To execute WebDriverManager CLI in Docker, you need to specify the type of browser to be resolved as environmental variable (`BROWSER`). The rest of WebDriverManager configuration parameters can be passed to the Docker container using env variables using the usual `-e` option in Docker. For example, in Linux:

```
docker run --rm -e BROWSER=chrome -e WDM_CHROMEVERSION=84 -e WDM_OS=LINUX -v ${PWD}:/wdm bonigarcia/webdrivermanager:4.2.1
```

... or Mac:

```
docker run --rm -e BROWSER=chrome -e WDM_CHROMEVERSION=84 -e WDM_OS=MAC -v ${PWD}:/wdm bonigarcia/webdrivermanager:4.2.1
```

... or Windows:


```
docker run --rm -e BROWSER=chrome -e WDM_CHROMEVERSION=84 -e WDM_OS=WIN -v %cd%:/wdm bonigarcia/webdrivermanager:4.2.1
```

## Known issues

### HTTP response code 403

Some of the drivers (e.g. *geckodriver* or *operadriver*) are hosted on GitHub. When several consecutive requests are made by WebDriverManager, GitHub servers return an **HTTP 403 error** response as follows:

```
Caused by: java.io.IOException: Server returned HTTP response code: 403 for URL: https://api.github.com/repos/operasoftware/operachromiumdriver/releases
    at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1840)
    at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1441)
    at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:254)
    at io.github.bonigarcia.wdm.BrowserManager.openGitHubConnection(BrowserManager.java:463)
    at io.github.bonigarcia.wdm.OperaDriverManager.getDrivers(OperaDriverManager.java:55)
    at io.github.bonigarcia.wdm.BrowserManager.manage(BrowserManager.java:168)
```

```
Caused by: java.io.IOException: Server returned HTTP response code: 403 for URL: https://api.github.com/repos/mozilla/geckodriver/releases
    at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1840)
    at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1441)
    at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:254)
    at io.github.bonigarcia.wdm.FirefoxDriverManager.getDrivers(FirefoxDriverManager.java:61)
    at io.github.bonigarcia.wdm.BrowserManager.manage(BrowserManager.java:163)
```

To avoid this problem, [authenticated requests] should be done. The procedure is the following:

1. Create a token/secret pair in your [GitHub account].
2. Tell WebDriverManager the value of this pair token/secret. To do that you should use the configuration keys ``wdm.gitHubTokenName`` and ``wdm.gitHubTokenSecret``. You can pass them as command line Java parameters as follows:

```properties
-Dwdm.gitHubTokenName=<your-token-name>
-Dwdm.gitHubTokenSecret=<your-token-secret>
```
... or as environment variables (e.g. in Travis CI) as follows:

```properties
WDM_GITHUBTOKENNAME=<your-token-name>
WDM_GITHUBTOKENSECRET=<your-token-secret>
```

### TravisCI fork builds

If you have a fork and want to build using the same `travis.yml` you have to add some enviroment variables to your TravisCI instance configuration:

- ``SONARCLOUD_ORGANIZATION`` = Your sonarcloud user organization
- ``SONAR_TOKEN`` = A user token you create on your sonar account <https://docs.travis-ci.com/user/sonarcloud/>

If you want disable the SonarSource validation add a variable `TRAVIS_PULL_REQUEST` with value ``true``.

### Tons of org.apache.http DEBUG log

WebDriverManager uses [Apache HTTP Client] to download drivers from online repositories. Internally, Apache HTTP client writes a lot of logging information using the `DEBUG` level of `org.apache.http` classes. To reduce this amount of logs, the level of this logger might be reduced. For instance, in the case of [Logback], the log configuration file should include the following:

```xml
<configuration>
    <logger name="org.apache.hc" level="WARN" />
</configuration>
```

You can find further information about others logging implementations in the [Apache HTTP Client logging practices] page.


## Help

If you have questions on how to use WebDriverManager properly with a special configuration or suchlike, please consider asking a question on [Stack Overflow] and tag it with  *webdrivermanager-java*.


## Backers

Thank you to all our backers! [[Become a backer](https://opencollective.com/webdrivermanager#backer)]

<a href="https://opencollective.com/webdrivermanager#backers" target="_blank"><img src="https://opencollective.com/webdrivermanager/backers.svg?width=890"></a>


## Sponsors

Support this project by becoming a sponsor. Your logo will show up here with a link to your website. [[Become a sponsor](https://opencollective.com/webdrivermanager#sponsor)]

<a href="https://opencollective.com/webdrivermanager/sponsor/0/website" target="_blank"><img src="https://opencollective.com/webdrivermanager/sponsor/0/avatar.svg"></a>
<a href="https://opencollective.com/webdrivermanager/sponsor/1/website" target="_blank"><img src="https://opencollective.com/webdrivermanager/sponsor/1/avatar.svg"></a>


## About

WebDriverManager (Copyright &copy; 2015-2020) is a project created and maintained by [Boni Garcia] and licensed under the terms of the [Apache 2.0 License]. Comments, questions and suggestions are always very [welcome][WebDriverManager issues]!

[Apache HTTP Client]: https://hc.apache.org/httpcomponents-client-ga/
[Apache HTTP Client logging practices]: https://hc.apache.org/httpcomponents-client-ga/logging.html
[Apache 2.0 License]: http://www.apache.org/licenses/LICENSE-2.0
[authenticated requests]: https://developer.github.com/v3/#rate-limiting
[Boni Garcia]: http://bonigarcia.github.io/
[curl]: https://curl.haxx.se/
[GitHub account]: https://github.com/settings/tokens
[GitHub Repository]: https://github.com/bonigarcia/webdrivermanager
[Logback]: https://logback.qos.ch/
[Logo]: http://bonigarcia.github.io/img/wdm.png
[npm.taobao.org]: http://npm.taobao.org/mirrors/https://docs.travis-ci.com/user/sonarcloud/
[Selenium WebDriver]: http://docs.seleniumhq.org/projects/webdriver/
[Selenium-Jupiter]: https://github.com/bonigarcia/selenium-jupiter/
[Stack Overflow]: https://stackoverflow.com/questions/tagged/webdrivermanager-java
[versions.properties]: https://github.com/bonigarcia/webdrivermanager/blob/master/src/main/resources/versions.properties
[WebDriverManager Examples]: https://github.com/bonigarcia/webdrivermanager-examples
[WebDriverManager issues]: https://github.com/bonigarcia/webdrivermanager/issues
[fat-jar]: https://github.com/bonigarcia/webdrivermanager/releases/download/webdrivermanager-4.2.1/webdrivermanager-4.2.1-fat.jar
[survey]: http://tiny.cc/wdm-survey
[Docker container]: https://hub.docker.com/repository/docker/bonigarcia/webdrivermanager
[chromedriver-latest]: https://chromedriver.storage.googleapis.com/LATEST_RELEASE
[msedgedriver-latest]: https://msedgedriver.azureedge.net/LATEST_STABLE
