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

import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.net.Proxy;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

/**
 * Test for proxy.
 * 
 * @since 1.6.1
 */
@RunWith(JMockit.class)
public class ProxyTest {

	protected static final Logger log = LoggerFactory
			.getLogger(ProxyTest.class);

	private static final String HTTP_PROXY = "HTTP_PROXY";
	private static final String PROXY_URL = "my.http.proxy";
	private static final String PROXY_PORT = "1234";

	private static final String[] PROXYS_TEST_STRINGS = {
			PROXY_URL + ":" + PROXY_PORT, PROXY_URL, "http://" + PROXY_URL,
			"http://" + PROXY_URL + ":" + PROXY_PORT,
			"https://" + PROXY_URL + ":" + PROXY_PORT };

	@Test
	public void testRealEnvProxyToNull() throws Exception {
		BrowserManager browserManager = ChromeDriverManager.getInstance();
		Method method = BrowserManager.class.getDeclaredMethod("createProxy");
		method.setAccessible(true);
		Proxy proxy = (Proxy) method.invoke(browserManager);

		String exp = System.getenv(HTTP_PROXY);
		if (exp != null && exp.length() > 0) {
			Assert.assertNotNull(proxy);
		} else {
			Assert.assertNull(proxy);
		}
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testMockedEnvProxy() throws Exception {
		MockUp<System> mockUp;
		for (String proxyTestString : PROXYS_TEST_STRINGS) {
			mockUp = setSystemGetEnvMock(proxyTestString);

			log.info("Testing proxy {}", proxyTestString);

			BrowserManager browserManager = ChromeDriverManager.getInstance();
			Method method = BrowserManager.class
					.getDeclaredMethod("createProxy");
			method.setAccessible(true);
			Proxy proxy = (Proxy) method.invoke(browserManager);

			assertThat(proxy.toString(),
					CoreMatchers.containsString(PROXY_URL));
			assertThat(proxy.toString(),
					CoreMatchers.containsString(PROXY_URL));
			mockUp.tearDown();
		}
	}

	private MockUp<System> setSystemGetEnvMock(final String httpProxyString) {
		MockUp<System> mockUp = new MockUp<System>() {
			@Mock
			public String getenv(final String string) {
				return httpProxyString;
			}
		};
		return mockUp;
	}

}