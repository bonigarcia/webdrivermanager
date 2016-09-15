package io.github.bonigarcia.wdm.test;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.Proxy;

import static io.github.bonigarcia.wdm.Downloader.createProxy;
import static org.junit.Assert.assertTrue;

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
		new MockUp<System>()
		{
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
