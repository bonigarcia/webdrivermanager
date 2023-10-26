package io.github.bonigarcia.wdm;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.managers.VoidDriverManager;
import io.github.bonigarcia.wdm.online.Downloader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebDriverManagerTest {

    @InjectMocks
    private VoidDriverManager webDriverManager;

    private MockedStatic<URI> uriMockedStatic;

    @Mock
    private Config config;

    @Mock
    private Downloader downloader;

    @Mock
    private URI uri;

    @Mock
    private URL url;

    @BeforeEach
    public void beforeEach() {
        uriMockedStatic = mockStatic(URI.class);
    }

    @AfterEach
    public void afterEach() {
        uriMockedStatic.close();
    }

    @Test
    @DisplayName("avoidExternalConnections() should fluently set to true the corresponding config parameter")
    void avoidExternalConnections() {
        when(config.setAvoidExternalConnections(true)).thenReturn(config);

        assertEquals(webDriverManager, webDriverManager.avoidExternalConnections());
    }

    @DisplayName("download")
    @ParameterizedTest(name = "with driver version {0} should download version {1}")
    @ValueSource(strings = { "123", ".123" })
    void download(String driverVersion) throws IOException {
        String chromeDownloadUrlPattern = "https://chromeDownloadUrlPattern";
        String expected = "expected";
        String cleanDriverVersion = "123";

        when(config.isAvoidExternalConnections()).thenReturn(true);
        when(config.getChromeDownloadUrlPattern())
                .thenReturn(chromeDownloadUrlPattern);

        when(URI.create(chromeDownloadUrlPattern)).thenReturn(uri);
        when(uri.toURL()).thenReturn(url);

        when(downloader.download(url, cleanDriverVersion, "", null))
                .thenReturn(expected);

        assertEquals(expected, webDriverManager.download(driverVersion));
    }
}