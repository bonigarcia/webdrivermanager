# Changelog

## [5.1.1] - 2022-04-08

### Added
- Improve OperaDriver support, to make it compatible with Selenium 4.1.3 (issue #808)

### Changed
- Include httpclient5 dependency explicitly (issue #802)

### Fixed
- Detection for snap installed browser (issue #795)
- Support for msedgedriver in Mac M1 (issues #804 and #812)
- Normalize path separators in WebDriverManager.zipFolder() (PR #815)

## [5.1.0] - 2022-02-17
### Added
- Add Docker Extra Hosts API method: dockerExtraHosts(String[]) (PR #788)
- Include static method isDockerAvailable() in WebDriverManager class
- Include static method zipFolder(Path sourceFolder) in WebDriverManager class
- Include static method isOnline(URL url) in WebDriverManager class
- Include API method to get Docker VNC URL
- Include API method to accept remote address as URL

### Fixed
- Use https://registry.npmmirror.com/ instead of https://npm.taobao.org/ for driver mirror (fix #781)
- Create config-dependent objects in setup logic (fix #751)
- Include arguments for whitelisted and allowed origins for chromedriver in Docker (fix #733)

### Changed
- Updated dependencies (e.g. docker-java) to the latest version
- Use varargs in setter for Docker volumes
- Include Apache Commons Lang3 as dependency

### Removed
- Remove Guava dependency (issue #779)
- Deprecated several API methods (recordingPrefix, recordingOutput, dockerImage)
- Deprecated several config methods (e.g. isAvoidingResolutionCache) (PR #769)


## [5.0.3] - 2021-09-17
### Added
- Include viewOnly (for noVNC) as API method and config parameter (issue #704)

### Fixed
- Filter ARM64 architecture using all possible labels (issue #700)


## [5.0.2] - 2021-09-13
### Added
- Support for ARM64 architecture in chromedriver for Mac (issue #697)
- Include current timestamp in recordings file name

### Changed
- Enhance cachePath and resolutionCachePath mkdir (issue #696)
- Postpone initialing DockerService (issue #691)


## [5.0.1] - 2021-08-31
### Changed
- Downgrade to Java 8


## [5.0.0] - 2021-08-30
### Added
- New documentation: https://bonigarcia.dev/webdrivermanager/ (sources: AsciiDoc, generated: HTML, PDF, EPUB)
- New high-level feature: browser finder (using the info of the commands database)
- New high-level feature: WebDriver builder (using local/remote browsers)
- New high-level feature: Browsers in Docker containers (using Aerokube images, with recording, VNC access, etc.)
- Improved CLI mode: allow to run browsers in Docker container and inspect them through noVNC
- Improved Sever mode: use WDM server as a Selenium Server (a "hub" in the classical Selenium Grid jargon)
- Include manager for safaridriver, used to get Safari path and dockerized browser (WebKit engine)
- Include shutdown hook for closing WebDriver objects (and release Docker containers, if any)
- Include API method gitHubToken() to specify a personal access token for authenticated GitHub requests
- Include API method avoidTmpFolder() to avoid the use of the temporal folder when downloading drivers (issue #657)
- Include API method arm64() to specify ARM64 architecture
- Bypass notarization requirement for geckodriver on Mac OS
- Include support for generic driver (using config key wdm.defaultBrowser)
- Allow to specify different driver version per operating system in the versions database (needed for problem detected in geckodriver 0.29.1 in Mac, but required for Windows when using Firefox 90)

### Fixed
- Register decompression for HTTP client (issue #677)
- Use --disable-gpu flag as default arguments for Docker containers (in Chrome and Edge)

### Changed
- Not using singletons in managers (e.g. chromedriver(), firefoxdriver()). Now, each manager returns a new instance each time
- Change name of configuration keys (and corresponding API methods) containing the word internetExplorer to iExplorer
- Download driver for IExplorer (IEDriverServer.exe) from GitHub (instead of Google storage)
- Use Java 11
- Use JUnit 5 in tests
- Use Selenium 4 in tests
- Use AssertJ for assertions in tests
- Use selenium-java as provided dependency

### Removed
- Remove managers for PhantomJS (deprecated browser) and SeleniumServerStandalone (scarce use)
- Remove API method globalConfig() (it has no sense since managers are not singleton anymore)
- Remove API methods gitHubTokenName() and gitHubTokenSecret(), replaced by gitHubToken()
- Remove API localRepositoryUser() and localRepositoryPassword() (redundant)
- Remove commons-io, commons-lang3, jarchivelib (compile), and okhttp (provided) artifacts


## [4.4.3] - 2021-05-09
### Added
- Support for ARM64 (Aarch64) architecture (issue #634)
- Include method arm64() in WDM API to specify ARM64 architecture

### Fixed
- Fix execution of registry query commands to detect browser version (for Windows)


## [4.4.2] - 2021-05-09
- Due to a problem in the release procedure, version 4.4.2 is identical to 4.4.1


## [4.4.1] - 2021-04-22
### Fixed
- Fix browser version detection in Mac (issue #632)


## [4.4.0] - 2021-03-06
### Added
- Automatic module name in MANIFEST.MF for JDK 9+ support (PR #615)
- Include config key wdm.browserVersionDetectionRegex, equivalent to API method browserVersionDetectionRegex()
- Expose method .exportParameter() in WebDriverManager API
- Include config key wdm.useChromiumDriverSnap to use Chromium snap package (false by default)
- Support local URLs (file://) for versions and commands properties
- Include new API methods: useLocalCommandsPropertiesFirst(), versionsPropertiesUrl(URL), and commandsPropertiesUrl(URL)

### Changed
- Extract commands database as a properties file (commands.properties)

### Removed
- Remove method browserPath() in WebDriverManager API (changed by browserVersionDetectionCommand())


## [4.3.1] - 2021-01-18
### Fixed
- Include dylib libraries together with msedgedriver (issue #593)

### Changed
- Clean logic for operating system handling


## [4.3.0] - 2021-01-14
### Added
- Detect Edge version also in Linux
- Read LATEST_RELEASE_version_OS (where OS=WINDOWS|LINUX|MACOS) for msedgedriver

### Fixed
- Make more robust browser distro detection (issue #586)

### Changed
- Make more robust browser major version detection from POSIX output (issue #576)


## [4.2.2] - 2020-09-21
### Fixed
- Actual fixed for issue #554 (Windows Edge driver download fails with 404)


## [4.2.1] - 2020-09-21
### Added
- Include syntactic sugar methods for operating systems in the API: win(), linux(), and mac()

### Fixed
- Init resolution cache also when clearResolutionCache() method is invoked

### Changed
- Avoid read release from repository in the retry process to resolve driver (issue #554)


## [4.2.0] - 2020-08-21
### Added
- Check ignoredVersion when resolving driver version (issue #529)
- Include configuration key (wdm.resolutionCachePath) to specify a path for resolution cache

### Fixed
- Fix filtering based on ignored versions
- Use Locale.ROOT in String case conversion (fix issue #521, which happens in Turkey locale)
- Fix support of WebDriverManager Docker container to resolve drivers

### Changed
- Change default cache path location to ~/.cache/selenium
- Store drivers in cache using the same folder structure: {cachePath}/driverName/os+arch/driverVersion
- Rename getBinaryPath() method to getDownloadedDriverPath() in WebDriverManager API
- Rename getDownloadedVersion() method to getDownloadedDriverVersion() in WebDriverManager API
- Rename clearCache() method to clearDriverCache() in WebDriverManager API
- Rename configuration key wdm.binaryPath to wdm.browserPath
- Build URL from pattern when no candidate is found after the filtering process


## [4.1.0] - 2020-07-14
### Added
- New method in WebDriverManager API: ttlBrowsers(int) -> TTL to store browser versions in resolution cache (issue #483)
- New method in WebDriverManager API: avoidReadReleaseFromRepository() -> to avoid reading driver version from repository
- Build download URL from pattern when it is no available in chromedriver and msedgedriver repository

### Fixed
- Add namespace context for Saxon compatibility (issue #503)
- Change default headers in HTTP client (fix support for msedgedriver)

### Changed
- Store value in resolution cache only when TTL (for drivers and browsers) is upper 0
- Parse BitBucket API to download PhantomJS drivers

### Removed
- Remove WebDriverManager survey link from log traces


## [4.0.0] - 2020-05-03
### Added
- Full support for Edge (Chromium-based) using https://msedgedriver.azureedge.net/
- Include resolution cache (former preferences) stored as properties (resolution.properties) in the cache folder
- Read Chrome version from registry when wmic does not discover the version (issue #394)
- Add Safari support for DriverManagerType
- Look for wmic.exe program also in wbem folder (issue #438)
- Improve driver version resolution algorithm
- Include label "alpha" for detecting beta versions
- Include WebDriverManager as Java agent using premain entry point
- Release WebDriverManager as Docker container (for CLI and Server) in Docker Hub
- Include WebDriverManager survey link in INFO traces: http://tiny.cc/wdm-survey
- New method in WebDriverManager API: browserVersion() -> to specify major browser version
- New method in WebDriverManager API: avoidFallback() -> to avoid the fallback mechanism if some exception happens
- New method in WebDriverManager API: getDriverManagerType() -> to get manager type (enum)

### Fixed
- Support Windows short names in cachePath
- Fix proxy credentials never set from env vars
- Bump Apache HttpClient to version 5.0 (fix issue #461)
- Remove hard coded values on Travis SonarCloud addon (issue #471)

### Changed
- Use LATEST_RELEASE_x from chromedriver and msedgedriver repository (x = given version)
- Use latest from cache as primary fallback mechanism
- Support for different locations of Opera in Windows (also in LOCALAPPDATA env)
- Refactor main and test logic in different packages
- Use streams for filtering URLs and cache
- Bump all dependencies (main and test) to latest stable versions
- Changed method in WebDriverManager API: clearPreferences() -> to path of cache, old clearResolutionCache()
- Changed method in WebDriverManager API: cachePath() -> to path of cache, old targetPath()
- Changed method in WebDriverManager API: driverVersion() -> to specify driver version, old version()
- Changed method in WebDriverManager API: avoidBrowserDetection() -> to avoid the detection of the browser version, old avoidAutoVersion()
- Changed method in WebDriverManager API: getDriverVersions() -> to get the list of available driver versions, old getVersions()
- Logo

### Removed
- Support for old versions of Edge (pre Chromium-based)
- Use of Java preferences (now: resolution.properties)
- Use of mirror in fallback mechanism
- Methods of WebDriverManager API: targetPath(), version(), avoidAutoVersion(), forceCache() (cache is used always)


## [3.8.1] - 2020-01-19
### Fixed
- Fix Chromium support (issue #429, PR #430)
- Avoid filtering for architecture with chromedriver in Windows
- Order files in cache alphabetically
- Include last / in URL for chromedriver mirror (to read properly LATEST_RELEASE)

### Changed
- Read from env LOCALAPPDATA first to detect Chromium version in Windows
- Update msedgedriver version 80.0.361.33 for Edge 80 in versions.properties
- Set geckodriver version 0.26.0 for Firefox 70, 71, and 72 in versions.properties


## [3.8.0] - 2020-01-11
### Added
- Chromium support, included snap package (issues #400 and #403)
- Include 2nd fallback mechanism using latest driver from cache (issue #415)
- Enhance artifact mirror support (issue #390)

### Changed
- Include clearPreferences() as a builder method of WebDriverManager
- Improve cache filtering by version (issue #391)
- Check different versions of Edge (stable, beta, dev) to find out version
- Set chromedriver version 80.0.3987.16 for Chrome 80 in versions.properties
- Set chromedriver version 79.0.3945.16 for Chrome 79 in versions.properties
- Set geckodriver version 0.26.0 for Firefox 69 in versions.properties
- Set operadriver version 79.0.3945.79 for Opera 66 in versions.properties
- Set operadriver version 78.0.3904.87 for Opera 65 in versions.properties
- Set operadriver version 77.0.3865.120 for Opera 64 in versions.properties
- Set msedgedriver version 80.0.361.23 for Edge 80 in versions.properties
- Set msedgedriver version 79.0.313.0 for Edge 79 in versions.properties
- Set msedgedriver version 78.0.277.0 for Edge 78 in versions.properties


## [3.7.1] - 2019-09-17
### Added
- Read LOCALAPPDATA env to find out Chrome version in Windows (issue #381)
- Add useLocalVersionsPropertiesFirst() in WebDriverManager API

### Changed
- Set chromedriver version 78.0.3904.11 for Chrome 78 in versions.properties
- Set operadriver version 76.0.3809.132 for Opera 63 in versions.properties
- Set geckodriver version 0.25.0 for Firefox 69 in versions.properties
- Update geckodriver version 0.25.0 for Firefox 68 in versions.properties


## [3.7.0] - 2019-09-10
### Added
- Store detected version of browser as preference

### Fixed
- Do not restrict mirrors to ones hosted on npm.taobao.org page (issue #379)

### Removed
- Remove avoidAutoVersion() by default in CLI mode (issue #369)

### Changed
- Read online version.properties by default to find driver version (configurable with wdm.versionsPropertiesOnlineFirst)
- Set geckodriver version 0.24.0 for Firefox 66, 67, and 68 in versions.properties
- Set operadriver version 75.0.3770.100 for Opera 62 in versions.properties
- Set chromedriver version 77.0.3865.10 for Chrome 77 in versions.properties
- Set msedgedriver version 77.0.237.0 for Edge 77 in versions.properties
- Update chromedriver version 76.0.3809.126 for Chrome 76 in versions.properties
- Update chromedriver version 77.0.3865.40 for Chrome 77 in versions.properties


## [3.6.2] - 2019-07-18
### Added
- Check PROGRAMFILES and PROGRAMFILES(X86) to find out browser versions in Windows (issue #351)

### Fixed
- Support for Edge driver 76 (issue #338 and #347)

### Changed
- Remove Edge 77 key from versions.properties for release (it will be maintained online)
- Update edgedriver version 76.0.183.0 for Edge 76 in versions.properties
- Update edgedriver version 75.0.139.20 for Edge 75 in versions.properties
- Update chromedriver version 76.0.3809.68 for Chrome 76 in versions.properties
- Update chromedriver version 75.0.3770.140 for Chrome 75 in versions.properties


## [3.6.1] - 2019-06-07
### Added
- Special case to find out Chromium version instead of Chrome (issue #348)

### Fixed
- Support for Edge driver 76 (issue #338 and #347)


## [3.6.0] - 2019-05-17
### Added
- Read chromedriver LATEST_RELEASE page when not using useBetaVersions() (issues #333, #341, #342)

### Fixed
- Enable Edge Dev test (issue #337)

### Changed
- Increase default value of TTL to 86400 seconds (i.e. one day)
- Change changelog format to Markdown (issue #331)


## [3.5.0] - 2019-05-05
### Added
- Support for msedgedriver (Edge based on Chromium)
- Allow WDM_ARCHITECTURE environment values (32, 64) in addition to the new ones (issue #334)
- Set msedgedriver version 75.0.137.0 for Edge 75 in versions.properties
- Set chromedriver version 75.0.3770.8 for Chrome 75 in versions.properties

### Fixed
- Fix Edge support, broken due to changes in web page (issues #335 #338 #339)

### Changed
- Rename version for Edge 44 as pre-installed


## [3.4.0] - 2019-03-27
### Added
- Allow global configuration with method globalConfig() (issue #313)
- Include clearCache() method in WebDriverManager API to clear cache
- Set chromedriver version 73.0.3683.68 for Chrome 73 in versions.properties
- Set chromedriver version 74.0.3729.6 for Chrome 74 in versions.properties

### Changed
- Improve logging when driver version in unknown


## [3.3.0] - 2019-02-06
### Added
- Force using mirror when first exception happens, e.g. 403 error (issue #302)
- Include version.properties URL as configuration key
- Set chromedriver version 2.46 for Chrome 71, 72, and 73 in versions.properties
- Include geckodriver version 0.24.0 for Firefox 65 in versions.properties
- Include operadriver version 2.42 for Opera 58 in versions.properties

### Changed
- Improve cache handling for retries


## [3.2.0] - 2019-01-07
### Added
- Update versions.properties: Chrome 72; Firefox 64; Opera 57
- Use single configuration instance per driver manager singleton
- Read beta versions for driver from versions.properties
- Read also https_proxy (in lower case) from environment variables (issue #292)
- Add method to clear preferences in WebDriverManager API

### Fixed
- Fix issue #296 (Chrome version not being detected on Windows)

### Changed
- Change preference key format to browser name plus version (e.g. chrome69)
- Use local (online if not found) versions.properties for stable and online (always) for beta versions


## [3.1.1] - 2018-12-10
### Added
- Update versions.properties: Opera 57

### Fixed
- Bug-fix: browser binary path for Linux and Mac was not correctly set


## [3.1.0] - 2018-12-09
### Added
- Store resolved latest versions as Java preferences with a time to live (ttl, by default 60 seconds)
- Reading versions.properties from GitHub if browser version not found in local
- Read Windows program file env depending on the platform (32|64 bits)
- Implement manager to download selenium-server-standalone jar files
- Support for Edge 44 (insiders)
- New API methods: ttl(), browserPath()
- Include clear-preferences option in CLI
- Update versions.properties: Chrome 68, 69, 70, 71; Firefox 63; Opera 56; Edge 44

### Changed
- Using default properties is some value is missing


## [3.0.0] - 2018-07-04
### Added
- Auto driver check for Chrome in Window, Linux, and Mac
- Auto driver check for Firefox in Window, Linux, and Mac
- Auto driver check for Opera in Window, Linux, and Mac
- Auto driver check for Edge in Window
- WebDriverManager Server

### Changed
- Set minimum Java compatibility version to 1.8

### Removed
- Drop compatibility with 1.x API, i.e. DriverManager.getInstance()

### Deprecated
- Use a unique name for driver name (deprecate the use of wires for Firefox)


## [2.2.5] - 2018-08-16
### Fixed
- Bug-fix: logic for checking drivers in cache (issue #232)

### Changed
- Improve cache logic (issue #229)


## [2.2.4] - 2018-07-04
### Fixed
- Bug-fix: Filter by driver name when seeking binaries in cache (issue #223)


## [2.2.3] - 2018-06-21
### Fixed
- Bug-fix: Avoid filtering by OS in the case of IEDriverServer
- Bug-fix: Update EdgeDriverManager due to changes in web repository


## [2.2.2] - 2018-06-08
### Changed
- Improve cache handling (issue #216)
- Remove unnecessary reverse of URL lists (issue #206)
- Exclude logback-classic from compile scope (issue #202)


## [2.2.1] - 2018-04-09
### Added
- Force architecture filtering when explicit setup (issue #200)
- Keep latest version value by manager instance (issue #197)

### Changed
- Clean Downloader logic and logging
- Rename operativeSystem() method to operatingSystem() (issue #196)
- Move post downloader logic for Edge binary into the proper manager


## [2.2.0] - 2018-03-23
### Added
- Configuration manager: WebDriverManager.config()
- Interactive mode #1: mvn exec:java -Dexec.args="browserName"
- Interactive mode #2: java -jar webdrivermanager.jar browserName
- Create fat-jar from source code: mvn compile assembly:single
- Method getVersions() to get all driver versions available (issue #191)

### Fixed
- Bug-fix: intermittently fails to download driver when running in parallel (issue #186)
- Bug-fix: Improve exit condition looking for binary file in post download logic (issues #193 and #194)
- Bug-fix: honor forceDownload() option (related to issue #186)

### Removed
- Remove envs WDM_GIT_HUB_TOKEN_NAME and WDM_GIT_HUB_TOKEN_SECRET (use WDM_GITHUBTOKENNAME and WDM_GITHUBTOKENSECRET instead)


## [2.1.0] - 2018-01-04
### Added
- Include class diagram using ObjectAid

### Changed
- Use multiton pattern (WebDriverManager class) to provide unique access point
- Keep getInstance() method for backwards compatibility, e.g. ChromeDriverManager.getInstance().setup();
- Rename forceOperativeSystem() method to operativeSystem()
- Rename useTaobaoMirror() method by useMirror()
- Remove logback.xml from packaged jar (issue #181 and #184)
- Change configuration key wdm.forceOs by wdm.os


## [2.0.1] - 2017-12-12
### Added
- Include configuration key wdm.forceOs to force operative system
- Include configuration keys wdm.proxy, wdm.proxyUser, and wdm.proxyPass for proxy settings
- Include configuration key wdm.useTaobaoMirror to use Taobao mirror

### Changed
- Configuration keys (wdm.*) are now optional (default values: false, "", 0)


## [2.0.0] - 2017-11-27
### Added
- New method in BrowserManager API: forceOperativeSystem(OperativeSystem operativeSystem)
- New method in BrowserManager API: ignoreVersions(String... versions)
- Use logback instead of simplelogger for logging
- Use SonarCloud to keep a good level of internal code quality
- Use Codecov to keep a good level of code coverage
- Reset state of browser managers after setup

### Changed
- Relicense to Apache 2.0
- Stop using typesafe config library for handling properties
- Override configuration values with environmental variables (e.g. WDM_TARGETPATH)
- Improve management of proxy
- Upgrade to Selenium 3.7 for end-to-end tests
- Improve test performance

### Removed
- Remove deprecated methods from version 1.x (MarionetteDriverManager, etc)


## [1.7.2] - 2017-09-17
### Added
- Add wdm.architecture configuration key (issue #154)
- Read properties values from system environment as fallback to properties


## [1.7.1] - 2017-07-10
### Added
- Use NTCredentials for NTLM AuthSchemes (PR #149 from andrew-sumner)
- Avoid throwing exception in the case of non supported managers (e.g. RemoteWebDriver)

### Changed
- Improve support for new versions of operadriver (e.g. 2.27 and 2.29)


## [1.7.0] - 2017-06-18
### Added
- Automatic retry forcing the use of cache on exception (issue #125)
- Skip beta versions by default (issue #127 and #136)
- Added getBinaryPath() method to BrowserManager API
- Prevent non-executable files being picked as driver executables (PR #133 from Konfuzzyus)

### Fixed
- Bug-fix: added connection manager shared (PR #140 from tharakadesilva)
- Bug-fix: Improve support for Edge (issue #135)
- Bug-fix: Support for latest version of operadriver (issues #134 and #146)

### Changed
- Format code with spaces instead of tabs

### Removed
- Remove guava, commons-codec dependencies


## [1.6.2] - 2017-04-04
### Added
- New method in BrowserManager API: forceDownload()
- Support proxy server with authentication (isee #118, PR #120 by kazuki43zoo)
- Checking that binary files are actually executable (issue #114 and #121)

### Fixed
- Bug-fix: issue #115 number format exception due to IEDriver beta
- Bug-fix: issue #116 Ensure files aren't folders to check distro name

### Changed
- Downgrade com.typesafe:config to 1.2.1 (for Java 7 compliance)
- Ignore Taobao test due to connection reset error


## [1.6.1] - 2017-03-08
### Added
- New method in BrowserManager API: proxy()

### Fixed
- Bug-fix: forceCache() method was not working properly


## [1.6.0] - 2017-02-07
### Added
- Support for taobao.org mirror in FirefoxDriverManager
- Support for taobao.org mirror in ChromeDriverManager
- Expose BrowserManager API following the builder pattern
- New methods in BrowserManager API: version(), forceCache(), architecture(Architecture), arch32(), arch64(), useTaobaoMirror(), driverRepositoryUrl(URL)

### Deprecated
- Deprecate setup method with parameters (now builder pattern is used)


## [1.5.1] - 2017-01-19
### Added
- Allow running in separate classloader (PR #83 from phillcunnington)
- Improve download internal logic (add pre/post download methods)

### Fixed
- Bug-fix: update EdgeDriverManager (PR #87 from oscarcarlsson)
- Bug-fix: support for PhantomJS 2.5.0-beta (issue #96)

### Changed
- Change default URL for PhantomJS from npm.taobao.org to bitbucket.org
- Change default URL for PhantomJS from npm.taobao.org to bitbucket.org


## [1.5.0] - 2016-11-15
### Fixed
- Issue #77: bug-fix for phantomjs 32-bit artifact

### Changed
- Change default name of properties file to webdrivermanager.properties
- Use of Selenium 3 in internal tests

### Deprecated
- Deprecate MarionetteDriverManager (use instead FirefoxDriverManager)


## [1.4.10] - 2016-10-13
### Added
- Compatibility with Selenium 3
- Support for taobao mirror for chromedriver and  (wdm.chromeDriverUrl=https://npm.taobao.org/mirrors/chromedriver/)
- Support for taobao mirror for operadriver (wdm.operaDriverUrl=https://npm.taobao.org/mirrors/operadriver/)
- Issue #61: Support HTTP proxy by means of env var HTTP_PROXY or HTTPS_PROXY (PR #62 from Sebl29)

### Changed
- Stop using htmlunit to download edge binaries (jsoup instead)


## [1.4.9] - 2016-09-11
### Added
- Issue #52: Force the use of cache when network is not available
- Add Travis support (thanks to PR #57, #58, #59 by hennr)

### Fixed
- Bug-fix issue #19 (regression): getDriverName returns a list of string

### Changed
- Issue #40 and #56: Replace bitbucket PhantomJS source by mirror using jsoup instead HtmlUnit


## [1.4.8] - 2016-08-15
### Fixed
- Bug-fix issue #51: Determine correct geckodriver to download (PR #48 from phillcunnington)


## [1.4.7] - 2016-08-01
### Fixed
- Bug-fix issue #44: Fixed PhantomJS downloads page change (PR #45 from ndtreviv)


## [1.4.6] - 2016-07-01
### Added
- Handle exceptions when parsing URLs, reading next if necessary
- Use unTarGz method if needed


## [1.4.5] - 2016-05-12
### Added
- Support for multiple binary names (caused by geckodriver)
- Key wdm.forceCache to force using the latest version of binaries in cache

### Fixed
- Bug-fix: correct support for Marionette in generic manager

### Changed
- Improve the way of handling the version number of Edge driver


## [1.4.4] - 2016-06-05
### Added
- Include several HTTP headers to connect with server in downloads
- Include token/secret configuration keys for GitHub credentials


## [1.4.3] - 2016-05-10
### Added
- Exception for seeking PhantomJS in the binary cache


## [1.4.2] - 2016-04-05
### Added
- Check that repository target folder exits (otherwise create)
- Honor property (wdm.*Url) if available for driver version

### Fixed
- Bug-fix issue #24: search driver in cache by architecture


## [1.4.1] - 2016-03-12
### Fixed
- Bug-fix issue #19: WDM is unable to download the 64-bit IEDriverServer
- Bug-fix issue #20: PhantomJsDriverManager exception

### Changed
- Change URL to download Edge driver (exe instead of msi)
- Improve compatibility with Microsoft Windows and Mac OS X
- Improve test coverage


## [1.4.0] - 2016-01-13
### Added
- Add PhantomJS support (pull request #13 from wilx/master-phantomjs)
- Add Marionette support (pull request #14 from rosolko:master)

### Changed
- Improve tests


## [1.3.1] - 2015-11-07
### Added
- Check that existing binaries are valid before exporting variable
- Retries when seeking remote URL to find out latest version of binaries

### Changed
- Change log level of important messages to INFO


## [1.3.0] - 2015-11-01
### Added
- Support for Microsoft Edge


## [1.2.4] - 2015-10-20
### Added
- Assess that binary exists in cache to avoid seeking remote server


## [1.2.3] - 2015-09-01
### Fixed
- Bug-fix: redirection of setup call to the proper method


## [1.2.2] - 2015-08-19
### Changed
- Improved automatic discover of latest version to be downloaded (GitHub issue #5)
- Improved test coverage
- Changed log level to DEBUG


## [1.2.1] - 2015-08-14
### Added
- Added getter to read the downloaded version

### Changed
- Improved driver version management


## [1.2.0] - 2019-03-27
### Added
- Thread safe download. Pull request thanks to Ivan Gracia (izanmail@gmail.com)


## [1.1.2] - 2015-07-06
### Changed
- Manager classes as singlentons


## [1.1.1] - 2015-06-23
### Added
- Added provided scope to slf4j-simple

### Fixed
- Bug-fix: store binary in path using version instead of LATEST


## [1.1.0] - 2015-06-23
### Added
- Added parameter to force the use of a concrete WebDriver binary version

### Changed
- Changed log dependency from slf4j-log4j12 to slf4j-simple
- Changed setup from static method to instantiation of DriverManager


## [1.0.1] - 2015-06-09
### Fixed
- Bug-fix: Wrong chromedriver version downloaded (GitHub issue #1)


## [1.0.0] - 2015-03-19
### Added
- Automated download of WebDriver binaries
- Automated export of WebDriver binaries path
- Supported WebDriver binaries: Chrome, Opera, Internet Explorer
- Customizable configuration parameters using Java system properties
- Capability to force WebDriver binary architecture (32/64 bits)
