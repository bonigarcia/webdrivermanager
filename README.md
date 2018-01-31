We forked this project to add "get drivers from Nexus" functionality and use it with Page-Factory framework.

# WebDriverManager [![][Logo]][GitHub Repository]

This library is aimed to automate the [Selenium Webdriver] binaries management in runtime for Java.

If you are using [Selenium Webdriver], you would know that to use some browsers such as **Chrome**, **Firefox**, **Opera**, **PhantomJS**, **Microsoft Edge**, or **Internet Explorer**, you need to download a binary which allows WebDriver to handle the browser. In addition, the absolute path to this binary must be set as JVM properties, as follows:

```java
System.setProperty("webdriver.chrome.driver", "/absolute/path/to/binary/chromedriver");
System.setProperty("webdriver.gecko.driver", "/absolute/path/to/binary/geckodriver");
System.setProperty("webdriver.opera.driver", "/absolute/path/to/binary/operadriver");
System.setProperty("phantomjs.binary.path", "/absolute/path/to/binary/phantomjs");
System.setProperty("webdriver.edge.driver", "C:/absolute/path/to/binary/MicrosoftWebDriver.exe");
System.setProperty("webdriver.ie.driver", "C:/absolute/path/to/binary/IEDriverServer.exe");
```

This is quite annoying since it forces you to link directly this binary in your source code. In addition, you have to check manually when new versions of the binaries are released. WebDriverManager comes to the rescue, performing in an automated way all this dirty job for you.

WebDriverManager is open source, released under the terms of [Apache 2.0 License].

## Usage

In order to use WebDriverManager in a Maven project, first add the following dependency to your `pom.xml` (Java 7 or upper required):

```xml
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>2.0.1</version>
</dependency>
```

WebDriverManager is typically used by tests, and therefore, the typical scope would be *test* (`<scope>test</scope>`).

Once we have included this dependency, you can let WebDriverManager to manage the WebDriver binaries for you. Take a look at this JUnit 4 example which uses Chrome with Selenium WebDriver (in order to use WebDriverManager in conjunction with **JUnit 5**, the extension [selenium-jupiter] is highly recommended):

```java
public class ChromeTest {

    private WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        ChromeDriverManager.getInstance().setup();
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

Notice that simply adding ``ChromeDriverManager.getInstance().setup();`` WebDriverManager does magic for you:

1. It checks for the latest version of the WebDriver binary.
2. It downloads the WebDriver binary if it's not present on your system.
3. It exports the required WebDriver Java environment variables needed by Selenium.

So far, WebDriverManager supports **Chrome**, **Firefox**, **Opera**, **PhantomJS**, **Microsoft Edge**, and **Internet Explorer**. For that, it provides several *drivers managers* for these browsers, i.e. `ChromeDriverManager`, `FirefoxDriverManager`, `OperaDriverManager`, `PhantomJsDriverManager`, `EdgeDriverManager`, and `InternetExplorerDriverManager`. These *drivers managers* can be used as follows:

```java
ChromeDriverManager.getInstance().setup();
FirefoxDriverManager.getInstance().setup();
OperaDriverManager.getInstance().setup();
PhantomJsDriverManager.getInstance().setup();
EdgeDriverManager.getInstance().setup();
InternetExplorerDriverManager.getInstance().setup();
```

Moreover, WebDriverManager provides a generic *driver manager* called `WebDriverManager`. This manager which can be parameterized using Selenium driver classes (e.g. `org.openqa.selenium.chrome.ChromeDriver`, `org.openqa.selenium.firefox.FirefoxDriver`, etc), as follows: 

```java
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

// ...

