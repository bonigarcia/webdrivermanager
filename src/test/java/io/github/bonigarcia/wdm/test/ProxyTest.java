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

import static org.apache.http.auth.AuthScope.ANY_HOST;
import static org.apache.http.auth.AuthScope.ANY_PORT;
import static org.apache.http.auth.AuthScope.ANY_REALM;
import static org.apache.http.client.config.AuthSchemes.BASIC;
import static org.apache.http.client.config.AuthSchemes.KERBEROS;
import static org.apache.http.client.config.AuthSchemes.NTLM;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.WdmHttpClient;
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

    private static final String PROXY_URL = "my.http.proxy";
    private static final String PROXY_PORT = "1234";

    private static final String[] PROXYS_TEST_STRINGS = {
            PROXY_URL + ":" + PROXY_PORT, PROXY_URL, "http://" + PROXY_URL,
            "http://" + PROXY_URL + ":" + PROXY_PORT,
            "https://" + PROXY_URL + ":" + PROXY_PORT,
            "https://test:test@" + PROXY_URL + ":" + PROXY_PORT, };

    @Test
    public void testRealEnvProxyToNull() throws Exception {
        BrowserManager browserManager = ChromeDriverManager.getInstance();
        Field field = BrowserManager.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(browserManager, new WdmHttpClient.Builder().build());

        setSystemGetEnvMock(null);

        Method method = BrowserManager.class.getDeclaredMethod("createProxy");
        method.setAccessible(true);
        Proxy proxy = (Proxy) method.invoke(browserManager);

        assertNull(proxy);
    }

    @Test
    public void testRealEnvProxyToNotNull() throws Exception {
        BrowserManager browserManager = ChromeDriverManager.getInstance();
        Field field = BrowserManager.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(browserManager, new WdmHttpClient.Builder().build());

        setSystemGetEnvMock(PROXY_URL);

        Method method = BrowserManager.class.getDeclaredMethod("createProxy");
        method.setAccessible(true);
        Proxy proxy = (Proxy) method.invoke(browserManager);

        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertThat(address.getHostName(), equalTo(PROXY_URL));
    }

    @Test
    public void testProxyCredentialsScope() throws Exception {
        WdmHttpClient wdmClient = new WdmHttpClient.Builder()
                .proxy("myproxy:8081").proxyUser("domain\\me").proxyPass("pass")
                .build();
        Field field = WdmHttpClient.class.getDeclaredField("httpClient");
        field.setAccessible(true);

        CloseableHttpClient client = (CloseableHttpClient) field.get(wdmClient);
        field = client.getClass().getDeclaredField("credentialsProvider");
        field.setAccessible(true);

        BasicCredentialsProvider provider = (BasicCredentialsProvider) field
                .get(client);

        assertThat(
                provider.getCredentials(
                        new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, NTLM)),
                instanceOf(NTCredentials.class));
        assertThat(
                provider.getCredentials(
                        new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, BASIC)),
                instanceOf(UsernamePasswordCredentials.class));
        assertThat(provider.getCredentials(new AuthScope(ANY_HOST, ANY_PORT)),
                instanceOf(UsernamePasswordCredentials.class));
        assertThat(
                provider.getCredentials(
                        new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, KERBEROS)),
                instanceOf(UsernamePasswordCredentials.class));
    }

    @Test
    public void testProxyCredentials() throws Exception {
        WdmHttpClient wdmClient = new WdmHttpClient.Builder()
                .proxy("myproxy:8081").proxyUser("domain\\me").proxyPass("pass")
                .build();
        Field field = WdmHttpClient.class.getDeclaredField("httpClient");
        field.setAccessible(true);

        CloseableHttpClient client = (CloseableHttpClient) field.get(wdmClient);
        field = client.getClass().getDeclaredField("credentialsProvider");
        field.setAccessible(true);

        BasicCredentialsProvider provider = (BasicCredentialsProvider) field
                .get(client);

        NTCredentials ntcreds = (NTCredentials) provider.getCredentials(
                new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, NTLM));
        assertThat(ntcreds.getDomain(), equalTo("DOMAIN"));
        assertThat(ntcreds.getUserName(), equalTo("me"));
        assertThat(ntcreds.getPassword(), equalTo("pass"));

        UsernamePasswordCredentials creds = (UsernamePasswordCredentials) provider
                .getCredentials(new AuthScope(ANY_HOST, ANY_PORT));
        assertThat(creds.getUserName(), equalTo("domain\\me"));
        assertThat(creds.getPassword(), equalTo("pass"));
    }

    @Test
    public void testMockedEnvProxy() throws Exception {
        for (String proxyTestString : PROXYS_TEST_STRINGS) {
            setSystemGetEnvMock(proxyTestString);

            log.info("Testing proxy {}", proxyTestString);

            BrowserManager browserManager = ChromeDriverManager.getInstance();
            Field field = BrowserManager.class.getDeclaredField("httpClient");
            field.setAccessible(true);
            field.set(browserManager, new WdmHttpClient.Builder().build());

            Method method = BrowserManager.class
                    .getDeclaredMethod("createProxy");
            method.setAccessible(true);
            Proxy proxy = (Proxy) method.invoke(browserManager);
            InetSocketAddress address = (InetSocketAddress) proxy.address();

            assertThat(address.getHostName(), equalTo(PROXY_URL));
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