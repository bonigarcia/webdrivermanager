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

# WebDriverManager [![][Logo]][GitHub Repository]

WebDriverManager allows to automate the management of the binary drivers (e.g. *chromedriver*, *geckodriver*, etc.) required by [Selenium WebDriver].

## Table of contents

1. [Motivation](#motivation)
2. [WebDriverManager as Java dependency](#webdrivermanager-as-java-dependency)
   1. [Basic usage](#basic-usage)
   2. [Examples](#examples)
   3. [Driver versions](#driver-versions)
   4. [WebDriverManager API](#webdrivermanager-api)
   5. [Configuration](#configuration)
3. [WebDriverManager CLI](#webdrivermanager-cli)
4. [WebDriverManager server](#webdrivermanager-server)
5. [Known issues](#known-issues)
6. [Help](#help)
7. [Backers](#backers)
8. [Sponsors](#sponsors)
9. [About](#about)

## Motivation

If you use [Selenium WebDriver], you will know that in order to use some browsers such as **Chrome**, **Firefox**, **Opera**, **PhantomJS**, **Microsoft Edge**, or **Internet Explorer**, first you need to download a binary file which allows WebDriver to handle browsers. In Java, the path to this binary must be set as JVM properties, as follows:

```java
System.setProperty("webdriver.chrome.driver", "/path/to/binary/chromedriver");
System.setProperty("webdriver.gecko.driver", "/path/to/binary/geckodriver");
System.setProperty("webdriver.opera.driver", "/path/to/binary/operadriver");
System.setProperty("phantomjs.binary.path", "/path/to/binary/phantomjs");
System.setProperty("webdriver.edge.driver", "C:/path/to/binary/MicrosoftWebDriver.exe");
System.setProperty("webdriver.ie.driver", "C:/path/to/binary/IEDriverServer.exe");
```

This is quite annoying since it forces you to link directly this binary file into your source code. In addition, you have to check manually when new versions of the binaries are released. WebDriverManager comes to the rescue, performing in an automated way all this dirty job for you. WebDriverManager can be used in 3 different ways:

1. [WebDriverManager as Java dependency](#webdrivermanager-as-java-dependency) (typically from test cases).
2. [WebDriverManager as a Command Line Interface (CLI) tool](#webdrivermanager-cli) (from the the shell).
3. [WebDriverManager as Server](#webdrivermanager-server) (using HTTP requests).

WebDriverManager is open source, released under the terms of [Apache 2.0 License].

## WebDriverManager as Java dependency

### Basic usage

In order to use WebDriverManager from tests in a Maven project, you need to add the following dependency in your `pom.xml` (Java 8 or upper required):

```xml
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>3.3.0</version>
    <scope>test</scope>
</dependency>
```

... or in Gradle project:

```
dependencies {
    testCompile("io.github.bonigarcia:webdrivermanager:3.3.0")
}
```

Once we have included this dependency, you can let WebDriverManager to manage the WebDriver binaries for you. Take a look at this JUnit 4 example which uses Chrome with Selenium WebDriver (in order to use WebDriverManager in conjunction with **JUnit 5**, the extension [Selenium-Jupiter] is highly recommended):

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
2. It checks the version of the driver (e.g. *chromedriver*, *geckodriver*). If unknown, it uses the latest version of the driver.
3. It downloads the WebDriver binary if it is not present on the WebDriverManager cache (``~/.m2/repository/webdriver`` by default).
4. It exports the proper WebDriver Java environment variables required by Selenium (not done when using WebDriverManager from the CLI or as a Server).

WebDriverManager resolves the driver binaries for the browsers **Chrome**, **Firefox**, **Opera**, **PhantomJS**, **Microsoft Edge**, and **Internet Explorer**. For that, it provides several *drivers managers* for these browsers. These *drivers managers* can be used as follows:

```java
WebDriverManager.chromedriver().setup();
WebDriverManager.firefoxdriver().setup();
WebDriverManager.operadriver().setup();
WebDriverManager.phantomjs().setup();
WebDriverManager.edgedriver().setup();
WebDriverManager.iedriver().setup();
```

**NOTE**: The old WebDriverManager API (version 1.x) has been deprecated as of version 3.x (`ChromeDriverManager.getInstance().setup();`, `FirefoxDriverManager.getInstance().setup();`, and so on).

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

### Examples

Check out the repository [WebDriverManager Examples] which contains different JUnit 4 test examples using WebDriverManager.


### Driver versions

The relationship between browser version and driver version is managed in a internal database by WebDriverManager stored as Java properties in the file [versions.properties]. The local version of this file (distributed within WebDriverManager) is used to find out the proper driver version. The online version of [versions.properties] (master branch) is used when the relationship between browser and driver is not present in local. 

In order to resolve the driver version for a given browser, first WebDriverManager try to find out the version of that browser. This mechanism depends on the browser (Chrome, Firefox, etc) and the platform (Linux, Windows, Mac). For instance, for Chrome in Linux, the command ``google-chrome --version`` is executed in the shell.  

Moreover in order to improve the performance of WebDriverManager, as of version 3.1.0, resolved driver versions for browsers are stored persistently as Java preferences. The validity of this relationship (browser version and driver version) stored as preferences is linked to a *time-to-live* (ttl). By default, this value is 3600 seconds.


### WebDriverManager API

WebDriverManager exposes its API by means of the **builder pattern**. This means that given a *WebDriverManger* instance, their capabilities can be tuned using different methods. The following table summarizes the WebDriverManager API, together with the equivalent configuration key:


| Method                               | Description                                                                                                                                                                                                                                                                                                                                                  | Equivalent configuration key                                                                                                                                                          |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ``version(String)``                  | By default, WebDriverManager tries to download the latest version of a given driver binary. A concrete version can be specified using this method.                                                                                                                                                                                                           | ``wdm.chromeDriverVersion``, ``wdm.operaDriverVersion``, ``wdm.internetExplorerDriverVersion``, ``wdm.edgeDriverVersion``, ``wdm.phantomjsDriverVersion``, ``wdm.geckoDriverVersion`` |
| ``targetPath(String)``               | Folder in which WebDriver binaries are stored (WedDriverManager *cache*).                                                                                                                                                                                                                                                                                    | ``wdm.targetPath``                                                                                                                                                                    |
| ``forceCache()``                     | By default, WebDriverManager connects to the specific driver repository URL to find out what is the latest version of the binary. This can be avoided forcing to use the latest version form the local repository.                                                                                                                                           | ``wdm.forceCache=true``                                                                                                                                                               |
| ``forceDownload()``                  | By default, WebDriverManager finds out the latest version of the binary, and then it uses the cached version if exists. This option forces to download again the binary even if it has been previously cached.                                                                                                                                               | ``wdm.override=true``                                                                                                                                                                 |
| ``useBetaVersions()``                | By default, WebDriverManager skip beta versions. With this method, WebDriverManager will download also beta versions.                                                                                                                                                                                                                                        | ``wdm.useBetaVersions=true``                                                                                                                                                          |
| ``architecture(Architecture)``       | By default, WebDriverManager would try to use the proper binary for the platform running the test case (i.e. 32-bit or 64-bit). This behavior can be changed by forcing a given architecture: 32-bits (``Architecture.x32``) or 64-bits (``Architecture.x64``);                                                                                              | ``wdm.architecture``                                                                                                                                                                  |
| ``arch32()``                         | Force to use the 32-bit version of a given driver binary.                                                                                                                                                                                                                                                                                                    | ``wdm.architecture=32``                                                                                                                                                               |
| ``arch64()``                         | Force to use the 64-bit version of a given driver binary.                                                                                                                                                                                                                                                                                                    | ``wdm.architecture=64``                                                                                                                                                               |
| ``operatingSystem(OperatingSystem)`` | By default, WebDriverManager downloads the binary for the same operating systems than the machine running the test. This can be changed using this method (accepted values: ``WIN``, ``LINUX``, ``MAC``).                                                                                                                                                    | ``wdm.os=WIN``, ``wdm.os=LINUX``, ``wdm.os=MAC``                                                                                                                                      |
| ``driverRepositoryUrl(URL)``         | This method allows to change the repository URL in which the binaries are hosted (see next section for default values).                                                                                                                                                                                                                                      | ``wdm.chromeDriverUrl``, ``wdm.operaDriverUrl``, ``wdm.internetExplorerDriverUrl``, ``wdm.edgeDriverUrl``, ``wdm.phantomjsDriverUrl``, ``wdm.geckoDriverUrl``                         |
| ``useMirror()``                      | The [npm.taobao.org] site is a mirror which hosts different software assets. Among them, it hosts *chromedriver*, *geckodriver*,  *operadriver*, and *phantomjs* driver. Therefore, this method can be used for Chrome, Firefox, Opera, and PhantomJS to force to use the taobao.org mirror.                                                                 | ``wdm.useMirror=true``                                                                                                                                                                |
| ``proxy(String)``                    | Use a HTTP proxy for the Internet connection using the following notation: ``my.http.proxy:1234`` or ``username:password@my.http.proxy:1234``. This can be also configured using the environment variable environment variable ``HTTPS_PROXY``.                                                                                                              | ``wdm.proxy``                                                                                                                                                                         |
| ``proxyUser(String)``                | Specify a username for HTTP proxy. This can be also configured using the environment variable environment variable ``HTTPS_PROXY_USER``.                                                                                                                                                                                                                     | ``wdm.proxyUser``                                                                                                                                                                     |
| ``proxyPass(String)``                | Specify a password for HTTP proxy. This can be also configured using the environment variable environment variable ``HTTPS_PROXY_PASS``.                                                                                                                                                                                                                     | ``wdm.proxyPass``                                                                                                                                                                     |
| ``ignoreVersions(String...)``        | Ignore some versions to be downloaded.                                                                                                                                                                                                                                                                                                                       | ``wdm.ignoreVersions``                                                                                                                                                                |
| ``gitHubTokenName(String)``          | Token name for authenticated requests (see [Known issues](#known-issues)).                                                                                                                                                                                                                                                                                   | ``wdm.gitHubTokenName``                                                                                                                                                               |
| ``gitHubTokenSecret(String)``        | Secret for authenticated requests (see [Known issues](#known-issues)).                                                                                                                                                                                                                                                                                       | ``wdm.gitHubTokenSecret``                                                                                                                                                             |
| ``timeout(int)``                     | Timeout (in seconds) to connect and download binaries from online repositories                                                                                                                                                                                                                                                                               | ``wdm.timeout``                                                                                                                                                                       |
| ``properties(String)``               | Properties file for configuration values (by default ``webdrivermanager.properties``).                                                                                                                                                                                                                                                                       | ``wdm.properties``                                                                                                                                                                    |
| ``avoidExport()``                    | Avoid exporting JVM properties with the path of binaries (i.e. ``webdriver.chrome.driver``, ``webdriver.gecko.driver``, etc). Only recommended for interactive mode.                                                                                                                                                                                         | ``wdm.avoidExport``                                                                                                                                                                   |
| ``avoidOutputTree()``                | Avoid create tree structure for downloaded binaries (e.g. ``webdriver/chromedriver/linux64/2.37/`` for ``chromedriver``). Used by default in interactive mode.                                                                                                                                                                                               | ``wdm.avoidOutputTree``                                                                                                                                                               |
| ``avoidAutoVersion()``               | Avoid checking the version of the installed browser (e.g. Chrome, Firefox) to find out the proper version of the required driver (e.g. *chromedriver*, *geckodriver*). Only recommended for WebDriverManager as Java dependency mode.                                                                                                                        | ``wdm.avoidAutoVersion``                                                                                                                                                              |
| ``browserPath()``                    | As of WebDriverManager 3.0.0, versions of drivers are contained in an internal database (versions.properties) wich matches the driver version to each browser version. In order to find the browser version, a command is executed on the shell (e.g. ``google-chrome --version`` in Linux). The path of the browser can be configured using this method     | ``wdm.binaryPath``                                                                                                                                                                    |
| ``ttl()``                            | As of WebDriverManager 3.1.0, resolved versions of drivers are stored as Java preferences. These values has a expiration time based on this Time To Live (TTL) value, measured in **seconds**. By default this value is 3600 (i.e. 1 hour).                                                                                                                  | ``wdm.ttl``                                                                                                                                                                           |
| ``setVersionsPropertiesUrl(URL)``    | URL of the online ``version.properties`` file, used if the relationship between browser and driver is unknown in the local version of that file. By default this value targets to the master branch of GitHub.                                                                                                                                               | ``wdm.versionsPropertiesUrl``                                                                                                                                                         |

The following table contains some examples:

| Example                                                           | Description                                                       |
|-------------------------------------------------------------------|-------------------------------------------------------------------|
| ``WebDriverManager.chromedriver().version("2.26").setup();``      | Force to use version 2.26 of *chromedriver*                       |
| ``WebDriverManager.firefoxdriver().arch32().setup();``            | Force to use the 32-bit version of *geckodriver*                  |
| ``WebDriverManager.operadriver().forceCache().setup();``          | Force to use the cache version of *operadriver*                   |
| ``WebDriverManager.phantomjs().useMirror().setup();``             | Force to use the taobao.org mirror to download *phantomjs* driver |
| ``WebDriverManager.chromedriver().proxy("server:port").setup();`` | Using proxy *server:port* for the connection                      |

Additional methods are exposed by WebDriverManager, namely:

* ``getVersions()``: This method allows to find out the list of of available binary versions for a given browser.
* ``getBinaryPath()``: This method allows to find out the path of the latest resolved binary.
* ``getDownloadedVersion()``: This method allows to find out the version of the latest resolved binary.
* ``clearPreferences()``: This methods allows to remove all Java preferences stored previously by WebDriverManager.
* ``clearCache()``: This methods allows to remove all binaries previously downloaded by WebDriverManager.


### Configuration

Configuration parameters for WebDriverManager are set in the ``webdrivermanager.properties`` file:

```properties
wdm.targetPath=~/.m2/repository/webdriver
wdm.forceCache=false
wdm.override=false
wdm.useMirror=false
wdm.useBetaVersions=false
wdm.avoidExport=false
wdm.avoidOutputTree=false
wdm.avoidAutoVersion=false
wdm.avoidAutoReset=false
wdm.avoidPreferences=false
wdm.timeout=30
wdm.serverPort=4041
wdm.ttl=3600

wdm.chromeDriverUrl=https://chromedriver.storage.googleapis.com/
wdm.chromeDriverMirrorUrl=http://npm.taobao.org/mirrors/chromedriver
wdm.chromeDriverExport=webdriver.chrome.driver

wdm.geckoDriverUrl=https://api.github.com/repos/mozilla/geckodriver/releases
wdm.geckoDriverMirrorUrl=http://npm.taobao.org/mirrors/geckodriver
wdm.geckoDriverExport=webdriver.gecko.driver

wdm.operaDriverUrl=https://api.github.com/repos/operasoftware/operachromiumdriver/releases
wdm.operaDriverMirrorUrl=http://npm.taobao.org/mirrors/operadriver
wdm.operaDriverExport=webdriver.opera.driver

wdm.phantomjsDriverUrl=https://bitbucket.org/ariya/phantomjs/downloads/
wdm.phantomjsDriverMirrorUrl=http://npm.taobao.org/mirrors/phantomjs
wdm.phantomjsDriverExport=phantomjs.binary.path

wdm.edgeDriverUrl=https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
wdm.edgeDriverExport=webdriver.edge.driver

wdm.internetExplorerDriverUrl=https://selenium-release.storage.googleapis.com/
wdm.internetExplorerDriverExport=webdriver.ie.driver

wdm.seleniumServerStandaloneUrl=https://selenium-release.storage.googleapis.com/

wdm.versionsPropertiesUrl=https://raw.githubusercontent.com/bonigarcia/webdrivermanager/master/src/main/resources/versions.properties
```

For instance, the variable ``wdm.targetPath`` is the default folder in which WebDriver binaries are going to be stored. By default the path of the Maven local repository is used. This property can be overwritten by Java system properties, for example:

```java
System.setProperty("wdm.targetPath", "/my/custom/path/to/driver/binaries");
```

... or by command line, for example:

```properties
-Dwdm.override=true
```

By default, WebDriverManager downloads the latest version of the WebDriver binary. But concrete versions of WebDriver binaries can be forced by changing the value of the variables ``wdm.chromeDriverVersion``, ``wdm.operaDriverVersion``,  ``wdm.internetExplorerDriverVersion``, or  ``wdm.edgeDriverVersion`` to a concrete version. For instance:

```properties
-Dwdm.chromeDriverVersion=2.25
-Dwdm.internetExplorerDriverVersion=2.46
-Dwdm.operaDriverVersion=0.2.0
-Dwdm.edgeDriverVersion=3.14366
-Dwdm.phantomjsDriverVersion=2.1.1
-Dwdm.geckoDriverVersion=0.11.1
```

If no version is specified, WebDriverManager sends a request to the server hosting the binary. In order to avoid this request and check if any binary has been previously downloaded, the key `wdm.forceCache` can be used.

The value of these properties can be overridden by means of *environmental variables*. The name of these variables result from putting the name in uppercase and replacing the symbol `.` by `_`. For example, the property ``wdm.targetPath`` can be overridden by the environment variable ``WDM_TARGETPATH``.

Moreover, configuration values can be customized from Java code. The configuration object is unique per driver manager (i.e. ``WebDriverManager.chromedriver()``,  ``WebDriverManager.firefoxdriver()``, etc.) and is accessed using the method ``config()``. The configuration parameters can be reset to default values using the method ``reset()``. For example:

```java
WebDriverManager.chromedriver().config().setProperties("/path/to/my-wdm.properties");
WebDriverManager.firefoxdriver().config().setForceCache(true);
WebDriverManager.operadriver().config().setOverride(true);
WebDriverManager.chromedriver().config().reset();
```

Finally, if a single configuration object needs to be specified for all the driver managers, it can be done using the static method ``globalConfig()``. This global configuration is reset as usual using the ``reset()`` method. For example:

```java
WebDriverManager.globalConfig().setTargetPath("/path/to/my-wdm-cache");
WebDriverManager.globalConfig().reset();
```

## WebDriverManager CLI

As of version 2.2.0, WebDriverManager can used interactively from the Command Line Interface (CLI), i.e. the shell, to resolve and download binaries for the supported browsers. There are two ways of using this feature:

* Directly from the source code, using Maven. The command to be used is ``mvn exec:java -Dexec.args="browserName"``. For instance:

```
> mvn exec:java -Dexec.args="chrome"
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building WebDriverManager 3.3.0
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ webdrivermanager ---
[INFO] Using WebDriverManager to resolve chrome
[INFO] Reading https://chromedriver.storage.googleapis.com/ to seek chromedriver
[INFO] Latest version of chromedriver is 2.37
[INFO] Downloading https://chromedriver.storage.googleapis.com/2.37/chromedriver_win32.zip to folder D:\projects\webdrivermanager
[INFO] Binary driver after extraction D:\projects\webdrivermanager\chromedriver.exe
[INFO] Resulting binary D:\projects\webdrivermanager\chromedriver.exe
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 7.306 s
[INFO] Finished at: 2018-03-23T09:53:58+01:00
[INFO] Final Memory: 17M/247M
[INFO] ------------------------------------------------------------------------
```

* Using WebDriverManager as a *fat-jar* (i.e. WebDriverManager with all its dependencies in a single executable JAR file). This JAR file can downloaded from [here](https://github.com/bonigarcia/webdrivermanager/releases/download/webdrivermanager-3.3.0/webdrivermanager-3.3.0-fat.jar) and also it can be created using the command ``mvn compile assembly:single`` from the source code. Once you get the *fat-jar*, you simply need to use the command ``java -jar webdrivermanager-3.3.0-fat.jar browserName``, for instance:

```
> java -jar webdrivermanager-3.3.0-fat.jar chrome
[INFO] Using WebDriverManager to resolve chrome
[INFO] Reading https://chromedriver.storage.googleapis.com/ to seek chromedriver
[INFO] Latest version of chromedriver is 2.37
[INFO] Downloading https://chromedriver.storage.googleapis.com/2.37/chromedriver_win32.zip to folder D:\projects\webdrivermanager
[INFO] Resulting binary D:\projects\webdrivermanager\target\chromedriver.exe
```

## WebDriverManager server

As of version 3.0.0, WebDriverManager can used as a server. To start this mode, the shell is used. Once again, two options are allowed:

* Directly from the source code and Maven. The command to be used is ``mvn exec:java -Dexec.args="server <port>"``. If the second argument is not specified, the default port will be used (4041):

```
$ mvn exec:java -Dexec.args="server"
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building WebDriverManager 3.3.0
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ webdrivermanager ---
[INFO] WebDriverManager server listening on port 4041
```

* Using WebDriverManager as a [fat-jar](https://github.com/bonigarcia/webdrivermanager/releases/download/webdrivermanager-3.3.0/webdrivermanager-3.3.0-fat.jar). For instance:

```
> java -jar webdrivermanager-3.3.0-fat.jar server
[INFO] WebDriverManager server listening on port 4041
```

When the WebDriverManager is up and running, HTTP request can be done to resolve driver binaries (*chromedriver*, *geckodriver*, etc.). For instance, supposing that WebDriverManager is running the local host and in the default port:

* http://localhost:4041/chromedriver : To download the latest version of *chromedriver*
* http://localhost:4041/firefoxdriver : To download the latest version of *geckodriver*
* http://localhost:4041/operadriver : To download the latest version of *operadriver*
* http://localhost:4041/phantomjs : To download the latest version of *phantomjs* driver
* http://localhost:4041/edgedriver : To download the latest version of *MicrosoftWebDriver*
* http://localhost:4041/iedriver : To download the latest version of *IEDriverServer*

These requests can be done using a normal browser. The driver binary is automatically downloaded by the browser since it is sent as an attachment in the HTTP response.

In addition, configuration parameters can be specified in the URL using query arguments. The name of these arguments are identical to the parameters in the ``webdrivermanager.properties`` file but skipping the prefix ``wdm.`` (see [configuration](#webdrivermanager-api) section). For instance: 

| Example                                                           | Description                                                       |
|-------------------------------------------------------------------|-------------------------------------------------------------------|
| http://localhost:4041/chromedriver?chromeDriverVersion=2.40       | Downloads the version 2.40 of *chromedriver*                      |
| http://localhost:4041/firefoxdriver?geckoDriverVersion=0.21.0     | Downloads the version 0.21.0 of *geckodriver*                     |
| http://localhost:4041/operadriver?os=WIN&forceCache=true          | Force to use the cache version of *operadriver* for Windows       |

Finally, requests to WebDriverManager server can be done interactively using tools such as [curl], as follows:

```
curl -O -J http://localhost:4041/chromedriver
curl -O -J "http://localhost:4041/chromedriver?chromeDriverVersion=2.40&forceCache=true"

```

## Known issues

### HTTP response code 403

Some of the binaries (for Opera and Firefox) are hosted on GitHub. When several consecutive requests are made by WebDriverManager, GitHub servers return an **HTTP 403 error** response as follows:

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

In order to avoid this problem, [authenticated requests] should be done. The procedure is the following:

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

### Tons of org.apache.http DEBUG log

WebDriverManager uses [Apache HTTP Client] to download WebDriver binaries from online repositories. Internally, Apache HTTP client writes a lot of logging information using the `DEBUG` level of `org.apache.http` classes. To reduce this amount of logs, the level of this logger might be reduced. For instance, in the case of [Logback], the log configuration file should include the following:

```xml
<configuration>
    <logger name="org.apache.http" level="WARN" />
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

## About

WebDriverManager (Copyright &copy; 2015-2019) is a project created by [Boni Garcia] and licensed under the terms of the [Apache 2.0 License]. Comments, questions and suggestions are always very [welcome][WebDriverManager issues]!

[Apache HTTP Client]: https://hc.apache.org/httpcomponents-client-ga/
[Apache HTTP Client logging practices]: https://hc.apache.org/httpcomponents-client-ga/logging.html
[Apache 2.0 License]: http://www.apache.org/licenses/LICENSE-2.0
[authenticated requests]: https://developer.github.com/v3/#rate-limiting
[Boni Garcia]: http://bonigarcia.github.io/
[curl]: https://curl.haxx.se/
[GitHub account]: https://github.com/settings/tokens
[GitHub Repository]: https://github.com/bonigarcia/webdrivermanager
[Logback]: https://logback.qos.ch/
[Logo]: http://bonigarcia.github.io/img/webdrivermanager.png
[npm.taobao.org]: http://npm.taobao.org/mirrors/
[Selenium WebDriver]: http://docs.seleniumhq.org/projects/webdriver/
[Selenium-Jupiter]: https://github.com/bonigarcia/selenium-jupiter/
[Stack Overflow]: https://stackoverflow.com/questions/tagged/webdrivermanager-java
[versions.properties]: https://github.com/bonigarcia/webdrivermanager/blob/master/src/main/resources/versions.properties
[WebDriverManager Examples]: https://github.com/bonigarcia/webdrivermanager-examples
[WebDriverManager issues]: https://github.com/bonigarcia/webdrivermanager/issues
