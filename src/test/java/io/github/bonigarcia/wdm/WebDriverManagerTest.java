package io.github.bonigarcia.wdm;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.managers.VoidDriverManager;
import io.github.bonigarcia.wdm.online.Downloader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

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

    @DisplayName("download")
    @ParameterizedTest(name = "with driver version {0} should download version {1}")
    @MethodSource("valuesProvider")
    void download(String driverVersion, String cleanDriverVersion, boolean avoidRemoteDownload) throws IOException {
        String chromeDownloadUrlPattern = "https://chromeDownloadUrlPattern";
        String expected = "expected";

        when(config.isAvoidExternalConnections()).thenReturn(avoidRemoteDownload);
        when(config.getChromeDownloadUrlPattern()).thenReturn(chromeDownloadUrlPattern);

        when(URI.create(chromeDownloadUrlPattern)).thenReturn(uri);
        when(uri.toURL()).thenReturn(url);

        when(downloader.download(url, cleanDriverVersion, "", null)).thenReturn(expected);

        assertEquals(expected, webDriverManager.download(driverVersion));
    }

    public static Stream<Arguments> valuesProvider() {
        return Stream.of(
                arguments("123", "123", true),
                arguments(".123", "123", true)
        );
    }
}
