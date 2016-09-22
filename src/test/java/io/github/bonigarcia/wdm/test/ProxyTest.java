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

import static io.github.bonigarcia.wdm.Downloader.createProxy;
import static org.junit.Assert.assertTrue;

import java.net.Proxy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

/**
 * Test for proxy.
 *
 * @author Sebl29
 * @since 1.4.10
 */
@RunWith(JMockit.class)
public class ProxyTest {

	@Test
	public void testRealEnvProxyToNull() {
		Proxy proxy = createProxy();
		String exp = System.getenv("HTTP_PROXY");
		if (exp != null && exp.length() > 0) {
			Assert.assertNotNull(proxy);
		} else {
			Assert.assertNull(proxy);
		}
	}

	@Test
	public void testMockedEnvProxy() {
		new MockUp<System>() {
			@Mock
			public String getenv(final String string) {
				return "my.http.proxy:1234";
			}
		};
		Proxy proxy = createProxy();
		assertTrue(proxy.toString().contains("my.http.proxy"));
		assertTrue(proxy.toString().contains("1234"));
	}

}
