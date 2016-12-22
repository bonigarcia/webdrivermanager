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

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.Proxy;

import static io.github.bonigarcia.wdm.Downloader.createProxy;
import static org.junit.Assert.assertThat;

/**
 * Test for proxy.
 *
 * @author Sebl29
 * @since 1.4.10
 */
@RunWith(JMockit.class)
public class ProxyTest {

	private static final String HTTP_PROXY = "HTTP_PROXY";
	private static final String PROXY_URL = "my.http.proxy";
	private static final String PROXY_PORT = "1234";

	private static final String[] PROXYS_TEST_STRINGS = {
			PROXY_URL + ":" + PROXY_PORT,
			"http://" + PROXY_URL + ":" + PROXY_PORT,
			"https://" + PROXY_URL + ":" + PROXY_PORT
	};

	@Test
	public void testRealEnvProxyToNull() {
		Proxy proxy = createProxy();
		String exp = System.getenv(HTTP_PROXY);
		if (exp != null && exp.length() > 0) {
			Assert.assertNotNull(proxy);
		} else {
			Assert.assertNull(proxy);
		}
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testMockedEnvProxy() {
		MockUp<System> mockUp;
		for (String proxyTestString: PROXYS_TEST_STRINGS) {
			mockUp = setSystemGetEnvMock(proxyTestString);;
			System.out.println("proxyTestString=" + proxyTestString);
			Proxy proxy = createProxy();
			assertThat(proxy.toString(), CoreMatchers.containsString(PROXY_URL));
			assertThat(proxy.toString(), CoreMatchers.containsString(PROXY_URL));
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
