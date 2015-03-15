WebDriverManager
================

This piece of software is a small library aimed to automate the [Selenium Webdriver] binaries management within a Java project.

If you have ever used [Selenium Webdriver], you probably know that in order to use some browsers (for example Chrome, Internet Explorer, or Opera) you need to download a binary which allows WebDriver to handle the browser. In addition, the absolute path to this binary must be set as Java variables, as follows:

	System.setProperty("webdriver.chrome.driver", "/absolute/path/to/binary/chromedriver");
	System.setProperty("webdriver.opera.driver", "/absolute/path/to/binary/operadriver");
	System.setProperty("webdriver.ie.driver", "C:/absolute/path/to/binary/IEDriverServer.exe");

This is quite annoying since it forces you to link directly this binary in your source code. In addition, you have to check manually when new versions of the binaries are released. This library comes to the rescue, performing in an automated way all this dirty job for you.

WebDriverManager is open source, released under the terms of [LGPL License]. The source code of this project can be cloned from the [GitHub Repository].

Usage
-----

In order to use WebDriverManager in a Maven project, first add the following dependency to your `pom.xml`:

	<dependency>
		<groupId>io.github.bonigarcia</groupId>
		<artifactId>webdrivermanager</artifactId>
		<version>1.0.0</version>
		<scope>test</scope>
	</dependency>

Then you can let WebDriverManager to do manage WebDriver binaries for your application/test. Take a look to this example which uses Chrome with Selenium WebDriver:

	public class ChromeTest {

		protected WebDriver driver;

		@Before
		public void setup() {
			ChromeDriverManager.setup();
			driver = new ChromeDriver();
		}

		@After
		public void teardown() {
			driver.quit();
		}

		@Test
		public void test() {
			// Use driver to carry out web testing
		}

	}

Notice that simple adding ``ChromeDriverManager.setup();`` WebDriverManager does magic for you:

1. It checks the latest version of the WebDriver binary file
2. It download the binary WebDriver if it is not present in your system
3. It exports the required Java variable by Selenium WebDriver

So far, version 1.0.0 of WebDriverManager supports **Chrome**, **Opera**, and **Internet Explorer**, as follows:

	ChromeDriverManager.setup();
	InternetExplorerDriverManager.setup();
	OperaDriverManager.setup();

Advanced
--------

Configuration parameters for WebDriverManager are set in the ``wdm.properties`` file:

	targetPath=~/.m2/repository/webdriver
	override=false
	downloadJustForMySystem=true

	chromeDriverUrl=http://chromedriver.storage.googleapis.com/
	chromeDriverExport=webdriver.chrome.driver

	operaDriverUrl=https://api.github.com/repos/operasoftware/operachromiumdriver/releases
	operaDriverExport=webdriver.opera.driver

	internetExplorerDriverUrl=http://selenium-release.storage.googleapis.com/
	internetExplorerExport=webdriver.ie.driver

The variable ``targetPath`` is the default folder in which WebDriver binaries are going to be stored. Notice that by default the path of the Maven local repository is used. The URLs to check the latest version of Chrome, Opera, and Internet Explorer are set using the variables ``chromeDriverUrl``, ``operaDriverExport``, and ``operaDriverUrl``. 

About
-----

WebDriverManager is a personal project of [Boni Garcia] (Copyright &copy; 2015). Licensed under [LGPL License]. Comments, questions and suggestions are always very welcome!

[Selenium Webdriver]: http://docs.seleniumhq.org/projects/webdriver/
[LGPL License]: http://www.gnu.org/licenses/lgpl-2.1.html
[Boni Garcia]: http://bonigarcia.github.io/
[GitHub Repository]: https://github.com/bonigarcia/webdrivermanager