Class<? extends WebDriver> driverClass = ChromeDriver.class;
WebDriverManager.getInstance(driverClass).setup();
WebDriver driver = driverClass.newInstance();
```

## Examples

Check out [WebDriverManager Examples][WebDriverManager Examples] for some JUnit 4 tests using WebDriverManager.


## WebDriverManager API

WebDriverManager exposes its API by means of the **builder pattern**. This means that given a *DriverManger* instance (e.g. ``ChromeDriverManager``, ``FirefoxDriverManager``, and so on), their capabilities can be tuned using different methods. The following table summarizes the WebDriverManager API, together with the equivalent configuration key:


| Method                                                    | Description                                                                                                                                                                                                                                                                                                                                                     | Equivalent configuration key                                                                                                                                              |
|-----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ``version()``                                             | By default, WebDriverManager tries to download the latest version of a given driver binary. A concrete version can be specified using this method.                                                                                                                                                                                                              | ``wdm.chromeDriverVersion``, ``wdm.operaDriverVersion``, ``wdm.internetExplorerVersion``, ``wdm.edgeVersion``, ``wdm.phantomjsDriverVersion``, ``wdm.geckoDriverVersion`` |
| ``forceCache()``                                          | By default, WebDriverManager connects to the specific driver repository URL to find out what is the latest version of the binary. This can be avoided forcing to use the latest version form the local repository.                                                                                                                                              | ``wdm.forceCache``=true                                                                                                                                                   |
| ``forceOperativeSystem(OperativeSystem operativeSystem)`` | By default, WebDriverManager downloads the binary for the same operative systems than the machine running the test. This can be changed using this method (accepted values: ``WIN``, ``LINUX``, ``MAC``).                                                                                                                                                       | ``wdm.forceOs``=WIN, ``wdm.forceOs``=LINUX, ``wdm.forceOs``=MAC                                                                                                           |
| ``forceDownload()``                                       | By default, WebDriverManager finds out the latest version of the binary, and then it uses the cached version if exists. This option forces to download again the binary even if it has been previously cached.                                                                                                                                                  | ``wdm.override``=true                                                                                                                                                     |
| ``useBetaVersions()``                                     | By default, WebDriverManager skip beta versions. With this method, WebDriverManager will download also beta versions.                                                                                                                                                                                                                                           | ``wdm.useBetaVersions``=true                                                                                                                                              |
| ``architecture(Architecture arch)``                       | By default, WebDriverManager would try to use the proper binary for the platform running the test case (i.e. 32-bit or 64-bit). This behavior can be changed by forcing a given architecture: 32-bits (``Architecture.x32``) or 64-bits (``Architecture.x64``);                                                                                                 | ``wdm.architecture``                                                                                                                                                      |
| ``arch32()``                                              | Force to use the 32-bit version of a given driver binary.                                                                                                                                                                                                                                                                                                       | ``wdm.architecture``=32                                                                                                                                                   |
| ``arch64()``                                              | Force to use the 64-bit version of a given driver binary.                                                                                                                                                                                                                                                                                                       | ``wdm.architecture``=64                                                                                                                                                   |
| ``driverRepositoryUrl(URL url)``                          | This method allows to change the repository URL in which the binaries are hosted (see next section for default values).                                                                                                                                                                                                                                         | ``wdm.chromeDriverUrl``, ``wdm.operaDriverUrl``, ``wdm.internetExplorerDriverUrl``, ``wdm.edgeDriverUrl``, ``wdm.phantomjsDriverUrl``, ``wdm.geckoDriverUrl``             |
| ``useTaobaoMirror()``                                     | The [npm.taobao.org] site is a mirror which hosts different software assets. Among them, it hosts *chromedriver*, *geckodriver*,  *operadriver*, and *phantomjs* driver. Therefore, this method can be used in ``ChromeDriverManager``, ``FirefoxDriverManager``, ``OperaDriverManager``, and ``PhantomJsDriverManager`` to force to use the taobao.org mirror. | ``wdm.useTaobaoMirror``=true                                                                                                                                              |
| ``proxy(String proxy)``                                   | Use a HTTP proxy for the Internet connection.                                                                                                                                                                                                                                                                                                                   | ``wdm.proxy``                                                                                                                                                             |
| ``proxyUser(String username)``                            | Specify a username for HTTP proxy.                                                                                                                                                                                                                                                                                                                              | ``wdm.proxyUser``                                                                                                                                                         |
| ``proxyPass(String password)``                            | Specify a password for HTTP proxy.                                                                                                                                                                                                                                                                                                                              | ``wdm.proxyPass``                                                                                                                                                         |

The following table contains some examples:

| Example                                                              | Description                                                       |
|----------------------------------------------------------------------|-------------------------------------------------------------------|
| ``ChromeDriverManager.getInstance().version("2.26").setup();``       | Force to use version 2.26 of *chromedriver*                       |
| ``FirefoxDriverManager.getInstance().arch32().setup();``             | Force to use the 32-bit version of *geckodriver*                  |
| ``OperaDriverManager.getInstance().forceCache().setup();``           | Force to use the cache version of *operadriver*                   |
| ``PhantomJsDriverManager.getInstance().useTaobaoMirror().setup();``  | Force to use the taobao.org mirror to download *phantomjs* driver |
| ``ChromeDriverManager.getInstance().proxy("server:port").setup();``  | Using proxy *server:port* for the connection                      |


## Configuration

Configuration parameters for WebDriverManager are set in the ``webdrivermanager.properties`` file:

```properties
wdm.targetPath=~/.m2/repository/webdriver
wdm.forceCache=false
wdm.override=false
wdm.useBetaVersions=false
wdm.timeout=30

wdm.chromeDriverUrl=https://chromedriver.storage.googleapis.com/
wdm.chromeDriverTaobaoUrl=http://npm.taobao.org/mirrors/chromedriver
wdm.chromeDriverExport=webdriver.chrome.driver
wdm.chromeDriverVersion=LATEST

wdm.operaDriverUrl=https://api.github.com/repos/operasoftware/operachromiumdriver/releases
wdm.operaDriverTaobaoUrl=http://npm.taobao.org/mirrors/operadriver
wdm.operaDriverExport=webdriver.opera.driver
wdm.operaDriverVersion=LATEST

