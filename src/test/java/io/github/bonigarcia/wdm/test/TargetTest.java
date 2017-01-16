/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package io.github.bonigarcia.wdm.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.Downloader;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;

/**
 * Target folder test.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.1
 */
@RunWith(Parameterized.class)
public class TargetTest {

	protected static final Logger log = LoggerFactory
			.getLogger(TargetTest.class);

	@Parameter(0)
	public String version;

	@Parameter(1)
	public String url;

	@Parameter(2)
	public String target;

	@Parameter(3)
	public BrowserManager manager;

	private static String targetPath = Downloader.getTargetPath();

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				// Chrome Linux
				{ "2.21",
						"http://chromedriver.storage.googleapis.com/2.21/chromedriver_linux64.zip",
						"/chromedriver/linux64/2.21/chromedriver_linux64.zip",
						ChromeDriverManager.getInstance() },

				// Opera Linux
				{ "0.2.2",
						"https://github.com/operasoftware/operachromiumdriver/releases/download/v0.2.2/operadriver_linux64.zip",
						"/operadriver/linux64/0.2.2/operadriver_linux64.zip",
						OperaDriverManager.getInstance() },

				// PhantomJS Linux
				{ "2.1.1",
						"https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2",
						"/phantomjs/linux-x86_64/2.1.1/phantomjs-2.1.1-linux-x86_64.tar.bz2",
						PhantomJsDriverManager.getInstance() },

				// PhantomJS Windows
				{ "2.1.1",
						"https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-windows.zip",
						"/phantomjs/windows/2.1.1/phantomjs-2.1.1-windows.zip",
						PhantomJsDriverManager.getInstance() },

				// PhantomJS Mac OS X
				{ "2.1.1",
						"https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-macosx.zip",
						"/phantomjs/macosx/2.1.1/phantomjs-2.1.1-macosx.zip",
						PhantomJsDriverManager.getInstance() },

				// Edge Windows #1
				{ "8D0D08CF-790D-4586-B726-C6469A9ED49C",
						"https://download.microsoft.com/download/1/4/1/14156DA0-D40F-460A-B14D-1B264CA081A5/MicrosoftWebDriver.exe",
						"/MicrosoftWebDriver/8D0D08CF-790D-4586-B726-C6469A9ED49C/MicrosoftWebDriver.exe",
						EdgeDriverManager.getInstance() },

				// Edge Windows #2
				{ "3.14361",
						"https://download.microsoft.com/download/1/4/1/14156DA0-D40F-460A-B14D-1B264CA081A5/MicrosoftWebDriver.exe",
						"/MicrosoftWebDriver/3.14361/MicrosoftWebDriver.exe",
						EdgeDriverManager.getInstance() },

				// Marionette Mac OS X
				{ "0.6.2",
						"https://github.com/jgraham/wires/releases/download/v0.6.2/wires-0.6.2-OSX.gz",
						"/wires/osx/0.6.2/wires-0.6.2-OSX.gz",
						FirefoxDriverManager.getInstance() },
				{ "0.3.0",
						"https://github.com/jgraham/wires/releases/download/0.3.0/wires-0.3.0-osx.tar.gz",
						"/wires/osx/0.3.0/wires-0.3.0-osx.tar.gz",
						FirefoxDriverManager.getInstance() },

				// Marionette Linux
				{ "0.6.2",
						"https://github.com/jgraham/wires/releases/download/v0.6.2/wires-0.6.2-linux64.gz",
						"/wires/linux64/0.6.2/wires-0.6.2-linux64.gz",
						FirefoxDriverManager.getInstance() },

				// Marionette Linux #2
				{ "0.8.0",
						"https://github.com/mozilla/geckodriver/releases/download/v0.8.0/geckodriver-0.8.0-linux64.gz",
						"/geckodriver/linux64/0.8.0/geckodriver-0.8.0-linux64.gz",
						FirefoxDriverManager.getInstance() } });

	}

	@Test
	public void testTarget() throws IOException {
		String result = Downloader.getTarget(version, new URL(url), manager);
		log.info(result);
		log.info(targetPath + target);

		File fileResult = new File(result);
		File fileReal = new File(targetPath + target);

		assertThat(fileResult, equalTo(fileReal));
	}

}
