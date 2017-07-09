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
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import io.github.bonigarcia.wdm.WdmHttpClient;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hamcrest.core.Is;
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

        Assert.assertNull(proxy);
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
        assertThat(address.getHostName(), Is.is(PROXY_URL));
    }

    @Test
    public void testCredentials() throws Exception {
        WdmHttpClient wdmClient = new WdmHttpClient.Builder().proxy("myproxy:8081").proxyUser("domain\\me").proxyPass("pass").build();
        Field field = WdmHttpClient.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        
        CloseableHttpClient client = (CloseableHttpClient)field.get(wdmClient);
        field = client.getClass().getDeclaredField("credentialsProvider");
        field.setAccessible(true);
        
        BasicCredentialsProvider provider = (BasicCredentialsProvider)field.get(client);
        
        assertThat(provider.getCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.NTLM)), Is.is(instanceOf(NTCredentials.class)));
        assertThat(provider.getCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.BASIC)), Is.is(instanceOf(UsernamePasswordCredentials.class)));
        assertThat(provider.getCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT)), Is.is(instanceOf(UsernamePasswordCredentials.class)));
        assertThat(provider.getCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.KERBEROS)), Is.is(instanceOf(UsernamePasswordCredentials.class)));
        
        NTCredentials ntcreds = (NTCredentials)provider.getCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.NTLM));
        assertThat(ntcreds.getDomain(), Is.is("DOMAIN"));
        assertThat(ntcreds.getUserName(), Is.is("me"));
        assertThat(ntcreds.getPassword(), Is.is("pass"));
        
        UsernamePasswordCredentials creds = (UsernamePasswordCredentials)provider.getCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT));
        assertThat(creds.getUserName(), Is.is("domain\\me"));
        assertThat(creds.getPassword(), Is.is("pass"));
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

            assertThat(address.getHostName(), Is.is(PROXY_URL));
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