wdm.internetExplorerDriverUrl=https://selenium-release.storage.googleapis.com/
wdm.internetExplorerExport=webdriver.ie.driver
wdm.internetExplorerVersion=LATEST

wdm.edgeDriverUrl=https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
wdm.edgeExport=webdriver.edge.driver
wdm.edgeVersion=LATEST

wdm.phantomjsDriverUrl=https://bitbucket.org/ariya/phantomjs/downloads/
wdm.phantomjsDriverTaobaoUrl=http://npm.taobao.org/mirrors/phantomjs
wdm.phantomjsDriverExport=phantomjs.binary.path
wdm.phantomjsDriverVersion=LATEST

wdm.geckoDriverUrl=https://api.github.com/repos/mozilla/geckodriver/releases
wdm.geckoDriverTaobaoUrl=http://npm.taobao.org/mirrors/geckodriver
wdm.geckoDriverExport=webdriver.gecko.driver
wdm.geckoDriverVersion=LATEST
```

The variable ``wdm.targetPath`` is the default folder in which WebDriver binaries are going to be stored. Notice that by default the path of the Maven local repository is used. The URLs to check the latest version of Chrome, Opera, Internet Explorer, Edge, PhantomJS, and Firefox are set using the variables ``wdm.chromeDriverUrl``, ``wdm.operaDriverExport``, ``wdm.operaDriverUrl``, ``wdm.edgeDriverUrl``, ``wdm.phantomjsDriverUrl``, and ``wdm.geckoDriverUrl``.

These properties can be overwritten by Java system properties, for example:

```java
System.setProperty("wdm.targetPath", "/my/custom/path/to/driver/binaries");
```

... or by command line, for example:

```properties
-Dwdm.override=true
```

By default, WebDriverManager downloads the latest version of the WebDriver binary. But concrete versions of WebDriver binaries can be forced by changing the value of the variables ``wdm.chromeDriverVersion``, ``wdm.operaDriverVersion``,  ``wdm.internetExplorerVersion``, or  ``wdm.edgeVersion`` from its default value (``LATEST``) to a concrete version. For instance:

```properties
-Dwdm.chromeDriverVersion=2.25
-Dwdm.internetExplorerVersion=2.46
-Dwdm.operaDriverVersion=0.2.0
-Dwdm.edgeVersion=3.14366
-Dwdm.phantomjsDriverVersion=2.1.1
-Dwdm.geckoDriverVersion=0.11.1
```

If no version is specified, WebDriverManager sends a request to the server hosting the binary. In order to avoid this request and check if any binary has been previously downloaded, the key `wdm.forceCache` can be used.

As of WebDriverManager 2, the value of these properties can be overridden by means of *environmental variables*. The name of these variables result from putting the name in uppercase and replacing the symbol `.` by `_`. For example, the property ``wdm.targetPath`` can be overridden by the environment variable ``WDM_TARGETPATH``.    

### HTTP Proxy

If you use an HTTP Proxy in your Internet connection, you can configure your settings by exporting the Java environment variable ``HTTPS_PROXY`` using the following notation: ``my.http.proxy:1234`` or ``username:password@my.http.proxy:1234``.
Also you can configure username and password using environment variables (``HTTPS_PROXY_USER`` and ``HTTPS_PROXY_PASS``).

### Known Issues

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

1. Create a token/secret pair in your [GitHub account]
2. Tell WebDriverManager the value of this pair token/secret. To do that you should use the configuration keys ``wdm.gitHubTokenName`` and ``wdm.gitHubTokenSecret``. You can pass them as command line Java parameters as follows:

```properties
-Dwdm.gitHubTokenName=<your-token-name>
-Dwdm.gitHubTokenSecret=<your-token-secret>
```

## Help

If you have questions on how to use WebDriverManager properly with a special configuration or suchlike, please consider asking a question on [stackoverflow](https://stackoverflow.com/questions/tagged/webdrivermanager-java) and tag it with  *webdrivermanager-java*.

## About

WebDriverManager (Copyright &copy; 2015-2017) is a project created by [Boni Garcia] and licensed under the terms of the [Apache 2.0 License]. Comments, questions and suggestions are always very [welcome][WebDriverManager issues].

[Logo]: http://bonigarcia.github.io/img/webdrivermanager.png
[Selenium Webdriver]: http://docs.seleniumhq.org/projects/webdriver/
[Apache 2.0 License]: http://www.apache.org/licenses/LICENSE-2.0
[Boni Garcia]: http://bonigarcia.github.io/
[GitHub Repository]: https://github.com/bonigarcia/webdrivermanager
[authenticated requests]: https://developer.github.com/v3/#rate-limiting
[GitHub account]: https://github.com/settings/tokens
[WebDriverManager issues]: https://github.com/bonigarcia/webdrivermanager/issues
[WebDriverManager Examples]: https://github.com/bonigarcia/webdrivermanager-examples
[npm.taobao.org]: http://npm.taobao.org/mirrors/
[selenium-jupiter]: https://github.com/bonigarcia/selenium-jupiter/