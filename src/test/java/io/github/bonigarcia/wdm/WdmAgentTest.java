package io.github.bonigarcia.wdm;

import org.junit.jupiter.api.Test;
import java.lang.instrument.Instrumentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the WdmAgent class.
 */
class WdmAgentTest {

    /**
     * Verifies that constructing an instance of WdmAgent throws an IllegalStateException.
     */
    @Test
    void constructorShouldThrowException() {
        assertThrows(IllegalStateException.class, WdmAgent::new);
    }

    /**
     * Verifies that calling the premain method with a valid Instrumentation instance does not throw an exception.
     */
    @Test
    void premainShouldNotThrowException() {
        assertDoesNotThrow(() -> WdmAgent.premain("", mock(Instrumentation.class)));
    }

    /**
     * Verifies that the DefineTransformer class transforms known driver classes correctly.
     */
    @Test
    void defineTransformerShouldTransformKnownDrivers() {
        WdmAgent.DefineTransformer transformer = new WdmAgent.DefineTransformer();

        byte[] transformedBuffer = assertDoesNotThrow(() -> transformer.transform(
                getClass().getClassLoader(),
                "org/openqa/selenium/chrome/ChromeDriver", null, null, new byte[0]));

        assertEquals(0, transformedBuffer.length);

        transformedBuffer = assertDoesNotThrow(() -> transformer.transform(
                getClass().getClassLoader(),
                "org/openqa/selenium/firefox/FirefoxDriver", null, null, new byte[0]));

        assertEquals(0, transformedBuffer.length);

        transformedBuffer = assertDoesNotThrow(() -> transformer.transform(
                getClass().getClassLoader(),
                "org/openqa/selenium/opera/OperaDriver", null, null, new byte[0]));

        assertEquals(0, transformedBuffer.length);

        transformedBuffer = assertDoesNotThrow(() -> transformer.transform(
                getClass().getClassLoader(),
                "org/openqa/selenium/edge/EdgeDriver", null, null, new byte[0]));

        assertEquals(0, transformedBuffer.length);

        transformedBuffer = assertDoesNotThrow(() -> transformer.transform(
                getClass().getClassLoader(),
                "org/openqa/selenium/ie/InternetExplorerDriver", null, null, new byte[0]));

        assertEquals(0, transformedBuffer.length);
    }

    /**
     * Verifies that the DefineTransformer class does not transform an unknown driver class.
     */
    @Test
    void defineTransformerShouldNotTransformUnknownDriver() {
        WdmAgent.DefineTransformer transformer = new WdmAgent.DefineTransformer();

        byte[] originalBuffer = new byte[0];
        byte[] transformedBuffer = assertDoesNotThrow(() -> transformer.transform(
                getClass().getClassLoader(),
                "com/some/unknown/DriverClass", null, null, originalBuffer));

        assertArrayEquals(originalBuffer, transformedBuffer);
    }
}
